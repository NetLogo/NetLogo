// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import
  org.nlogo.core.{ model, Model },
    model.DummyXML._

import org.nlogo.api.PreviewCommands

class NLogoXPreviewCommandsFormatTest extends NLogoXFormatTest[PreviewCommands] {

  def subject = new NLogoXPreviewCommandsFormat(ScalaXmlElementFactory)

  def modelComponent(model: Model): PreviewCommands =
    model.optionalSectionValue("org.nlogo.modelsection.previewcommands").get

  def attachComponent(preview: PreviewCommands): Model =
    Model().withOptionalSection("org.nlogo.modelsection.previewcommands", Some(preview), PreviewCommands.Default)

  testDeserializes("empty section", Elem("previewCommands", Seq(), Seq()), PreviewCommands.Default, _.source)
  testDeserializes("multiline section",
    Elem("previewCommands", Seq(), Seq(namedText("compiled", Seq("crt 5 [", "  fd 1", "]").mkString("\n")))),
    PreviewCommands.Custom("crt 5 [\n  fd 1\n]"), _.source)
  testRoundTripsObjectForm("default preview commands", PreviewCommands.Default)
  testRoundTripsObjectForm("manual preview", PreviewCommands.Manual)
}
