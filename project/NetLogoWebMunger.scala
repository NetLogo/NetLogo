import sbt._

object NetLogoWebMunger {
  def apply(): Unit = {
    // Add in <meta> tag to set charset as utf-8
    // Find the <script type="text/nlogo"> tag and put a `<NetLogoModel />` tag inside
    //  --- this makes it easier for the code to simply search and replace
    //  --- raise error if there is no text/nlogo element
    //  --- probably use JSoup, scala's xml parser falls over when parsing a NetLogo Web export
    //  --- replace data-filename="whatever" with data-filename="<NetLogoModelName />"
    // Inject custom error javascript for referring people to netlogoweb.org if their model
    //   will not compile
    // save file into target location
  }
}
