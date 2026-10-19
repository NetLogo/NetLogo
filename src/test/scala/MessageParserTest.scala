package arduino

import org.scalatest.Inside
import org.scalatest.funsuite.AnyFunSuite

class MessageParserTest extends AnyFunSuite with Inside {
  testInvalidFieldCount("")
  testInvalidFieldCount("abc,")
  testInvalidFieldCount("abc,s,def,bar")
  testInvalidFieldCount("abc,s,def\\,bar,qux")
  testInvalidFieldCount("abc,s,def\\,\\,bar,qux")

  testParsesString("d,ef", "d\\,ef")
  testParsesString("d,e,f", "d\\,e\\,f")
  testParsesString("d\\f", "d\\f")

  test("parseStream parses the empty string to an empty set of values") {
    assertResult(("", Seq.empty[Either[ErrorRecord, ValuePair]]))(
      MessageParser.parseStream(""))
  }

  test("parseStream does not parse strings without semicolons and returns them") {
    assertResult(("abc,d,1", Seq.empty[Either[ErrorRecord, ValuePair]]))(
      MessageParser.parseStream("abc,d,1"))
  }

  test("parseStream parses strings with semicolons and returns unparsed portion") {
    assertResult(("def", Seq(Right(ValuePair("abc", Double.box(1))))))(
      MessageParser.parseStream("abc,d,1;def"))
  }

  test("parseStream parses multiple results and returns them") {
    assertResult(("", Seq(Right(ValuePair("abc", Double.box(1))),
      Left(ErrorRecord("def,", "Arduino values must have three comma-separated fields", None)))))(
        MessageParser.parseStream("abc,d,1;def,;"))
  }

  test("parseStream does not list errors for multiple semicolons together") {
    assertResult(("", Seq.empty[Either[ErrorRecord, ValuePair]]))(
      MessageParser.parseStream(";;;"))
  }

  test("does not raise exception when passed a message with a blank type field") {
    assertResult(("", Seq(Left(ErrorRecord("abc,,1", "Unknown type '' for value 'abc'", None)))))(
      MessageParser.parseStream("abc,,1;"))
  }

  test("parseEntry parses a string") {
    assertValid("abc", "def", "abc,s,def")
    assertValid("abc", "def", "ABC,S,def")
  }

  test("parseEntry parses a double") {
    assertValid("abc", Double.box(123), "abc,D,123")
    assertValid("abc", Double.box(123), "ABC,d,123")
  }

  test("parseEntry returns an error when the entry is of an unknown type") {
    assertInvalid("abc,J,qux", "Unknown type 'J' for value 'abc'")
  }

  test("parseEntry returns an error value for an unparseable double") {
    inside(MessageParser.parseEntry("A,D,ABC")) {
      case Left(ErrorRecord("A,D,ABC", "Cannot parse number", Some(e))) =>
        assert(e.getMessage == """For input string: "ABC"""")
    }
  }

  def assertInvalid(entry: String, desc: String, expectedException: Option[Exception] = None) = {
    assertResult(Left(ErrorRecord(entry, desc, expectedException)))(
      MessageParser.parseEntry(entry))
  }

  def assertValid(key: String, value: AnyRef, entry: String): Unit = {
    assertResult(Right(ValuePair(key, value)))(MessageParser.parseEntry(entry))
  }

  def testParsesString(expected: String, stringEntry: String): Unit = {
    test(s"parses '$stringEntry' to string value '$expected'") {
      val fullEntry = s"abc,s,${stringEntry}"
      assertValid("abc", expected, fullEntry)
    }
  }

  def testInvalidFieldCount(entry: String): Unit = {
    test(s"returns an error for the entry '$entry'") {
      assertInvalid(entry, "Arduino values must have three comma-separated fields")
    }
  }
}
