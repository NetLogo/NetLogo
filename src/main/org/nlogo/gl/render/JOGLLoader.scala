// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.gl.render

import org.nlogo.api.JavaLibraryPath

object JOGLLoader {

  private val VersionMismatch =
    "NetLogo found an old version of JOGL on your computer.\n" +
    "You will need to remove it. For instructions, see Library Conflicts\n" +
    "in the System Requirements section of the NetLogo User Manual."

  private var _isLoaded = false
  def isLoaded = _isLoaded

  @throws(classOf[JOGLException])
  def load(classLoader: ClassLoader) {
    checkJOGLVersion(classLoader)
    // if we let jogl load itself, it might try to load a native library from the VM's extensions
    // directory instead of our copy of the library, and it might be the wrong version.  so let's
    // force-load our own native library. - ST 2/25/05
    // first, tell JOGL not to call System.loadLibrary() to load native libraries - ST 2/25/05,
    // 2/28/05
    // this next bit is based on code from NativeLibLoader - ST 2/28/05
    // if both the 2003 build and a newer build are in the classpath the version number will be
    // reported wrong and this call will fail.

    //try com.sun.opengl.impl.NativeLibLoader.disableLoading()
    //catch {
    //  case e: NoSuchMethodError =>
    //    throw new JOGLException(VersionMismatch, e)
    //}
    val isMac = System.getProperty("os.name").startsWith("Mac")
    def withErrorReporting(body: => Unit) {
      try body
      catch {
        case e: UnsatisfiedLinkError =>
          // something else might have loaded JAWT itself
          if(!e.getMessage.containsSlice("already loaded")) {
            val msg =
              "NetLogo could not find the required JOGL libraries.\n" +
              "Please contact bugs@ccl.northwestern.edu for assistance."
            throw new JOGLException(msg, e)
          }
      }
    }
    val joglPath = libraryPath + System.getProperty("file.separator") + System.mapLibraryName("jogl")
    val joglAwtPath = libraryPath + System.getProperty("file.separator") + System.mapLibraryName("jogl_awt")
    if (!isMac)
      withErrorReporting { System.loadLibrary("jawt") }
    withErrorReporting { System.load(joglPath) }
    withErrorReporting { System.load(joglAwtPath) }
    _isLoaded = true
  }

  private lazy val libraryPath: String =
    try {
      val osname = System.getProperty("os.name")
      val libdir =
        new java.io.File("lib").getAbsolutePath +
        System.getProperty("file.separator") +
        (if (osname == "Mac OS X")
           osname
         else if(osname.startsWith("Windows"))
           "Windows"
         else {
           val arch = System.getProperty("os.arch")
           if(arch.endsWith("86"))
             "Linux-x86"
           else
             "Linux-" + arch
         })
      JavaLibraryPath.add(new java.io.File(libdir))
      libdir
    }
    catch {
      case e: java.io.IOException =>
        throw new JOGLException(
          "NetLogo could not find the required JOGL libraries.\n"+
          "Please contact bugs@ccl.northwestern.edu for assistance.", e)
    }

  ///

  private val RecommendedVersion = "1.1.1"

  // Note that it's possible to have more than one version of JOGL in your classpath, which means
  // that the classloader may get some classes from one and some from the other.  In particular, the
  // Version class doesn't exist in some old versions, so calling getVersion() and getting the right
  // answer back guarantees that the right JOGL is present, but it doesn't guarantee that it is the
  // only JOGL present.  Note however that in load() above we call NativeLibLoader.disableLoading().
  // In the particular old JOGL that we tested (a 2003 build), that class exists but the method does
  // not, which means that doing that call at least guarantees that a version that old is first
  // doesn't come first in the classpath, so that means some additional version checking is
  // happening.  This is all still far from bulletproof since there may be some old JOGLs that don't
  // have Version but do have disableLoading(); we don't know.  It doesn't seem worth stressing
  // about. - ST 3/1/05
  // not entirely certain how relevant all this is anymore versioning seems to have changed
  // drastically. ev 5/4/06
  def getVersion(classLoader: ClassLoader) = {
    val pkgName = "javax.media.opengl"
    val className = "GL"
    try {
      classLoader.loadClass(pkgName + "." + className)
      Option(Package.getPackage(pkgName))
        .map(_.getImplementationVersion)
        .getOrElse("not available")
    }
    catch {
      case e: ClassNotFoundException => "not available"
    }
  }

  private def checkJOGLVersion(classLoader: ClassLoader)   {
    val pkgName = "javax.media.opengl"
    val className = "GL"
    try {
      classLoader.loadClass(pkgName + "." + className)
      val p = Package.getPackage(pkgName)
      if(p == null)
        throw new JOGLException(VersionMismatch, null)
      val implVersion = p.getImplementationVersion
      if(implVersion == null)
        throw new JOGLException(VersionMismatch, null)
      if(!implVersion.startsWith(RecommendedVersion)) {
        throw new JOGLException(
          "NetLogo found JOGL Version: " + implVersion + ".\n" +
          "Version: " + RecommendedVersion + " is recommended.\n" +
          "You may need to remove your existing\n" +
          "JOGL installation.\n" +
          "For instructions, see the Library Conflicts section \n" +
          "in the NetLogo User Manual.")
      }
    }
    catch {
      case e: ClassNotFoundException =>
        throw new JOGLException(
          "NetLogo could not find the required JOGL libraries.\n\n"+
          "Please contact bugs@ccl.northwestern.edu for assistance.", e)
      // it's annoying to have to use reflection here but Sun in their wisdom issue an unsuppressable
      // warning if we try to catch InvalidJarIndexException directly.  On the unsuppressability of
      // the warning, see bugs.sun.com/bugdatabase/view_bug.do?bug_id=6476630 - ST 5/6/10
      case e: RuntimeException =>
        try
          throw
            if(Class.forName("sun.misc.InvalidJarIndexException").isAssignableFrom(e.getClass))
              new JOGLException(VersionMismatch, e)
            else e
        catch {
          case e2: ClassNotFoundException => // thrown by Class.forName
            throw e
        }
    }
  }

}
