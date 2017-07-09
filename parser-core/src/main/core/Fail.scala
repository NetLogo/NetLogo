// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object Fail {

  // "desc" is by-name so we don't retrieve error messages from i18n bundles unless the error is
  // actually triggered, thereby avoiding spurious warnings for missing errors on non-English
  // locales (issue #218). I'd like "token" and "node" to be by-name too, but then the two overloads
  // for cAssert don't compile because they're same after erasure.  It's fixable (e.g. by choosing
  // different names for the two methods), but choosing to leave it for now. - ST 10/4/12

  // "assert" is in Predef, so...
  def cAssert(condition: Boolean, desc: =>String, locatable: SourceLocatable) {
    if(!condition)
      exception(desc, locatable)
  }
  def cAssert(condition: Boolean, desc: =>String, location: SourceLocation) {
    if(!condition)
      exception(desc, location)
  }
  def exception(message: String, locatable: SourceLocatable): Nothing =
    exception(message, locatable.sourceLocation)
  def exception(message: String, location: SourceLocation): Nothing =
    exception(message, location.start, location.end, location.filename)
  def exception(message: String, start: Int, end: Int, filename: String): Nothing =
    throw new CompilerException(message, start, end, filename)

}
