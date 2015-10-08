// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.workspace

import java.net.URL

import org.nlogo.api.{ ClassManager, CompilerException, Dump, ErrorSource,
  ExtensionException, ExtensionObject, File, ImportErrorHandler, Primitive, Reporter }
import java.lang.{ Iterable => JIterable }
import java.io.{ File => JFile, FileNotFoundException, IOException, PrintWriter }
import java.net.{ MalformedURLException, JarURLConnection, URLClassLoader }
import
  java.util.{ ArrayList => JArrayList, jar => javajar, List => JList },
    javajar.Attributes,
      Attributes.{ Name => AttributeName }

import org.nlogo.nvm.FileManager

import scala.collection.JavaConversions._
import scala.util.Try

/**
 * Some simple notes on loading and unloading extensions:
 * - The load method is called when an extension appears in the extensions block when it wasn't
 * there in the last compilation
 * - The unload method is called when an extension is removed from the extensions block
 *
 * Before a compilation, N extensions might be loaded.
 * For example, if the extensions block previously said: extensions [ array table ]
 * Then the array and table extensions will be loaded and added to the set of live jars.
 * For a new compilation, we have to remember which extensions are loaded so that we dont call load on them again.
 * But we have to know which extensions were removed from the list, so that we call unload on them.
 * For example, if the extensions block now says: extensions [ array ]
 * then the table extension needs to be unloaded. But the array extension does not need to be loaded again.
 * During the main compile when/if we reach the extensions block
 * the compiler calls startFullCompilation and
 * the ExtensionManager sets the set of live jars to empty
 * Then, for each extension in the block, it is added to the set of live jars.
 * If the extension wasnt previously in the block, its loaded flag will be false
 * and the ExtensionManager will set loaded to true and call the load method on it.
 * Then, when we come across extension primitives later in compilation, we simply check
 * whether it is in the live set. If the extension is not in the live set, then the
 * primitive isn't found.  For example, if someone removed table from the extensions block,
 * the table extension will no longer be in the live set, and if they call table:make,
 * then error. At the end of main compilation, the compiler calls the finishFullCompilation,
 * and the ExtensionManager calls unload on any extensions that have loaded=true and are
 * not in the live set.
 *
 * Subprogram compilations just check the live set in the same way, and everyone is happy.
 *
 * That is how it works now, but here is some info on the bug was that led to the addition
 * of the live flag. (You shouldn't really have to read this, but maybe it might someday
 * be useful).  After a main compile, the ExtensionManager would set reloaded to false on
 * all extensions.  During the main compile, extensions previously and currently in the
 * extensions block would have loaded=true.  When we encountered a primitive during the
 * main compile, we checked the loaded flag.  But this was true even for extensions that
 * had been removed from the extensions block!  So we would say that table:make was valid,
 * even if table had just been removed.  Subprograms managed to still work because they
 * would run after the main compile, after the loaded flags were set to false. They would
 * get set to false if reloaded!=true.  It was only during the main compile that there was
 * confusion.
 */
object ExtensionManager {
  val EXTENSION_NOT_FOUND: String = "Can't find extension: "

  @throws(classOf[java.net.MalformedURLException])
  private def toURL(file: JFile): URL = {
    return file.toURI.toURL
  }

  def extensionPath: String =
    System.getProperty("netlogo.extensions.dir", "extensions");

  case class ExtensionData(extensionName: String, fileURL: URL, prefix: String, classManagerName: String, version: Option[String], modified: Long)

  class JarContainer(val jarClassLoader: URLClassLoader, val data: ExtensionData) {
    val extensionName  = data.extensionName
    val fileURL        = data.fileURL
    val prefix: String = data.prefix
    val modified: Long = data.modified
    val primManager: ExtensionPrimitiveManager = new ExtensionPrimitiveManager(extensionName)
    var classManager: ClassManager = null
    var loaded: Boolean = false

