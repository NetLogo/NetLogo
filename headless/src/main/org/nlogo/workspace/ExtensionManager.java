// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace;

import org.nlogo.api.CompilerException;
import org.nlogo.api.Dump;
import org.nlogo.api.ErrorSource;
import org.nlogo.api.ExtensionException;
import org.nlogo.api.Primitive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
public strictfp class ExtensionManager
    implements org.nlogo.api.ExtensionManager {

  private final Map<String, JarContainer> jars =
      new HashMap<String, JarContainer>();
  private final AbstractWorkspace workspace;

  private int jarsLoaded = 0;

  // cities and other extensions may want access to this.  it
  // means violating the org.nlogo.api interface, but it it does
  // give them a way to manipulate the world and such without
  // breaking the existing extensions API -- CLB
  public AbstractWorkspace workspace() {
    return workspace;
  }

  public boolean profilingEnabled() {
    return workspace.profilingEnabled();
  }

  public ExtensionManager(AbstractWorkspace workspace) {
    this.workspace = workspace;
  }

  ///

  public boolean anyExtensionsLoaded() {
    return jarsLoaded > 0;
  }

  public String getSource(String filename)
      throws java.io.IOException {
    return workspace.getSource(filename);
  }

  public org.nlogo.api.File getFile(String path)
      throws ExtensionException {
    org.nlogo.nvm.FileManager fm = workspace().fileManager();
    return fm.getFile(getFullPath(path));
  }

  // ugly stuff to ensure that we only load
  // the soundbank once. guess anyone else can use it too.
  private Object obj = null;

  public void storeObject(Object obj) {
    this.obj = obj;
  }

  public Object retrieveObject() {
    return obj;
  }

  private String identifierToJar(String id) {
    // If we are given a jar name, then we look for that otherwise
    // we assume that we have been given an extensions name.
    // Extensions are folders which have a jar with the same name
    // in them (plus other things if needed) -- CLB
    if (!id.endsWith(".jar")) {
      return id + java.io.File.separator + id + ".jar";
    } else {
      return id;
    }

  }

  // called each time extensions is parsed
  public void importExtension(String extName, ErrorSource errors)
      throws CompilerException {
    String jarPath = identifierToJar(extName);

    try {
      jarPath = resolvePathAsURL(jarPath);
    } catch (RuntimeException ex) {
      ex.printStackTrace();
      errors.signalError("Can't find extension: " + extName);
      return;
    }

    try {
      java.net.URLClassLoader myClassLoader = getClassLoader(jarPath, errors, getClass().getClassLoader());

      if (myClassLoader == null) {
        return;
      }

      org.nlogo.api.ClassManager classManager =
          getClassManager(jarPath, myClassLoader, errors);

      if (classManager == null) {
        return;
      }

      JarContainer theJarContainer = jars.get(jarPath);

      long modified = getModified(jarPath, errors);

      // Check to see if he have seen this Jar before
      if ((theJarContainer == null) ||
          (theJarContainer.modified != modified)) {
        if (theJarContainer != null) {
          theJarContainer.classManager.unload(this);
        }
        theJarContainer = new JarContainer(extName, jarPath, myClassLoader, modified);
        try {
          // compilation tests shouldn't initialize the extension
          if (!workspace.compilerTestingMode()) {
            classManager.runOnce(this);
          }
        } catch (org.nlogo.api.ExtensionException ex) {
          System.err.println("Error while initializing extension.");
          System.err.println("Error is: " + ex);
          throw ex;
        }
        jars.put(jarPath, theJarContainer);
      }

      // Check to see if it has been loaded into the model
      if (!theJarContainer.loaded) {
        jarsLoaded++;
        theJarContainer.loaded = true;
        theJarContainer.classManager = classManager;
        theJarContainer.primManager = new ExtensionPrimitiveManager(extName);
        classManager.load(theJarContainer.primManager);
      }

      // Jars that have been removed won't get this flag set
      theJarContainer.live = true;

      theJarContainer.prefix = getExtensionName(jarPath, errors);
    } catch (org.nlogo.api.ExtensionException ex) {
      errors.signalError(ex.getMessage());
      return;
    } catch (java.lang.IncompatibleClassChangeError ex) {
      // thrown if extension classes from different version are incompatible
      // catching this is necessary so it doesn't just choke
      errors.signalError("This extension doesn't work with this version of NetLogo");
      System.err.println(ex);
    }
  }

  public void addToLibraryPath(Object classManager, String directory) {
    org.nlogo.api.JavaLibraryPath.setLibraryPath(classManager.getClass(), directory);
  }

  public String resolvePath(String path) {
    try {
      java.io.File result = new java.io.File(workspace.attachModelDir(path));
      try {
        return result.getCanonicalPath();
      } catch (java.io.IOException ex) {
        return result.getPath();
        }
    } catch (java.net.MalformedURLException ex) {
      throw new IllegalStateException(path + " is not a valid pathname: " + ex);
    }
  }

  public String resolvePathAsURL(String path) {
    java.net.URL jarURL;

    // Is this a URL right off the bat?
    try {
      jarURL = new java.net.URL(path);
      return jarURL.toString();
    } catch (java.net.MalformedURLException ex) {
      org.nlogo.util.Exceptions.ignore(ex);
    }

    // If it's a path, look for it relative to the model location
    if (path.contains(java.io.File.separator)) {
      try {
        java.io.File jarFile = new java.io.File(workspace.attachModelDir(path));
        if (jarFile.exists()) {
          return toURL(jarFile).toString();
        }
      } catch (java.net.MalformedURLException ex) {
        org.nlogo.util.Exceptions.ignore(ex);
      }
    }

    // If it's not a path, try the model location
    try {
      java.io.File jarFile =
          new java.io.File(workspace.attachModelDir(path));
      if (jarFile.exists()) {
        return toURL(jarFile).toString();
      }
    } catch (java.net.MalformedURLException ex) {
      org.nlogo.util.Exceptions.ignore(ex);
    }

    // Then try the extensions folder
    try {
      java.io.File jarFile =
          new java.io.File("extensions" + java.io.File.separator + path);
      if (jarFile.exists()) {
        return toURL(jarFile).toString();
      }
    } catch (java.net.MalformedURLException ex) {
      org.nlogo.util.Exceptions.ignore(ex);
    }

    // Give up
    throw new IllegalStateException
        ("Can't find extension " + path);
  }

  public String getFullPath(String path)
      throws ExtensionException {
    try {
      String fullPath = workspace.attachModelDir(path);
      java.io.File f = new java.io.File(fullPath);
      if (f.exists()) {
        return fullPath;
      }
    } catch (java.net.MalformedURLException ex) {
      org.nlogo.util.Exceptions.ignore(ex);
    }

    // Then try the extensions folder
    java.io.File f = new java.io.File("extensions" + java.io.File.separator + path);
    if (f.exists()) {
      return f.getPath();
    }

    // Give up
    throw new ExtensionException("Can't find file " + path);
  }

  // We only want one ClassLoader for every Jar per NetLogo instance
  private java.net.URLClassLoader getClassLoader(String jarPath, ErrorSource errors, ClassLoader parentLoader)
      throws CompilerException {
    JarContainer theJarContainer = jars.get(jarPath);
    if (theJarContainer != null) {
      return theJarContainer.jarClassLoader;
    }
    try {
      java.net.URL jarURL = new java.net.URL(jarPath);

      // all the urls our class loader will look at
      List<java.net.URL> urls =
          new ArrayList<java.net.URL>();

      // start with the original extension jar
      urls.add(jarURL);

      // Get other Jars in the extensions own dir
      java.io.File folder = new java.io.File(new java.io.File(jarURL.getFile()).getParent());
      urls.addAll(getAdditionalJars(folder));

      // Get other Jars in extensions folder
      folder = new java.io.File("extensions");
      urls.addAll(getAdditionalJars(folder));

      // We use the URLClassLoader.newInstance method because that works with
      // the applet SecurityManager, even tho newLClassLoader(..) does not.
      return java.net.URLClassLoader.newInstance
          (urls.toArray(new java.net.URL[urls.size()]), parentLoader);

    } catch (java.net.MalformedURLException ex) {
      errors.signalError("Invalid URL: " + jarPath);
      return null;
    }
  }

  // We want a new ClassManager per Jar Load
  private org.nlogo.api.ClassManager getClassManager(String jarPath,
                                                     java.net.URLClassLoader myClassLoader,
                                                     ErrorSource errors)
      throws CompilerException {
    JarContainer theJarContainer = jars.get(jarPath);

    if ((theJarContainer != null) && (theJarContainer.loaded)) {
      return theJarContainer.classManager;
    }

    String classMangName = null;

    try {
      // Class must be named in Manifest file
      classMangName = getClassManagerName(jarPath, errors);

      if (classMangName == null) {
        errors.signalError("Bad extension: Couldn't locate Class-Manager tag in Manifest File");
      }

      Object classMang = myClassLoader.loadClass(classMangName).newInstance();

      try {
        return (org.nlogo.api.ClassManager) classMang;
      } catch (ClassCastException ex) {
        errors.signalError("Bad extension: The ClassManager doesn't implement "
            + "org.nlogo.api.ClassManager");
      }
    } catch (java.io.FileNotFoundException ex) {
      errors.signalError("Can't find extension " + jarPath);
    } catch (java.io.IOException ex) {
      errors.signalError("Can't open extension " + jarPath);
    } catch (InstantiationException ex) {
      throw new IllegalStateException(ex);
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException(ex);
    } catch (ClassNotFoundException ex) {
      errors.signalError("Can't find class " + classMangName
          + " in extension");
    }

    return null;
  }

  /**
   * Gets the name of an extension's ClassManager implementation from the manifest.
   */
  private String getClassManagerName(String jarPath, ErrorSource errors)
      throws java.io.IOException, CompilerException {
    java.net.URL jarURL = new java.net.URL("jar", "", jarPath + "!/");
    java.net.JarURLConnection jarConnection = (java.net.JarURLConnection) jarURL.openConnection();
    String name = null;

    if (jarConnection.getManifest() == null) {
      errors.signalError("Bad extension: Can't find a Manifest file in extension");
    }

    java.util.jar.Attributes attr = jarConnection.getManifest().getMainAttributes();
    name = attr.getValue("Class-Manager");

    if (!checkVersion(attr)) {
      errors.signalError("User halted compilation");
    }

    return name;
  }

  /**
   * Gets the extension name from the manifest.
   */
  private String getExtensionName(String jarPath, ErrorSource errors)
      throws CompilerException {
    try {
      java.net.URL jarURL = new java.net.URL("jar", "", jarPath + "!/");
      java.net.JarURLConnection jarConnection = (java.net.JarURLConnection) jarURL.openConnection();
      String name = null;

      if (jarConnection.getManifest() == null) {
        errors.signalError("Bad extension: Can't find Manifest file in extension");
      }

      java.util.jar.Attributes attr = jarConnection.getManifest().getMainAttributes();
      name = attr.getValue("Extension-Name");

      if (name == null) {
        errors.signalError("Bad extension: Can't find extension name in Manifest.");
      }

      return name;
    } catch (java.io.FileNotFoundException ex) {
      errors.signalError("Can't find extension " + jarPath);
    } catch (java.io.IOException ex) {
      errors.signalError("Can't open extension " + jarPath);
    }

    return null;
  }

  public Object readFromString(String source)
      throws CompilerException {
    return ((AbstractWorkspaceScala) workspace).readFromString(source);
  }

  public void clearAll() {
    for (JarContainer jar : jars.values()) {
      jar.classManager.clearAll();
    }
  }

  public org.nlogo.api.ExtensionObject readExtensionObject(String extName,
                                                           String typeName,
                                                           String value)
      throws CompilerException {
    JarContainer theJarContainer = null;

    extName = extName.toUpperCase();

    // Locate the class in all the loaded Jars
    Iterator<Map.Entry<String, JarContainer>> entries = jars.entrySet().iterator();

    while (entries.hasNext()) {
      Map.Entry<String, JarContainer> entry = entries.next();
      theJarContainer = entry.getValue();
      String name = theJarContainer.primManager.name().toUpperCase();

      if (theJarContainer.loaded && name != null && name.equals(extName)) {
        try {
          return theJarContainer.classManager.readExtensionObject
              (this, typeName, value);
        } catch (org.nlogo.api.ExtensionException ex) {
          System.err.println(ex);
          throw new IllegalStateException
              ("Error reading extension object "
                  + extName + ":" + typeName
                  + " " + value + " ==> " + ex.getMessage());
        }
      }
    }
    return null;
  }

  public Primitive replaceIdentifier(String name) {
    Primitive prim = null;
    boolean qualified = name.indexOf(':') != -1;

    // Locate the class in all the loaded Jars
    Iterator<Map.Entry<String, JarContainer>> entries = jars.entrySet().iterator();

    while (entries.hasNext() && prim == null) {
      Map.Entry<String, JarContainer> entry = entries.next();
      JarContainer theJarContainer = entry.getValue();
      String extName = theJarContainer.primManager.name().toUpperCase();

      if (theJarContainer.live) {
        // Qualified references must match the extension name
        if (qualified) {
          String prefix = name.substring(0, name.indexOf(':'));
          String pname = name.substring(name.indexOf(':') + 1);
          if (prefix.equals(extName)) {
            prim = theJarContainer.primManager.getPrimitive(pname);
          }
        } else {
          if (theJarContainer.primManager.autoImportPrimitives()) {
            prim = theJarContainer.primManager.getPrimitive(name);
          }
        }
      }
    }
    return prim;
  }

  /**
   * Returns a String describing all the loaded extensions.
   */
  public String dumpExtensions() {
    String str = "EXTENSION\tLOADED\tMODIFIED\tJARPATH\n";
    str += "---------\t------\t---------\t---\n";

    JarContainer theJarContainer = null;

    // Locate the class in all the loaded Jars
    Iterator<JarContainer> values = jars.values().iterator();
    while (values.hasNext()) {
      theJarContainer = values.next();
      str += theJarContainer.prefix + "\t" + theJarContainer.loaded + "\t" + theJarContainer.modified + "\t" + theJarContainer.jarName + "\n";
    }

    return str;
  }

  public java.util.List<String> getJarPaths() {
    java.util.ArrayList<String> names = new java.util.ArrayList<String>();
    for (JarContainer jar : jars.values()) {
      names.add(jar.extensionName + '/' + jar.extensionName + ".jar");
      for (String additionalJar : jar.classManager.additionalJars()) {
        names.add(jar.extensionName + '/' + additionalJar);
      }
    }
    return names;
  }

  public java.util.List<String> getExtensionNames() {
    java.util.ArrayList<String> names = new java.util.ArrayList<String>();
    for (JarContainer jar : jars.values()) {
      names.add(jar.extensionName);
    }
    return names;
  }

  /**
   * Returns a String describing all the loaded extensions.
   */
  public String dumpExtensionPrimitives() {
    String pstr = "\n\nEXTENSION\tPRIMITIVE\tTYPE\n";
    pstr += "----------\t---------\t----\n";

    JarContainer theJarContainer = null;

    // Locate the class in all the loaded Jars
    Iterator<JarContainer> values = jars.values().iterator();
    while (values.hasNext()) {
      theJarContainer = values.next();
      Iterator<String> k = theJarContainer.primManager.getPrimitiveNames();
      while (k.hasNext()) {
        String name = k.next();
        Primitive p = theJarContainer.primManager.getPrimitive(name);
        String type = (p instanceof org.nlogo.api.Reporter ? "Reporter" : "Command");
        pstr += theJarContainer.prefix + "\t" + name + "\t" + type + "\n";
      }
    }

    return pstr;
  }


  // Called by CompilerManager when a model is changed
  public void reset() {
    for (JarContainer jc : jars.values()) {
      try {
        jc.classManager.unload(this);
      } catch (org.nlogo.api.ExtensionException ex) {
        System.err.println(ex);
        // don't throw an illegal state exception,
        // just because one extension throws an error
        // doesn't mean we shouldn't unload the rest
        // and continue with the operation ev 7/3/08
        ex.printStackTrace();
      }
      jc.loaded = false;
      jc.live = false;
      jc.jarClassLoader = null;
    }

    jars.clear();
    jarsLoaded = 0;
  }


  List<java.net.URL> getAdditionalJars(java.io.File folder) {
    List<java.net.URL> urls =
        new ArrayList<java.net.URL>();
    if (folder.exists() &&
        folder.isDirectory()) {
      java.io.File[] files = folder.listFiles();
      for (int n = 0; n < files.length; n++) {
        if (files[n].isFile() && files[n].getName().toUpperCase().endsWith(".JAR")) {
          try {
            urls.add(toURL(files[n]));
          } catch (java.net.MalformedURLException ex) {
            throw new IllegalStateException(ex);
          }
        }
      }
    }
    return urls;
  }

  public void startFullCompilation() {
    // forget which extensions are in the extensions [ ... ] block
    for (JarContainer nextJarContainer : jars.values()) {
      nextJarContainer.live = false;
    }
  }

  // Used to see if any IMPORT keywords have been removed since last compilation
  public void finishFullCompilation() {
    for (JarContainer nextJarContainer : jars.values()) {
      try {
        if ((nextJarContainer.loaded) && (!nextJarContainer.live)) {
          jarsLoaded--;
          jars.remove(nextJarContainer.prefix);
          nextJarContainer.loaded = false;
          nextJarContainer.classManager.unload(this);
        }
      } catch (org.nlogo.api.ExtensionException ex) {
        // i'm not sure how to handle this yet
        System.err.println("Error unloading extension: " + ex);
      }
    }
  }

  private boolean checkVersion(java.util.jar.Attributes attr) {
    String currentVer = org.nlogo.api.APIVersion.version();
    String jarVer = attr.getValue("NetLogo-Extension-API-Version");

    if (jarVer == null) {
      return workspace.warningMessage(
          "Could not determine version of NetLogo extension.  NetLogo can "
              + "try to load the extension, but it might not work.");
    } else if (!currentVer.equals(jarVer)) {
      return workspace.warningMessage(
          "You are attempting to open a NetLogo extension file that was created " +
              "for a different version of the NetLogo Extension API.  (This NetLogo uses Extension API "
              + currentVer + "; the extension uses NetLogo Extension API " + jarVer
              + ".)  NetLogo can try to load the extension, " +
              "but it might not work.");
    }
    return true;
  }

  private long getModified(String jarPath, ErrorSource errors)
      throws CompilerException {
    try {
      return new java.net.URL(jarPath)
          .openConnection().getLastModified();
    } catch (java.io.IOException ex) {
      System.err.println(ex);
      errors.signalError("Can't open extension");
      // this is unreachable, since signalError never returns.
      // we have to have it, though, since jikes can't figure that out.
      throw new IllegalStateException("this code is unreachable");
    }
  }

  public void exportWorld(java.io.PrintWriter writer) {
    writer.println(Dump.csv().encode("EXTENSIONS"));
    writer.println();
    for (JarContainer container : jars.values()) {
      StringBuilder data = container.classManager.exportWorld();
      if (data.length() > 0) {
        writer.println(Dump.csv().encode(container.extensionName));
        writer.print(data);
        writer.println();
      }
    }
  }

  public void importExtensionData(String name, List<String[]> data,
                                  org.nlogo.api.ImportErrorHandler handler)
      throws org.nlogo.api.ExtensionException {
    JarContainer jar = getJarContainerByIdentifier(name);
    if (jar != null) {
      jar.classManager.importWorld(data, this, handler);
    } else {
      throw new org.nlogo.api.ExtensionException("there is no extension named " + name + "in this model");
    }
  }

  public boolean isExtensionName(String name) {
    return getJarContainerByIdentifier(name) != null;
  }

  private JarContainer getJarContainerByIdentifier(String identifier) {
    for (JarContainer jar : jars.values()) {
      if (jar.extensionName.equalsIgnoreCase(identifier)) {
        return jar;
      }
    }
    return null;
  }

  private class JarContainer {
    public final String extensionName;
    public final String jarName;
    public java.net.URLClassLoader jarClassLoader;
    public final long modified;
    public ExtensionPrimitiveManager primManager;
    public org.nlogo.api.ClassManager classManager;

    // loaded means that the load method has been called for this extension.
    // any further recompiles with extension still in it should not call the load method.
    // the extension can later be unloaded by removing it from the extensions [ ... ] block
    // at that time, its unload method will be called, and loaded will be set to false.
    // if it ever reappears in the extensions [ ... ] block, then load will be called again
    // etc, etc. JC - 12/3/10
    public boolean loaded;

    // live means the extension is currently in the extensions [ ... ] block in the code.
    // if an extension is live, then its primitives are available to be called. JC - 12/3/10
    public boolean live;

    public String prefix;

    JarContainer(String extensionName, String jarName,
                 java.net.URLClassLoader jarClassLoader, long modified) {
      this.extensionName = extensionName;
      this.jarName = jarName;
      this.jarClassLoader = jarClassLoader;
      loaded = false;
      live = false;
      prefix = null;
      this.modified = modified;
    }
  }

  // for 4.1 we have too much fragile, difficult-to-understand,
  // under-tested code involving URLs -- we can't get rid of our
  // uses of toURL() until 4.2, the risk of breakage is too high.
  // so for now, at least we make this a separate method so the
  // SuppressWarnings annotation is narrowly targeted. - ST 12/7/09
  @SuppressWarnings("deprecation")
  private static java.net.URL toURL(java.io.File file)
      throws java.net.MalformedURLException {
    return file.toURL();
  }

}
