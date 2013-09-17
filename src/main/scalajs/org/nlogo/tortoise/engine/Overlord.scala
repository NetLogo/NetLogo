package org.nlogo.tortoise.engine

import
  org.nlogo.tortoise.adt.{ ArrayJS, Dictionary, JSW, NumberJS, UpdateJS }

// Abstractionize to reference `UpdateJS` --JAB (9/11/13)
// Eww... all these references to `Dictionary`.... --JAB (9/11/13)
// This code is used so heavily that writing it as idiomatic Scala isn't acceptable; it cripples performance --JAB (9/11/13)
object Overlord {

  private var updates = ArrayJS[UpdateJS]()

  def flushUpdates(): Unit =
    updates = ArrayJS[UpdateJS]()

  def collectUpdates(): ArrayJS[UpdateJS] = {
    val collected =
      if (NumberJS.toDouble(updates.length) == 0)
        ArrayJS(UpdateJS())
      else
        updates
    flushUpdates()
    collected
  }

  // gross hack - ST 1/25/13
  private[engine] def registerDeath(id: ID): Unit =
    updates.push(UpdateJS(Dictionary(id.value.toString -> Dictionary("WHO" -> -1))))

  private[engine] def registerUpdate(id: ID, updateType: UpdateType, changePairs: ArrayJS[(String, JSW)]): Unit = {

    val agentChange = {
      val dict = Dictionary()
      changePairs.E foreach {
        case (varName, value) =>
          val key = VarNameMapper(varName)
          dict(key) = value.toJS
      }
      dict
    }

    val fullChange = Dictionary(id.value.toString -> agentChange)

    import UpdateType._
    val update = updateType match {
      case TurtleType => UpdateJS(fullChange,   Dictionary())
      case PatchType  => UpdateJS(Dictionary(), fullChange)
    }

    updates.push(update)

  }

  sealed trait UpdateType
  object UpdateType {
    case object TurtleType extends UpdateType
    case object PatchType  extends UpdateType
  }

}

