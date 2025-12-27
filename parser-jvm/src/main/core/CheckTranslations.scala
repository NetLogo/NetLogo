// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.util.Locale

import scala.jdk.CollectionConverters.EnumerationHasAsScala

object CheckTranslations {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      println("You must specify one or more language codes as inputs.")

      return
    }

    val englishKeys: Set[String] = I18N.gui.getBundle(Locale.US).getKeys.asScala.toSet

    args.foreach { lang =>
      val missingKeys = englishKeys &~ I18N.gui.getBundle(new Locale(lang)).getKeys.asScala.toSet

      if (missingKeys.nonEmpty)
        println(s"Language $lang is missing translations for the following keys:\n  ${missingKeys.toSeq.sorted.mkString("\n  ")}")
    }
  }
}
