// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import org.scalatest.FunSuite

class MersenneTwisterFastTests extends FunSuite {
  // Note: I got the expected values in the following tests by actually running our actual code, not
  // by comparing to some other reference implementation or anything like that.  So this is just
  // regression testing, not actual correctness testing.  - ST 6/14/04
  test("integer repeatability") {
    val generator = new MersenneTwisterFast(78645) // just some number I made up
    assert(35398 === generator.nextInt(100000))
    assert(43344 === generator.nextInt(100000))
    assert(62618 === generator.nextInt(100000))
    // now make sure the same seed gives us the same numbers again
    generator.setSeed(78645)
    assert(35398 === generator.nextInt(100000))
    assert(43344 === generator.nextInt(100000))
    assert(62618 === generator.nextInt(100000))
  }
  test("double repeatability") {
    val generator = new MersenneTwisterFast(91773) // just some number I made up
    // when you use assert with doubles you have to supply a delta... here we use 0 because
    // we're using strict math so we should get precisely the right number
    assert(0.25234274343179397 === generator.nextDouble(),0)
    assert(0.5371597936195429 ===  generator.nextDouble(),0)
    assert(0.5418160939231073 ===  generator.nextDouble(),0)
    // now make sure the same seed gives us the same numbers again
    generator.setSeed(91773)
    assert(0.25234274343179397 === generator.nextDouble(),0)
    assert(0.5371597936195429 ===  generator.nextDouble(),0)
    assert(0.5418160939231073 ===  generator.nextDouble(),0)
  }
  test("save and load") {
    val generator = new MersenneTwisterFast(39291) // just some number I made up
    // first generate a bunch of numbers that we ignore
    for(_ <- 0 until 1000) generator.nextInt()
    // then save the state of the generator
    val s = generator.save
    // then generate a bunch of numbers and remember them
    def vals = (0 until 1000).map(_ => generator.nextInt).toList
    val oldVals = vals
    // restore the state, make sure we can generate the same numbers as before
    generator.load(s)
    assert(oldVals === vals)
  }
}
