package org.nlogo.extensions.py

import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.ObjectMapper

import java.awt.GraphicsEnvironment
import java.io.{ BufferedReader, Closeable, File, IOException, InputStreamReader }
import java.lang.ProcessBuilder.Redirect
import java.nio.file.Paths

import org.json4s.jackson.{ JsonMethods, Json4sScalaModule }

import org.nlogo.languagelibrary.Subprocess.path
import org.nlogo.languagelibrary.Subprocess
import org.nlogo.languagelibrary.config.{ Config, ConfigProperty, FileProperty, Menu }

import org.nlogo.api
import org.nlogo.api._
import org.nlogo.core.{ LogoList, Syntax }
import org.nlogo.workspace.AbstractWorkspace

import scala.collection.immutable.ArraySeq

object PythonExtension {
  val codeName   = "py"
  val longName   = "Python"
  val extLangBin = if (System.getProperty("os.name").toLowerCase.startsWith("win")) { "python" } else { "python3" }

  private var _pythonProcess: Option[Subprocess] = None

  var menu: Option[Menu] = None
  val config: Config     = Config.createForPropertyFile(classOf[PythonExtension], PythonExtension.codeName)

  var isHeadless = true

  def pythonProcess: Subprocess = {
    _pythonProcess.getOrElse(throw new ExtensionException(
      "Python process has not been started. Please run PY:SETUP before any other python extension primitive."))
  }

  def pythonProcess_=(proc: Subprocess): Unit = {
    _pythonProcess.foreach(_.close())
    _pythonProcess = Some(proc)
  }

  def killPython(): Unit = {
    _pythonProcess.foreach(_.close())
    _pythonProcess = None
  }

}

class PythonExtension extends api.DefaultClassManager {
  override def load(manager: api.PrimitiveManager): Unit = {
    manager.addPrimitive("setup", SetupPython)
    manager.addPrimitive("run", Run)
    manager.addPrimitive("runresult", RunResult)
    manager.addPrimitive("set", Set)
    manager.addPrimitive("python2",
      FindPython(() => PythonSubprocess.python2)
    )
    manager.addPrimitive("python3",
      FindPython(() => PythonSubprocess.python3)
    )
    manager.addPrimitive("python",
      FindPython(() => PythonSubprocess.anyPython)
    )
    manager.addPrimitive("__path", Path)
  }

  override def runOnce(em: ExtensionManager): Unit = {
    super.runOnce(em)

    PythonExtension.isHeadless = !em.workspaceContext.workspaceGUI

    if (!PythonExtension.isHeadless) {
      val py2Message  = s"It is recommended to use Python 3 if possible and enter its path above.  If you must use Python 2, enter the path to its executable folder below."
      val py2Property = new FileProperty("python2", "python2", PythonExtension.config.get("python2").getOrElse(""), py2Message)
      PythonExtension.menu = Menu.create(em, PythonExtension.longName, PythonExtension.extLangBin, PythonExtension.config, Seq(py2Property))
    }
  }

  override def unload(em: ExtensionManager): Unit = {
    super.unload(em)
    PythonExtension.killPython()

    if (!PythonExtension.isHeadless)
      PythonExtension.menu.foreach(_.unload())
  }

}

object Using {
  def apply[A <: Closeable, B](resource: A)(fn: A => B): B =
    apply(resource, (x: A) => x.close())(fn)

  def apply[A, B](resource: A, cleanup: A => Unit)(fn: A => B): B =
    try fn(resource) finally if (resource != null) cleanup(resource)
}

object PythonSubprocess {
  def python2: Option[File] = {
    val maybePy2File = PythonExtension.config.get("python2").map( (dir) => {
      val bin  = if (System.getProperty("os.name").toLowerCase.startsWith("win")) { "python.exe" } else { "python2" }
      val path = Paths.get(dir, bin)
      new File(path.toString)
    })
    maybePy2File.orElse(pythons.find(_.version._1 == 2).map(_.file))
  }

  def python3: Option[File] = {
    val maybePythonRuntimeFile = Config.getRuntimePath(
        PythonExtension.extLangBin
      , PythonExtension.config.runtimePath.getOrElse("")
      , "--version"
    ).map(new File(_))
    maybePythonRuntimeFile.orElse(pythons.find(_.version._1 == 3).map(_.file))
  }

  def anyPython: Option[File] = python3 orElse python2

  def pythons: LazyList[PythonBinary] =
    path.to(LazyList)
      .flatMap(_.listFiles((_, name) => name.toLowerCase.matches(raw"python[\d\.]*(?:\.exe)??")))
      .flatMap(PythonBinary.fromFile)
}

