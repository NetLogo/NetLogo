// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait LogoThunkFactory {
  def makeReporterThunk(code: String, jobOwnerName: String): ReporterLogoThunk
  def makeCommandThunk(code: String, jobOwnerName: String): CommandLogoThunk
}
