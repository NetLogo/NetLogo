// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

class DummyLogoThunkFactory extends LogoThunkFactory {
  def makeReporterThunk(code: String, ownerName: String): ReporterLogoThunk = {
    return new ReporterLogoThunk {
      def call: Object = {
        throw new UnsupportedOperationException
      }
    }
  }

  def makeCommandThunk(code: String, jobOwnerName: String): CommandLogoThunk = {
    return new CommandLogoThunk {
      def call: Boolean = {
        throw new UnsupportedOperationException
      }
    }
  }
}
