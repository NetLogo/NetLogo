package org.nlogo.tortoise

import
  scala.reflect.ClassTag

package object adt {

  type AnyJS       = scala.js.Any
  type ArrayJS[T]  = scala.js.Array[T]
  type BooleanJS   = scala.js.Boolean
  type Dictionary  = scala.js.Dictionary
  type DynamicJS   = scala.js.Dynamic
  type NumberJS    = scala.js.Number
  type ObjectJS    = scala.js.Object
  type StringJS    = scala.js.String
  type ThunkJS[T]  = scala.js.Function0[T]
  type UndefinedJS = scala.js.Undefined

  val AnyJS       = scala.js.Any
  val ArrayJS     = scala.js.Array
  val BooleanJS   = scala.js.Boolean
  val Dictionary  = scala.js.Dictionary
  val DynamicJS   = scala.js.Dynamic
  val NumberJS    = scala.js.Number
  val ObjectJS    = scala.js.Object
  val StringJS    = scala.js.String
  val UndefinedJS = AnyJS.fromUnit(())

  implicit def thunk2JS[R](thunk: () => R): ThunkJS[R] = AnyJS.fromFunction0(thunk)

  type JSW = JSWrapper

  implicit class EnhancedArray[T : ClassTag](arr: ArrayJS[T]) {
    def E: EnhancedArray[T] = this
    def head:                      T          = arr(0)
    def tail:                      Array[T]   = arr.slice(1, arr.length - 1)
    def isEmpty:                   Boolean    = this.length == 0
    def length:                    Int        = NumberJS.toDouble(arr.length).toInt
    def map[U](f: (T) => U):       ArrayJS[U] = arr.map[U] ((      x: T, i: NumberJS, arr: ArrayJS[T]) => f(x))
    def filter(f: (T) => Boolean): ArrayJS[T] = arr.filter ((      x: T, i: NumberJS, arr: ArrayJS[T]) => (f andThen AnyJS.fromBoolean _)(x))
    def foreach[U](f: (T) => U):   Unit       = arr.forEach((      x: T, i: NumberJS, arr: ArrayJS[T]) => f(x))
    def foldLeft[U](z: U)(f: (U, T) => U): U  = arr.reduce ((u: U, x: T, i: NumberJS, arr: ArrayJS[T]) => f(u, x), z)
  }

}
