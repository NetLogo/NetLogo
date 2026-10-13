package org.nlogo.extensions.vid

import java.awt.Frame

import org.nlogo.swing.MessageDialog
import org.nlogo.util.Utils

import scala.language.reflectiveCalls

object PlatformErrors {

  def isMacOS = {
    System.getProperty("os.name").toLowerCase.startsWith("mac")
  }

  def showDialog(frame: Frame, title: String, message: String, ex: Throwable) = {
    val stackTrace = Utils.getStackTrace(ex)
    val dialog = new MessageDialog(frame) {
      doShow(title, s"$message\n\nIf it still does not work, please report the below error message to bugs@ccl.northwestern.edu or at https://github.com/NetLogo/Vid-Extension/issues\n\n$stackTrace", 15, 60)
    }
  }

  def isPossibleMacOSSecurityError(ex: Exception) = {
    isMacOS && ex.getMessage == "read() Error: Could not read frame in start()."
  }

  def showMacOSSecurityMessage(frame: Frame, ex: Exception) = {
    val message = "It appears that NetLogo does not have permission to use the cameras on this system.  Please open System Preferences, Privacy & Security, select Camera in the list, and allow NetLogo to use the camera.  You will need to quit and re-open NetLogo for the change to take effect.  See this link for more details:  https://support.apple.com/en-euro/guide/mac-help/mchlf6d108da/mac"
    showDialog(frame, "macOS Camera Permission Needed", message, ex)
  }

}
