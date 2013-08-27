// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.parse

import org.nlogo.api.{ CompilerException, Token }

object Fail {

  // "desc" is by-name so we don't retrieve error messages from i18n bundles unless the error is
  // actually triggered, thereby avoiding spurious warnings for missing errors on non-English
  // locales (issue #218). I'd like "token" and "node" to be by-name too, but then the two overloads
  // for cAssert don't compile because they're same after erasure.  It's fixable (e.g. by choosing
  // different names for the two methods), but choosing to leave it for now. - ST 10/4/12

  // "assert" is in Predef, so...
  def cAssert(condition: Boolean, desc: =>String, token: Token) {
    if(!condition)
      exception(desc, token)
  }

  def exception(message: String, start: Int, end: Int, filename: String) =
    throw new CompilerException(message, start, end, filename)
  def exception(message: String, token: Token) =
    throw new CompilerException(message, token.start, token.end, token.filename)

}
