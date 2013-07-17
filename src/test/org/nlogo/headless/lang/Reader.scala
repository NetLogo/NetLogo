// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

import org.scalatest.Tag
import java.io.File

// We parse the tests first, then run them.
// Parsing is separate so we can write tests for the parser itself.
trait Reader { this: Finder =>
  def tests = Parser.parseFiles(files)
  // parse tests first, then run them
  for(t <- tests if shouldRun(t))
    test(t.fullName, new Tag(t.suiteName){}, new Tag(t.fullName){}) {
      Runner(t)
    }
  // on the core branch the _3D tests are gone, but extensions tests still have them since we
  // didn't branch the extensions, so we still need to filter those out - ST 1/13/12
  def shouldRun(t: LanguageTest) =
    !t.testName.endsWith("_3D") && {
      import org.nlogo.api.Version.useGenerator
      if (t.testName.startsWith("Generator"))
        useGenerator
      else if (t.testName.startsWith("NoGenerator"))
        !useGenerator
      else true
    }

}
