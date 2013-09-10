package org.nlogo.tortoise.api.wrapper

trait Wrapper {
  // It wasn't my first inclination to make this a type variable, but it's the
  // best way to move types around quietly in implicits and such. --JAB (8/31/13)
  type ValueType
  def value: ValueType // Unfortunately, limitations of my `expose` annotation in ScalaJS prevent me from exposing this.  --JAB (9/9/13)
}