    val normalizedName: String =
      primManager.name.toUpperCase

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
        case ex: ExtensionException =>
          System.err.println("Error unloading extension: " + ex)
          ex.printStackTrace
      }
    }
  }
}

class ExtensionManager(_workspace: ExtendableWorkspace) extends org.nlogo.api.ExtensionManager {
  import ExtensionManager.{ ExtensionData, JarContainer }
  import ExtensionManagerException._

  // for backwards compatibility during refactor
  val workspace: AbstractWorkspace = if (_workspace.isInstanceOf[AbstractWorkspace]) _workspace.asInstanceOf[AbstractWorkspace] else null

  private var jars = Map[URL, JarContainer]()
  private var liveJars = Set[JarContainer]()

  def anyExtensionsLoaded: Boolean =
    jars.nonEmpty

  def loadedExtensions: JIterable[ClassManager] =
    asJavaIterable(jars.values.map(_.classManager))

  @throws(classOf[ExtensionException])
  def getFile(path: String): File =
    _workspace.fileManager.getFile(getFullPath(path))

  private var obj: AnyRef = null

  def storeObject(obj: AnyRef): Unit = {
    this.obj = obj
  }

  def retrieveObject: AnyRef = obj

  private def identifierToJar(id: String): String =
    if (!id.endsWith(".jar"))
      return id + java.io.File.separator + id + ".jar"
    else
      return id

