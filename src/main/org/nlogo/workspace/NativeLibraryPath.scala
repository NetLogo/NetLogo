// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.workspace

import java.util.Properties

/**
 * This code came from http://forum.java.sun.com/thread.jspa?threadID=627890&start=15
 *
 * The Terms of Use for the website, in the "Content Submitted to Sun" section states: You (the
 * author) grant Sun and all other users of the Website an irrevocable, worldwide, royalty-free,
 * nonexclusive license to use, reproduce, modify, distribute, transmit, display, perform, adapt,
 * resell and publish such Content (including in digital form).
 *
 * http://www.sun.com/termsofuse.jsp
 *
 * The username of the original post has no name or email associated with it.
 *
 * Adds a path to the java.library.path System property and updates the ClassLoader. Uses reflection
 * to allow update to private system members. Will not work if JVM security policy gets in the way
 * (like in an applet).  Will not work if Sun changes the private members.  This really shouldn't be
 * used at all...
 */

object NativeLibraryPath {

  def setLibraryPath(myClass: Class[_], dirName: String) {
    // Reset the "sys_paths" field of the ClassLoader to null.
    val filesep = System.getProperty("file.separator")
    val basedir = new java.io.File(myClass.getProtectionDomain.getCodeSource.getLocation.getFile)
      .getParent
    var libdir = basedir + filesep + dirName + filesep
    val osname = System.getProperty("os.name")
    System.out.println("OS name is: " + osname) //@
    if (osname == "Mac OS X") {
      System.out.println("We've taken the Mac OS branch") //@
      libdir ++= osname
    }
    else if (osname.startsWith("Windows")) {
      System.out.println("We've taken the Windows branch") //@
      libdir ++= "Windows"
    }
    else {
      System.out.println("We've taken the 'Other' branch") //@
      val arch = System.getProperty("os.arch")
      if (arch.endsWith("86")) {
        System.out.println("We're using x86") //@
        libdir ++= osname + "-x86"
      }
      else {
        System.out.println("We're using a different architecture: " + arch) //@
        libdir ++= osname + "-" + arch
      }
      // I lack any real understanding of what's going on here and I'm scared of breaking it since I
      // don't have a 64-bit Linux machine to test it on, let alone separate Intel and AMD machines.
      // But one user (George Weichhart) is reporting that the above code, even after having been
      // tweaked several times in the past in ways I don't understand, is still looking for a
      // directory name with "LinuxLinux" in it.  So I'm going to make the minimum change that will
      // just make the problem go away for now, and that's this kludge. - ST 8/11/09
      libdir = libdir.replaceAll("LinuxLinux", "Linux")
    }
    add(new java.io.File(libdir))
  }

  def add(path: java.io.File) {
    // Append the specified path to the existing java.library.path (if there is one already)
    var newLibraryPath = System.getProperty("java.library.path")
    if (!Option(newLibraryPath).exists(_.nonEmpty))
      newLibraryPath = path.getCanonicalPath
    else
      newLibraryPath ++= java.io.File.pathSeparator + path.getCanonicalPath

    // Reflect into java.lang.System to get the static Properties reference
    val f = classOf[System].getDeclaredField("props")
    f.setAccessible(true)
    val props = f.get(null).asInstanceOf[Properties]
    // replace the java.library.path with our new one
    props.put("java.library.path", newLibraryPath)

    // The classLoader may have already been initialized, so it needs to be fixed up.
    // Reflect into java.lang.ClassLoader to get the static String[] of user paths to native
    // libraries
    val usr_pathsField = classOf[ClassLoader].getDeclaredField("usr_paths")
    usr_pathsField.setAccessible(true)
    val usr_paths = usr_pathsField.get(null).asInstanceOf[Array[String]]
    val newUsr_paths =
      Array.fill(if (usr_paths == null) 1 else (usr_paths.length + 1))(null: String)
    if (usr_paths != null)
      System.arraycopy(usr_paths, 0, newUsr_paths, 0, usr_paths.size)
    // Add the specified path to the end of a new String array of user paths to native libraries
    newUsr_paths(newUsr_paths.size - 1) = path.getAbsolutePath
    usr_pathsField.set(null, newUsr_paths)
  }

}
