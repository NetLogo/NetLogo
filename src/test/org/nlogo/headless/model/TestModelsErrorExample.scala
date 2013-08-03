// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package model

import org.nlogo.api.ModelCreator._

class TestModelsErrorExample extends AbstractTestModels {
  testModel("error", Model("to-report zero report 0 end")) {
    testError(observer>>"print 1 / zero", "Division by zero.")
  }
}
