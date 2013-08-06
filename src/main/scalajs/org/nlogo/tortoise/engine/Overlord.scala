package org.nlogo.tortoise.engine

import scala.js.Dictionary

object Overlord {

  private type Change      = Map[String, AnyJS]
  private type AgentUpdate = Map[ID, Change]

  private var updates = Array[Update]()

  def flushUpdates(): Unit =
    updates = Array()

  def collectUpdates(): ArrayJS[Dictionary] = {
    val collected =
      if (updates.isEmpty)
        Array(Update())
      else
        updates
    flushUpdates()
    AnyJS.fromArray(collected map updateToDictionary)
  }

  // gross hack - ST 1/25/13
  private[engine] def registerDeath(id: ID): Unit =
    updates = updates :+ Update(Map(id -> Map("WHO" -> -1)))

  private[engine] def registerUpdate(id: ID, updateType: UpdateType, changePairs: (String, JSW)*): Unit = {

    val agentChange = changePairs.foldLeft(Map[String, AnyJS]()) { case (acc, (varName, value)) => acc + (VarNameMapper(varName) -> value.toJS) }

    val fullChange = Map(id -> agentChange)

    import UpdateType._
    val update = updateType match {
      case TurtleType => Update(fullChange, Map())
      case PatchType  => Update(Map(), fullChange)
    }

    updates = updates :+ update

  }

  private def updateToDictionary(update: Update): Dictionary = {
    def changeToDict(change: Change)      = Dictionary(change.toSeq: _*)
    def updateToDict(update: AgentUpdate) = Dictionary(update map { case (k, v) => (k.value.toString, changeToDict(v)) } toSeq: _*)
    val turtlesEntry = "turtles" -> updateToDict(update.turtleUpdates)
    val patchesEntry = "patches" -> updateToDict(update.patchUpdates)
    Dictionary(turtlesEntry, patchesEntry)
  }

  private case class Update(turtleUpdates: AgentUpdate = Map(), patchUpdates: AgentUpdate = Map()) {
    def mapTurtles[T](f: (AgentUpdate) => AgentUpdate): Update = this.copy(turtleUpdates = f(turtleUpdates))
    def mapPatches[T](f: (AgentUpdate) => AgentUpdate): Update = this.copy(patchUpdates = f(patchUpdates))
  }

  sealed trait UpdateType
  object UpdateType {
    case object TurtleType extends UpdateType
    case object PatchType extends UpdateType
  }

}
