// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

import org.nlogo.api.{ NetLogoLegacyDialect, NetLogoThreeDDialect }
import org.nlogo.util.AnyFunSuiteEx

class TestAllTokens extends AnyFunSuiteEx {
  val tokenMappers = Seq(
    NetLogoLegacyDialect.tokenMapper,
    NetLogoThreeDDialect.tokenMapper)
  tokenMappers.foreach { tokenMapper =>
    test("all listed primitives exist for " + tokenMapper.getClass.getSimpleName) {
      tokenMapper.allCommandNames.foreach(n => assert(tokenMapper.getCommand(n).nonEmpty))
      tokenMapper.allReporterNames.foreach(n => assert(tokenMapper.getReporter(n).nonEmpty))
    }
  }
}
