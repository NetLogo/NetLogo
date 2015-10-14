// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.scalatest.FunSuite

class AutoConverter1Tests extends FunSuite {
  def test(version:String,before:String,after:String) { test(version,before,after,true) }

  def test2DAnd3d(version:String,before:String,after:String) {
    test(version,before,after,true)
    test("3D " + version,before,after,true)
  }

  def test(version:String,before:String,after:String,subprogram:Boolean) {
    assertResult(after) {
      new AutoConverter1()(Compiler.Tokenizer2D)
        .convert(before, subprogram, false,"NetLogo " + version)
    }
  }
  test("stamp1") { test("1.0", "stamp red", "set pcolor red") }
  test("stamp2") { test("1.0", "STAMP red", "set pcolor red") }
  test("oldScreenPrimitives") {
    test("1.0", "print screen-edge-x", "print max-pxcor")
    test("1.0", "print screen-edge-y", "print max-pycor")
    test("1.0", "print screen-size-x", "print world-width")
    test("1.0", "print screen-size-y", "print world-height")
  }
  test("wildCardPlus") { test("1.0", "print random (a + b)", "print random-or-random-float (a + b)") }
  test("CCT1") { test("3.1", "create-custom-turtles 10 [ fd 1 ]", "crt 10 [ fd 1 ]") }
  test("CCT2") { test("3.1", "CREATE-CUSTOM-TURTLES 10 [ fd 1 ]", "crt 10 [ fd 1 ]") }
  test("CCT3") { test("3.1", "create-custom-frogs 10 [ fd 1 ]", "create-frogs 10 [ fd 1 ]") }
  test("CCT4") { test("3.1", "CREATE-CUSTOM-FROGS 10 [ fd 1 ]", "create-FROGS 10 [ fd 1 ]") }
  test("CCT5") { test("3.1", "cct-frogs 10 [ fd 1 ]", "create-frogs 10 [ fd 1 ]") }
  test("CCT6") { test("3.1", "CCT-FROGS 10 [ fd 1 ]", "create-FROGS 10 [ fd 1 ]") }
  test("locals0") { test("1.0", "to foo locals [] end", "to foo  end", false) }
  test("locals1") { test("1.0", "to foo locals [a] print a end", "to foo let a 0\n   print a end", false) }
  test("locals2") { test("1.0", "to foo locals [a b] print word a b end", "to foo let a 0\n  let b 0\n   print word a b end", false) }
  test("locals3") { test("1.0", "to foo locals [] end to bar locals [] end", "to foo  end to bar  end", false) }
  test("dashOf1") { test("3.1", "print color-of turtle 5", "print [color] of turtle 5") }
  test("dashOf2") {
    test("3.0","print (shade-of? pcolor-of patch-ahead 1 color)","print (shade-of? [pcolor] of patch-ahead 1 color)")
  }
  test("dashOf3") { // test case based on user model. I couldn't make it any shorter and still trigger the bug. - ST 11/24/08
    val before =
      ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\n" +
      "to high\n" +
      " print (shade-of? pcolor-of patch-ahead 1 color)\n" +
      "end\n" +
      "to low\n" +
      " print (shade-of? pcolor-of patch-ahead 1 color)\n" +
      "end"
    val after =
      ";;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;\n" +
      "to high\n" +
      " print (shade-of? [pcolor] of patch-ahead 1 color)\n" +
      "end\n" +
      "to low\n" +
      " print (shade-of? [pcolor] of patch-ahead 1 color)\n" +
      "end"
    test("3.0",before,after,false)
  }
  test("setup-plots") {
    test2DAnd3d("4.1", "to setup-plots end", "to my-setup-plots end")
    test2DAnd3d("4.1", "to update-plots end", "to my-update-plots end")
    test2DAnd3d("4.1", "to go setup-plots end", "to go my-setup-plots end")
    test2DAnd3d("4.1", "to go update-plots end", "to go my-update-plots end")
    test2DAnd3d("4.1", "to setup-plots setup-plots end", "to my-setup-plots my-setup-plots end")
    test2DAnd3d("4.1", "to update-plots update-plots end", "to my-update-plots my-update-plots end")
  }

  test("clear-all") {
    test2DAnd3d("4.2pre4", "to setup clear-all end", "to setup " + AutoConverter1.clearAllAndResetTicks + " end")
    test2DAnd3d("4.2pre4", "to setup ca end", "to setup " + AutoConverter1.clearAllAndResetTicks + " end")
  }
}
