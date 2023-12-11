package org.nlogo.extensions.bspace

import org.nlogo.api.{ Argument, Command, Context, LabDefaultValues, Reporter }
import org.nlogo.core.I18N
import org.nlogo.core.Syntax._
import org.nlogo.swing.BrowserLauncher

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