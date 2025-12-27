import sbt.{ Def, InputKey }

import scala.util.Try

object CheckTranslations {
  private val checkTranslations = InputKey[Try[Unit]]("checkTranslations", "Check for missing translation keys")

  val settings = {
    Def.setting(checkTranslations, Running.makeMainTask("org.nlogo.core.CheckTranslations"))
  }
}
