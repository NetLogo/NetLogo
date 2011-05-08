package org.nlogo.api

import java.util.{MissingResourceException, Locale, ResourceBundle}

object I18N {

  case class Prefix(name:String)

  class BundleKind(name:String){

    val defaultLocale = {
      import java.util.prefs._
      val netLogoPrefs = Preferences.userRoot.node("/org/nlogo/NetLogo")
      def getPref(p:String): Option[String] = Option(netLogoPrefs.get(p, "")).filter(_.nonEmpty)
      val prefLocale = (getPref("user.language"), getPref("user.region")) match {
        case (Some(l), Some(r)) => Some(new Locale(l,r))
        case (Some(l), _) => Some(new Locale(l))
        case _ => None
      }
      prefLocale.getOrElse(Locale.getDefault)
    }

    private var defaultBundle = getBundle(defaultLocale)
    private val englishBundle = getBundle(Locale.US)
    def getBundle(locale:Locale) = {
      try ResourceBundle.getBundle(name, locale)
      catch {
        case m:MissingResourceException =>
          println("language unsupported: " + Locale.getDefault + ", falling back to English")
          ResourceBundle.getBundle(name, Locale.US)
      }
    }
    def apply(key:String)(implicit prefix: Prefix) = get(prefix.name + "." + key)
    def get(key:String) = getN(key)
    def getNJava(key:String, args:Array[String]) = getN(key, args:_*)
    def getN(key:String, args: AnyRef*) = {
      def getFromBundle(bundle: ResourceBundle): Option[String] =
        try Some(bundle.getString(key)) catch { case m:MissingResourceException => None }
      val preformattedText = getFromBundle(defaultBundle).getOrElse{
        // fallback to english here.
        println("unable to find translation for: " + key + " in " + name + " for locale: " + defaultBundle.getLocale)
        getFromBundle(englishBundle).getOrElse(error("coding error, bad translation key: " + key + " for " + name))
      }
      //println(preformattedText)
      //println(args.mkString(","))
      java.text.MessageFormat.format(preformattedText, args:_*)
    }
    // internal use only
    def withLanguage[T](locale:Locale)(f: => T): T = {
      val oldBundle = defaultBundle
      defaultBundle = getBundle(locale)
      val v = f
      defaultBundle = oldBundle
      v
    }
    // internal use only, get all the keys for the given locale.
    // use getKeys not keySet since keySet is new in Java 6 - ST 2/11/11
    def keys(locale:Locale) =
      org.nlogo.util.JCL.toScalaIterable(getBundle(locale).getKeys).toSet
    // internal use only, used to set the locale for error messages in the GUI only.
    def setLanguage(locale:Locale) { defaultBundle = getBundle(locale) }
    // for use in Java classes that we don't want to depend on I18N
    def fn = get _
  }

  object Gui extends BundleKind("GUI_Strings")
  def gui = Gui // so java can call this easily.

  object Errors extends BundleKind("Errors")
  def errors = Errors // so java can call this easily.

  object Prims extends BundleKind("Primitives")
  def prims = Prims // so java can call this easily.
}

//val allBundles = DateFormat.getAvailableLocales.map(getBundle).toSet
