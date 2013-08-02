package org.nlogo.engine

import scala.js.{ Any => AnyJS, Array => ArrayJS, Dictionary }

object Overlord {

  private type Change       = Map[String, AnyJS]
  private type AgentUpdate  = Map[ID, Change]
  private type UpdateBundle = Array[Update]

  var updates: UpdateBundle = Array()

  case class Update(turtleUpdates: AgentUpdate = Map(), patchUpdates: AgentUpdate = Map()) {
    def mapTurtles[T](f: (AgentUpdate) => AgentUpdate): Update = this.copy(turtleUpdates = f(turtleUpdates))
    def mapPatches[T](f: (AgentUpdate) => AgentUpdate): Update = this.copy(patchUpdates = f(patchUpdates))
  }

  // Contrived typeclass to test the excellence of ScalaJS --JAB (7/19/13)
  private trait Dictionaryable {
    def toDictionary: Dictionary
  }

  private implicit def update2Dictionary(update: Update) = new Dictionaryable {
    override def toDictionary: Dictionary = {
      def changeToDict(change: Change)      = Dictionary(change.toSeq: _*)
      def updateToDict(update: AgentUpdate) = Dictionary(update map { case (k, v) => (k.value.toString, changeToDict(v)) } toSeq: _*)
      val turtlesEntry = "turtles" -> updateToDict(update.turtleUpdates)
      val patchesEntry = "patches" -> updateToDict(update.patchUpdates)
      Dictionary(turtlesEntry, patchesEntry)
    }
  }

  def flushUpdates(): Unit =
    updates = Array()

  def collectUpdates(): ArrayJS[Dictionary] = {
    val collected =
      if (updates.isEmpty)
        Array(Update())
      else
        updates
    flushUpdates()

//    println(AnyJS.fromString("collected: " + collected.map {
//      thing =>
//        def convertIt(updates: AgentUpdate) = updates.map { case (id, change) => id.value.toString -> change.mkString }.mkString
//        val patches = convertIt(thing.patchUpdates)
//        val turtles = convertIt(thing.turtleUpdates)
//        "patches: " + patches + "\nturtles: " + turtles
//    }.mkString("\n")))

    AnyJS.fromArray(collected map (_.toDictionary))
  }

  // gross hack - ST 1/25/13
  def registerDeath(id: ID): Unit =
    updates = updates :+ Update(Map(id -> Map("WHO" -> -1)))

  def registerUpdate(id: ID, updateType: UpdateType, changePairs: (String, JSW)*): Unit = {

    // Hardcoding these strings is bad.  --JAB (8/2/13)
    val agentChange =
      changePairs.foldLeft(Map[String, AnyJS]()) {
        case (acc, (varName, value)) =>
          val key = varName match {
            case "plabelcolor" => "PLABEL-COLOR"
            case "labelcolor"  => "LABEL-COLOR"
            case "pensize"     => "PEN-SIZE"
            case "penmode"     => "PEN-MODE"
            case "hidden"      => "HIDDEN?"
            case "id"          => "WHO"
            case _             => varName.toUpperCase
          }
          acc + (key -> value.toJS)
      }

    val fullChange = Map(id -> agentChange)

    import UpdateType._
    val update = updateType match {
      case TurtleType => Update(fullChange, Map())
      case PatchType  => Update(Map(), fullChange)
    }

    updates = updates :+ update

  }

  sealed trait UpdateType
  object UpdateType {
    case object TurtleType extends UpdateType
    case object PatchType extends UpdateType
  }

}
