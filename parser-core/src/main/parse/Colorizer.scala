// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import java.awt.Color

import org.nlogo.core.{ ColorizerTheme, Program, Token, TokenColorizer, TokenType }

import scala.io.Source

// code in, HTML out!

object Colorizer extends TokenColorizer {

  // for standalone use, for example on a web server
  def main(argv: Array[String]): Unit = {
    for (line <- Source.fromInputStream(System.in).getLines())
      println(Colorizer.toHtml(line, ColorizerTheme.Light))
  }

  // just enough of a subset of Namer to do syntax highlighting with;
  // just knows built-in commands, reporters & agent variables.
  object Namer extends (Token => Token) {
    import org.nlogo.parse.Namer0
    val handlers: Seq[NameHandler] =
      Seq(new CommandHandler(FrontEnd.tokenMapper),
        new ReporterHandler(FrontEnd.tokenMapper),
        new AgentVariableReporterHandler(Program.empty()))
    def apply(token: Token): Token =
      if (token.tpe != TokenType.Ident)
        token
      else
        handlers.iterator
          .map(_(token))
          .collectFirst{
            case Some ((tpe, _)) =>
              token.copy(tpe = tpe)()}
          .getOrElse(Namer0(token))
  }

  def toHtml(line: String, theme: ColorizerTheme): String = {
    // getCharacterColors gives us a color for every character, but we want to wait until
    // the color changes before we start a new font tag.  So we group the colors into
    // sublists of equal colors.
    val colorGroups = group(colorizeLine(line, theme).toList)
    // use a mutable StringBuilder and tail recursion so we don't blow the stack - ST 6/30/09
    val result = new StringBuilder
    def loop(source: String, colorGroups: List[List[Color]]): Unit = {
      if (!colorGroups.isEmpty) {
        val group = colorGroups.head
        result ++= encode(source.take(group.size), group.head)
        loop(source.drop(group.size), colorGroups.tail)
      }
    }
    loop(line, colorGroups)
    result.toString.replace("\n", "<br />")
  }

  def colorizeLine(line: String, theme: ColorizerTheme): Vector[Color] = {
    val result = Array.fill(line.size)(theme.getColor(null))
    for {
      tok <- FrontEnd.tokenizer.tokenizeString(line)
      j <- tok.start until tok.end
      // guard against any bugs in tokenization causing out-of-bounds positions
      if result.isDefinedAt(j)
    } result(j) = colorizeToken(Colorizer.Namer(tok), theme)
    result.toVector
  }

  def colorizeToken(token: Token, theme: ColorizerTheme): Color = {
    // "breed" can be either a keyword or a turtle variable, which means we can't reliably colorize
    // it correctly; so as a kludge we colorize it as a keyword only if it's at the beginning of the
    // line (position 0) - ST 7/11/06
    val tpe =
      if (token.tpe == TokenType.Reporter &&
          token.start == 0 &&
          token.text.equalsIgnoreCase("BREED"))
        TokenType.Keyword
      else
        token.tpe
    theme.getColor(tpe)
  }

  /// HTML generation helpers

  private val escapeMap = Map('&' -> "&amp;",
                              '<' -> "&lt;",
                              '>' -> "&gt;",
                              '"' -> "&quot;")

  private def encode(source: Seq[Char], color: Color) = {
    def toHex(i: Int) =
      (if(i < 16) "0" else "") + i.toHexString
    def escape(c: Char): String =
      escapeMap.getOrElse(c, c.toString)
    "<font color=\"#" +
      toHex(color.getRed) +
      toHex(color.getGreen) +
      toHex(color.getBlue) +
      "\">" +
      source.map(escape).mkString +
      "</font>"
  }

  // like group in Haskell. e.g. group(List(1, 1, 2, 3, 3, 3)) = List(List(1, 1), List(2), List(3, 3, 3)).
  // imperative using ListBuffer so we don't blow the stack on long inputs. - ST 6/30/09
  private def group[T](xs: List[T]): List[List[T]] = {
    val result = new collection.mutable.ListBuffer[List[T]]
    var rest = xs
    while (!rest.isEmpty) {
      val (firstGroup, more) = rest.span(_ == rest.head)
      result += firstGroup
      rest = more
    }
    result.toList
  }

}
