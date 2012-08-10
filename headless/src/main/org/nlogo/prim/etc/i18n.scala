// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.api.{ I18N, Syntax }
import org.nlogo.nvm.{ Command, Context }
import java.util.Locale

class _seterrorlocale extends Command {
  override def syntax =
    Syntax.commandSyntax(Array(Syntax.StringType, Syntax.StringType), "O---", false)
  override def perform(context: Context) {
    I18N.errors.setLanguage(new Locale(argEvalString(context, 0), argEvalString(context, 1)))
    context.ip = next
  }
}

class _spanish extends Command {
  override def syntax = Syntax.commandSyntax(Array(), "O---", false)
  override def perform(context: Context) {
    I18N.errors.setLanguage(new Locale("es", "MX"))
    context.ip = next
  }
}
class _english extends org.nlogo.nvm.Command {
  override def syntax = Syntax.commandSyntax(Array(), "O---", false)
  override def perform(context: Context) {
    I18N.errors.setLanguage(new Locale("en", "US"))
    context.ip = next
  }
}

class _changelanguage extends org.nlogo.nvm.Command {
  override def syntax = Syntax.commandSyntax(Array(), "O---", false)
  override def perform(context: Context) {
    workspace.changeLanguage()
    context.ip = next
  }
}

