// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import
  cats.data.Validated.{ Invalid, Valid }

import
  org.scalatest.FunSuite

class XmlReaderTest extends FunSuite {
  import DummyXML._

  test("xml choice reader") {
    val choiceA = namedText("a", "xyz")
    val choiceB = namedText("b", "123")
    val wrongChoice = namedText("c", "xyz")

    val aReader = XmlReader.elemReader("a")
    val bReader = XmlReader.elemReader("b")
    val reader = XmlReader.choiceElementReader(Seq(aReader, bReader)).atPath("foo")

    assertResult(namedText("a", "xyz"))(reader.read(choiceA).toOption.get)
    assertResult(namedText("b", "123"))(reader.read(choiceB).toOption.get)
    assertResult(new MissingElement(Seq("foo"), "a or b"))(reader.read(wrongChoice).swap.toOption.get)
  }

  test("xml sequence reader") {
    val seqEmpty = Elem("seq", Seq(), Seq())
    val seqA = Elem("seq", Seq(), Seq(namedText("a", "xyz")))
    val seqAA = Elem("seq", Seq(), Seq(namedText("a", "xyz"), namedText("a", "123")))
    val seqB = Elem("seq", Seq(), Seq(namedText("b", "ABC")))

    val reader = XmlReader.sequenceElementReader("seq", 1, XmlReader.elemReader("a").map(XmlReader.childText _))
    assertResult(Invalid(MissingElement(Seq("seq"), "a")))(reader.read(seqEmpty))
    assertResult(Seq("xyz"))(reader.read(seqA).toOption.get)
    assertResult(Seq("xyz", "123"))(reader.read(seqAA).toOption.get)
    assertResult(Invalid(MissingElement(Seq("seq"), "a")))(reader.read(seqB))
  }

  test("xml chain reader (min 0) reads element sequence") {
    val seqA = Seq(namedText("a", "xyz"))
    val seqB = Seq(namedText("b", "xyz"))
    val seqAB = seqA ++ seqB

    val reader = XmlReader.chainElementReader(0, None, XmlReader.elemReader("a").map(XmlReader.childText _))

    assertResult(Valid((Seq(), Seq())))(reader.read(Seq()))
    assertResult(Valid((Seq("xyz"), Seq())))(reader.read(seqA))
    assertResult(Valid((Seq(), seqB)))(reader.read(seqB))
    assertResult(Valid((Seq("xyz"), seqB)))(reader.read(seqAB))
  }

  test("xml chain reader passes on errors of child readers") {
    val seqA = Seq(namedText("a", "xyz"))

    val reader = XmlReader.chainElementReader(0, None, XmlReader.elemReader("a").flatMap(XmlReader.doubleReader("foo").read _))

    assertResult(Invalid(MissingKeys(Seq(), Seq("foo"))))(reader.read(seqA))
  }

  test("xml chain reader enforces minimum number of elements") {
    val seqA = Seq(namedText("a", "xyz"))
    val seqB = Seq(namedText("b", "xyz"))
    val seqAB = seqA ++ seqB

    val reader = XmlReader.chainElementReader(1, None, XmlReader.elemReader("a").map(XmlReader.childText _))

    assertResult(Invalid(TooFewElements(Seq(), "a", 1, 0)))(reader.read(Seq()))
    assertResult(Valid((Seq("xyz"), Seq())))(reader.read(seqA))
    assertResult(Invalid(TooFewElements(Seq(), "a", 1, 0)))(reader.read(seqB))
    assertResult(Valid((Seq("xyz"), seqB)))(reader.read(seqAB))
  }

  test("xml chain reader enforces maximum number of elements") {
    val a = namedText("a", "xyz")
    val seqAAA = Seq(a, a, a)
    val reader = XmlReader.chainElementReader(0, Some(2), XmlReader.elemReader("a").map(XmlReader.childText _))

    assertResult(Invalid(TooManyElements(Seq(), "a", 2, 3)))(reader.read(seqAAA))
  }

  test("xml pointsReader") {
    val xml = Elem("x", Seq(Attr("points", "1,2 4,5 7,9")), Seq())
    val missingNumXml = Elem("x", Seq(Attr("points", "1,")), Seq())
    val invalidNumXml = Elem("x", Seq(Attr("points", "1,abc")), Seq())
    val threeNumXml = Elem("x", Seq(Attr("points", "1,2,3")), Seq())
    val reader = XmlReader.pointsReader("points")
    assertResult(Valid(Seq((1, 2), (4, 5), (7, 9))))(reader.read(xml))
    assertResult(Invalid(InvalidAttribute("points", "1,")))(reader.read(missingNumXml))
    assertResult(Invalid(InvalidAttribute("points", "1,abc")))(reader.read(invalidNumXml))
    assertResult(Invalid(InvalidAttribute("points", "1,2,3")))(reader.read(threeNumXml))
  }

  test("xml dashArrayReader") {
    val xml = Elem("x", Seq(Attr("dashes", "0,1,2,3")), Seq())
    val emptyStringXml = Elem("x", Seq(Attr("dashes", "")), Seq())
    val blankStringXml = Elem("x", Seq(Attr("dashes", " ")), Seq())
    val invalidNumXml = Elem("x", Seq(Attr("dashes", "1,abc")), Seq())
    val reader = XmlReader.dashArrayReader("dashes")

    assertResult(Valid(Seq(0.0f, 1.0f, 2.0f, 3.0f)))(reader.read(xml))
    assertResult(Invalid(InvalidAttribute("dashes", "")))(reader.read(emptyStringXml))
    assertResult(Invalid(InvalidAttribute("dashes", " ")))(reader.read(blankStringXml))
    assertResult(Invalid(InvalidAttribute("dashes", "1,abc")))(reader.read(invalidNumXml))
  }
}
