// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless

class TestModels extends TestLanguage(
  TxtsInDir("models/test")
    .filterNot(_.getName.startsWith("HubNet"))
    .filterNot(_.getName.startsWith("SDM"))
    .filterNot(_.getName.startsWith("Artificial-Neural-Net")))
