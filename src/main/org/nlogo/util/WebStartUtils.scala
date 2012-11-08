package org.nlogo.util

import java.util.zip.ZipEntry
import java.io.FileNotFoundException

object WebStartUtils {

  private val tempDirPathTemplate = {
    val tempProp = System.getProperty("java.io.tmpdir")
    val sep      = System.getProperty("file.separator")
    val tempDir  = if (tempProp endsWith sep) tempProp else tempProp + sep
    tempDir + "%s" + sep
  }

  def extractAllFilesFromJarByMarker(markerFileName: String,
                                     desiredFolderName: String,
                                     entryFilter: ZipEntry => Boolean = (_ => true)): List[java.io.File] = {
    val extensionsDir = getWebStartPath(desiredFolderName)
    val localFilePath = getPathToContainingJar(markerFileName, extensionsDir)
    ZipUtils.extractFilesFromJar(localFilePath, extensionsDir, entryFilter)
  }

  // Find the '.jar' that contains the desired file, downloads it to (to the "temp" directory), and returns its filepath
  def getPathToContainingJar(resourceName: String, destinationDir: String): String = {

    disableSecurityManager()
    FileUtils.createDirectoryAnew(destinationDir)

    val urlToJar = Option(this.getClass.getClassLoader.getResource(resourceName)).
                      getOrElse (throw new FileNotFoundException("File '%s' not found!".format(resourceName)))
    val JarLocMatcher = """jar:(.*)!/.*""".r
    val JarLocMatcher(jarLoc) = urlToJar.toString
    NetUtils.downloadFile(jarLoc, destinationDir)

  }

  def getWebStartPath(folderName: String): String = tempDirPathTemplate.format(folderName)

  /* Disables security for the rest of the program's execution (specifically added for GoGo with WebStart)
     * You might then think that I could put this in the GoGo extension code, but, at that point in the
     * code, we don't have permissions to be changing the security manager, so... something needs to be done
     * outside of the extension's code.
     *
     * I originally only included this around the calls to classManager.runOnce() in this.importExtension(),
     * (since it was originally only for GoGoWindowsHandler), but it turns out that permissions are also necessary
     * when GoGo goes to load the serial libraries.  I see no other obvious way to get permissions to that
     * context than to simply turn the security manager off altogether.
     *
     * Possible Alternatives:
     * a) .policy file -> System.setProperty("java.security.policy", policyFilePath) -> Policy.getPolicy.refresh
     *    --Tried; fidgetty, hard to debug, didn't seem to be getting applied to the extension's context properly
     * b) Manually setting security policies through ProtectionDomain
     *    --Tried; settings did not persist into the extension's context.  Might work better if the constructor
     *      that involves a ClassLoader were used, but... might actually be bad to persist those policy changes
     *      beyond the call to runOnce()
     *
     * Things that _Won't_ Work:
     * a) Any changes inside the _extension's_ code.  The WebStart JNLP's <all-permissions/> tag only affects the
     *    main code that corresponds to the JNLP's `resources` -> `jar` (with `main="true"`).  Other code has to
     *    get its own permissions set (somehow).  See bugs.sun.com/bugdatabase/view_bug.do?bug_id=4809366 (relevant
     *    information is towards the bottom)
     * b) Simply signing the individual extension .jar files
     * c) Other rain dances/security dances
     *
     * If you so choose to do this without disabling the security manager, you'll want to run much of
     * JavaLibraryPath through this function (and maybe make it available through ExtensionManager so
     * extensions can use it, too):
     *
     * private def doWithPrivs[T](block: => T): T = {
     *   AccessController.doPrivileged(new PrivilegedAction[T]() {
     *     def run : T = {
     *       block
     *     }
     *   })
     * }
     *
     * --JAB (February, 2012)
     *
     */
    def disableSecurityManager() {
      System.setSecurityManager(null)
    }

}
