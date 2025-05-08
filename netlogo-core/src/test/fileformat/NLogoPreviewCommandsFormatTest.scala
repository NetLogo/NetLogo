// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.nlogo.core.Model
import org.nlogo.api.PreviewCommands

class NLogoPreviewCommandsFormatTest extends NLogoFormatTest[PreviewCommands] {

  def subject = new NLogoPreviewCommandsFormat

  def modelComponent(model: Model): PreviewCommands =
    model.optionalSectionValue("org.nlogo.modelsection.previewcommands").get.asInstanceOf[PreviewCommands]

  def attachComponent(preview: PreviewCommands): Model =
    Model().withOptionalSection("org.nlogo.modelsection.previewcommands", Some(preview), PreviewCommands.Default)

  testDeserializes("empty section", Array[String](), PreviewCommands.Default, _.source)
  testDeserializes("multiline section", Array[String]("crt 5 [", "  fd 1", "]"), PreviewCommands.Custom("crt 5 [\n  fd 1\n]"), _.source)
  testRoundTripsObjectForm("default preview commands", PreviewCommands.Default)
  testRoundTripsObjectForm("manual preview", PreviewCommands.Manual)
}
