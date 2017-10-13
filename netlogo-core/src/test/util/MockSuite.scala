// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.util

import org.scalatest.{ FunSuite, Tag }
import scala.util.DynamicVariable
import org.hamcrest.{Description, BaseMatcher, Matcher}
import reflect.ClassTag

import
  org.jmock.{ AbstractExpectations, api, Expectations, integration, lib, Mockery, Sequence },
    api.Action,
    integration.junit4.JUnit4Mockery,
    lib.{ concurrent, legacy },
      concurrent.Synchroniser,
      legacy.ClassImposteriser

/**
  This is a little internal DSL to make JMock easier to use from Scala.
  http://www.jmock.org/cheat-sheet.html is a good getting-started guide.

  ScalaTest has: org.scalatest.mock.JMockCycle.scala, but I hate it.
  it forces you to do a bunch of crazy importing boilerplate all over.
  just so that it doesnt have to use a var. this version uses DynamicVariable
  instead, and removes that boilerplate. -JC 6/23/10

  Tests extending mock test will typically have tests of the following form:

  mockTest("something"){
    // create some up front objects including mocks:
    val objectUnderTest = new ObjectUnderTest
    val m = mock[SomeObjectNotUnderTest]

    // some precondtion assertions
    assert(objectUnderTest.something == somethingElse)

    // set up some expectations on the mock
    expecting {
       one(m).someCall(arg(a), ..., arg(n))
       // maybe some more calls potentially on more mock objects here.
       // but if there are too many, its a sign that your code needs refactoring.
    }

    // the expected should happen When you call a function using the object under test ...
    when{
      objectUnderTest.call(m, andMaybeSomeOtherArgs)
    }

    // then finally some postcondition assertions
    assert(objectUnderTest.something == somethingElse)
  }
 */

// We use JUnit4Mockery because with a regular Mockery I was getting uninformative
// errors like:
//    [info] - extension literal *** FAILED *** (142 milliseconds)
//    [info]   org.jmock.api.ExpectationError: unexpected invocation
// but with JUnit4Mockery we get:
//    [info] - extension literal *** FAILED *** (113 milliseconds)
//    [info]   java.lang.AssertionError: unexpected invocation: extensionManager.readExtensionObject("foo", "", "bar bazz")
//    [info] expectations:
//    [info]   expected once, never invoked: extensionManager.readExtensionObject("foo", "", "bar baz"); returns a default value
//    [info] what happened before this: nothing!
// - ST 8/16/11

trait MockSuite extends FunSuite {

  // this is the main test method provided by this trait.
  def mockTest(name: String, tags: Tag*)(f: => Unit) {
    test(name, tags: _*) {
      val mockery = new JUnit4Mockery() {
        setThreadingPolicy(new Synchroniser())
        setImposteriser(ClassImposteriser.INSTANCE)
      }
      _context.withValue(mockery) {
        _expectations.withValue(new Expectations()) {
          f
        }
      }
    }
  }

  // use this method to create a mock object
  def mock[T : ClassTag]: T = context.mock(erasure[T])

  // use this method to set up expectations on the mock objects
  def expecting(f: => Unit) {
    f
    context.checking(expectations)
  }

  // use this method to call into the objects under test
  // at the end, it makes sure that all the expectations were met.
  def when(f: => Unit) {f; context.assertIsSatisfied()}

  //
  // the following functions are used in setting up expectations
  //

  //
  // Invocation Count
  //

  // one: The invocation is expected once and once only. (we like the shorter name better - ST 6/23/10)
  def one[T] = expectations.oneOf[T] _
  // exactly(n).of: The invocation is expected exactly n times. Note: one is a convenient shorthand for exactly(1).
  def exactly(n:Int) = expectations.exactly(n)
  // atLeast(n).of: The invocation is expected at least n times.
  def atLeast(n:Int) = expectations.atLeast(n)
  // atMost(n).of: The invocation is expected at most n times.
  def atMost(n:Int) = expectations.atMost(n)
  // between(min, max).of: The invocation is expected at least min times and at most max times.
  def between(min:Int, max:Int) = expectations.between(min, max)
  //allowing: The invocation is allowed any number of times but does not have to happen.
  def allowing[T] = expectations.allowing[T] _
  //ignoring: The same as allowing. Allowing or ignoring should be chosen to make the test code clearly express intent.
  def ignoring[T] = expectations.ignoring[T] _
  //never: The invocation is not expected at all. This is used to make tests more explicit and so easier to understand.
  def never[T] = expectations.never[T] _

  //
  //  Argument Matchers
  //

  //  The most commonly used matchers are defined in the Expectations class.
  // (More matchers are defined as static factory methods of the Hamcrest Matchers class,
  //  which can be statically imported at the top of the test code.)

  //  equal(n): The argument is equal to n.
  def equal[T](t:T) = AbstractExpectations.equal(t)
  //  same(o): The argument is the same object as o.
  def same[T](t:T) = AbstractExpectations.same(t)

