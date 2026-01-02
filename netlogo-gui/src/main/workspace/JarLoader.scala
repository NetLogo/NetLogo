// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.io.{ File, FileNotFoundException, IOException }
import java.net.{ JarURLConnection, MalformedURLException, URL, URLClassLoader }
import java.nio.file.{ Files, Path, Paths }

import ExtensionManager.ExtensionData
import ExtensionManagerException._

import org.nlogo.api.{ ClassManager, ExtensionManager => APIEM }

import scala.collection.mutable.Map
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.Try

object JarLoader {
  private val copyRoot = Paths.get(System.getProperty("java.io.tmpdir"), "nl_ext_temp")

  // these are stored globally so that existing copies can be reused in cases like BehaviorSpace
  // where each new workspace has a separate JarLoader instance (Isaac B 1/2/26)
  private val copiedURLs = Map[URL, URLCache]()

  // called during initialization of App to reduce tmpdir bloat (Isaac B 1/2/26)
  def deleteCopies(): Unit = {
    if (Files.exists(copyRoot))
      copyRoot.toFile.listFiles.foreach(deleteRecursive)
  }

  private def deleteRecursive(file: File): Unit = {
    if (file.isDirectory)
      file.listFiles.foreach(deleteRecursive)

    file.delete()
  }

  private case class URLCache(primary: URL, others: Array[URL])
}

class JarLoader(workspace: ExtendableWorkspace) extends ExtensionManager.ExtensionLoader {

  import JarLoader._

  def locateExtension(extensionName: String): Option[URL] =
    try {
      resolvePathAsURL(identifierToJar(extensionName))
    } catch {
      case ex@(_: RuntimeException | _: MalformedURLException)=>
        None
    }

  def extensionData(extensionName: String, fileURL: URL) = {
    val connection = connectToJar(getCopiedExtension(fileURL).primary)
    Option(connection.getManifest).map { manifest =>
      val attr = manifest.getMainAttributes

      val version       = Option(attr.getValue("NetLogo-Extension-API-Version"))
      // note - the prefix is not really used anywhere, the extensionName drives the behavior.
      // But no assertion is ever made that prefix == extensionName
      val prefix        = Option(attr.getValue("Extension-Name")).getOrElse(throw new ExtensionManagerException(NoExtensionName(extensionName)))
      val classMangName = Option(attr.getValue("Class-Manager")).getOrElse(throw new ExtensionManagerException(NoClassManager(extensionName)))

      ExtensionData(extensionName, fileURL, prefix, classMangName, version, new File(fileURL.toURI).lastModified)
    }.getOrElse(throw new ExtensionManagerException(NoManifest(extensionName)))
  }

  def extensionClassLoader(fileURL: URL, parentLoader: ClassLoader): ClassLoader = {
    val cache = getCopiedExtension(fileURL)

    URLClassLoader.newInstance(cache.primary +: cache.others, parentLoader)
  }

  def extensionClassManager(classLoader: ClassLoader, data: ExtensionData): ClassManager =
    try {
      classLoader.loadClass(data.classManagerName).getDeclaredConstructor().newInstance() match {
        case cm: ClassManager => cm
        case _                =>
          throw new ExtensionManagerException(InvalidClassManager(data.extensionName))
      }
    } catch {
      case ex: ClassNotFoundException =>
        throw new ExtensionManagerException(NotFoundClassManager(data.classManagerName))
      case ex@(_: InstantiationException | _: IllegalAccessException) =>
        throw new IllegalStateException(ex)
    }

  private def connectToJar(fileURL: URL): JarURLConnection =
    try {
      val jarURL = new URL("jar", "", fileURL.toString + "!/")
      jarURL.openConnection.asInstanceOf[JarURLConnection]
    } catch {
      case _ : FileNotFoundException | _ : IOException =>
        throw new ExtensionManagerException(ExtensionNotFound(fileURL.toString))
    }

  private def identifierToJar(id: String): String =
    if (!id.endsWith(".jar"))
      id + File.separator + id + ".jar"
    else
      id

  private[workspace] def resolvePathAsURL(path: String): Option[URL] =
    Seq(
      workspace.attachModelDir(path),
      s"${APIEM.extensionsPath}${File.separator}$path",
      s"${APIEM.userExtensionsPath}${File.separator}$path",
      s"${APIEM.extensionsPath}${File.separator}.bundled${File.separator}$path"
    ).map(new File(_)).filter(_.exists).map(_.toURI.toURL).headOption

  private def getAdditionalJars(folder: File): Array[URL] =
    if (folder.exists && folder.isDirectory)
      folder.listFiles
        .filter(file => file.isFile && file.getName.toUpperCase.endsWith(".JAR"))
        .map(f => Try(f.toURI.toURL).recover {
          case ex: MalformedURLException => throw new IllegalStateException(ex)
        }.get)
    else
      Array[URL]()

  // the current implementation of URLConnection does not release file locks until the app is closed, which
  // prevents users from uninstalling an extension that they used during that session, even if the extension
  // is unloaded. the only way to get around that issue at the moment is to copy the extension to a temporary
  // directory and load it from there instead, allowing the removal of the original extension files to proceed
  // as expected. (Isaac B 1/2/26)
  private def getCopiedExtension(primary: URL): URLCache = {
    if (!JarLoader.copiedURLs.contains(primary)) {
      Files.createDirectories(JarLoader.copyRoot)

      val jarPath = Path.of(primary.toURI)
      val folderContainingJar = jarPath.getParent
      val tempDir = Files.createTempDirectory(JarLoader.copyRoot, null)

      Files.walk(folderContainingJar).iterator.asScala.foreach { path =>
        if (!Files.isDirectory(path)) {
          val dest = tempDir.resolve(folderContainingJar.relativize(path))

          Files.createDirectories(dest.getParent)
          Files.copy(path, dest)
        }
      }

      val tempPrimary = tempDir.resolve(folderContainingJar.relativize(jarPath)).toUri.toURL
      val others = getAdditionalJars(tempDir.toFile) ++ getAdditionalJars(new File("extensions"))

      JarLoader.copiedURLs(primary) = URLCache(tempPrimary, others)
    }

    JarLoader.copiedURLs(primary)
  }
}
