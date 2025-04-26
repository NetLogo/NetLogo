// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import scala.language.experimental.macros
import scala.reflect.macros.blackbox.{ Context => BlackBoxContext}

import java.util.{ Locale, ResourceBundle }

object I18NBundle {
  def errorBundle: Map[String, String] = macro i18nbundle

  def i18nbundle(c: BlackBoxContext): c.Tree = {
    import c.universe._
    // Limitation - this is US-only at the moment
    // It can be made more robust, but we need some way to feed in
    // the current locale that doesn't rely on java.util.Locale
    import scala.jdk.CollectionConverters.SetHasAsScala
    val bundle = ResourceBundle.getBundle("i18n.Errors", Locale.US)
    val localizedStringMap = bundle
      .keySet
      .asScala
      .map(k => q"$k -> ${bundle.getString(k)}")
    q"Map(..$localizedStringMap)"
  }
}
