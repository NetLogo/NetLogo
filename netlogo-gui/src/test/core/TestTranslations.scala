// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.util.Locale

import org.nlogo.util.AnyFunSuiteEx

import scala.jdk.CollectionConverters.EnumerationHasAsScala

class TestTranslations extends AnyFunSuiteEx {
  val englishKeys: Set[String] = I18N.gui.getBundle(Locale.US).getKeys.asScala.toSet

  Seq("de", "es", "ja", "pt", "ru", "zh").foreach { lang =>
    test(lang) {
      val missingKeys = englishKeys &~ I18N.gui.getBundle(new Locale(lang)).getKeys.asScala.toSet

      if (missingKeys.nonEmpty)
        fail(s"Missing translations for the following keys:\n${missingKeys.toSeq.sorted.mkString("\n")}")
    }
  }
}
