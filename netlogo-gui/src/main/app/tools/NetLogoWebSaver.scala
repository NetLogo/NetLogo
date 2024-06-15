// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import java.awt.Component
import java.io.IOException

import org.nlogo.api.FileIO
import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.swing.OptionDialog
import org.nlogo.workspace.AbstractWorkspaceScala

import scala.util.matching.Regex

trait NLWTemplateLoader {
  def loadTemplate(): String
}

class JarTemplateLoader(resourceName: String) extends NLWTemplateLoader {
  override def loadTemplate(): String = {
    if (getClass.getResourceAsStream(resourceName) != null)
      FileIO.getResourceAsString(resourceName)
    else
      throw new Exception("Could not find " + resourceName)
  }
}

class NetLogoWebSaver(loader: NLWTemplateLoader, saveFunction: String => Unit, workspace: AbstractWorkspaceScala) {
  val ModelContents = "<NetLogoModel />"
  val ModelName     = "<NetLogoModelName />"

  @throws(classOf[IOException])
  private def collectIncludes(str: String): List[(String, String)] = {
    val includes = workspace.compiler.findIncludes(workspace.getModelPath, str, workspace.getCompilationEnvironment)

    if (includes.isEmpty)
      return Nil
    
    includes.get.flatMap({ case (name, path) =>
      val file = scala.io.Source.fromFile(path)
      val source = file.mkString

      file.close()

      (name, source) :: collectIncludes(source)
    }).toList
  }

  @throws(classOf[UserCancelException])
  @throws(classOf[IOException])
  def save(modelString: String, modelName: String, parent: Component) = {
    val includes = collectIncludes(modelString)

    if (includes.nonEmpty &&
        OptionDialog.showMessage(parent, I18N.gui.get("common.messages.warning"),
                                          I18N.gui.get("menu.file.nlw.prompt.includesWarning"),
                                          Array[Object](I18N.gui.get("common.buttons.ok"),
                                                        I18N.gui.get("common.buttons.cancel"))) == 1)
      throw new UserCancelException()

    var stringMut = modelString

    if (includes.nonEmpty) {
      stringMut = "; Main code\n\n" + stringMut

      for ((name, source) <- includes) {
        stringMut = "; " + name + "\n\n" + source + "\n\n" + stringMut
      }

      stringMut = new Regex("__includes\\s*\\[.*?\\]").replaceAllIn(stringMut, "") // will break if __includes is in a string (IB 6/15/24)
    }

    saveFunction(templateHTML(loader.loadTemplate(), stringMut, modelName))
  }

  def templateHTML(htmlTemplate: String, model: String, modelName: String): String = {
    if (htmlTemplate.contains(ModelContents))
      htmlTemplate
        .replaceAllLiterally(ModelContents, model)
        .replaceAllLiterally(ModelName, modelName.stripSuffix(".html") + ".nlogo")
    else
      throw new IllegalArgumentException("Invalid HTML Template")
  }
}

object NetLogoWebSaver {
  val TemplateFileName = "/system/net-logo-web.html"
  val loader    = new JarTemplateLoader(TemplateFileName)

  def apply(filePath: String, workspace: AbstractWorkspaceScala): NetLogoWebSaver =
    new NetLogoWebSaver(loader, (s) => FileIO.writeFile(filePath, s), workspace)
}
