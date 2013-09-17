package org.nlogo.tortoise.adt

import
  VarMap.{ Key, Value }

// A fake-y linked dictionary
// It's abhorrent but the best way to get these objects into the `Dictionary`
// seems to be to wrap them in thunks and turn them into JS functions --JAB (9/14/13)
class VarMap private (array: ArrayJS[(Key, Value)], dictionary: Dictionary) {

  // Carn sarn this lossy dictionary! --JAB (9/13/13)
  def apply(key: Key): Value        = unravel(key)
  def apply(n: Int):   (Key, Value) = array(n)

  def +=(pair: (Key, Value)): Unit = {
    val (key, value) = pair
    this(key) = value
  }

  def ++=(pairs: ArrayJS[(Key, Value)]): Unit = {
    pairs.E foreach { case (key, value) => this += key -> value }
  }

  def -=(key: Key): Unit = this -= this.indexOfKey(key)
  def -=(n: Int):   Unit = {
    val key = array(n)._1
    array.splice(n, 1)
    ravel(key -> ()) // It would be nice to have access to the `delete` keyword; also, this will probably bite me in the ass. --JAB (7/13/13)
  }

  def pairs: ArrayJS[(Key, Value)] = array

  def size: Int = NumberJS.toDouble(array.length).toInt

  def update(key: Key, value: Value): Unit = {
    if (unravel(key) != ((): JSW))
      array.push(key -> value)
    ravel(key -> value)
  }

  // Not great.  --JAB (9/13/13)
  def exists(key: Key): Boolean = this.indexOfKey(key) != -1

  private def indexOfKey(key: Key): Int = {
    var i = 0
    array.E foreach {
      case (k, _) =>
        if (k == key)
          return i
        else
          i += 1
    }
    -1
  }

  private def unravel(key: Key): Value = {
    val lookup = dictionary(key)
    if (lookup == UndefinedJS)
      ()
    else
      lookup.asInstanceOf[ThunkJS[Value]]()
  }

  private def ravel(pair: (Key, Value)): Unit  = {
    val (key, value) = pair
    dictionary(key) = () => value
  }

}

object VarMap {

  private type Key   = String
  private type Value = JSW

  def apply(pairs: ArrayJS[(Key, Value)]): VarMap = {
    val dict = Dictionary(AnyJS.toArray(pairs.E map { case (x, y) => (x, () => y)}): _*)
    new VarMap(pairs, dict)
  }

}
