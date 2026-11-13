// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.util.Locale

object LocaleReader {
  private val available: Map[String, Seq[String]] = {
    Map(
      ("en", Resource.lines("/i18n/en.po"))
    )
  }

  def isAvailable(locale: Locale): Boolean =
    available.contains(locale.getLanguage)

  def readLines(lang: String): Seq[String] =
    available(lang)
}
