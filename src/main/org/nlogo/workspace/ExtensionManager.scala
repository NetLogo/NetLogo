// (C) 2012 Uri Wilensky. https://github.com/NetLogo/NetLogo
package org.nlogo.workspace

import java.util.ArrayList
import java.util.Iterator
import java.util.List
import java.util.Map
import annotation.strictfp
import java.net.{JarURLConnection, URL, URLClassLoader, MalformedURLException}
import java.io.{PrintWriter, FileNotFoundException, IOException}
import org.nlogo.api.{Dump, ImportErrorHandler, Reporter, ExtensionObject, Primitive, ExtensionException, ClassManager, ErrorSource}
import collection.mutable.HashMap

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

@strictfp
class ExtensionManager(val workspace: AbstractWorkspace) extends org.nlogo.api.ExtensionManager {

  /* cities and other extensions may want access to [workspace].  it
   * means violating the org.nlogo.api interface, but it it does
   * give them a way to manipulate the world and such without
   * breaking the existing extensions API -- CLB
   */


  // ugly stuff to ensure that we only load
  // the soundbank once. guess anyone else can use it too.
  private var obj: Option[AnyRef] = None
  private var jarsLoaded = 0
  private final val jars = new HashMap[String, ExtensionManager#JarContainer]


  override def profilingEnabled : Boolean = {
    workspace.profilingEnabled
  }

  override def getSource(filename: String) : String = {
    workspace.getSource(filename)
  }

  override def getFile(path: String) : org.nlogo.api.File = {
    workspace.fileManager.getFile(getFullPath(path))
  }

  override def readFromString(source: String) : AnyRef = {
    workspace.readFromString(source)
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
  private def identifierToJar(id: String) : String = {
    // If we are given a jar name, then we look for that otherwise
    // we assume that we have been given an extensions name.
    // Extensions are folders which have a jar with the same name
    // in them (plus other things if needed) -- CLB
    val ending = {
      if (!id.endsWith(".jar")) {
        if (AbstractWorkspace.isApplet) {
          "/" + id + ".jar"
        }
        else {
          java.io.File.separator + id + ".jar"
        }
      }
      else {
        ""
      }
    }
    id + ending
  }

  override def importExtension(extName: String, errors: ErrorSource) {
    var jarPath: String = identifierToJar(extName)
    try {
      jarPath = resolvePathAsURL(jarPath)
      if (AbstractWorkspace.isApplet) {
        val url: URL = new URL(jarPath)
        // added in r43348. motivation: https://trac.assembla.com/nlogo/ticket/647 .
        // we need to work around http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6785446
        url.openConnection.setUseCaches(false)
      }
    }
    catch {
      case ex: RuntimeException => {
        ex.printStackTrace()
        errors.signalError("Can't find extension: " + extName)
        return
      }
      case e: MalformedURLException => {
        e.printStackTrace()
      }
      case e: IOException => {
        e.printStackTrace()
      }
    }
    try {
      val myClassLoader = getClassLoader(jarPath, errors, getClass.getClassLoader)
      if (myClassLoader == null) {
        return
      }
      val classManager = getClassManager(jarPath, myClassLoader, errors)
      if (classManager == null) {
        return
      }
      var theJarContainer = jars.get(jarPath)
      val modified = getModified(jarPath, errors)
      // Check to see if he have seen this Jar before
      if ((theJarContainer == null) || (theJarContainer.modified != modified)) {
        if (theJarContainer != null) {
          theJarContainer.classManager.unload(this)
        }
        theJarContainer = new JarContainer(extName, jarPath, myClassLoader, modified)
        try {
          // compilation tests shouldn't initialize the extension
          if (!workspace.compilerTestingMode) {
            classManager.runOnce(this)
          }
        }
        catch {
          case ex: ExtensionException => {
            System.err.println("Error while initializing extension.")
            System.err.println("Error is: " + ex)
            throw ex
          }
        }
        jars.put(jarPath, theJarContainer)
      }
      // Check to see if it has been loaded into the model
      if (!theJarContainer.loaded) {
        ({
          jarsLoaded += 1; jarsLoaded
        })
        theJarContainer.loaded = true
        theJarContainer.classManager = classManager
        theJarContainer.primManager = new ExtensionPrimitiveManager(extName)
        classManager.load(theJarContainer.primManager)
      }
      // Jars that have been removed won't get this flag set
      theJarContainer.live = true
      theJarContainer.prefix = getExtensionName(jarPath, errors)
    }
    catch {
      case ex: ExtensionException => {
        errors.signalError(ex.getMessage)
        return
      }
      case ex: IncompatibleClassChangeError => {
        // thrown if extension classes from different version are incompatible
        // catching this is necessary so it doesn't just choke
        errors.signalError("This extension doesn't work with this version of NetLogo")
        System.err.println(ex)
      }
    }
  }

  override def addToLibraryPath(classManager: AnyRef, directory: String) {
    org.nlogo.api.JavaLibraryPath.setLibraryPath(classManager.getClass, directory)
  }

  override def resolvePath(path: String) : String = {
    try {
      val result: java.io.File = new java.io.File(workspace.attachModelDir(path))
      if (AbstractWorkspace.isApplet) {
        result.getPath
      }
      else {
        try {
          result.getCanonicalPath
        }
        catch {
          case ex: IOException => {
            result.getPath
          }
        }
      }
    }
    catch {
      case ex: MalformedURLException => {
        throw new IllegalStateException(path + " is not a valid pathname: " + ex)
      }
    }
  }

  override def resolvePathAsURL(path: String) : String = {
    var jarURL: URL = null
    if (AbstractWorkspace.isApplet) {
      try {
        val jarPath: String = workspace.fileManager.attachPrefix(path)
        if (org.nlogo.api.RemoteFile.exists(jarPath)) {
          return jarPath
        }
        else {
          throw new IllegalStateException("Can't find extension " + path + " using URL " + jarPath)
        }
      }
      catch {
        case ex: MalformedURLException => {
          throw new IllegalStateException(path + " is not a valid pathname: " + ex)
        }
      }
    }

    // Is this a URL right off the bat?
    try {
      jarURL = new URL(path)
      return jarURL.toString
    }
    catch {
      case ex: MalformedURLException => {
        org.nlogo.util.Exceptions.ignore(ex)
      }
    }

    // If it's a path, look for it relative to the model location
    if (path.indexOf('/') > -1) {
      try {
        val jarFile: java.io.File = new java.io.File(workspace.attachModelDir(path))
        if (jarFile.exists) {
          return ExtensionManager.toURL(jarFile).toString
        }
      }
      catch {
        case ex: MalformedURLException => {
          org.nlogo.util.Exceptions.ignore(ex)
        }
      }
    }

    // If it's not a path, try the model location
    try {
      val jarFile: java.io.File = new java.io.File(workspace.attachModelDir(path))
      if (jarFile.exists) {
        return ExtensionManager.toURL(jarFile).toString
      }
    }
    catch {
      case ex: MalformedURLException => {
        org.nlogo.util.Exceptions.ignore(ex)
      }
    }
    // Then try the extensions folder
    try {
      val jarFile: java.io.File = new java.io.File("extensions" + java.io.File.separator + path)
      if (jarFile.exists) {
        return ExtensionManager.toURL(jarFile).toString
      }
    }
    catch {
      case ex: MalformedURLException => {
        org.nlogo.util.Exceptions.ignore(ex)
      }
    }
    // Give up
    throw new IllegalStateException("Can't find extension " + path)
  }

  def getFullPath(path: String) : String = {
    if (AbstractWorkspace.isApplet) {
      try {
        val fullPath: String = workspace.fileManager.attachPrefix(path)
        if (org.nlogo.api.RemoteFile.exists(fullPath)) {
          return fullPath
        }
        else {
          throw new ExtensionException("Can't find file " + path + " using " + fullPath)
        }
      }
      catch {
        case ex: MalformedURLException => {
          throw new ExtensionException(path + " is not a valid pathname: " + ex)
        }
      }
    }
    try {
      val fullPath: String = workspace.attachModelDir(path)
      val f: java.io.File = new java.io.File(fullPath)
      if (f.exists) {
        return fullPath
      }
    }
    catch {
      case ex: MalformedURLException => {
        org.nlogo.util.Exceptions.ignore(ex)
      }
    }
    // Then try the extensions folder
    val f: java.io.File = new java.io.File("extensions" + java.io.File.separator + path)
    if (f.exists) {
      return f.getPath
    }
    // Give up
    throw new ExtensionException("Can't find file " + path)
  }

  // We only want one ClassLoader for every Jar per NetLogo instance
  private def getClassLoader(jarPath: String, errors: ErrorSource, parentLoader: ClassLoader) : URLClassLoader = {
    val theJarContainer: ExtensionManager#JarContainer = jars.get(jarPath)
    if (theJarContainer != null) {
      return theJarContainer.jarClassLoader
    }
    try {
      val jarURL: URL = new URL(jarPath)
      // all the urls our class loader will look at
      val urls: List[URL] = new ArrayList[URL]
      // start with the original extension jar
      urls.add(jarURL)
      // Get other Jars in the extensions own dir
      var folder: java.io.File = new java.io.File(new java.io.File(jarURL.getFile).getParent)
      urls.addAll(getAdditionalJars(folder))
      // Get other Jars in extensions folder
      folder = new java.io.File("extensions")
      urls.addAll(getAdditionalJars(folder))
      // We use the URLClassLoader.newInstance method because that works with
      // the applet SecurityManager, even tho newLClassLoader(..) does not.
      java.net.URLClassLoader.newInstance(urls.toArray(new Array[URL](urls.size)), parentLoader)
    }
    catch {
      case ex: MalformedURLException => {
        errors.signalError("Invalid URL: " + jarPath)
        null
      }
    }
  }

  // We want a new ClassManager per Jar Load
  private def getClassManager(jarPath: String, myClassLoader: URLClassLoader, errors: ErrorSource) : ClassManager = {
    val theJarContainer: ExtensionManager#JarContainer = jars.get(jarPath)
    if ((theJarContainer != null) && (theJarContainer.loaded)) {
      return theJarContainer.classManager
    }
    var classMangName: String = ""
    try {
      // Class must be named in Manifest file
      classMangName = getClassManagerName(jarPath, errors)
      if (classMangName == null) {
        errors.signalError("Bad extension: Couldn't locate Class-Manager tag in Manifest File")
      }
      try {
        return myClassLoader.loadClass(classMangName).newInstance.asInstanceOf[ClassManager]
      }
      catch {
        case ex: ClassCastException => {
          errors.signalError("Bad extension: The ClassManager doesn't implement " + "org.nlogo.api.ClassManager")
        }
      }
    }
    catch {
      case ex: FileNotFoundException => {
        errors.signalError("Can't find extension " + jarPath)
      }
      case ex: IOException => {
        errors.signalError("Can't open extension " + jarPath)
      }
      case ex: InstantiationException => {
        throw new IllegalStateException(ex)
      }
      case ex: IllegalAccessException => {
        throw new IllegalStateException(ex)
      }
      case ex: ClassNotFoundException => {
        errors.signalError("Can't find class " + classMangName + " in extension")
      }
    }
    null
  }

  /**
   * Gets the name of an extension's ClassManager implementation from the manifest.
   */
  private def getClassManagerName(jarPath: String, errors: ErrorSource) : String = {
    val jarURL: URL = new URL("jar", "", jarPath + "!/")
    val jarConnection: JarURLConnection = jarURL.openConnection.asInstanceOf[JarURLConnection]
    var name: String = null
    if (jarConnection.getManifest == null) {
      errors.signalError("Bad extension: Can't find a Manifest file in extension")
    }
    val attr: java.util.jar.Attributes = jarConnection.getManifest.getMainAttributes
    name = attr.getValue("Class-Manager")
    if (!checkVersion(attr)) {
      errors.signalError("User halted compilation")
    }
    name
  }

  /**
   * Gets the extension name from the manifest.
   */
  private def getExtensionName(jarPath: String, errors: ErrorSource) : String = {
    try {
      val jarURL: URL = new URL("jar", "", jarPath + "!/")
      val jarConnection: JarURLConnection = jarURL.openConnection.asInstanceOf[JarURLConnection]
      var name: String = null
      if (jarConnection.getManifest == null) {
        errors.signalError("Bad extension: Can't find Manifest file in extension")
      }
      val attr: java.util.jar.Attributes = jarConnection.getManifest.getMainAttributes
      name = attr.getValue("Extension-Name")
      if (name == null) {
        errors.signalError("Bad extension: Can't find extension name in Manifest.")
      }
      return name
    }
    catch {
      case ex: FileNotFoundException => {
        errors.signalError("Can't find extension " + jarPath)
      }
      case ex: IOException => {
        errors.signalError("Can't open extension " + jarPath)
      }
    }
    null
  }

  def clearAll() {
    for (jar <- jars.values) {
      jar.classManager.clearAll()
    }
  }

  override def readExtensionObject(extName: String, typeName: String, value: String) : ExtensionObject = {
    var theJarContainer: ExtensionManager#JarContainer = null
    // Locate the class in all the loaded Jars
    val entries: Iterator[Map.Entry[String, ExtensionManager#JarContainer]] = jars.entrySet.iterator
    while (entries.hasNext) {
      val entry: Map.Entry[String, ExtensionManager#JarContainer] = entries.next
      theJarContainer = entry.getValue
      val name: String = theJarContainer.primManager.name.toUpperCase
      if (theJarContainer.loaded && name != null && (name == extName.toUpperCase)) {
        try {
          return theJarContainer.classManager.readExtensionObject(this, typeName, value)
        }
        catch {
          case ex: ExtensionException => {
            System.err.println(ex)
            throw new IllegalStateException("Error reading extension object " + extName + ":" + typeName + " " + value + " ==> " + ex.getMessage)
          }
        }
      }
    }
    null
  }

  override def replaceIdentifier(name: String) : Primitive = {
    var prim: Primitive = null
    val qualified = name.indexOf(':') != -1
    // Locate the class in all the loaded Jars
    val entries: Iterator[Map.Entry[String, ExtensionManager#JarContainer]] = jars.entrySet.iterator
    while (entries.hasNext && prim == null) {
      val entry: Map.Entry[String, ExtensionManager#JarContainer] = entries.next
      val theJarContainer: ExtensionManager#JarContainer = entry.getValue
      val extName: String = theJarContainer.primManager.name.toUpperCase
      if (theJarContainer.live) {
        // Qualified references must match the extension name
        if (qualified) {
          val prefix: String = name.substring(0, name.indexOf(':'))
          val pname: String = name.substring(name.indexOf(':') + 1)
          if (prefix == extName) {
            prim = theJarContainer.primManager.getPrimitive(pname)
          }
        }
        else {
          if (theJarContainer.primManager.autoImportPrimitives) {
            prim = theJarContainer.primManager.getPrimitive(name)
          }
        }
      }
    }
    prim
  }

  /**
   * Returns a String describing all the loaded extensions.
   */
  override def dumpExtensions : String = {
    var str = "EXTENSION\tLOADED\tMODIFIED\tJARPATH\n"
    str += "---------\t------\t---------\t---\n"
    var theJarContainer: ExtensionManager#JarContainer = null
    // Locate the class in all the loaded Jars
    val values: Iterator[ExtensionManager#JarContainer] = jars.values.iterator
    while (values.hasNext) {
      theJarContainer = values.next
      str += theJarContainer.prefix + "\t" + theJarContainer.loaded + "\t" + theJarContainer.modified + "\t" + theJarContainer.jarName + "\n"
    }
    str
  }

  override def getJarPaths : List[String] = {
    val names = new ArrayList[String]
    for (jar <- jars.values) {
    import scala.collection.JavaConversions._
      names.add(jar.extensionName + java.io.File.separator + jar.extensionName + ".jar")
      for (additionalJar <- jar.classManager.additionalJars) {
        names.add(jar.extensionName + java.io.File.separator + additionalJar)
      }
    }
    names
  }

  override def getExtensionNames : List[String] = {
    jars.values map (_.extensionName)
  }

  /**
   * Returns a String describing all the loaded extensions.
   */
  override def dumpExtensionPrimitives : String = {
    var pstr: String = "\n\nEXTENSION\tPRIMITIVE\tTYPE\n"
    pstr += "----------\t---------\t----\n"
    var theJarContainer: ExtensionManager#JarContainer = null
    // Locate the class in all the loaded Jars
    val values: Iterator[ExtensionManager#JarContainer] = jars.values.iterator
    while (values.hasNext) {
      theJarContainer = values.next
      val k: Iterator[String] = theJarContainer.primManager.getPrimitiveNames()
      while (k.hasNext) {
        val name: String = k.next
        val p: Primitive = theJarContainer.primManager.getPrimitive(name)
        val ptype: String = (if (p.isInstanceOf[Reporter]) "Reporter" else "Command")
        pstr += theJarContainer.prefix + "\t" + name + "\t" + ptype + "\n"
      }
    }
    pstr
  }

  // Called by CompilerManager when a model is changed
  override def reset {
    for (jc <- jars.values) {
      try {
        jc.classManager.unload(this)
      }
      catch {
        case ex: ExtensionException => {
          System.err.println(ex)
          // don't throw an illegal state exception,
          // just because one extension throws an error
          // doesn't mean we shouldn't unload the rest
          // and continue with the operation ev 7/3/08
          ex.printStackTrace()
        }
      }
      jc.loaded = false
      jc.live = false
      jc.jarClassLoader = null
    }
    jars.clear()
    jarsLoaded = 0
  }

  private[workspace] def getAdditionalJars(folder: java.io.File) : List[URL] = {
    val urls = new ArrayList[URL]
    if (!AbstractWorkspace.isApplet && folder.exists && folder.isDirectory) {
      folder.listFiles() foreach {
        case file =>
          if (file.isFile && file.getName.toUpperCase.endsWith(".JAR")) {
            try {
              urls.add(ExtensionManager.toURL(file))
            }
            catch {
              case ex: MalformedURLException => {
                throw new IllegalStateException(ex)
              }
            }
          }
      }
    }
    urls
  }

  override def startFullCompilation {
    // forget which extensions are in the extensions [ ... ] block
    for (nextJarContainer <- jars.values) {
      nextJarContainer.live = false
    }
  }

  // Used to see if any IMPORT keywords have been removed since last compilation
  override def finishFullCompilation {
    for (nextJarContainer <- jars.values) {
      try {
        if ((nextJarContainer.loaded) && (!nextJarContainer.live)) {
          ({
            jarsLoaded -= 1; jarsLoaded
          })
          jars.remove(nextJarContainer.prefix)
          nextJarContainer.loaded = false
          nextJarContainer.classManager.unload(this)
        }
      }
      catch {
        case ex: ExtensionException => {
          // i'm not sure how to handle this yet
          System.err.println("Error unloading extension: " + ex)
        }
      }
    }
  }

  private def checkVersion(attr: java.util.jar.Attributes) : Boolean = {
    val jarVer = attr.getValue("NetLogo-Extension-API-Version")
    val currentVer = org.nlogo.api.APIVersion.version
    if (jarVer == null)
      workspace.warningMessage("Could not determine version of NetLogo extension.  NetLogo can " + "try to load the extension, but it might not work.")
    else if (currentVer != jarVer)
      workspace.warningMessage("You are attempting to open a NetLogo extension file that was created " + "for a different version of the NetLogo Extension API.  (This NetLogo uses Extension API " + currentVer + "; the extension uses NetLogo Extension API " + jarVer + ".)  NetLogo can try to load the extension, " + "but it might not work.")
    else
      true
  }

  private def getModified(jarPath: String, errors: ErrorSource) : Long = {
    try {
      new URL(jarPath).openConnection.getLastModified
    }
    catch {
      case ex: IOException => {
        System.err.println(ex)
        errors.signalError("Can't open extension")
        // this is unreachable, since signalError never returns.
        // we have to have it, though, since jikes can't figure that out.
        throw new IllegalStateException("this code is unreachable")
      }
    }
  }

  def exportWorld(writer: PrintWriter) {
    writer.println(Dump.csv.encode("EXTENSIONS"))
    writer.println()
    for (container <- jars.values) {
      val data = container.classManager.exportWorld
      if (data.length > 0) {
        writer.println(Dump.csv.encode(container.extensionName))
        writer.print(data)
        writer.println()
      }
    }
  }

  def importExtensionData(name: String, data: List[Array[String]], handler: ImportErrorHandler) {
    val jar: ExtensionManager#JarContainer = getJarContainerByIdentifier(name)
    if (jar != null) {
      jar.classManager.importWorld(data, this, handler)
    }
    else {
      throw new ExtensionException("there is no extension named " + name + "in this model")
    }
  }

  def isExtensionName(name: String): Boolean = {
    getJarContainerByIdentifier(name) != null
  }

  private def getJarContainerByIdentifier(identifier: String) : ExtensionManager#JarContainer = {
    for (jar <- jars.values) {
      if (jar.extensionName.equalsIgnoreCase(identifier)) {
        return jar
      }
    }
    null
  }


  private[workspace] class JarContainer(val extensionName: String, val jarName: String, var jarClassLoader: URLClassLoader, val modified: Long) {
    var prefix: String = null
    var primManager: ExtensionPrimitiveManager = null
    var classManager: ClassManager = null
    var live = false                                     /* live means the extension is currently in the extensions [ ... ] block in the code.
                                                          * if an extension is live, then its primitives are available to be called. JC - 12/3/10
                                                          */
    var loaded = false                                   /* loaded means that the load method has been called for this extension.
                                                          * any further recompiles with extension still in it should not call the load method.
                                                          * the extension can later be unloaded by removing it from the extensions [ ... ] block
                                                          * at that time, its unload method will be called, and loaded will be set to false.
                                                          * if it ever reappears in the extensions [ ... ] block, then load will be called again
                                                          * etc, etc. JC - 12/3/10
                                                          */
  }

}

object ExtensionManager {
  // for 4.1 we have too much fragile, difficult-to-understand,
  // under-tested code involving URLs -- we can't get rid of our
  // uses of toURL() until 4.2, the risk of breakage is too high.
  // so for now, at least we make this a separate method so the
  // SuppressWarnings annotation is narrowly targeted. - ST 12/7/09
  @SuppressWarnings(Array("deprecation")) private def toURL(file: java.io.File): java.net.URL = {
    file.toURI.toURL
  }
}