object SetupPython extends api.Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(
    right = List(Syntax.StringType | Syntax.RepeatableType)
  )

  override def perform(args: Array[Argument], context: Context): Unit = {
    val pyExtensionDirectory = Config.getExtensionRuntimeDirectory(classOf[PythonExtension], PythonExtension.codeName)
    val pythonCmd            = ArraySeq.unsafeWrapArray(args.map(_.getString))
    val maybePyFile          = new File(pyExtensionDirectory, "pyext.py")
    val pyFile               = if (maybePyFile.exists) { maybePyFile } else { (new File("pyext.py")).getCanonicalFile }
    val pyScript: String     = pyFile.toString
    try {
      val mapper = new JsonMethods {
        override def mapper: ObjectMapper =
          JsonMapper.builder.addModule(new Json4sScalaModule).enable(JsonReadFeature.ALLOW_NON_NUMERIC_NUMBERS).build()
      }
      PythonExtension.pythonProcess = Subprocess.start(context.workspace, pythonCmd, Seq(pyScript),
                                                       PythonExtension.codeName, PythonExtension.longName,
                                                       customMapper = Option(mapper))

      if (!PythonExtension.isHeadless)
        PythonExtension.menu.foreach(_.setup(PythonExtension.pythonProcess.evalStringified))
    } catch {
      case e: Exception =>
        // Different errors can manifest in different operating systems. Thus, rather than dispatching in the specific
        // exception position, we catch all problems with Python bootup, look for common problems, and then offer advice
        // accordingly.
        val prefix = "Python failed to start."
        val wrongPathTip = "Check to make sure the correct path was entered in the Python configuration" +
          " menu or supply the correct path as an argument to PY:SETUP."
        val details = s"Details:\n\n${e.getLocalizedMessage}"
        val suffix = s"$wrongPathTip\n\n$details"

        val pythonFile = new File(pythonCmd.head)
        pythonFile.length()
        if (!pythonFile.exists) {
          throw new ExtensionException(
            s"$prefix Expected path to Python executable but '$pythonFile' does not exist. $suffix", e
          )
        } else if (pythonFile.isDirectory) {
          throw new ExtensionException(
            s"$prefix Expected path to Python executable but '$pythonFile' is a directory. $suffix", e
          )
        } else if (!pythonFile.canExecute) {
          throw new ExtensionException(s"$prefix NetLogo does not have permission to run '$pythonFile'. $suffix", e)
        } else {
          throw new ExtensionException(s"$prefix Encountered an error while running '$pythonFile'. $suffix", e)
        }
    }
  }
}

object Run extends api.Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(
    right = List(Syntax.StringType | Syntax.RepeatableType)
  )

  override def perform(args: Array[Argument], context: Context): Unit =
    PythonExtension.pythonProcess.exec(args.map(_.getString).mkString("\n"))
}

object RunResult extends api.Reporter {
  override def getSyntax: Syntax = Syntax.reporterSyntax(
    right = List(Syntax.StringType),
    ret = Syntax.WildcardType
  )

  override def report(args: Array[Argument], context: Context): AnyRef =
    PythonExtension.pythonProcess.eval(args.map(_.getString).mkString("\n"))
}

object Set extends api.Command {
  override def getSyntax: Syntax = Syntax.commandSyntax(
    right = List(Syntax.StringType, Subprocess.convertibleTypesSyntax))

  override def perform(args: Array[Argument], context: Context): Unit =
    PythonExtension.pythonProcess.assign(args(0).getString, args(1).get)
}

case class FindPython(pyFinder: () => Option[File]) extends api.Reporter {

  override def report(args: Array[Argument], context: Context): String =
    pyFinder().map(_.toString).getOrElse(
      throw new ExtensionException("Couldn't find an appropriate version of Python. Please set the path to your Python executable in the Python > Configure menu.\n")
    )

  override def getSyntax: Syntax = Syntax.reporterSyntax(ret = Syntax.StringType)
}

object Path extends api.Reporter {
  override def report(args: Array[Argument], context: Context): LogoList =
    LogoList.fromVector(Subprocess.path.flatMap(_.listFiles.filter(_.getName.toLowerCase.matches(raw"python[\d\.]*(?:\.exe)??"))).map(_.getAbsolutePath).toVector)

  override def getSyntax: Syntax = Syntax.reporterSyntax(ret = Syntax.ListType)
}

case class PythonBinary(file: File, version: (Int, Int, Int))

object PythonBinary {
  def fromPath(s: String): Option[PythonBinary] = fromFile(new File(s))

  def fromFile(f: File): Option[PythonBinary] = {
    try {
      val proc = new ProcessBuilder(f.getAbsolutePath, "-V")
        .redirectError(Redirect.PIPE)
        .redirectInput(Redirect.PIPE)
        .start()
      Option(new BufferedReader(new InputStreamReader(proc.getInputStream)).readLine()).orElse(
        Option(new BufferedReader(new InputStreamReader(proc.getErrorStream)).readLine())
      ).flatMap { verString =>
        parsePythonVersion(verString).map({ case (version) => PythonBinary(f, version) })
      }
    } catch {
      case _: IOException => None
      case _: IllegalStateException => None
      case _: SecurityException => None
    }
  }

  def parsePythonVersion(v: String): Option[(Int, Int, Int)] = {
    val m = """Python (\d+)\.(\d+)\.(\d+)""".r.findAllIn(v)
    if (m.groupCount == 3) {
      Some((m.group(1).toInt, m.group(2).toInt, m.group(3).toInt))
    } else {
      None
    }
  }
}
