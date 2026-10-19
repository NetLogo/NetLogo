package org.nlogo.extensions.vid

import org.nlogo.app.App
import org.nlogo.swing.MessageDialog
import org.nlogo.util.Utils

import scala.language.reflectiveCalls

object PlatformErrors {

  def isWindows = {
    System.getProperty("os.name").toLowerCase.startsWith("win")
  }

  def isMacOS = {
    System.getProperty("os.name").toLowerCase.startsWith("mac")
  }

  def showDialog(title: String, message: String, ex: Throwable) = {
    val stackTrace = Utils.getStackTrace(ex)
    val dialog = new MessageDialog(App.app.frame) {
      doShow(title, s"$message\n\nIf it still does not work, please report the below error message to bugs@ccl.northwestern.edu or at https://github.com/NetLogo/Vid-Extension/issues\n\n$stackTrace", 15, 60)
    }
  }

  def isPossibleMacOSSecurityError(ex: Exception) = {
    isMacOS && ex.getMessage == "read() Error: Could not read frame in start()."
  }

  def showMacOSSecurityMessage(ex: Exception) = {
    val message = "It appears that NetLogo does not have permission to use the cameras on this system.  Please open System Preferences, Privacy & Security, select Camera in the list, and allow NetLogo to use the camera.  You will need to quit and re-open NetLogo for the change to take effect.  See this link for more details:  https://support.apple.com/en-euro/guide/mac-help/mchlf6d108da/mac"
    showDialog("macOS Camera Permission Needed", message, ex)
  }

  def isPossibleWinMissingVcppRuntimeError = {
    isWindows
  }

  def showMissingVcppRuntimeMessage(ex: java.lang.ExceptionInInitializerError) = {
    val is64 = System.getProperty("os.arch").contains("64")
    // URLs are taken from here: https://docs.microsoft.com/en-us/cpp/windows/latest-supported-vc-redist?view=msvc-170#visual-studio-2010-vc-100-sp1-no-longer-supported
    val url = if (is64) {
      "https://download.microsoft.com/download/1/6/5/165255E7-1014-4D0A-B094-B6A430A6BFFC/vcredist_x64.exe"
    } else {
      "https://download.microsoft.com/download/1/6/5/165255E7-1014-4D0A-B094-B6A430A6BFFC/vcredist_x86.exe"
    }
    val message = s"It appears that a Windows library needed by the Vid extension is not installed.  Please visit this link to download the Microsoft Visual C++ 10 runtime library installer: $url\n\nAfter installing it try running the Vid extension one more time."
    showDialog("Missing Windows Library", message, ex)
  }

}
