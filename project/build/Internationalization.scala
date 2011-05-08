import sbt._


/**
  The language of your JVM/OS will be used by default. If that default language isn't supported,
  NetLogo falls back to en_US. If devs in other countries prefer to work in English instead of
  their default language, they can just run the english task. Or, they can call the set-lang task
  which takes two arguments, the language and the country.
  Currently we only only support en_ES (Spanish) anyway, but that will likely change soon.
 */
trait Internationalization { this: DefaultProject =>

  lazy val setLang = task { args =>
    if (args.length == 2) langSetter(args(0), args(1))
    else task {Some("Usage: set-lang <language> <country>")}
  }

  lazy val english = langSetter("en", "US")
  lazy val spanish = langSetter("es", "ES")

  def langSetter(language: String, country: String) = task {
    println("setting lang to (" + language + "," + country + ")")
    System.setProperty("user.language",language)
    System.setProperty("user.country", country)
    None
  }
}
