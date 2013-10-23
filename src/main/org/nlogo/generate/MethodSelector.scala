// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import org.nlogo.nvm.{ Instruction, Reporter }
import java.lang.reflect.Method

object MethodSelector {
  // the Long is the total cost of using the method, including the costs of all its arguments
  type Result = List[(Method, Long)]
  def select(i: Instruction, returnType: Class[_], profilingEnabled: Boolean): Option[Method] =
    if (!BytecodeUtils.isRejiggered(i)) None
    else {
      val result = cheapestOption(returnType, evaluate(i, profilingEnabled)).map(_._1)
      result.foreach(m => i.chosenMethod = m)
      result
    }
  def evaluate(i: Instruction, profilingEnabled: Boolean): Result = {
    val ms = methods(i, profilingEnabled).sortBy(_.getReturnType.toString)
    if (ms.isEmpty) List((BytecodeUtils.getUnrejiggeredMethod(i), 0))
    else {
      val argResults = i.args.toList.map(evaluate(_, profilingEnabled))
      val pairs = for (m <- ms; cost <- totalCostOption(m, argResults))
        yield (m, cost)
      group(pairs)(_._1.getReturnType).map(_.minBy(_._2))
    }
  }
  private def cheapest(p1: (Method, Long), p2: (Method, Long)) =
    if (p2._2 < p1._2) p2 else p1
  private def methods(i: Instruction, profilingEnabled: Boolean): List[Method] =
    BytecodeUtils.getMethods(i.getClass, profilingEnabled)
      // the "+ 1" is because rejiggered methods take a Context argument
      .filter(_.getParameterTypes.size == i.args.size + 1)
  // returns None if the method is inapplicable, or a Some(Long), the lower the better
  private def totalCostOption(m: Method, args: List[Result]): Option[Long] = {
    val costOptions =
      for ((typeTo, arg) <- m.getParameterTypes.toList.tail zip args) // tail = skip Context
        yield cheapestOption(typeTo, arg).map(_._2)
    if (!costOptions.forall(_.isDefined)) None
    else Some(costOptions.flatten.sum)
  }
  private def cheapestOption(typeTo: Class[_], arg: Result): Option[(Method, Long)] = {
    val results = for ((method, cost1) <- arg; cost2 <- conversionCost(method.getReturnType, typeTo))
      yield (method, cost1 + cost2)
    if (results.isEmpty) None
    else Some(results.reduceLeft(cheapest))
  }
  // non-private for unit testing
  def conversionCost(typeFrom: Class[_], typeTo: Class[_]): Option[Long] =
    if (typeTo == typeFrom || typeTo == classOf[Reporter]) Some(0)
    else if (isUnboxingConversion(typeFrom, typeTo)) Some(100)
    else if (typeTo.isAssignableFrom(typeFrom)) Some(10000 * countInheritanceLevels(typeFrom, typeTo))
    else if (isBoxingConversion(typeFrom, typeTo)) Some(1000000)
    else if (isConversionPossible(typeFrom, typeTo)) Some(100000000L)
    else None
  private def isBoxingConversion(typeFrom: Class[_], typeTo: Class[_]) =
    (typeFrom, typeTo) == ((java.lang.Boolean.TYPE, classOf[java.lang.Boolean])) ||
      (typeFrom, typeTo) == ((java.lang.Boolean.TYPE, classOf[Object])) ||
      (typeFrom, typeTo) == ((java.lang.Double.TYPE, classOf[java.lang.Double])) ||
      (typeFrom, typeTo) == ((java.lang.Double.TYPE, classOf[Object]))
  private def isUnboxingConversion(typeFrom: Class[_], typeTo: Class[_]) =
    (typeFrom, typeTo) == ((classOf[java.lang.Double], java.lang.Double.TYPE)) ||
      (typeFrom, typeTo) == ((classOf[java.lang.Boolean], java.lang.Boolean.TYPE))
  private def isConversionPossible(typeFrom: Class[_], typeTo: Class[_]) =
    typeFrom.isAssignableFrom(typeTo) ||
      isBoxingConversion(typeTo, typeFrom) ||
      isUnboxingConversion(typeTo, typeFrom)
  private def countInheritanceLevels(child: Class[_], ancestor: Class[_]): Int =
    if (child == ancestor) 0
    else 1 + countInheritanceLevels(child.getSuperclass, ancestor)
  // like group in Haskell, but with a key function. without reordering, break into sublists where
  // all the elements in each sublist are equal according to the key function.
  // example: group(List((1,9),(1,8),(2,7),(3,6),(3,5)))(_._1)
  //          => List(List((1,9),(1,8)),List((2,7)),List((3,6),(3,5)))
  private def group[T, U](xs: List[T])(fn: T => U): List[List[T]] = {
    def equivalent(t1: T, t2: T) = fn(t1) == fn(t2)
    xs match {
      case Nil => Nil
      case _ =>
        val (first, rest) = xs.span(equivalent(_, xs.head))
        first :: group(rest)(fn)
    }
  }
}
