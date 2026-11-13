// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.util.Locale

object LocaleContext {
  private var currentLocale: Locale = Locale.US

  private var strings = Map[String, Map[Seq[String], Seq[String]]]()

  def getLocale: Locale =
    currentLocale

  def setLocale(locale: Locale): Unit = {
    currentLocale = locale
  }

  private def getString(lang: String, input: Seq[String]): Seq[String] = {
    if (!strings.contains(lang)) {
      try {
        strings += ((lang, LocaleReader.readLines(lang).sliding(2).collect {
          case Seq(s"msgid \"$key\"", s"msgstr \"$value\"") =>
            ((key.split("\\{\\}").toSeq, value.split("\\{\\}").toSeq))
        }.toMap))
      } catch {
        case _ =>
          strings += ((lang, Map()))
      }
    }

    strings.get(lang).flatMap(_.get(input)).getOrElse(input)
  }

  @scala.annotation.static
  def format(parts: Seq[String], args: Seq[String]): String = {
    val stripped: Seq[String] = parts.map(_.split('\n').map("\\s*\\|".r.replaceFirstIn(_, "")).mkString(" "))

    getString(currentLocale.getLanguage, stripped).zip(args :+ "").map(_ + _).mkString.replace("\\n", "\n")
  }
}

implicit class LocaleContext(sc: StringContext) extends AnyVal {
  // use this context for strings that should be localized. for multi line strings, each line should begin with a `|`
  // so that excess whitespace can be removed correctly, like with `stripMargin`. line breaks will be converted to
  // spaces unless `\n` is included at the end of the line. (Isaac B 11/17/26)
  def loc(args: Any*): String =
    LocaleContext.format(sc.parts, args.map(_.toString))
}
