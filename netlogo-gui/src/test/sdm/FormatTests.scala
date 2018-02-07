// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm

import
  org.nlogo.core.{ Model => CoreModel }

import
  org.nlogo.xmllib.DummyXml,
		DummyXml.{ Attr, Elem, namedText }

import
  org.nlogo.fileformat.NLogoXFormatTest

import
  scala.collection.mutable.ListBuffer

class FormatTests extends NLogoXFormatTest[Model] {
  def subject = new NLogoXSDMFormat(DummyXml.Factory)
  def modelComponent(model: CoreModel): Model =
    model.optionalSectionValue[Model]("org.nlogo.modelsection.systemdynamics").get
  def attachComponent(sdmModel: Model): CoreModel =
    CoreModel().withOptionalSection("org.nlogo.modelsection.systemdynamics", Some(sdmModel), sdmModel)

  val sampleGUI =
   """|    org.nlogo.sdm.gui.AggregateDrawing 3
      |        org.nlogo.sdm.gui.StockFigure "attributes" "attributes" 1 "FillColor" "Color" 225 225 182 388 270 60 40
      |            org.nlogo.sdm.gui.WrappedStock "stock" "1" 0
      |        org.nlogo.sdm.gui.ReservoirFigure "attributes" "attributes" 1 "FillColor" "Color" 192 192 192 81 274 30 30
      |        org.nlogo.sdm.gui.RateConnection 3 111 289 243 289 376 289 NULL NULL 0 0 0
      |            org.jhotdraw.figures.ChopEllipseConnector REF 3
      |            org.jhotdraw.standard.ChopBoxConnector REF 1
      |            org.nlogo.sdm.gui.WrappedRate "stock + 3" "inflow"
      |                org.nlogo.sdm.gui.WrappedReservoir  REF 2 0""".stripMargin

  val sampleElems = ListBuffer[ModelElement](
  {
    val s = new Stock
    s.setName("stock")
    s.setInitialValueExpression("1")
    s.setNonNegative(false)
    s
  },
  {
    val r = new Reservoir
    r
  },
  {
    val r = new Rate
    r.setExpression("stock + 3")
    r.setName("inflow")
    r
  })

	val sampleModel =
    new Model("Test Model", 2.0,
      elements = sampleElems,
      serializedGUI = sampleGUI)

  override def compareDeserialized(a: Model, b: Model): Boolean =
    modelEquals(a, b)

  def displayModel(m: Model): String =
    s"Model(${m.name}, ${m.dt}, ${m.elements.length}, ${m.serializedGUI})"

  def modelEquals(m1: Model, m2: Model): Boolean =
    m1.name == m2.name && m1.dt == m2.dt && m1.elements.length == m2.elements.length && m1.serializedGUI == m2.serializedGUI

	testDeserializes("empty model",
    Elem("systemDynamics", Seq(Attr("dt", "2.0")), Seq(namedText("jhotdraw6", sampleGUI))),
    sampleModel, displayModel _)

  testRoundTripsObjectForm("empty model", sampleModel)
}
