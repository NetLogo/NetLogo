// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import scala.util.Try

class DummyLogoThunkFactory extends LogoThunkFactory {
  def makeReporterThunk(code: String, ownerName: String): ReporterLogoThunk = {
    return new ReporterLogoThunk {
      def call: Try[AnyRef] = {
        throw new UnsupportedOperationException
      }
    }
  }

  def makeCommandThunk(code: String, jobOwnerName: String): CommandLogoThunk = {
    return new CommandLogoThunk {
      def call: Try[Boolean] = {
        throw new UnsupportedOperationException
      }
    }
  }
}