  // a(Class<T> type) an(Class<T> type) aNonNull(Class<T> type)
  // The argument is an instance of type or a subclass of type and not null.
  // The type argument is required to force Java to type-check the argument at compile time.
  def a[T : ClassTag]: Matcher[T] = aMatcher
  def an[T : ClassTag]: Matcher[T] = aMatcher
  def aNonNull[T : ClassTag]: Matcher[T] = aMatcher
  private def aMatcher[T: ClassTag]: Matcher[T] = new BaseMatcher[T]() {
    def describeTo(description:Description){
      description.appendText("<" + erasure[T].toString + ">")
    }
    override def matches(a: Any) = {
      if (a == null) false
      else {
        implicitly[ClassTag[T]] match {
          case ClassTag.Int => a.isInstanceOf[java.lang.Integer]
          case ClassTag.Double => a.isInstanceOf[java.lang.Double]
          case ClassTag.Boolean => a.isInstanceOf[java.lang.Boolean]
          case ClassTag.Byte => a.isInstanceOf[java.lang.Byte]
          case ClassTag.Short => a.isInstanceOf[java.lang.Short]
          case ClassTag.Long => a.isInstanceOf[java.lang.Long]
          case ClassTag.Float => a.isInstanceOf[java.lang.Float]
          case ClassTag.Char => a.isInstanceOf[java.lang.Character]
          case _ => erasure[T].isAssignableFrom(a.asInstanceOf[AnyRef].getClass)
        }
      }
    }
  }

  // aNull(Class<T> type): The argument is null.
  // The type argument is required to force Java to type-check the argument at compile time.
  def aNull[T : ClassTag]: Matcher[T] = AbstractExpectations.aNull(erasure[T])

  //  not(m): The argument does not match the Matcher m.
  def not[T](m:Matcher[T]) = arg(org.hamcrest.core.IsNot.not(m))
  def not[T](t:T) = arg(org.hamcrest.core.IsNot.not(t))
  //  anyOf(m1, m2, ..., mn): The argument matches one of the Matchers m1 to mn.
  // had to add ClassTag here only because if its not there the two methods
  // have the same signature after erasure, and wont compile. nasty hack, but it works.
  // same for allOf below. - JC 6/24/10
  def anyOf[T : ClassTag](ts:T*) = org.hamcrest.core.AnyOf.anyOf(ts.map(equal(_)):_*)
  def anyOf[T](ts:Matcher[T]*) = org.hamcrest.core.AnyOf.anyOf(ts:_*)
  //  allOf(m1, m2, ..., mn): The argument matches all of the Matchers m1 to mn.
  def allOf[T : ClassTag](ts:T*) = org.hamcrest.core.AllOf.allOf(ts.map(equal(_)):_*)
  def allOf[T](ts:Matcher[T]*) = org.hamcrest.core.AllOf.allOf(ts:_*)


  // "with" is a keyword in Scala, so call it arg instead - ST 6/23/10
  def arg[T](matcher: Matcher[T]): T = expectations.`with`(matcher)
  def arg[T](t: T): T = expectations.`with`(t)


  //
  //  Actions
  //

  def will(a:Action) = expectations.will(a)
  def returnValue[T](t:T) = AbstractExpectations.returnValue(t)
  def throwException[T <: Throwable](t:T) = AbstractExpectations.throwException(t)
  def willReturn[T](t:T) = will(returnValue(t))
  def willThrow[T <: Throwable](t:T) = will(throwException(t))
  //  will(doAll(a1, a2, ..., an)): Do all actions a1 to an on every invocation.


  //
  // Ordering
  // allows (and enforces) - one(m).x andThen one(m).y
  // (stolen from specs source code
  //  http://code.google.com/p/specs/source/browse/trunk/src/main/scala/org/specs/mock/JMocker.scala)
  //

  /** adds a constraint to the expectations to declare that an expected call must happen in sequence */
  def inSequence(sequence: Sequence) = expectations.inSequence(sequence)

  /** this class allows an expectation to declare that another expectation should follow */
  implicit class InSequenceThen(firstExpectation: =>Any) {
    val sequence = {
      val s = context.sequence("s")
      firstExpectation; inSequence(s)
      s
    }
    def andThen(otherExpectation: Any) = {
      inSequence(sequence)
      this
    }
  }


  //
  // private helpers
  //
  private val _context = new DynamicVariable[Mockery](null)
  private def context = {
    if(_context != null) _context.value
    else throw new IllegalStateException("must be inside mockTest(testname){...} to make this call.")
  }
  private val _expectations = new DynamicVariable[Expectations](null)
  private def expectations = {
    if(_expectations != null) _expectations.value
    else throw new IllegalStateException("must be inside mockTest(testname){...} to make this call.")
  }
  private def erasure[T](implicit mf: ClassTag[T]): Class[T] = mf.runtimeClass.asInstanceOf[Class[T]]
}
