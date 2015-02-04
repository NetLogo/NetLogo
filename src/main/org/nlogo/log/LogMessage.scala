// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.log

object LogMessage {
  def createGlobalMessage(tyype: String): LogMessage = {
    val msg = new LogMessage("event")
    msg.attributes = Array(Array("type", tyype))
    msg.elements = Array(new LogMessage("name"),
                         new LogMessage("value"))
    msg
  }
  def createSliderParameterMessage(): LogMessage = {
    val msg = new LogMessage("parameters")
    msg.elements = Array(new LogMessage("min"),
                         new LogMessage("max"),
                         new LogMessage("inc"))
    msg
  }
  def createSliderMessage(): LogMessage = {
    val msg = new LogMessage("event")
    msg.attributes = Array(Array("type", "slider"))
    msg.elements = Array(new LogMessage("action"),
                         new LogMessage("name"),
                         new LogMessage("value"),
                         createSliderParameterMessage())
    msg.elements(0).data = "changed"
    msg
  }
  def createButtonMessage(): LogMessage = {
    val msg = new LogMessage("event")
    msg.attributes = Array(Array("type", "button"))
    msg.elements = Array(new LogMessage("name"),
                         new LogMessage("action"),
                         new LogMessage("releaseType"))
    msg
  }
  def createWidgetMessage(): LogMessage =  {
    val msg = new LogMessage("event")
    msg.attributes = Array(Array("type", null))
    msg.elements = Array(new LogMessage("name"),
                         new LogMessage("action"))
    msg
  }
  def createCommandMessage(): LogMessage = {
    val msg = new LogMessage("event")
    msg.attributes = Array(Array("type", null))
    msg.elements = Array(new LogMessage("action"),
                         new LogMessage("code"),
                         new LogMessage("agentType"),
                         new LogMessage("errorMessage"))
    msg.elements(3).attributes = Array(Array("startPos", null),
                                       Array("endPos", null))
    msg
  }
  def createCodeTabMessage(): LogMessage = {
    val msg = new LogMessage("event")
    msg.attributes = Array(Array("type", null))
    msg.elements = Array(new LogMessage("code"),
                         new LogMessage("errorMessage"))
    msg.elements(1).attributes = Array(Array("startPos", null),
                                       Array("endPos", null))
    msg
  }
  def createSpeedMessage(): LogMessage = {
    val msg = new LogMessage("event")
    msg.attributes = Array(Array("type", "speed"))
    msg.elements = Array(new LogMessage("value"))
    msg
  }
  def createAgentMessage(): LogMessage = {
    val msg = new LogMessage("event")
    msg.attributes = Array(Array("type", null))
    msg.elements = Array(new LogMessage("name"),
                         new LogMessage("action"),
                         new LogMessage("breed"))
    msg
  }
  def createCustomMessage(): LogMessage = {
    val msg = new LogMessage("special-event")
    msg.attributes = Array(Array("type", "custom message"))
    msg.elements = Array(new LogMessage("message"))
    msg
  }
  def createCustomGlobals(): LogMessage = {
    val msg = new LogMessage("special-event")
    msg.attributes = Array(Array("type", "custom globals"))
    msg.elements = Array(new LogMessage("globals"))
    msg
  }
}

class LogMessage private (val tag: String) {

  var attributes: Array[Array[String]] = _
  var elements: Array[LogMessage] = _
  var data: String = _

  def hasAttributes = attributes != null
  def hasElements = elements != null

  def updateGlobalMessage(name: String, value: String) {
    elements(0).data = name
    elements(1).data = value
  }
  def updateSliderMessage(name: String, value: Double, min: Double, max: Double, inc: Double) {
    elements(1).data = name
    elements(2).data = value.toString
    elements(3).elements(0).data = min.toString
    elements(3).elements(1).data = max.toString
    elements(3).elements(2).data = inc.toString
  }
  def updateButtonMessage(name: String, action: String, who: String) {
    elements(0).data = name
    elements(1).data = action
    elements(2).data = who
  }
  def updateWidgetMessage(tyype: String, name: String, action: String) {
    attributes(0)(1) = tyype
    elements(0).data = name
    elements(1).data = action
  }
  def updateAgentMessage(tyype: String, name: String, action: String, breed: String) {
    attributes(0)(1) = tyype
    elements(0).data = name
    elements(1).data = action
    elements(2).data = breed
  }
  def updateCommandMessage(tyype: String, action: String, code: String, agentType: String, errorMessage: String, startPos: Int, endPos: Int) {
    attributes(0)(1) = tyype
    elements(0).data = action
    elements(1).data = code
    elements(2).data = agentType
    elements(3).data = errorMessage
    elements(3).attributes(0)(1) = startPos.toString
    elements(3).attributes(1)(1) = endPos.toString
  }
  def updateCodeTabMessage(tyype: String, code: String, errorMessage: String, startPos: Int, endPos: Int) {
    attributes(0)(1) = tyype
    elements(0).data = code
    elements(1).data = errorMessage
    elements(1).attributes(0)(1) = startPos.toString
    elements(1).attributes(1)(1) = endPos.toString
  }
  def updateSpeedMessage(value: String) {
    elements(0).data = value
  }
  def updateCustomMessage(msg: String): Unit = {
    elements(0).data = msg
  }
  def updateCustomGlobals(nameValuePairs: Seq[(String, String)]): Unit = {
    val elems = nameValuePairs.map {
      case (name, value) =>
        val msg = new LogMessage("global")
        msg.attributes = Array(Array("name", name), Array("value", value))
        msg
    }.toArray
    elements(0).elements = elems
  }

}
