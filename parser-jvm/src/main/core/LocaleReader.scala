// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.util.Locale

import scala.util.Try

object LocaleReader {
  def isAvailable(locale: Locale): Boolean =
    Try(Resource.asString(s"/i18n/${locale.getLanguage}.po")).isSuccess

  def readLines(lang: String): Iterator[String] =
    Resource.lines(s"/i18n/$lang.po")
}
