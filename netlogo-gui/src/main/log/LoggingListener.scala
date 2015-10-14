// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

import org.nlogo.api.{ CompilerException, NetLogoListener }
import Logger._

trait LoggingListener extends NetLogoListener {

  // logging this would be excessive - ST 11/11/10
  override def possibleViewUpdate() {}

  // no clue why we do nothing with these two - ST 8/6/11
  override def buttonPressed(buttonName: String) {}
  override def buttonStopped(buttonName: String) {}

  override def tickCounterChanged(ticks: Double) {
    tickMsg.updateGlobalMessage("ticks", ticks.toString)
    Globals.info(tickMsg)
  }
  override def sliderChanged(name: String, value: Double, min: Double,
                    increment: Double, max: Double, valueChanged: Boolean,
                    buttonReleased: Boolean) {
    if (valueChanged) {
      sliderMsg.updateSliderMessage(name.toUpperCase, value, min, max, increment)
      if (buttonReleased)
        Greens.info(sliderMsg)
      else
        Greens.debug(sliderMsg)
    }
  }
  override def switchChanged(name: String, value: Boolean, valueChanged: Boolean) {
    if (valueChanged) {
      switchMsg.updateGlobalMessage(name.toUpperCase, value.toString)
      Greens.info(switchMsg)
    }
  }
  override def chooserChanged(name: String, value: AnyRef, valueChanged: Boolean) {
    if (valueChanged) {
      chooserMsg.updateGlobalMessage(name.toUpperCase, value.toString)
      Greens.info(chooserMsg)
    }
  }
  override def inputBoxChanged(name: String, value: AnyRef, valueChanged: Boolean) {
    if (valueChanged) {
      inputBoxMsg.updateGlobalMessage(name.toUpperCase, value.toString)
      Greens.info(inputBoxMsg)
    }
  }
  override def commandEntered(owner: String, text: String, agentType: Char, error: CompilerException) {
    val (message, start, end) =
      if (error == null)
        ("success", 0, 0)
      else
        (error.getMessage, error.startPos, error.endPos)
    commandMsg.updateCommandMessage(
        owner.toLowerCase, "compiled", text, agentType.toString, message, start, end)
    if (!owner.startsWith("Slider"))
      Code.info(commandMsg)
  }
  override def codeTabCompiled(text: String, error: CompilerException) {
    val (message, start, end) =
      if (error == null)
        ("success", 0, 0)
      else
        (error.getMessage, error.startPos, error.endPos)
    codeTabMsg.updateCodeTabMessage("compiled", text, message, start, end)
    Code.info(codeTabMsg)
  }

}
