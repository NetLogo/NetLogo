// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Component

import org.nlogo.api.FileIO
import org.nlogo.core.I18N
import org.nlogo.window.{ DebuggingInfo, ErrorDialog, ErrorInfo }

class LibraryManagerErrorDialog(owner: Component)
extends ErrorDialog(owner, I18N.gui.get("error.dialog.librariesMetadata")) {
  message = I18N.gui.get("error.dialog.librariesMetadata.message")

  override def show(errorInfo: ErrorInfo, debugInfo: DebuggingInfo): Unit = {
    details = FileIO.fileToString("libraries.conf") + "\n\n" + debugInfo.detailedInformation
    doShow(true)
  }
}
