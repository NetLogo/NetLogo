// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.util.{ MissingResourceException, Locale, ResourceBundle }

object I18N {

  // if the users locale from the preferences is available, use it.
  LocaleContext.setLocale(localeFromPreferences.getOrElse {
    // if not, see if the default (from the OS or JVM) is available. if so, use it.
    // otherwise, fall back.
    localeIfAvailable(Locale.getDefault).getOrElse(Locale.US)
  })

  def availableLocales: Array[Locale] =
    Locale.getAvailableLocales.filter(available)

  def available(locale: Locale) =
    try {
      val rb = ResourceBundle.getBundle("i18n.Errors", locale, getClass.getClassLoader)
      // if there's a bundle with the right language, that's good enough.
      // don't worry if the country code doesn't match. - ST 10/31/11
      rb.getLocale.getLanguage == locale.getLanguage
    }
    catch { case m: MissingResourceException => false }

  def localeIfAvailable(loc: Locale): Option[Locale] =
    if(available(loc)) Some(loc)
    else None

  // loads the locale data from the users preferences
  // but only if that locale is available.
  def localeFromPreferences: Option[Locale] = {
    def getPref(p: String): Option[String] =
      try {
        Option(NetLogoPreferences.get(p, "")).filter(_.nonEmpty)
      }
      catch {
        // security manager might say no
        case _: SecurityException =>
          None
      }
    (getPref("user.language"), getPref("user.country")) match {
      case (Some(l), Some(r)) => localeIfAvailable(Locale.of(l, r))
      case (Some(l), _) => localeIfAvailable(Locale.of(l))
      case _ => None
    }
  }

}
