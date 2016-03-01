// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.api.FileIO
import org.nlogo.util.Utils
import scala.io.Source

trait NLWTemplateLoader {
  def loadTemplate(): String
}

class JarTemplateLoader(resourceName: String) extends NLWTemplateLoader {
  override def loadTemplate(): String = {
    if (getClass.getResourceAsStream(resourceName) != null)
      Utils.getResourceAsString(resourceName)
    else
      throw new Exception("Could not find " + resourceName)
  }
}

class NetLogoWebSaver(loader: NLWTemplateLoader, saveFunction: String => Unit) {
  val ModelContents = "<NetLogoModel />"
  val ModelName     = "<NetLogoModelName />"

  def save(modelString: String, modelName: String) =
    saveFunction(templateHTML(loader.loadTemplate(), modelString, modelName))

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

  def apply(filePath: String): NetLogoWebSaver =
    new NetLogoWebSaver(loader, (s) => FileIO.writeFile(filePath, s))
}
