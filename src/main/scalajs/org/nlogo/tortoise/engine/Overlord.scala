package org.nlogo.tortoise.engine

import
  scala.collection.mutable.ArrayBuffer

object Overlord {

  private var updates = ArrayBuffer[Update]()

  def flushUpdates(): Unit =
    updates = ArrayBuffer[Update]()

  def collectUpdates(): Array[Update] = {
    val collected =
      if (updates.isEmpty)
        ArrayBuffer(Update())
      else
        updates
    flushUpdates()
    collected.result().toArray
  }

  // gross hack - ST 1/25/13
  private[engine] def registerDeath(id: ID): Unit =
    updates += Update(Map(id -> Map("WHO" -> -1)))

  private[engine] def registerUpdate(id: ID, updateType: UpdateType, changePairs: (String, JSW)*): Unit = {

    val agentChange = changePairs.foldLeft(Map[String, AnyJS]()) { case (acc, (varName, value)) => acc + (VarNameMapper(varName) -> value.toJS) }

    val fullChange = Map(id -> agentChange)

    import UpdateType._
    val update = updateType match {
      case TurtleType => Update(fullChange, Map())
      case PatchType  => Update(Map(), fullChange)
    }

    updates += update

  }

  sealed trait UpdateType
  object UpdateType {
    case object TurtleType extends UpdateType
    case object PatchType extends UpdateType
  }

}

case class Update(turtleUpdates: AgentUpdate = Map(), patchUpdates: AgentUpdate = Map()) {
  def mapTurtles[T](f: (AgentUpdate) => AgentUpdate): Update = this.copy(turtleUpdates = f(turtleUpdates))
  def mapPatches[T](f: (AgentUpdate) => AgentUpdate): Update = this.copy(patchUpdates = f(patchUpdates))
}
