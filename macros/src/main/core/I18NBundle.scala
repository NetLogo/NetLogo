// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.util.{ Locale, ResourceBundle }

import scala.quoted.*

object I18NBundle {

  inline def errorBundle: Map[String, String] = ${ i18nbundle }

  private def i18nbundle(using Quotes): Expr[Map[String, String]] = {

    import scala.jdk.CollectionConverters.SetHasAsScala

    // Limitation - this is US-only at the moment
    // It can be made more robust, but we need some way to feed in
    // the current locale that doesn't rely on java.util.Locale
    val bundle = ResourceBundle.getBundle("i18n.Errors", Locale.US)

    val localizedStringMap =
      bundle
        .keySet
        .asScala
        .map(k => k -> bundle.getString(k))
        .toMap

    Expr(localizedStringMap)

  }

}
