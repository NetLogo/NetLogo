package org.nlogo.api;

// don't import java.io.File or we run afoul of lampsvn.epfl.ch/trac/scala/ticket/3936 - ST 4/25/11

import java.lang.reflect.Field;
import java.util.Properties;

/**
 * This code came from:
 * http://forum.java.sun.com/thread.jspa?threadID=627890&start=15
 * <p/>
 * The Terms of Use for the website, in the "Content Submitted to Sun"
 * section states: You (the author) grant Sun and all other users of
 * the Website an irrevocable, worldwide, royalty-free, nonexclusive
 * license to use, reproduce, modify, distribute, transmit, display,
 * perform, adapt, resell and publish such Content (including in
 * digital form).
 * <p/>
 * http://www.sun.com/termsofuse.jsp
 * <p/>
 * The username of the original post has no name or email associated
 * with it.
 * <p/>
 * Adds a path to the java.library.path System property
 * and updates the ClassLoader. Uses reflection to allow
 * update to private system members. Will not work if JVM
 * security policy gets in the way (like in an applet).
 * Will not work if Sun changes the private members.
 * This really shouldn't be used at all...
 */

public final strictfp class JavaLibraryPath {
  private JavaLibraryPath() {
    throw new IllegalStateException();
  }

  public static void setLibraryPath(Class<?> myClass, String dirName) {
    // Reset the "sys_paths" field of the ClassLoader to null.
    try {
      final String filesep = System.getProperty("file.separator");
      final String basedir =
          new java.io.File(myClass.getProtectionDomain().getCodeSource().getLocation().getFile()).getParent();
      String libdir = basedir + filesep + dirName + filesep;

      String osname = System.getProperty("os.name");
      if (osname.equals("Mac OS X")) {
        libdir = libdir + osname;
      } else if (osname.startsWith("Windows")) {
        libdir = libdir + "Windows";
      } else {
        String arch = System.getProperty("os.arch");
        if (arch.endsWith("86")) {
          libdir = libdir + "-x86";
        } else {
          libdir = libdir + osname + "-" + arch;
        }
        // I lack any real understanding of what's going on
        // here and I'm scared of breaking it since I don't
        // have a 64-bit Linux machine to test it on, let
        // alone separate Intel and AMD machines.  But one
        // user (George Weichhart) is reporting that the above
        // code, even after having been tweaked several times
        // in the past in ways I don't understand, is still
        // looking for a directory name with "LinuxLinux" in
        // it.  So I'm going to make the minimum change that
        // will just make the problem go away for now, and that's
        // this kludge. - ST 8/11/09
        libdir = libdir.replaceAll("LinuxLinux", "Linux");
      }
      add(new java.io.File(libdir));
    } catch (java.io.IOException ex) {
      throw new IllegalStateException(ex);
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException(ex);
    } catch (NoSuchFieldException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public static void add(java.io.File path)
      throws java.io.IOException, IllegalAccessException, NoSuchFieldException {
    // Append the specified path to the
    // existing java.library.path (if there is one already)
    String newLibraryPath = System.getProperty("java.library.path");
    if (newLibraryPath == null || newLibraryPath.length() < 1) {
      newLibraryPath = path.getCanonicalPath();
    } else {
      newLibraryPath += java.io.File.pathSeparator +
          path.getCanonicalPath();
    }

    // Reflect into java.lang.System to get the
    // static Properties reference
    Field f = System.class.getDeclaredField("props");
    f.setAccessible(true);
    Properties props = (Properties) f.get(null);
    // replace the java.library.path with our new one
    props.put("java.library.path", newLibraryPath);

    // The classLoader may have already been initialized,
    // so it needs to be fixed up.
    // Reflect into java.lang.ClassLoader to get the
    // static String[] of user paths to native libraries
    Field usr_pathsField =
        ClassLoader.class.getDeclaredField("usr_paths");
    usr_pathsField.setAccessible(true);
    String[] usr_paths = (String[]) usr_pathsField.get(null);
    String[] newUsr_paths = new String[usr_paths == null ? 1 :
        usr_paths.length + 1];
    if (usr_paths != null) {
      System.arraycopy(usr_paths, 0, newUsr_paths,
          0, usr_paths.length);
    }
    // Add the specified path to the end of a new String[]
    // of user paths to native libraries
    newUsr_paths[newUsr_paths.length - 1] = path.getAbsolutePath();
    usr_pathsField.set(null, newUsr_paths);
  }
}