  @throws(classOf[CompilerException])
  def importExtension(extName: String, errors: ErrorSource): Unit = {
    try {
      val fileURL: URL =
        try {
          resolvePathAsURL(identifierToJar(extName))
        } catch {
          case ex: RuntimeException =>
            ex.printStackTrace()
            throw new ExtensionManagerException(ExtensionNotFound(extName))
          case ex: IOException =>
            ex.printStackTrace()
            try {
              new URL(identifierToJar(extName))
            } catch {
              case e: MalformedURLException =>
                throw new ExtensionManagerException(ExtensionNotFound(extName))
            }
        }

      val data = extensionData(extName, fileURL, connectToJar(fileURL))

      var theJarContainer: Option[JarContainer] =
        jars.get(fileURL)

      val myClassLoader: URLClassLoader =
        theJarContainer.map(_.jarClassLoader)
          .getOrElse(getClassLoader(fileURL, getClass.getClassLoader))
      assert(myClassLoader != null)

      val classManager: ClassManager =
        theJarContainer.filter(_.loaded).map(_.classManager)
          .getOrElse {
            checkVersion(data.version)
            initializedClassManager(getClassManager(myClassLoader, data))
          }
      assert(classManager != null)

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

  private def initializeJarContainer(classLoader: URLClassLoader, data: ExtensionData): JarContainer = {
    val newJarContainer = new JarContainer(classLoader, data)
    jars += data.fileURL -> newJarContainer
    newJarContainer
  }

  private[workspace] def resolvePathAsURL(path: String): URL = {
    try {
      return new URL(path)
    } catch {
      case ex: MalformedURLException => org.nlogo.util.Exceptions.ignore(ex)
    }

    val files = potentialPaths(path)

    val foundPath = files.find(f => f.exists).flatMap(f =>
        try {
          Some(ExtensionManager.toURL(f))
        } catch {
          case ex: MalformedURLException =>
            org.nlogo.util.Exceptions.ignore(ex)
            None
        }).headOption

    if (foundPath.nonEmpty)
      return foundPath.get

    val triedPathsString: String = (path +: files.map(_.getPath)).mkString(", ")
    throw new IllegalStateException(ExtensionManager.EXTENSION_NOT_FOUND + path + " looked in: " + triedPathsString)
  }

  private def potentialPaths(path: String): Seq[JFile] = {
    val paths = Seq(new JFile(ExtensionManager.extensionPath + java.io.File.separator + path))
    Try {
      new JFile(_workspace.attachModelDir(path))
    }.toOption.map(_ +: paths).getOrElse(paths)
  }

  @throws(classOf[ExtensionException])
  def getFullPath(path: String): String =
    potentialPaths(path).find(_.exists).map(_.getPath).getOrElse(
      throw new ExtensionException(s"Can't find file: $path"))

  private def getClassLoader(fileURL: URL, parentLoader: ClassLoader): URLClassLoader = {
    val folderContainingJar = new JFile(new JFile(fileURL.getFile).getParent)
    val urls = fileURL +: (getAdditionalJars(folderContainingJar) ++ getAdditionalJars(new JFile(ExtensionManager.extensionPath)))
    java.net.URLClassLoader.newInstance(urls.toArray, parentLoader)
  }

  private def getClassManager(myClassLoader: URLClassLoader, data: ExtensionData): ClassManager = {
    Try {
      myClassLoader.loadClass(data.classManagerName).newInstance match {
        case cm: ClassManager => cm
        case _ =>
          throw new ExtensionManagerException(InvalidClassManager)
      }
    }.recover {
      case ex: ClassNotFoundException =>
        throw new ExtensionManagerException(NotFoundClassManager(data.classManagerName))
      case ex: InstantiationException =>
        throw new IllegalStateException(ex)
      case ex: IllegalAccessException =>
        throw new IllegalStateException(ex)
    }.get
  }

  private def initializedClassManager(cm: ClassManager): ClassManager = {
    try {
      if (!_workspace.compilerTestingMode)
        cm.runOnce(this)
    } catch {
      case ex: ExtensionException =>
        System.err.println("Error while initializing extension.")
        System.err.println("Error is: " + ex)
        throw ex
    }
    cm
  }

  private def connectToJar(fileURL: URL): JarURLConnection = {
    try {
      val jarURL = new URL("jar", "", fileURL.toString + "!/")
      jarURL.openConnection.asInstanceOf[JarURLConnection]
    } catch {
      case _ : FileNotFoundException | _ : IOException =>
        throw new ExtensionManagerException(ExtensionNotFound(fileURL.toString))
    }
  }

  // throwing an IOException indicates an established connection is not working, so we have a problem
  @throws(classOf[IOException])
  private def extensionData(extensionName: String, fileURL: URL, jarConnection: JarURLConnection): ExtensionData = {
    Option(jarConnection.getManifest).map { manifest =>
      val attr = manifest.getMainAttributes

      val version       = Option(attr.getValue("NetLogo-Extension-API-Version"))
      val prefix        = Option(attr.getValue("Extension-Name")).getOrElse(throw new ExtensionManagerException(NoExtensionName))
      val classMangName = Option(attr.getValue("Class-Manager")).getOrElse(throw new ExtensionManagerException(NoClassManager))

      ExtensionData(extensionName, fileURL, prefix, classMangName, version, jarConnection.getLastModified)

    }.getOrElse(throw new ExtensionManagerException(NoManifest))
  }

  // used by extensions
  @throws(classOf[CompilerException])
  def readFromString(source: String): AnyRef =
    return _workspace.readFromString(source)

  def clearAll(): Unit =
    for (jar <- jars.values) {
      jar.classManager.clearAll
    }

  @throws(classOf[CompilerException])
  def readExtensionObject(extName: String, typeName: String, value: String): ExtensionObject = {
    val upcaseExtName = extName.toUpperCase
    jars.values
      .filter(theJarContainer =>
        theJarContainer.loaded && theJarContainer.normalizedName == upcaseExtName)
      .map { theJarContainer =>
        try {
          theJarContainer.classManager.readExtensionObject(this, typeName, value)
        } catch {
          case ex: ExtensionException =>
            System.err.println(ex)
            throw new IllegalStateException(s"Error reading extension object $upcaseExtName:$typeName $value ==> ${ex.getMessage}")
        }
      }.headOption.getOrElse(null)
  }

  def replaceIdentifier(name: String): Primitive = {
    val (primName, relevantFilter) =
      if (name.contains(':')) {
        val Array(prefix, pname) = name.split(":")
        (pname, { (jc: ExtensionManager.JarContainer) => prefix == jc.normalizedName })
      } else
        (name,  { (jc: ExtensionManager.JarContainer) => jc.primManager.autoImportPrimitives })
    jars.values.filter(liveJars.contains)
      .filter(relevantFilter)
      .map(_.primManager.getPrimitive(primName)).headOption.orNull
  }

  /**
   * Returns a String describing all the loaded extensions.
   */
  def dumpExtensions: String = tabulate(
    Seq("EXTENSION", "LOADED", "MODIFIED", "JARPATH"),
    (jarContainer =>
        Seq(Seq(jarContainer.prefix, jarContainer.loaded.toString, jarContainer.modified.toString, jarContainer.fileURL.toString))))

  /**
   * Returns a String describing all the loaded extensions.
   */
  def dumpExtensionPrimitives: String = tabulate(
    Seq("EXTENSION", "PRIMITIVE", "TYPE"),
    (jarContainer => jarContainer.primManager.getPrimitiveNames.map { n =>
      val p = jarContainer.primManager.getPrimitive(n)
      Seq(jarContainer.prefix, n, if (p.isInstanceOf[Reporter]) "Reporter" else "Command")
    }.toSeq))

  def reset() = {
    jars.values.foreach(_.unload(this))
    jars = Map[URL, JarContainer]()
    liveJars = Set[JarContainer]()
  }

  private def tabulate(header: Seq[String], generateRows: (ExtensionManager.JarContainer) => Seq[Seq[String]]) = {
    val separator = header.map(n => "-" * n.length)
    val rows = jars.values.flatMap(generateRows)
    (Seq(header, separator) ++ rows).map(_.mkString("\t")).mkString("", "\n", "\n")
  }

  private[workspace] def getAdditionalJars(folder: JFile): Seq[URL] =
    if (folder.exists && folder.isDirectory)
      folder.listFiles
        .filter(file => file.isFile && file.getName.toUpperCase.endsWith(".JAR"))
        .map(f => Try(ExtensionManager.toURL(f)).recover {
          case ex: MalformedURLException => throw new IllegalStateException(ex)
        }.get)
    else
      Seq[URL]()

  def startFullCompilation(): Unit = {
    liveJars = Set[ExtensionManager.JarContainer]()
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
        _workspace.warningMessage(
          """|Could not determine version of NetLogo extension.
             |NetLogo can try to load the extension, but it might not work.""".stripMargin.lines.mkString(" "))
      else if (currentVer != extensionVer.get)
        _workspace.warningMessage(
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

  private def getJarContainerByIdentifier(identifier: String): Option[ExtensionManager.JarContainer] =
    jars.values.find(_.extensionName.equalsIgnoreCase(identifier))
}

object ExtensionManagerException {
  sealed trait Cause {
    def message: String = "An error occurred in ExtensionManager"
  }
  case class ExtensionNotFound(extName: String) extends Cause {
    override def message: String = ExtensionManager.EXTENSION_NOT_FOUND + extName
  }
  case object NoExtensionName extends Cause {
    override def message: String = "Bad extension: Can't find extension name in Manifest."
  }
  case object NoClassManager extends Cause {
    override def message: String = "Bad extension: Couldn't locate Class-Manager tag in Manifest File"
  }
  case object InvalidClassManager extends Cause {
    override def message: String = "Bad extension: The ClassManager doesn't implement org.nlogo.api.ClassManager"
  }
  case class NotFoundClassManager(name: String) extends Cause {
    override def message: String = s"Can't find class $name in extension"
  }
  case object NoManifest extends Cause {
    override def message: String = "Bad extension: Can't find a Manifest file in extension"
  }
  case object UserHalted extends Cause {
    override def message: String = "User halted compilation"
  }
}

class ExtensionManagerException(message: String, val cause: ExtensionManagerException.Cause) extends Exception(message) {
  def this(cause: ExtensionManagerException.Cause) = this(cause.message, cause)
}
