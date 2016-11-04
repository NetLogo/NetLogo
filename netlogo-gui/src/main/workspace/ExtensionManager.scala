// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
//
package org.nlogo.workspace

import org.nlogo.core.{ CompilerException, ErrorSource, ExtensionObject, Primitive }
import java.net.URL

import org.nlogo.api.{ ClassManager, Dump, ExtensionException, ImportErrorHandler, Reporter }

import org.nlogo.nvm.{ ExtensionManager => NvmExtensionManager }

import java.lang.{ ClassLoader, Iterable => JIterable }
import java.io.{ Closeable, IOException, PrintWriter }
import java.util.{ List => JList }

import org.nlogo.nvm.FileManager

import scala.collection.JavaConverters._

/**
 * Some simple notes on loading and unloading extensions:
 * - The load method is called when an extension appears in the extensions block when it wasn't
 * there in the last compilation
 * - The unload method is called when an extension is removed from the extensions block
 *
 * ExtensionManager lifecycle call chart.
 * In [square brackets] are methods called on the extension manager.
 * In {curly braces} are the methods called on the class manager of the extension.
 * Conditions are specified in (parens)
 *
 * [startFullCompilation]                     // called by compiler
 *   |                 |
 * (no exts)         (ext foo)
 *   |                 |
 *   |                 v
 *   |           [importExtension(foo)]       // called by compiler
 *   |            |            |
 *   |      (foo loaded)   (foo not loaded)
 *   |            |            |
 *   |            v            v
 *   |         {unload}   {runOnce}
 *   |               |     |
 *   |               v     v
 *   |         +-----{load}
 *   |         |          |                    // return control to compiler
 *   |(foo:bar not used) (foo:bar used)
 *   |         |          |
 *   |         |          v
 *   |         |   [replaceIdentifer(foo:bar)] // foo added to live set
 *   |         |          |
 *   |         |          |
 *   v         v          v
 *  [finishFullCompilation]
 *   |         |          |
 *(no ext) (foo live) (foo not live)
 *   |         |          |
 *  ---       ---         v
 *                      {unload}
 *                        |
 *                       ---
 */
object ExtensionManager {
  val EXTENSION_NOT_FOUND: String = "Can't find extension: "

  def extensionPath: String =
    System.getProperty("netlogo.extensions.dir", "extensions");

  case class ExtensionData(extensionName: String, fileURL: URL, prefix: String, classManagerName: String, version: Option[String], modified: Long)

  class JarContainer(val jarClassLoader: ClassLoader, data: ExtensionData) {
    val extensionName  = data.extensionName
    val normalizedName = data.extensionName.toUpperCase
    val fileURL        = data.fileURL
    val modified: Long = data.modified
    val primManager: ExtensionPrimitiveManager = new ExtensionPrimitiveManager(extensionName)
    var classManager: ClassManager = null
    var loaded: Boolean = false

    def load(instantiatedClassManager: ClassManager): Unit = {
      loaded = true
      classManager = instantiatedClassManager
      classManager.load(primManager)
    }

    def unload(extensionManager: ExtensionManager) = {
      loaded = false
      try {
        classManager.unload(extensionManager)
      } catch {
        case ex: Exception => org.nlogo.api.Exceptions.ignore(ex)
      }
    }
  }

  trait ExtensionLoader {
    // should return None when extension cannot be found
    def locateExtension(extensionName: String): Option[URL]

    def extensionData(extensionName: String, url: URL): ExtensionData

    def extensionClassLoader(fileURL: URL, parent: ClassLoader): ClassLoader

    // should *not* call runOnce on ClassManager
    def extensionClassManager(classLoader: ClassLoader, data: ExtensionData): ClassManager
  }
}

import ExtensionManager._

class ExtensionManager(val workspace: ExtendableWorkspace, loader: ExtensionLoader) extends NvmExtensionManager {
  import ExtensionManagerException._

  private var loaders  = Seq[ExtensionLoader](loader)
  private var jars     = Map[URL, JarContainer]()
  private var liveJars = Set[JarContainer]()

  def anyExtensionsLoaded: Boolean = jars.nonEmpty

  def loadedExtensions: JIterable[ClassManager] =
    jars.values.map(_.classManager).asJava

