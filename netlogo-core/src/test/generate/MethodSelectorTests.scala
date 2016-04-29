// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.scalatest.FunSuite
import java.lang.reflect.Method
import org.nlogo.agent.{ AgentSet, Agent, Turtle }
import org.nlogo.prim._
import org.nlogo.core.LogoList
import org.nlogo.nvm.Reporter

class MethodSelectorTests extends FunSuite {
  import MethodSelector._
  /// helpers
  def cost(c1: Class[_], c2: Class[_]) =
    conversionCost(c1, c2).get
  def dump(rs: Result) =
    rs.map{case (method, cost) =>
            (method.getReturnType.getSimpleName, cost)}
      .mkString
  def dump(m: Method) = {
    val parms =
      m.getParameterTypes.toList.tail
        .map(_.getSimpleName)
        .mkString("", ",", " => ")
    parms + m.getReturnType.getSimpleName
  }
  // janky that we need actual prims at runtime to run the tests, but oh well - ST 5/4/13
  def instantiate[T](name: String) = {
    import scala.language.existentials
    val klass =
      try Class.forName("org.nlogo.prim.etc." + name)
      catch {
        case e: ClassNotFoundException =>
          Class.forName("org.nlogo.prim." + name)
      }
      klass.newInstance
      .asInstanceOf[T]
  }
  // make sure our cost metric prefers specific types.  there is no actual conversion cost, but
  // presumably the method that knows to handle the specific type will be more efficient than the
  // method for the general type.
  test("cost: inheritance") {
     assert(cost(classOf[java.lang.Double], classOf[Object]) >
            cost(classOf[java.lang.Double], classOf[java.lang.Double]))
     assert(cost(classOf[Turtle], classOf[Object]) >
            cost(classOf[Turtle], classOf[Agent]))
     assert(cost(classOf[Agent], classOf[Object]) >
            cost(classOf[Agent], classOf[Agent]))
  }
  // converting a Double or a Boolean to an object is a no-op, while unboxing it actually takes a
  // non-zero amount of work.  nonetheless, we prefer to unbox because if we have a method that
  // expects a primitive type, it's probably the most efficient method, and is probably so by a
  // wider margin then the unboxing cost.
  test("cost: unboxing better than widening") {
    assert(cost(classOf[java.lang.Double], classOf[Object]) >
           cost(classOf[java.lang.Double], java.lang.Double.TYPE))
    assert(cost(classOf[java.lang.Boolean], classOf[Object]) >
           cost(classOf[java.lang.Boolean], java.lang.Boolean.TYPE))
  }
  // however we don't want to unbox if a method exists that knows how to handle the boxed
  // value of that type
  test("cost: don't unbox if method exists wanting boxed") {
    assert(cost(classOf[java.lang.Double], java.lang.Double.TYPE) >
           cost(classOf[java.lang.Double], classOf[java.lang.Double]))
  }
  // ok, enough cost tests, now test actual method evaluation.  first some trivial ones
  test("const string") {
    assertResult("(String,0)")(
      dump(evaluate(new _conststring(""), false)))
  }
  test("const double") {
    assertResult("(Double,0)(double,0)")(
      dump(evaluate(new _constdouble(0.0), false)))
  }
  test("const list") {
    assertResult("(LogoList,0)")(
      dump(evaluate(new _constlist(LogoList.Empty), false)))
  }
  // _lessthan's report methods are as follows:
  // boolean report_1(Object, Object)
  // boolean report_2(String, String)
  // boolean report_3(double, double)
  // boolean report_4(Turtle, Turtle)
  // boolean report_5(Patch, Patch)
  // boolean report_6(Link, Link)
  // boolean report_7(double, Object)
  // boolean report_8(Object, double)
  test("less than 1") {
    val root = new _lessthan
    root.args = Array(new _conststring(""), new _conststring(""))
    assertResult("(boolean,0)")(dump(evaluate(root, false)))
    assertResult("String,String => boolean")(
      dump(select(root, java.lang.Boolean.TYPE, false).get))
  }
  test("less than 2") {
    val root = new _lessthan
    root.args = Array(new _observervariable(0), new _conststring(""))
    assertResult("(boolean,10000)")(dump(evaluate(root, false)))
    assertResult("Object,Object => boolean")(
      dump(select(root, java.lang.Boolean.TYPE, false).get))
  }
  test("less than 3") {
    val root = new _lessthan
    root.args = Array(new _observervariable(0), new _constdouble(0.0))
    assertResult("(boolean,0)")(dump(evaluate(root,false)))
    assertResult("Object,double => boolean")(
      dump(select(root, java.lang.Boolean.TYPE, false).get))
  }
  test("less than 4") {
    val root = new _lessthan
    root.args = Array(new _constdouble(0.0), new _observervariable(0))
    assertResult("(boolean,0)")(dump(evaluate(root, false)))
    assertResult("double,Object => boolean")(
      dump(select(root, java.lang.Boolean.TYPE, false).get))
  }
  test("less than 5") {
    val root = new _lessthan
    root.args = Array(new _constdouble(0.0), new _constdouble(0.0))
    assertResult("(boolean,0)")(dump(evaluate(root, false)))
    assertResult("double,double => boolean")(
      dump(select(root, java.lang.Boolean.TYPE, false).get))
  }
  // check handling of "sum" on input known to be a list
  test("sum list 1") {
    val root = new _sum
    root.args = Array(new _constlist(LogoList.Empty))
    assertResult("(double,0)")(dump(evaluate(root, false)))
    assertResult("LogoList => double")(
      dump(select(root, java.lang.Double.TYPE, false).get))
  }
  test("sum list 2") {
    val root = new _sum
    root.args = Array(new _sentence)
    assertResult("(double,0)")(dump(evaluate(root, false)))
    assertResult("LogoList => double")(
      dump(select(root, java.lang.Double.TYPE, false).get))
  }
  // check handling of "length" on unrejiggered input known to be a list
  test("length list 1") {
    val root = instantiate[Reporter]("_length")
    root.args = Array(instantiate[Reporter]("_filter"))
    assertResult("(double,0)")(dump(evaluate(root, false)))
    assertResult("LogoList => double")(
      dump(select(root, java.lang.Double.TYPE, false).get))
  }
  // check on handling of reporter blocks (which show up as Reporter arguments
  // in the args array)
  test("with 1") {
    val root = new _with
    root.args = Array(new _turtles, new _constboolean(true))
    assertResult("(AgentSet,0)")(dump(evaluate(root, false)))
    assertResult("AgentSet,Reporter => AgentSet")(
      dump(select(root, classOf[AgentSet], false).get))
  }
  // make sure we handle unrejiggered primitives with equanimity
  test("unrejiggered command") {
    val root = new _sprout
    root.args = Array(new _constdouble(1.0))
    assertResult("(void,0)")(dump(evaluate(root, false)))
  }
  // at the moment, _patch is unrejiggered. these tests will need to be changed if we ever rejigger
  // it.  at the moment it is unrejiggered because the generator doesn't know what to do with the
  // AgentException try/catch in the body - ST 2/6/09
  test("unrejiggered reporter at root") {
    val root = instantiate[Reporter]("_patch")
    root.args = Array(new _constdouble(0.0), new _constdouble(0.0))
    assertResult("(Object,0)")(dump(evaluate(root, false)))
  }
  test("unrejiggered reporter inside") {
    val root = new _equal
    root.args = Array(instantiate[Reporter]("_patch"), instantiate[Reporter]("_patch"))
    root.args(0).args = Array(new _constdouble(0.0), new _constdouble(0.0))
    root.args(1).args = Array(new _constdouble(0.0), new _constdouble(0.0))
    assertResult("(boolean,0)")(dump(evaluate(root, false)))
    assertResult("Object,Object => boolean")(
      dump(select(root, java.lang.Boolean.TYPE, false).get))
  }
}
