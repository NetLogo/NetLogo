// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise
package dock

class TestStrings extends DockingSuite {

  tester("word 0") {
    defineProcedures("")
    compare("(word)")
  }

  tester("word 1") {
    defineProcedures("")
    compare("(word 1)")
  }

  tester("word") {
    defineProcedures("")
    compare("(word 1 2 3)") // 123, and hopefully not, god forbid, 6
  }

}
