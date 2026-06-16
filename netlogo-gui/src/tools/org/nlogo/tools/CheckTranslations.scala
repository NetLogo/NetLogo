// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tools

import java.util.Locale

import org.nlogo.core.I18N

import scala.jdk.CollectionConverters.EnumerationHasAsScala

object CheckTranslations {
  def main(args: Array[String]): Unit = {
    if (args.isEmpty) {
      println("You must specify one or more language codes as inputs.")

      return
    }

    val englishGUI: Set[String] = keySet(I18N.gui, Locale.US)
    val englishShared: Set[String] = keySet(I18N.shared, Locale.US)
    val englishErrors: Set[String] = keySet(I18N.errors, Locale.US)

    args.foreach { lang =>
      val locale = new Locale(lang)

      val missingGUI: Set[String] = englishGUI &~ keySet(I18N.gui, locale)
      val missingShared: Set[String] = englishShared &~ keySet(I18N.shared, locale)
      val missingErrors: Set[String] = englishErrors &~ keySet(I18N.errors, locale)

      if (missingGUI.nonEmpty || missingShared.nonEmpty || missingErrors.nonEmpty) {
        println(s"Language $lang is missing translations for the following keys:")
        printKeys(missingGUI, "(GUI) ")
        printKeys(missingShared, "(Shared) ")
        printKeys(missingErrors, "(Errors) ")
      }
    }
  }

  private def keySet(bundle: I18N.BundleKind, locale: Locale): Set[String] =
    bundle.getBundle(locale).getKeys.asScala.toSet

  private def printKeys(keys: Set[String], prefix: String): Unit = {
    if (keys.nonEmpty)
      println(keys.mkString(s"  $prefix", s"\n  $prefix", ""))
  }
}
