// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.nlogo.core.{ Button, Monitor, View, Widget, WorldDimensions }

import org.scalatest.FunSuite

object HubNetWidgetXmlTest {
  import DummyXML._
  import WidgetXmlTest.dimensions

  val buttonXml = Elem("button",
    dimensions :+ Attr("actionKey", "a"),
    Seq(namedText("display", "foo")))

  val button =
    Button(source = None, 150, 200, 250, 300, display = Some("foo"), actionKey = Some('a'))

  val viewXml = Elem("view",
    dimensions,
    Seq(Elem("dimensions", Seq(
      Attr("minPxcor", "-10"),
      Attr("maxPxcor", "10"),
      Attr("minPycor", "-10"),
      Attr("maxPycor", "10")),
    Seq())))

  val view =
    View(150, 200, 250, 300, dimensions = WorldDimensions(-10, 10, -10, 10))

  val monitorXml = Elem("monitor",
    dimensions :+ Attr("precision", "5"),
    Seq(namedText("source", "10 + 5"),
      namedText("display", "foo")))

  val monitor = Monitor(Some("10 + 5"), 150, 200, 250, 300, Some("foo"), 5, 11)

  val pairs = Seq[(String, Elem, Widget)](
    ("hubnet button", buttonXml, button),
    ("hubnet view", viewXml, view),
    ("hubnet monitor", monitorXml, monitor)
  )

}

class HubNetWidgetXmlTest extends FunSuite {
  import DummyXML._

  def readToWidget(xml: Element): Widget =
    HubNetWidgetXml.read(xml).toOption.get

  def writeToXml(w: Widget): Element =
    HubNetWidgetXml.write(w, Factory)

  for {
    (name, xml, widget) <- HubNetWidgetXmlTest.pairs
  } {
    test(s"reads $name from xml") {
      assertResult(widget)(readToWidget(xml))
    }

    test(s"writes $name to xml") {
      assertResult(xml)(writeToXml(widget))
    }
  }
}
