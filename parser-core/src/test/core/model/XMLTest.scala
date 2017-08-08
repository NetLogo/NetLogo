// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.scalatest.FunSuite
import org.scalatest.Inside

abstract class XMLProviderTest extends FunSuite with Inside {
  def newElementFactory: ElementFactory
  def emptyElement(tag: String): Element
  def parseXMLString(s: String): Element

  test("empty element has appropriate tag, no children or attributes") {
    val e = emptyElement("foo")
    assert(e.tag == "foo")
    assert(e.attributes.isEmpty)
    assert(e.children.isEmpty)
  }

  test("empty element matches built empty element") {
    val e = emptyElement("foo")
    val f = newElementFactory.newElement("foo").build
    assertResult(f, "empty element matches empty element created via factory")(e)
  }

  test("preserves text") {
    val fooBuilder = newElementFactory.newElement("foo")
    val foo = fooBuilder.withText("abc").build
    assert(foo.children.nonEmpty)
    inside(foo.children.head) {
      case t: Text => assertResult("abc", "text nodes contain specified text")(t.text)
    }
  }

  test("preserves xml-unsafe text") {
    val fooBuilder = newElementFactory.newElement("foo")
    val foo = fooBuilder.withText("<bar />").build
    assert(foo.children.nonEmpty)
    inside(foo.children.head) {
      case t: Text => assertResult("<bar />", "text nodes contain xml-unsafe text verbatim")(t.text)
    }
  }

  test("preserves attributes") {
    val fooBuilder = newElementFactory.newElement("foo")
    val foo = fooBuilder.withAttribute("abc", "def").build
    assert(foo.attributes.nonEmpty)
    inside(foo.attributes.head) {
      case a: Attribute =>
        assertResult("abc", "attribute has name as specified")(a.name)
        assertResult("def", "attribute has value as specified")(a.value)
    }
  }

  test("multiple attributes") {
    val fooBuilder = newElementFactory.newElement("foo")
    val foo = fooBuilder
      .withAttribute("abc", "def")
      .withAttribute("xyz", "123")
      .withAttribute("ghi", "ABC")
      .build
    assert(foo.attributes.nonEmpty)
    assert(foo.attributes.length == 3)
    inside(foo.attributes(1)) {
      case a: Attribute =>
        assertResult("xyz", "attribute has name as specified")(a.name)
        assertResult("123", "attribute has value as specified")(a.value)
    }
  }

  test("preserves xml-unsafe attributes") {
    val fooBuilder = newElementFactory.newElement("foo")
    val foo = fooBuilder.withAttribute("abc", """d"e"f""").build
    assert(foo.attributes.nonEmpty)
    inside(foo.attributes.head) {
      case a: Attribute =>
        assertResult("abc", "attribute has name as specified")(a.name)
        assertResult("""d"e"f""", "xml-unsafe attributes are available as specified")(a.value)
    }
  }

  test("nests elements") {
    val fooBuilder = newElementFactory.newElement("foo")
    val barBuilder = newElementFactory.newElement("bar").withAttribute("baz", "qux")
    val foo = fooBuilder.withElement(barBuilder.build).build
    assert(foo.children.nonEmpty)
    inside(foo.children.head) {
      case e: Element =>
        assertResult("bar", "sub-element has specified tag")(e.tag)
        assertResult("qux", "sub-element has specified attributes")(e.attributes.head.value)
    }
  }

  test("parses basic xml") {
    val xmlSnippet = "<foo>abc</foo>"
    val e = parseXMLString(xmlSnippet)
    assertResult("foo", "has correct tag from parsed xml")(e.tag)
    assert(e.children.length == 1, s"expect foo in $xmlSnippet to contain one child")
    inside(e.children.head) {
      case t: Text => assertResult("abc", s"expect text in $xmlSnippet to contain appropriate text")(t.text)
    }
  }

  test("parses xml with attributes") {
    val xmlSnippet = """<foo bar="qux"></foo>"""
    val e = parseXMLString(xmlSnippet)
    assertResult("foo", "has correct tag from parsed xml")(e.tag)
    assert(e.children.length == 0, s"expect foo in $xmlSnippet to have no children")
    assert(e.attributes.length == 1, s"expect foo in $xmlSnippet to have one attribute")
    inside(e.attributes.head) {
      case a: Attribute =>
        assertResult("bar", s"expect foo in $xmlSnippet to have attribute named bar")(a.name)
        assertResult("qux", s"expect foo[@bar] in $xmlSnippet to have value qux")(a.value)
    }
  }

  test("parses CDATA xml") {
    val xmlSnippet = "<foo><![CDATA[<bar>]]></foo>"
    val e = parseXMLString(xmlSnippet)
    assert(e.children.length == 1, s"expect foo in $xmlSnippet to have one child")
    inside(e.children.head) {
      case t: Text =>
        assertResult("<bar>", s"expect foo's text node in $xmlSnippet to contain text '<bar>'")(t.text)
    }
  }

  test("parses xml with appropriately-escaped attribute values") {
    val xmlSnippet = """<foo bar="&lt;qux&gt;"></foo>"""
    val e = parseXMLString(xmlSnippet)
    assert(e.attributes.length == 1, s"expect foo in $xmlSnippet to have one attribute")
    inside(e.attributes.head) {
      case a: Attribute =>
        assertResult("bar", s"expect foo in $xmlSnippet to have attribute named bar")(a.name)
        assertResult("<qux>", s"expect foo[@bar] in $xmlSnippet to contain unescaped value <qux>")(a.value)
    }
  }
}
