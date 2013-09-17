package org.nlogo.tortoise.engine

import
  scala.js.annotation.JSName

import
  org.nlogo.tortoise.adt.{ NumberJS, ObjectJS }

@JSName("Random")
object RandomJS extends ObjectJS {
  def nextDouble():              NumberJS = ???
  def nextInt(limit: NumberJS):  NumberJS = ???
  def nextLong(limit: NumberJS): NumberJS = ???
}

@JSName("StrictMath")
object StrictMathJS extends ObjectJS {
  def abs      (n: NumberJS): NumberJS = ???
  def sin      (n: NumberJS): NumberJS = ???
  def cos      (n: NumberJS): NumberJS = ???
  def round    (n: NumberJS): NumberJS = ???
  def toRadians(n: NumberJS): NumberJS = ???
}

