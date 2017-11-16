// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import
  cats.data.Validated.Valid

import
  org.nlogo.core.ModelInfo

import
  org.nlogo.xmllib.DummyXml, DummyXml._

import
  org.scalatest.{ FunSuite, Matchers },
    Matchers._

object ModelInfoTest {

  val title = namedText("title", "Model Title")
  val tags = namedText("subject", "biology,hubnet,unverified")

  val modelInfoXml =
    Elem("modelInfo",
      Seq(),
      Seq(title, tags))

  val modelInfo = ModelInfo("Model Title", Seq("biology", "hubnet", "unverified"))
}

class ModelInfoTest extends FunSuite with XmlEquality {
  import ModelInfoTest._
  import DummyXml.Factory

  test("reads ModelInfo from xml") {
    assertResult(Valid(modelInfo))(ModelInfoXml.read(modelInfoXml))
  }

  test("writes ModelInfo to xml") {
    ModelInfoXml.write(modelInfo, Factory) should beXmlEqualTo (modelInfoXml)
  }
}