  private var obj: AnyRef = null

  private[nlogo] def addLoader(alternateLoader: ExtensionLoader): Unit = {
    loaders = loaders :+ alternateLoader
  }

  def storeObject(obj: AnyRef): Unit = {
    this.obj = obj
  }

  def retrieveObject: AnyRef = obj

  @throws(classOf[CompilerException])
  def importExtension(extName: String, errors: ErrorSource): Unit = {
    try {
      val (fileURL, loader) = loaders.foldLeft(Option.empty[(URL, ExtensionLoader)]) {
        case (None,             ldr) => ldr.locateExtension(extName).map((_, ldr))
        case (location@Some(_), _)   => location
      }.getOrElse(throw new ExtensionManagerException(ExtensionNotFound(extName)))

      val data = loader.extensionData(extName, fileURL)

      var theJarContainer: Option[JarContainer] =
        jars.get(fileURL)

      val myClassLoader: ClassLoader =
        theJarContainer.map(_.jarClassLoader)
          .getOrElse(loader.extensionClassLoader(fileURL, getClass.getClassLoader))

      val classManager: ClassManager =
        theJarContainer.filter(_.loaded).map(_.classManager)
          .getOrElse {
            checkVersion(data.version)
            initializedClassManager(loader.extensionClassManager(myClassLoader, data))
          }

      val modifiedSinceLoad = theJarContainer.exists(_.modified != data.modified)
      def needsLoad = ! theJarContainer.exists(_.loaded)

      if (modifiedSinceLoad) {
        theJarContainer.foreach(_.unload(this))
        theJarContainer = Some(initializeJarContainer(myClassLoader, data))
      } else if (theJarContainer.isEmpty)
        theJarContainer = Some(initializeJarContainer(myClassLoader, data))
      if (needsLoad)
        theJarContainer.foreach(_.load(classManager))
      theJarContainer.foreach(liveJars += _)
    } catch {
      case ex @ (_: ExtensionManagerException | _: ExtensionException) =>
        errors.signalError(ex.getMessage)
      case ex: IOException =>
        errors.signalError(s"There was a problem while reading extension $extName")
      case ex: IncompatibleClassChangeError =>
        errors.signalError("This extension doesn't work with this version of NetLogo")
        System.err.println(ex)
    }
  }

  private def initializeJarContainer(classLoader: ClassLoader, data: ExtensionData): JarContainer = {
    val newJarContainer = new JarContainer(classLoader, data)
    jars += data.fileURL -> newJarContainer
    newJarContainer
  }

  private def initializedClassManager(cm: ClassManager): ClassManager =
    try {
      if (!workspace.compilerTestingMode)
        cm.runOnce(this)
      cm
    } catch {
      case ex: ExtensionException =>
        System.err.println("Error while initializing extension.")
        System.err.println("Error is: " + ex)
        throw ex
    }

  @throws(classOf[CompilerException])
  def readFromString(source: String): AnyRef =
    workspace.readFromString(source)

  def clearAll(): Unit =
    for (jar <- jars.values) {
      jar.classManager.clearAll
    }

  @throws(classOf[CompilerException])
  def readExtensionObject(extName: String, typeName: String, value: String): ExtensionObject = {
    val upcaseExtName = extName.toUpperCase
    def catchExtensionException(f: JarContainer => ExtensionObject): JarContainer => ExtensionObject = { j: JarContainer =>
      try { f(j) }
      catch {
        case ex: ExtensionException =>
          System.err.println(ex)
          throw new IllegalStateException(s"Error reading extension object $upcaseExtName:$typeName $value ==> ${ex.getMessage}")
      }
    }
    jars.values.filter(theJarContainer =>
        theJarContainer.loaded && theJarContainer.normalizedName == upcaseExtName)
      .map(catchExtensionException(_.classManager.readExtensionObject(this, typeName, value)))
      .headOption.getOrElse(null)
  }

  def replaceIdentifier(name: String): Primitive = {
    val (primName, isRelevantJar) = name.split(":") match {
      case Array(prefix, pname) => (pname, (jc: JarContainer) => prefix.toUpperCase == jc.normalizedName)
      case _                    => (name,  (jc: JarContainer) => jc.primManager.autoImportPrimitives)
    }
    jars.values.filter(liveJars.contains)
      .filter(isRelevantJar)
      .map(_.primManager.getPrimitive(primName)).headOption.orNull
  }

