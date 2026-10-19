package org.nlogo.extensions.resource

import java.awt.EventQueue
import java.io.File

import org.nlogo.api.{ Argument, Context, DefaultClassManager, ExtensionException, ExternalResourceManager,
                       PrimitiveManager, Reporter }
import org.nlogo.core.{ ExternalResource, I18N, LogoList, Syntax }
import org.nlogo.workspace.AbstractWorkspace

import scala.io.Source

class ResourceExtension extends DefaultClassManager {
  def load(manager: PrimitiveManager): Unit = {
    manager.addPrimitive( "get", Get)
    manager.addPrimitive("list", ListPrim)
  }
}

object Get extends Reporter {
  override def getSyntax: Syntax =
    Syntax.reporterSyntax(right = List(Syntax.StringType), ret = Syntax.StringType)

  def report(args: Array[Argument], context: Context): String = {
    context.workspace.getResourceManager.getResource(args(0).getString) match {
      case Some(resource) =>
        resource.data
      case None =>
        if (context.workspace.workspaceContext.appGUI) {
          throw new ExtensionException(I18N.gui.getN("resource.noResource", args(0).getString))
        } else {
          // I18N can't be used in headless, so default to the English message (Isaac B 7/29/25)
          throw new ExtensionException(s"Resource \"${args(0).getString}\" does not exist")
        }
    }
  }
}

object ListPrim extends Reporter {

  override def getSyntax: Syntax = Syntax.reporterSyntax(ret = Syntax.ListType)

  override def report(args: Array[Argument], context: Context): LogoList = {
    val names = context.workspace.getResourceManager.getResources.map(_.name)
    LogoList(names*)
  }

}
