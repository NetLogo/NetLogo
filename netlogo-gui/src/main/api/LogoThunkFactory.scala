// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait LogoThunkFactory {
  @throws(classOf[CompilerException])
  def makeReporterThunk(code: String, jobOwnerName: String): ReporterLogoThunk
  @throws(classOf[CompilerException])
  def makeCommandThunk(code: String, jobOwnerName: String): CommandLogoThunk
}