  /**
   * Returns a String describing all the loaded extensions.
   */
  def dumpExtensions: String = tabulate(
    Seq("EXTENSION", "LOADED", "MODIFIED", "JARPATH"),
    { jarContainer =>
        Seq(Seq(jarContainer.extensionName, jarContainer.loaded.toString, jarContainer.modified.toString, jarContainer.fileURL.toString))
    }
  )

  /**
   * Returns a String describing all the loaded extensions.
   */
  def dumpExtensionPrimitives: String = tabulate(
    Seq("EXTENSION", "PRIMITIVE", "TYPE"),
    { jarContainer =>
      jarContainer.primManager.getPrimitiveNames.asScala.map { n =>
        val p = jarContainer.primManager.getPrimitive(n)
        Seq(jarContainer.extensionName, n, if (p.isInstanceOf[Reporter]) "Reporter" else "Command")
      }.toSeq
    }
  )

  def reset() = {
    jars.values.foreach { jar =>
      jar.unload(this)
      jar.jarClassLoader match {
        case closeable: Closeable =>
          try {
            closeable.close()
          } catch {
            case e: IOException =>
              val error = s"Error while unloading extension ${e.getMessage} (this can probably be ignored)"
              System.err.println(error)
          }
        case _ =>
      }
    }
    jars = Map[URL, JarContainer]()
    liveJars = Set[JarContainer]()
  }

  private def tabulate(header: Seq[String], generateRows: (JarContainer) => Seq[Seq[String]]) = {
    val separator = header.map(n => "-" * n.length)
    val rows = jars.values.flatMap(generateRows)
    (Seq(header, separator) ++ rows).map(_.mkString("\t")).mkString("", "\n", "\n")
  }

  def startFullCompilation(): Unit = {
    liveJars = Set[JarContainer]()
  }

  def finishFullCompilation(): Unit = {
    for (nextJarContainer <- jars.values) {
      if (nextJarContainer.loaded && !liveJars.contains(nextJarContainer)) {
        jars -= nextJarContainer.fileURL
        nextJarContainer.unload(this)
      }
    }
  }

  private def checkVersion(extensionVer: Option[String]): Unit = {
    val currentVer: String = org.nlogo.api.APIVersion.version
    val shouldContinue: Boolean =
      if (extensionVer.isEmpty)
        workspace.warningMessage(
          """|Could not determine version of NetLogo extension.
             |NetLogo can try to load the extension, but it might not work.""".stripMargin.lines.mkString(" "))
      else if (currentVer != extensionVer.get)
        workspace.warningMessage(
          s"""|You are attempting to open a NetLogo extension file that was created
              |for a different version of the NetLogo Extension API.
              |(This NetLogo uses Extension API $currentVer;
              |the extension uses NetLogo Extension API ${extensionVer.get}.)
              |NetLogo can try to load the extension, but it might not work.""".stripMargin.lines.mkString(" "))
      else
        true
    if (! shouldContinue)
      throw new ExtensionManagerException(UserHalted)
  }

  def exportWorld(writer: PrintWriter) {
    writer.println(Dump.csv.encode("EXTENSIONS"))
    writer.println()
    for (container <- jars.values) {
      val data = container.classManager.exportWorld
      if (data.length > 0) {
        writer.println(Dump.csv.encode(container.extensionName))
        writer.print(data)
        writer.println
      }
    }
  }

  @throws(classOf[org.nlogo.api.ExtensionException])
  def importExtensionData(name: String, data: JList[Array[String]], handler: ImportErrorHandler) {
    val jar = getJarContainerByIdentifier(name).getOrElse(
      throw new ExtensionException(s"there is no extension named $name in this model"))
    jar.classManager.importWorld(data, this, handler)
  }

  def isExtensionName(name: String): Boolean =
    getJarContainerByIdentifier(name).nonEmpty

  private def getJarContainerByIdentifier(identifier: String): Option[JarContainer] =
    jars.values.find(_.extensionName.equalsIgnoreCase(identifier))
}
