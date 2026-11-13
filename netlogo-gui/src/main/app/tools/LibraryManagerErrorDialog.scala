// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Component

import com.typesafe.config.ConfigException

import org.nlogo.api.FileIO
import org.nlogo.core.LocaleContext
import org.nlogo.window.{ DebuggingInfo, ErrorDialog, ErrorInfo }

class LibraryManagerErrorDialog(owner: Component)
extends ErrorDialog(owner, loc"Metadata Loading Error") {
  message = loc"""|We had a problem parsing the libraries metadata.  Please report the problem\n
                  |at https://github.com/NetLogo/NetLogo-Libraries/issues, or to bugs@ccl.northwestern.edu, and paste\n
                  |the contents of this window into your report."""

  override def show(errorInfo: ErrorInfo, debugInfo: DebuggingInfo): Unit = {
    val confFile = errorInfo.throwable.getCause.asInstanceOf[ConfigException].origin.filename
    details = FileIO.fileToString(confFile) + "\n\n" + debugInfo.detailedInformation
    doShow(true)
  }
}
