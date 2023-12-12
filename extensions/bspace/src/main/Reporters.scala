// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.extensions.bspace

import org.nlogo.api.{ Argument, Command, Context, LabDefaultValues, Reporter }
import org.nlogo.core.I18N
import org.nlogo.core.Syntax._
import org.nlogo.swing.BrowserLauncher
import org.nlogo.window.GUIWorkspace

object GotoBehaviorspaceDocumentation extends Command {
  override def getSyntax = {
    commandSyntax()
  }

  def perform(args: Array[Argument], context: Context) {
    BrowserLauncher.openPath(BrowserLauncher.docPath("behaviorspace.html"), "")
  }
}

object GotoBspaceExtensionDocumentation extends Command {
  override def getSyntax = {
    commandSyntax()
  }

  def perform(args: Array[Argument], context: Context) {
    // will fill in once i know the link
  }
}

object GetDefaultParallelRuns extends Reporter {
  override def getSyntax = {
    reporterSyntax(ret = NumberType)
  }

  override def report(args: Array[Argument], context: Context): java.lang.Double = {
    LabDefaultValues.getDefaultThreads
  }
}

object GetRecommendedMaxParallelRuns extends Reporter {
  override def getSyntax = {
    reporterSyntax(ret = NumberType)
  }

  override def report(args: Array[Argument], context: Context): java.lang.Double = {
    LabDefaultValues.getRecommendedMaxThreads
  }
}

object GetReturnValue extends Reporter {
  override def getSyntax = {
    reporterSyntax(right = List(StringType), ret = WildcardType)
  }

  override def report(args: Array[Argument], context: Context): AnyRef = {
    val values = context.workspace.asInstanceOf[GUIWorkspace].getBehaviorSpaceReturnValues
    
    if (!values.contains(args(0).getString)) {
      BehaviorSpaceExtension.nameError(I18N.gui.getN("tools.behaviorSpace.extension.noReturn", args(0).getString), context)

      return null
    }
    
    return values(args(0).getString)
  }
}
