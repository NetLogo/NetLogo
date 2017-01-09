// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import
  org.nlogo.core.Let

import
  org.scalacheck.{ Arbitrary, Gen }, Arbitrary._

import
  org.scalatest.{ FunSuite, prop },
    prop.GeneratorDrivenPropertyChecks

class BindingTests extends FunSuite with GeneratorDrivenPropertyChecks {

  trait BindingTestHelper {
    val abc = new Let("abc")
    var binding = new Binding()
  }

  test("Setting a let without defining it raises an IllegalStateException") { new BindingTestHelper {
    intercept[IllegalStateException] { binding.setLet(abc, Double.box(0)) }
  } }

  test("getLet raises NoSuchElementException if the let hasn't been defined") { new BindingTestHelper {
    intercept[NoSuchElementException] { binding.getLet(abc) }
  } }

  test("setLet can be used on a let after definition") { new BindingTestHelper {
    binding.let(abc, Double.box(0))
    binding.setLet(abc, Double.box(1))
  } }

  test("getLet retrieves the set value of the let") { new BindingTestHelper {
    binding.let(abc, Double.box(0))
    assert(binding.getLet(abc) == Double.box(0))
  } }

  test("getLet retrieves the value of the let set by setLet") { new BindingTestHelper {
    binding.let(abc, Double.box(0))
    binding.setLet(abc, Double.box(1))
    assert(binding.getLet(abc) == Double.box(1))
  } }

  test("enter scope leaves previously defined let variables accessible") { new BindingTestHelper {
    binding.let(abc, Double.box(0))
    binding = binding.enterScope()
    assert(binding.getLet(abc) == Double.box(0))
  } }

  test("exit scope leaves variables defined in the scope inaccessible") { new BindingTestHelper {
    binding = binding.enterScope()
    binding.let(abc, Double.box(0))
    binding = binding.exitScope()
    intercept[NoSuchElementException] { binding.getLet(abc) }
    intercept[IllegalStateException] { binding.setLet(abc, Double.box(1)) }
  } }

  test("exit scope doesn't clear lets defined before the scope was entered") { new BindingTestHelper {
    binding.let(abc, Double.box(0))
    binding = binding.enterScope()
    binding = binding.exitScope()
    assert(binding.getLet(abc) == Double.box(0))
  } }

  test("lets defined before the scope was entered can be modified") { new BindingTestHelper {
    binding.let(abc, Double.box(0))
    binding = binding.enterScope()
    binding = binding.exitScope()
    binding.setLet(abc, Double.box(1))
    assert(binding.getLet(abc) == Double.box(1))
  } }

  test("lets can be modified from inner contexts") { new BindingTestHelper {
    binding.let(abc, Double.box(0))
    binding = binding.enterScope()
    binding.setLet(abc, Double.box(1))
    binding = binding.exitScope()
    assert(binding.getLet(abc) == Double.box(1))
  } }

  test("adds creates a fresh binding on a second let") { new BindingTestHelper {
    binding.let(abc, Double.box(0))
    binding.let(abc, Double.box(1))
    assert(binding.getLet(abc) == Double.box(1))
    assert(binding.head.next eq Binding.EmptyBinding)
  } }

  test("allValues returns a list of all Lets and values") { new BindingTestHelper {
    val xyz = Let("xyz")
    binding.let(abc, Double.box(0))
    binding.enterScope()
    binding.let(xyz, Double.box(1))
    assertResult(List(xyz -> Double.box(1), abc -> Double.box(0)))(binding.allLets)
  } }
}
