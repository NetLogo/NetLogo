// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.parboiled2._

import org.nlogo.core.{ LiteralParser, Widget }

object BaseWidgetParser extends ParsingStringUtils {
  trait RichRule { this: Parser =>
    implicit class EnrichedRule[S](r: Rule1[S]) {
      def through(otherRule: Rule0): Rule1[S] =
        rule { (r ~ otherRule) }
      def mapThrough(otherRule: Rule1[S => S]) =
        rule { (r ~ otherRule) ~> ((s: S, f: S => S) => f(s)) }
    }

    def DoubleValue: Rule1[Double] = rule { capture(DoubleDigits) ~> ((digits: String) => digits.toDouble) }

    def DoubleDigits: Rule0 = rule { IntDigits ~ optional("." ~ zeroOrMore(Digit) ~ optional("E" ~ IntDigits)) }

    def Digit: Rule0 = rule { "0" - "9" }

    def IgnoredText: Rule0 = rule { zeroOrMore(noneOf("\n"))  }

    def IgnoredLine: Rule0 = rule { IgnoredText ~ NewLine }

    def IntValue: Rule1[Int] = rule { capture(IntDigits) ~> ((digits: String) => digits.toInt) }

    def IntDigits: Rule0 = rule { optional("-") ~ oneOrMore(Digit) }

    def NewLine: Rule0 = rule { "\n" }

    def NillableString: Rule1[Option[String]] = rule {
      capture(StringValue) ~> ((s: String) => if (s == "NIL") None else Some(s))
    }

    def StringRule: Rule1[String] =
      rule { capture(StringValue) }

    def StringValue: Rule0 = rule { zeroOrMore(noneOf("\n")) }

    def BooleanDigit: Rule1[Boolean] = rule { "0" ~ push(false) | "1" ~ push(true) }

    def PositionInformation: Rule1[(Int, Int, Int, Int)] = rule {
      4.times(IntValue ~ NewLine) ~> ((dims: Seq[Int]) =>
          (dims(0), dims(1), dims(2), dims(3)))
    }
  }
}

trait BaseWidgetParser {
  type ParsedWidget <: Widget
  trait DefaultRule { this: Parser =>
    def defaultRule: Rule1[ParsedWidget]
  }
  type InternalParser <: Parser with DefaultRule

  def parser(lines: List[String], literalParser: LiteralParser): InternalParser

  def validatingParser(lines: List[String]): InternalParser

  def runRule(internalParser: InternalParser): Option[this.ParsedWidget] = {
    val res = internalParser.defaultRule.run()
    res match {
      case scala.util.Failure(e: ParseError) =>
        println(internalParser.formatError(e))
      case _ =>
    }
    res.toOption
  }

  def validate(lines: List[String]): Boolean =
    runRule(validatingParser(lines)).isDefined

  def parse(lines: List[String], literalParser: LiteralParser): ParsedWidget =
    runRule(parser(lines, literalParser)).get
}

// this should be renamed
trait ConstWidgetParser { this: BaseWidgetParser =>
  def parserFromString(s: String): InternalParser

  def parser(lines: List[String], literalParser: LiteralParser): InternalParser =
    parserFromString(lines.mkString("\n"))

  def validatingParser(lines: List[String]): InternalParser =
    parserFromString(lines.mkString("\n"))
}
