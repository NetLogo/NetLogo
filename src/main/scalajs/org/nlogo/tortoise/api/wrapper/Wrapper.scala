package org.nlogo.tortoise.api.wrapper

trait Wrapper {
  // It wasn't my first inclination to make this a type variable, but it's the
  // best way to move types around quietly in implicits and such. --JAB (8/31/13)
  type ValueType
  def value: ValueType
}
