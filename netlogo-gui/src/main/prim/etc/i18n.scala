// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import java.util.Locale
import org.nlogo.core.I18N
import org.nlogo.nvm.{ Command, Context }

class _seterrorlocale extends Command {

  override def perform(context: Context): Unit = {
    I18N.errors.setLanguage(new Locale(argEvalString(context, 0), argEvalString(context, 1)))
    context.ip = next
  }
}

class _spanish extends Command {

  override def perform(context: Context): Unit = {
    I18N.errors.setLanguage(new Locale("es", "MX"))
    context.ip = next
  }
}
class _english extends org.nlogo.nvm.Command {

  override def perform(context: Context): Unit = {
    I18N.errors.setLanguage(new Locale("en", "US"))
    context.ip = next
  }
}
