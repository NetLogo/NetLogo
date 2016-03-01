// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.scalatest.FunSuite

class NetLogoWebSaverTests extends FunSuite {
  test("nlw jar file locator raises exception when file cannot be located") {
    val loader = new JarTemplateLoader("notfound.html")
    intercept[Exception] {
      loader.loadTemplate()
    }
  }

  test("nlw jar file locator produces string contents of located file") {
    val loader = new JarTemplateLoader("/system/about.txt")
    assert(loader.loadTemplate().contains("html"))
  }

  test("nlwsaver errors when the template doesn't contain a space for the model") {
    var saveFunction: String => Unit = { t => }
    val saver = new NetLogoWebSaver(emptyTemplateLoader, saveFunction)
    intercept[Exception] {
      saver.save(dummyModel, "model name")
    }
  }

  test("nlwsaver returns the template filled in with the transformed html file") {
    var savedText = ""
    var saveFunction: String => Unit = { t => savedText = t }
    val saver = new NetLogoWebSaver(templateLoader, saveFunction)
    saver.save(dummyModel, "model name")
    assert(savedText == dummyTemplate(dummyModel, "model name.nlogo"))
  }

  test("nlwsaver strips the html suffix off received filenames") {
    var savedText = ""
    var saveFunction: String => Unit = { t => savedText = t }
    val saver = new NetLogoWebSaver(templateLoader, saveFunction)
    saver.save(dummyModel, "model name.html")
    assert(savedText == dummyTemplate(dummyModel, "model name.nlogo"))
  }

  val dummyModel = "model text"

  val emptyTemplateLoader = new NLWTemplateLoader() {
    def loadTemplate(): String = ""
  }

  val templateLoader = new NLWTemplateLoader() {
    def loadTemplate(): String = dummyTemplate()
  }

  def dummyTemplate(modelContents: String = "<NetLogoModel />",
                    modelName: String = "<NetLogoModelName />") =
    """|<!DOCTYPE html>
       |<html><body>
       |<script type="application/javascript">
       | function foo() { if (true && false) { return null; } }</script>
       |<script type="text/nlogo" data-filename="""".stripMargin + modelName +
       """">""" + modelContents + """</script></body></html>""".stripMargin
}
