// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.fileformat

import org.parboiled.scala._

import org.nlogo.core.{ LiteralParser, Widget }

object ParboiledWidgetParser extends ParsingStringUtils {
  trait RichRule { this: Parser =>
    implicit class EnrichedRule[S](r: Rule1[S]) {
      def through(otherRule: Rule0): Rule1[S] =
        rule { group(r ~ otherRule) }
      def mapThrough(otherRule: Rule1[S => S]) =
        (r ~ otherRule) ~~> ((s: S, f: S => S) => f(s))
    }

    def DoubleValue: Rule1[Double] = rule { DoubleDigits ~> (digits => digits.toDouble) }

    def DoubleDigits: Rule0 = rule { IntDigits ~ optional("." ~ zeroOrMore(Digit) ~ optional("E" ~ IntDigits)) }

    def Digit: Rule0 = rule { "0" - "9" }

    def IgnoredText: Rule0 = rule { zeroOrMore(noneOf("\n"))  }

    def IgnoredLine: Rule0 = rule { IgnoredText ~ NewLine }

    def IntValue: Rule1[Int] = rule { IntDigits ~> (digits => digits.toInt) }

    def IntDigits: Rule0 = rule { optional("-") ~ oneOrMore(Digit) }

    def NewLine: Rule0 = rule { "\n" }

    def NillableString: Rule1[Option[String]] = rule {
      StringValue ~> (s => if (s == "NIL") None else Some(s))
    }

    def StringRule: Rule1[String] =
      rule { StringValue ~> identity }

    def StringValue: Rule0 = rule { zeroOrMore(noneOf("\n")) }

    def BooleanDigit: Rule1[Boolean] = rule { "0" ~ push(false) | "1" ~ push(true) }

    def PositionInformation: Rule1[(Int, Int, Int, Int)] = rule {
      nTimes(4, IntValue ~ NewLine) ~~> (dims =>
          (dims(0), dims(1), dims(2), dims(3)))
    }
  }
}

trait ParboiledWidgetParser {
  type ParsedWidget <: Widget

  def validatingParseRule(lines: List[String]): Rule1[this.ParsedWidget]

  def parsingRule(lines: List[String], literalParser: LiteralParser): Rule1[this.ParsedWidget]

  def runRule(rule: Rule1[this.ParsedWidget], lines: List[String]): Option[this.ParsedWidget]

  def validate(lines: List[String]): Boolean =
    runRule(validatingParseRule(lines), lines).isDefined

  def parse(lines: List[String], literalParser: LiteralParser): ParsedWidget =
    runRule(parsingRule(lines, literalParser), lines).get
}

trait DebuggingWidgetParser { this: ParboiledWidgetParser =>
  override def runRule(rule: Rule1[this.ParsedWidget], lines: List[String]): Option[this.ParsedWidget] = {
    val formattedLines = lines.mkString("\n")
    val result =
      ReportingParseRunner(rule).run(formattedLines).result
    if (result.isEmpty) {
      println("initial parse failed for:\n" + formattedLines)
      TracingParseRunner(rule).run(formattedLines).result
    }
    result
  }
}

trait ConstantRuleWidgetParser { this: ParboiledWidgetParser =>
  def parseRule: Rule1[this.ParsedWidget]

  def validatingParseRule(lines: List[String]): Rule1[this.ParsedWidget] = parseRule

  def parsingRule(lines: List[String], literalParser: LiteralParser): Rule1[this.ParsedWidget] = parseRule
}

trait DefaultParboiledWidgetParser extends ParboiledWidgetParser with DebuggingWidgetParser with ConstantRuleWidgetParser

trait ParsingStringUtils {
  def stripLines(st: String): String =
    st.flatMap{
      case '\n' => "\\n"
      case '\\' => "\\\\"
      case '\"' => "\\\""
      case c => c.toString
    }

  def restoreLines(s: String): String = {
    @scala.annotation.tailrec
    def loop(acc: Vector[Char], rest: String): Vector[Char] = {
      if (rest.size < 2)
        acc ++ rest
      else if (rest.head == '\\')
        rest.tail.head match {
          case 'n'  => loop(acc :+ '\n', rest.tail.tail)
          case '\\' => loop(acc :+ '\\', rest.tail.tail)
          case '"'  => loop(acc :+ '"', rest.tail.tail)
      case _    => sys.error("invalid escape sequence in \"" + s + "\"")
        }
        else loop(acc :+ rest.head, rest.tail)
    }
    loop(Vector(), s).mkString
  }
}
