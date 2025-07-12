// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.tools

import org.scalatest.funsuite.AnyFunSuite

class NetLogoWebSaverTests extends AnyFunSuite {
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
    val saveFunction: String => Unit = { t => }
    val saver = new NetLogoWebSaver(emptyTemplateLoader, saveFunction)
    intercept[Exception] {
      saver.save(dummyModel, "model name")
    }
  }

  test("nlwsaver returns the template filled in with the transformed html file") {
    var savedText = ""
    val saveFunction: String => Unit = { t => savedText = t }
    val saver = new NetLogoWebSaver(templateLoader, saveFunction)
    saver.save(dummyModel, "model name")
    assert(savedText == dummyTemplate(dummyModel, "model name.nlogox"))
  }

  test("nlwsaver strips the html suffix off received filenames") {
    var savedText = ""
    val saveFunction: String => Unit = { t => savedText = t }
    val saver = new NetLogoWebSaver(templateLoader, saveFunction)
    saver.save(dummyModel, "model name.html")
    assert(savedText == dummyTemplate(dummyModel, "model name.nlogox"))
  }

  test("nlwsaver bundles plainly formatted included files") {
    val modelString = "__includes [ \"New File 1.nls\" ]\n"
    var savedText = ""

    new NetLogoWebSaver(templateLoader, savedText = _)
      .save(modelString, "test.html", Seq(("New File 1.nls", "to test\nend\n")))

    assert(savedText == dummyTemplate("; New File 1.nls\n\nto test\nend\n\n; Main code\n\n", "test.nlogox"))
  }

  test("nlwsaver bundles strangely formatted included files") {
    val modelString = "\n\n       __includes [\n\n   \"New File 1.nls\"    \n\n\n\n]\n"
    var savedText = ""

    new NetLogoWebSaver(templateLoader, savedText = _)
      .save(modelString, "test.html", Seq(("New File 1.nls", "to test\nend\n")))

    assert(savedText == dummyTemplate("; New File 1.nls\n\nto test\nend\n\n; Main code\n\n\n\n       ", "test.nlogox"))
  }

  test("nlwsaver bundles multiple included files") {
    val modelString = "__includes [\n  \"New File 1.nls\"\n  \"New File 2.nls\"\n]\n"
    var savedText = ""

    new NetLogoWebSaver(templateLoader, savedText = _)
      .save(modelString, "test.html", Seq(("New File 1.nls", "to test\nend\n"), ("New File 2.nls", "to test2\nend\n")))

    assert(savedText == dummyTemplate(
      "; New File 2.nls\n\nto test2\nend\n\n; New File 1.nls\n\nto test\nend\n\n; Main code\n\n", "test.nlogox"))
  }

  test("nlwsaver preserves __includes in procedure names") {
    val modelString = "to test__includes []\nend\n"
    var savedText = ""

    new NetLogoWebSaver(templateLoader, savedText = _).save(modelString, "test.html")

    assert(savedText == dummyTemplate(modelString, "test.nlogox"))
  }

  test("nlwsaver preserves __includes in strings") {
    val modelString = "to test\n  let x \"__includes [ \\\"New File 1.nls\\\" ]\"\nend\n"
    var savedText = ""

    new NetLogoWebSaver(templateLoader, savedText = _).save(modelString, "test.html")

    assert(savedText == dummyTemplate(modelString, "test.nlogox"))
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
