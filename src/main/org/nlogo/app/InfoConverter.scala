// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

/**
 * Through NetLogo 4.1 (since 2.0 or so), we used a custom wiki-type format, with custom code for
 * converting it to HTML for display.  Now we use Markdown, but old models must be supported, so we
 * convert the old markup to Markdown.
 */

object InfoConverter {

  /// to convert, we just chain a bunch of replacer functions together

  lazy val convert: String => String =
    List(ignoreLinesOfDashes, convertHeaders,
         convertPreformattedText, putBlankLinesAroundPreformattedSections,
         forceLineBreaks, collapseBlankLines)
     .reduceLeft(_ andThen _)

  /// here's the individual regex-based replacer functions
  /// (note that (?m) turns on multiline matching)

  private val ignoreLinesOfDashes =
    replace("""(?m)^\s*-+\s*$""",  // whitespace, dashes, whitespace
            "")
  private val convertHeaders = {
    val anythingButLowercaseLetter = """[\p{Print}&&\P{Lower}]*"""
    // a header's a line that comes after a blank line and contains capital letters but no lower case letters.
    // (\A matches the beginning of input, to handle the special case where the first line is a header.)
    replace("""(?m)^(\s*\n|\A)(""" + anythingButLowercaseLetter + "[A-Z]" + anythingButLowercaseLetter + ")$",
            "$1## $2\n")
  }
  private val convertPreformattedText =
    replace("""(?m)^\|""",  // lines beginning with |
            "    ")
  private val putBlankLinesAroundPreformattedSections =
    replace("""(?m)((    .*\n?)+)""",
            "\n$0\n")
  private val forceLineBreaks =
    replace("""([^\n])(\n\S)""",
            "$1  $2")  // Markdown indicates forced line breaks by ending the line with two spaces
  // some of the previous rules tends to result in runs of blank lines, so try to tighten those up
  private val collapseBlankLines =
    replace("\n\n\n",
            "\n\n")

  /// helper for defining the replacer functions

  private def replace(s1: String, s2: String) =
    (_: String).replaceAll(s1, s2)

}
