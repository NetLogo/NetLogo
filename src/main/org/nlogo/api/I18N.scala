// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.util.{ MissingResourceException, Locale, ResourceBundle }

object I18N {

  def availableLocales: Array[Locale] =
    Locale.getAvailableLocales.filter(available)

  def available(locale: Locale) =
    try {
      val rb = ResourceBundle.getBundle("Errors", locale, getClass.getClassLoader)
      // if there's a bundle with the right language, that's good enough.
      // don't worry if the country code doesn't match. - ST 10/31/11
      rb.getLocale.getLanguage == locale.getLanguage
    }
    catch { case m: MissingResourceException => false }

  def localeIfAvailable(loc: Locale): Option[Locale] =
    if(available(loc)) Some(loc)
    else None

  case class Prefix(name: String)

  class BundleKind(name: String) extends I18NJava {

    val defaultLocale = {
      import java.util.prefs._
      def getPref(p: String): Option[String] =
        try {
          val netLogoPrefs = Preferences.userRoot.node("/org/nlogo/NetLogo")
          Option(netLogoPrefs.get(p, "")).filter(_.nonEmpty)
        }
        catch {
          case _: java.security.AccessControlException =>
            None  // we must be in the applet
        }
      // loads the locale data from the users preferences
      // but only if that locale is available.
      val localeFromPreferences: Option[Locale] =
        (getPref("user.language"), getPref("user.region")) match {
          case (Some(l), Some(r)) => localeIfAvailable(new Locale(l, r))
          case (Some(l), _) => localeIfAvailable(new Locale(l))
          case _ => None
        }
      // if the users locale from the preferences is available, use it.
      localeFromPreferences.getOrElse(
        // if not, see if the default (from the OS or JVM) is available. if so, use it.
        // otherwise, fall back.
        localeIfAvailable(Locale.getDefault).getOrElse(Locale.US)
      )
    }

    // here we get both bundles (both of which should be available)
    // we get both because the default bundle might not contain all the keys
    // maybe some got left out in translation. if that happens
    // we need to fall back and use the english string instead of erroring.
    // its very possible (and in fact in most cases, likely) that the
    // defaultBundle IS the english bundle, but that is ok.
    private var defaultBundle = getBundle(defaultLocale)
    private val englishBundle = getBundle(Locale.US)
    def getBundle(locale: Locale) = ResourceBundle.getBundle(name, locale)
    def apply(key: String)(implicit prefix: Prefix) = get(prefix.name + "." + key)
    override def get(key: String) = getN(key)
    override def getN(key: String, args: AnyRef*) = {
      def getFromBundle(bundle: ResourceBundle): Option[String] =
        try Some(bundle.getString(key))
        catch { case m: MissingResourceException => None }
      val preformattedText = getFromBundle(defaultBundle).getOrElse{
        // fallback to english here.
        println("unable to find translation for: " + key + " in " + name + " for locale: " + defaultBundle.getLocale)
        getFromBundle(englishBundle).getOrElse(sys.error("coding error, bad translation key: " + key + " for " + name))
      }
      java.text.MessageFormat.format(preformattedText, args: _*)
    }
    // internal use only
    def withLanguage[T](locale: Locale)(f: => T): T = {
      val oldBundle = defaultBundle
      defaultBundle = getBundle(locale)
      val v = f
      defaultBundle = oldBundle
      v
    }
    // internal use only, get all the keys for the given locale.
    // use getKeys not keySet since keySet is new in Java 6 - ST 2/11/11
    def keys(locale: Locale): Iterator[String] = {
      import collection.JavaConverters._
      getBundle(locale).getKeys.asScala
    }
    // internal use only, used to set the locale for error messages in the GUI only.
    def setLanguage(locale: Locale) {
      defaultBundle = getBundle(locale)
    }
    // for use in Java classes that we don't want to depend on I18N
    override val fn = get _
  }

  lazy val errors = new BundleKind("Errors")

  // for easy use from Java
  def errorsJ: I18NJava = errors

}
