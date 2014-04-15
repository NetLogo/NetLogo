// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api.model

import org.scalatest.FunSuite

class ModelTest extends FunSuite {

  test("Parse a button") {
  /*
    println(ButtonReader.format(new Button("B1", 0, 0, 0, 0, "", false)))
    println(ButtonReader.validate("""|BUTTON
                                  |B1
                                  |0
                                  |1
                                  |2
                                  |3
                                  |Some Code
                                  |1""".stripMargin.split("\n").toList))
    println(ButtonReader.validate("""|BUTTON
                                  |B1
                                  |0
                                  |1
                                  |asdf
                                  |3
                                  |Some Code
                                  |1""".stripMargin.split("\n").toList))
    println(ButtonReader.parse("""|BUTTON
                                  |B1
                                  |0
                                  |1
                                  |2
                                  |3
                                  |Some Code
                                  |1""".stripMargin.split("\n").toList))*/
  }

  /*
  test("empty source for commands should do nothing, most importantly - not explode") {
  }

  test("empty source for commands shouldnt depend on context at all") {
    //e.withContext(null){ e.makeCommandThunk("", null, null).call() }
  }*/
}
