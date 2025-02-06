// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import org.nlogo.api.FileIO

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

class NetLogoWebSaver(loader: NLWTemplateLoader, saveFunction: String => Unit) {
  val ModelContents = "<NetLogoModel />"
  val ModelName     = "<NetLogoModelName />"

  def save(modelString: String, modelName: String, includes: List[(String, String)] = Nil) = {
    var stringMut = modelString

    if (includes.nonEmpty) {
      stringMut = "; Main code\n\n" + stringMut

      for ((name, source) <- includes) {
        stringMut = "; " + name + "\n\n" + source + "\n\n" + stringMut
      }

      stringMut = new Regex("__includes\\s*\\[.*?\\]").replaceAllIn(stringMut, "") // will break if __includes is in a string (Isaac B 6/15/24)
    }

    saveFunction(templateHTML(loader.loadTemplate(), stringMut, modelName))
  }

  def templateHTML(htmlTemplate: String, model: String, modelName: String): String = {
    if (htmlTemplate.contains(ModelContents))
      htmlTemplate
        .replaceAllLiterally(ModelContents, model)
        .replaceAllLiterally(ModelName, modelName.stripSuffix(".html") + ".nlogox")
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
