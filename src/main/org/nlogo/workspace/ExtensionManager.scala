// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.net.{JarURLConnection, URL, URLClassLoader, MalformedURLException}
import org.nlogo.api.{Dump, ImportErrorHandler, Reporter, ExtensionObject, Primitive, ExtensionException, ClassManager, ErrorSource}
import collection.mutable.HashMap
import scala.util.control.Exception
import java.io.{IOException, FileNotFoundException, PrintWriter}
import org.nlogo.util.WebStartUtils

/**
 * Some simple notes on loading and unloading extensions:
 * - The load method is called when an extension appears in the extensions block when it wasn't
 * there in the last compilation
 * - The unload method is called when an extension is removed from the extensions block
 *
 * Before a compilation, N extensions might be loaded.
 * For example, if the extensions block previously said: extensions [ array table ]
 * Then the array and table extensions will have their loaded and live flags set to true.
 * For a new compilation, we have to remember which extensions are loaded so that we dont call load on them again.
 * But we have to know which extensions were removed from the list, so that we call unload on them.
 * For example, if the extensions block now says: extensions [ array ]
 * then the table extension needs to be unloaded. But the array extension does not need to be loaded again.
 * To accomplish this we need another flag, which i call 'live'. (it used to be reloaded).
 * During the main compile when/if we reach the extensions block
 * the compiler calls startFullCompilation and
 * the ExtensionManager sets the live flag on all extensions to false.
 * Then, for each extension in the block, its live flag is set to true.
 * If the extension wasnt previously in the block, its loaded flag will be false
 * and the ExtensionManager will set loaded to true and call the load method on it.
 * Then, when we come across extension primitives later in compilation, we simply check
 * the live flag. If the extension isn't live, then the primitive isn't found.
 * For example, if someone removed table from the extensions block, the table extension
 * will have live=false, and if they call table:make, then error.
 * At the end of main compilation, the compiler calls the finishFullCompilation,
 * and the ExtensionManager calls unload on any extensions that have loaded=true and live=false.
 *
 * Subprogram compilations just check the live flag in the same way, and everyone is happy.
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

class ExtensionManager(val workspace: AbstractWorkspace) extends org.nlogo.api.ExtensionManager {

  /* cities and other extensions may want access to [workspace].  it
   * means violating the org.nlogo.api interface, but it it does
   * give them a way to manipulate the world and such without
   * breaking the existing extensions API -- CLB
   */

  private val ArchiveFileEnding = ".jar"
  private val WsExtensionsFolderName = "netlogo_extensions"
  private val WsPolicyFileName = "extensions.jarmarker"

  // ugly stuff to ensure that we only load
  // the soundbank once. guess anyone else can use it too.
  private var obj: Option[AnyRef] = None
  private var jarsLoaded = 0
  private var isFirst = true
  private final val jars = new HashMap[String, JarContainer]


  override def profilingEnabled : Boolean = {
    workspace.profilingEnabled
  }

  override def loadedExtensions : java.lang.Iterable[ClassManager] = {
    import scala.collection.JavaConverters._
    jars.values.map(_.classManager).asJava
  }

  override def getSource(filename: String): String = {
    workspace.getSource(filename)
  }

  override def getFile(path: String): org.nlogo.api.File = {
    workspace.fileManager.getFile(getFullPath(path).get)
  }

  override def readFromString(source: String): AnyRef = {
    workspace.readFromString(source)
  }

  def isWebStart: Boolean = {
    workspace.isWebStart
  }

  override def anyExtensionsLoaded : Boolean = {
    jarsLoaded > 0
  }

  override def storeObject(obj: AnyRef) {
    this.obj = Option(obj)
  }

  override def retrieveObject : AnyRef = {
    obj.get
  }
  
  // called each time extensions is parsed
  private def identifierToJar(id: String): String = {
    // If we are given a jar name, then we look for that otherwise
    // we assume that we have been given an extensions name.
    // Extensions are folders which have a jar with the same name
    // in them (plus other things if needed) -- CLB
    val ending = {
      if (!id.endsWith(ArchiveFileEnding))
        if (AbstractWorkspace.isApplet)
          "/" + id + ArchiveFileEnding
        else
          java.io.File.separator + id + ArchiveFileEnding
      else
        ""
    }
    id + ending
  }

  override def importExtension(extName: String, errors: ErrorSource) {

    // A better alternative to `isFirst` would be preferable
    if (isFirst && isWebStart) {
      isFirst = false
      WebStartUtils.extractAllFilesFromJarByMarker(WsPolicyFileName, WsExtensionsFolderName)
    }

    // This spaghetti is a bit much for me to refactor properly... --JAB
    var jarPath = {
      val temp = this.getClass.getClassLoader.getResource(extName + ArchiveFileEnding)
      if (temp != null)
        temp.toString
      else
        identifierToJar(extName)
    }

    try {
      jarPath = resolvePathAsURL(jarPath)
      if (AbstractWorkspace.isApplet) {
        val url = new URL(jarPath)
        // added in r43348. motivation: https://trac.assembla.com/nlogo/ticket/647 .
        // we need to work around http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6785446
        url.openConnection.setUseCaches(false)
      }
    }
    catch {
      case ex: RuntimeException =>
        ex.printStackTrace()
        errors.signalError("Can't find extension: " + extName)
        return
      case e: MalformedURLException =>
        e.printStackTrace()
      case e: IOException =>
        e.printStackTrace()
    }

    try {

      val myClassLoader = getClassLoader(jarPath, errors, getClass.getClassLoader).getOrElse(return)
      val classManager = getClassManager(jarPath, myClassLoader, errors).getOrElse(return)
      val containerOption = jars.get(jarPath)
      val modified = getModified(jarPath, errors)

      // Check to see if he have seen this Jar before
      val container = if (containerOption.isEmpty || (containerOption.get.modified != modified)) {

        containerOption foreach (_.classManager.unload(this))

        val innerContainer = new JarContainer(extName, jarPath, myClassLoader, modified)

        try {
          // compilation tests shouldn't initialize the extension
          if (!workspace.compilerTestingMode)
            classManager.runOnce(this)
        }
        catch {
          case ex: ExtensionException =>
            System.err.println("Error while initializing extension.")
            System.err.println("Error is: " + ex)
            throw ex
        }

        jars.put(jarPath, innerContainer)
        innerContainer

      }
      else {
        containerOption.get
      }

      // Check to see if it has been loaded into the model
      if (!container.loaded) {
        jarsLoaded += 1
        container.loaded = true
        container.classManager = classManager
        container.primManager = new ExtensionPrimitiveManager(extName)
        classManager.load(container.primManager)
      }

      // Jars that have been removed won't get this flag set
      container.live = true
      container.prefix = getExtensionName(jarPath, errors)

    }

    catch {
      case ex: ExtensionException =>
        errors.signalError(ex.getMessage)
        return
      case ex: IncompatibleClassChangeError =>
        // thrown if extension classes from different version are incompatible
        // catching this is necessary so it doesn't just choke
        errors.signalError("This extension doesn't work with this version of NetLogo")
        System.err.println(ex)
    }

  }

  override def addToLibraryPath(classManager: AnyRef, directory: String) {
    org.nlogo.api.JavaLibraryPath.setLibraryPath(classManager.getClass, directory)
  }

  override def resolvePath(path: String): String = {
    try {
      val result = new java.io.File(workspace.attachModelDir(path))
      if (AbstractWorkspace.isApplet)
        result.getPath
      else {
        try
          result.getCanonicalPath
        catch {
          case ex: IOException =>
            result.getPath
        }
      }
    }
    catch {
      case ex: MalformedURLException =>
        throw new IllegalStateException(path + " is not a valid pathname: " + ex)
    }
  }

  override def resolvePathAsURL(path: String): String = {

    if (AbstractWorkspace.isApplet) {
      try {
        val jarPath = workspace.fileManager.attachPrefix(path)
        if (org.nlogo.api.RemoteFile.exists(jarPath))
          return jarPath
        else
          throw new IllegalStateException("Can't find extension " + path + " using URL " + jarPath)
      }
      catch {
        case ex: MalformedURLException =>
          throw new IllegalStateException(path + " is not a valid pathname: " + ex)
      }
    }

    Exception.ignoring(classOf[MalformedURLException]) {
      if (isWebStart) {
        val jarFile = new java.io.File(WebStartUtils.getWebStartPath(WsExtensionsFolderName) + path)
        if (jarFile.exists)
          return ExtensionManager.file2LocalURLStr(jarFile)
      }
    }

    // Is this a URL right off the bat?
    Exception.ignoring(classOf[MalformedURLException]) {
      return new URL(path).toString
    }

    // If it's a path, look for it relative to the model location
    Exception.ignoring(classOf[MalformedURLException]) {
    if (path.contains(java.io.File.separator)) {
        val jarFile = new java.io.File(workspace.attachModelDir(path))
        if (jarFile.exists)
          return ExtensionManager.file2LocalURLStr(jarFile)
      }
    }

    // If it's not a path, try the model location
    Exception.ignoring(classOf[MalformedURLException]) {
      val jarFile = new java.io.File(workspace.attachModelDir(path))
      if (jarFile.exists)
        return ExtensionManager.file2LocalURLStr(jarFile)
    }

    // Then try the extensions folder
    Exception.ignoring(classOf[MalformedURLException]) {
      val jarFile = new java.io.File("extensions" + java.io.File.separator + path)
      if (jarFile.exists)
        return ExtensionManager.file2LocalURLStr(jarFile)
    }

    // Give up
    throw new IllegalStateException("Can't find extension " + path)

  }

  def getFullPath(path: String): Option[String] = {
    if (AbstractWorkspace.isApplet) {
      try {
        val fullPath = workspace.fileManager.attachPrefix(path)
        if (org.nlogo.api.RemoteFile.exists(fullPath))
          Option(fullPath)
        else
          throw new ExtensionException("Can't find file " + path + " using " + fullPath)
      }
      catch {
        case ex: MalformedURLException =>
          throw new ExtensionException(path + " is not a valid pathname: " + ex)
      }
    }
    else {
      Exception.handling(classOf[MalformedURLException]).by(_ => None) {
        val fullPath = workspace.attachModelDir(path)
        if (new java.io.File(fullPath).exists)
          Option(fullPath)
        else {
          // Then try the extensions folder
          val f = new java.io.File("extensions" + java.io.File.separator + path)
          if (f.exists)
            Option(f.getPath)
          else // Give up
            throw new ExtensionException("Can't find file " + path)
        }
      }
    }
  }

  // We only want one ClassLoader for every Jar per NetLogo instance
  private def getClassLoader(jarPath: String, errors: ErrorSource, parentLoader: ClassLoader): Option[URLClassLoader] = {

    jars.get(jarPath) map (_.jarClassLoader) orElse {

      try {

        val jarURL = new URL(jarPath)

        // Have the class loader look at URLs from the original extension .jar, the other .jars in the dir,
        // and the others in the `extensions` folder
        val parentFolder = new java.io.File(new java.io.File(jarURL.getFile).getParent)
        val extensionsFolder = new java.io.File("extensions")
        val urls = jarURL :: getAdditionalJars(parentFolder) ::: getAdditionalJars(extensionsFolder)

        // We use the URLClassLoader.newInstance method because that works with
        // the applet SecurityManager, even tho newLClassLoader(..) does not.
        Option(java.net.URLClassLoader.newInstance(urls.toArray, parentLoader))

      }
      catch {
        case ex: MalformedURLException =>
          errors.signalError("Invalid URL: " + jarPath)
          None
      }

    }

  }

  // We want a new ClassManager per Jar Load
  private def getClassManager(jarPath: String, myClassLoader: URLClassLoader, errors: ErrorSource): Option[ClassManager] = {

    jars.get(jarPath) foreach (container => if (container.loaded) return Option(container.classManager))

    try {
      // Class must be named in Manifest file
      val classMangName = getClassManagerName(jarPath, errors)
      if (classMangName != null) {
        try
          return Option(myClassLoader.loadClass(classMangName).newInstance.asInstanceOf[ClassManager])
        catch {
          case ex: ClassCastException =>
            errors.signalError("Bad extension: The ClassManager doesn't implement " + "org.nlogo.api.ClassManager")
        }
      }
      else
        errors.signalError("Bad extension: Couldn't locate Class-Manager tag in Manifest File")
    }
    catch {
      case ex: FileNotFoundException =>
        errors.signalError("Can't find extension " + jarPath)
      case ex: IOException =>
        errors.signalError("Can't open extension " + jarPath)
      case ex: InstantiationException =>
        throw new IllegalStateException(ex)
      case ex: IllegalAccessException =>
        throw new IllegalStateException(ex)
      case ex: ClassNotFoundException =>
        errors.signalError("Can't find class " + getClassManagerName(jarPath, errors) + " in extension")
    }

    None

  }

  /**
   * Gets the name of an extension's ClassManager implementation from the manifest.
   */
  private def getClassManagerName(jarPath: String, errors: ErrorSource): String = {

    val jarConnection = new URL("jar", "", jarPath + "!/").openConnection.asInstanceOf[JarURLConnection]

    if (jarConnection.getManifest == null)
      errors.signalError("Bad extension: Can't find a Manifest file in extension")

    val attr = jarConnection.getManifest.getMainAttributes
    val name = attr.getValue("Class-Manager")

    if (!checkVersion(attr))
      errors.signalError("User halted compilation")

    name

  }

  /**
   * Gets the extension name from the manifest.
   */
  private def getExtensionName(jarPath: String, errors: ErrorSource): String = {

    try {

      val jarConnection = new URL("jar", "", jarPath + "!/").openConnection.asInstanceOf[JarURLConnection]

      if (jarConnection.getManifest == null)
        errors.signalError("Bad extension: Can't find Manifest file in extension")

      val attr = jarConnection.getManifest.getMainAttributes
      val name = attr.getValue("Extension-Name")

      if (name == null)
        errors.signalError("Bad extension: Can't find extension name in Manifest.")

      return name

    }
    catch {
      case ex: FileNotFoundException =>
        errors.signalError("Can't find extension " + jarPath)
      case ex: IOException =>
        errors.signalError("Can't open extension " + jarPath)
    }

    null

  }

  def clearAll() {
    jars.values.toList foreach { _.classManager.clearAll() }
  }

  override def readExtensionObject(extName: String, typeName: String, value: String): ExtensionObject = {
    def findExtensionObject(extName: String, typeName: String, value: String): Option[ExtensionObject] = {
      jars.values.toList collectFirst {
        case container if (container.loaded && (container.primManager.name.equalsIgnoreCase(extName))) =>
          try
            container.classManager.readExtensionObject(this, typeName, value)
          catch {
            case ex: ExtensionException =>
              System.err.println(ex)
              throw new IllegalStateException("Error reading extension object " + extName + ":" + typeName + " " + value + " ==> " + ex.getMessage)
          }
      }
    }
    findExtensionObject(extName, typeName, value).getOrElse(null)  // Totally gross... --JAB
  }

  override def replaceIdentifier(name: String): Option[Primitive] = {

    val sepIndex = name.indexOf(':')
    val (prefix, pname) = name.splitAt(sepIndex) match { case (x, y) => (x, y drop 1) } // Get rid of ':' at beginning of `y`
    val isQualified = sepIndex != -1

    jars.values.toList.collectFirst {
      case container if (container.live && isQualified && (prefix == container.primManager.name.toUpperCase)) => (container, pname)
      case container if (container.live && !isQualified && container.primManager.autoImportPrimitives)        => (container, name)
    } flatMap { case (container, id) => Option(container.primManager.getPrimitive(id)) }

  }

  /**
   * Returns a String describing all the loaded extensions.
   */
  override def dumpExtensions : String = {
    val types = List("EXTENSION", "LOADED", "MODIFIED", "JARPATH")
    val str = types.mkString("", "\t", "\n") + types.map (x => List.fill(x.size)('-').mkString).mkString("", "\t", "\n")
    val extras = jars.values.toList map { container => import container._; List(prefix, loaded, modified, jarName).mkString("", "\t", "\n") }
    (str :: extras).mkString
  }

  override def getJarPaths : List[String] = {
    import scala.collection.JavaConversions._
    jars.values.toList flatMap {
      jar =>
        val thisJarPath = jar.extensionName + '/' + jar.extensionName + ArchiveFileEnding
        val additionalJarPaths = jar.classManager.additionalJars.toList map (aJar => jar.extensionName + '/' + aJar)
        thisJarPath :: additionalJarPaths
    }
  }

  override def getExtensionNames : List[String] = {
    jars.values.toList map (_.extensionName)
  }

  /**
   * Returns a String describing all the loaded extensions.
   */
  override def dumpExtensionPrimitives : String = {

    val ptypes = List("EXTENSION", "PRIMITIVE", "TYPE")
    val pstr = ptypes.mkString("\n\n", "\t", "\n") + ptypes.map (x => List.fill(x.size)('-').mkString).mkString("", "\t", "\n")

    import scala.collection.JavaConversions._   // Necessary so we can deal with the Java iterator returned by getPrimitiveNames()
    val extras = jars.values.toList flatMap {
      jarContainer =>
        jarContainer.primManager.getPrimitiveNames().toList map {
          name =>
            val p = jarContainer.primManager.getPrimitive(name)
            val ptype = (p match { case r: Reporter => "Reporter"; case c => "Command"})
            List(jarContainer.prefix, name, ptype).mkString("", "\t", "\n")
        }
    }

    (pstr :: extras).mkString

  }

  // Called by CompilerManager when a model is changed
  override def reset {

    jars.values.toList foreach {
     container =>

        try
          container.classManager.unload(this)
        catch {
          case ex: ExtensionException =>
            System.err.println(ex)
            // don't throw an illegal state exception,
            // just because one extension throws an error
            // doesn't mean we shouldn't unload the rest
            // and continue with the operation ev 7/3/08
            ex.printStackTrace()
        }

        container.loaded = false
        container.live = false
        container.jarClassLoader = null

    }

    jars.clear()
    jarsLoaded = 0

  }

  private def getAdditionalJars(folder: java.io.File): List[URL] = {
    if (!AbstractWorkspace.isApplet && folder.exists && folder.isDirectory) {
      folder.listFiles().toList collect {
        case file if (file.isFile && file.getName.toUpperCase.endsWith(ArchiveFileEnding.toUpperCase)) =>
          try
            ExtensionManager.toURL(file)
          catch {
            case ex: MalformedURLException =>
              throw new IllegalStateException(ex)
          }
      }
    }
    else
      List()
  }

  // forget which extensions are in the extensions [ ... ] block
  override def startFullCompilation {
    jars.values.toList foreach (_.live = false)
  }

  // Used to see if any IMPORT keywords have been removed since last compilation
  override def finishFullCompilation {
    jars.values.toList foreach {
      container =>
        if (container.loaded && !container.live) {
          try {
            jarsLoaded -= 1
            jars.remove(container.prefix)
            container.loaded = false
            container.classManager.unload(this)
          }
          catch {
            case ex: ExtensionException =>
              // i'm not sure how to handle this yet
              System.err.println("Error unloading extension: " + ex)
          }
        }
    }
  }

  private def checkVersion(attr: java.util.jar.Attributes): Boolean = {

    val jarVer = attr.getValue("NetLogo-Extension-API-Version")
    val currentVer = org.nlogo.api.APIVersion.version

    if (jarVer == null)
      workspace.warningMessage("Could not determine version of NetLogo extension.  NetLogo can " + "try to load the extension, but it might not work.")
    else if (currentVer != jarVer)
      workspace.warningMessage("You are attempting to open a NetLogo extension file that was created " + "for a different version of the NetLogo Extension API.  (This NetLogo uses Extension API " + currentVer + "; the extension uses NetLogo Extension API " + jarVer + ".)  NetLogo can try to load the extension, " + "but it might not work.")
    else
      true

  }

  private def getModified(jarPath: String, errors: ErrorSource): Long = {
    try
      new URL(jarPath).openConnection.getLastModified
    catch {
      case ex: IOException =>
        System.err.println(ex)
        errors.signalError("Can't open extension")
        // this is unreachable, since signalError never returns.
        // we have to have it, though, since jikes can't figure that out.
        throw new IllegalStateException("this code is unreachable")
    }
  }

  def exportWorld(writer: PrintWriter) {

    writer.println(Dump.csv.encode("EXTENSIONS"))
    writer.println()

    jars.values.toList foreach {
      container =>
        val data = container.classManager.exportWorld
        if (data.length > 0) {
          writer.println(Dump.csv.encode(container.extensionName))
          writer.print(data)
          writer.println()
        }
    }

  }

  def importExtensionData(name: String, data: java.util.List[Array[String]], handler: ImportErrorHandler) {
    getJarContainerByIdentifier(name)
            .getOrElse(throw new ExtensionException("there is no extension named " + name + "in this model"))
            .classManager.importWorld(data, this, handler)
  }

  def isExtensionName(name: String): Boolean = {
    !getJarContainerByIdentifier(name).isEmpty
  }

  private def getJarContainerByIdentifier(identifier: String): Option[JarContainer] = {
    jars.values.toList collectFirst { case jar if (jar.extensionName.equalsIgnoreCase(identifier)) => jar }
  }

  private class JarContainer(val extensionName: String, val jarName: String, var jarClassLoader: URLClassLoader, val modified: Long) {
    var prefix: String = null
    var primManager: ExtensionPrimitiveManager = null
    var classManager: ClassManager = null

    // live means the extension is currently in the extensions [ ... ] block in the code.
    // if an extension is live, then its primitives are available to be called. JC - 12/3/10
    var live = false

    // loaded means that the load method has been called for this extension.
    // any further recompiles with extension still in it should not call the load method.
    // the extension can later be unloaded by removing it from the extensions [ ... ] block
    // at that time, its unload method will be called, and loaded will be set to false.
    // if it ever reappears in the extensions [ ... ] block, then load will be called again
    // etc, etc. JC - 12/3/10
    var loaded = false
  }

}

object ExtensionManager {
  // for 4.1 we have too much fragile, difficult-to-understand,
  // under-tested code involving URLs -- we can't get rid of our
  // uses of toURL() until 4.2, the risk of breakage is too high.
  // so for now, at least we make this a separate method so the
  // SuppressWarnings annotation is narrowly targeted. - ST 12/7/09
  @SuppressWarnings(Array("deprecation"))
  private def toURL(file: java.io.File): URL = {
    file.toURI.toURL
  }

  private def file2LocalURLStr(file: java.io.File): String = {
    toURL(file).toString.replaceAll("%20", " ")
  }

}
