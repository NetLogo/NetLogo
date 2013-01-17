// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mirror

import scala.collection.mutable
// no dependencies except Scala standard library

abstract class Kind
case class AgentKey(kind: Kind, id: Long)
case class Birth(agent: AgentKey, values: Seq[AnyRef])
case class Death(agent: AgentKey)
case class Change(variable: Int, value: AnyRef)
case class Update(deaths: Seq[Death] = Seq(),
                  births: Seq[Birth] = Seq(),
                  changes: Seq[(AgentKey, Seq[Change])] = Seq())

trait Mirrorable {
  def agentKey: AgentKey
  def kind: Kind
  val variables: Map[Int, AnyRef]
  def nbVariables = variables.size
  def getVariable(index: Int) = variables(index)
}

object Mirroring {

  type State = Map[AgentKey, Seq[AnyRef]]

  private def valueDiffs(was: Seq[AnyRef], now: Seq[AnyRef]): Seq[Change] =
    for (i <- was.indices if was(i) != now(i))
      yield Change(i, now(i))

  def diffs(oldState: State, mirrorables: TraversableOnce[Mirrorable]): (State, Update) = {
    val births: mutable.Buffer[Birth] = mutable.ArrayBuffer()
    val deaths: mutable.Buffer[Death] = mutable.ArrayBuffer()
    var changes: Map[AgentKey, Seq[Change]] = Map()
    var newState: State = oldState
    var seen: Set[AgentKey] = Set()
    for (obj <- mirrorables) {
      val key = obj.agentKey
      seen += key
      val vars = (0 until obj.nbVariables).map(obj.getVariable)
      if (oldState.contains(key)) {
        val vd = valueDiffs(was = oldState(key), now = vars)
        if (vd.nonEmpty) {
          changes += key -> vd
          newState += key -> vars
        }
      } else {
        births += Birth(key, vars)
        newState += key -> vars
      }
    }
    for (key <- oldState.keys)
      if (!seen.contains(key)) {
        deaths += Death(key)
        newState -= key
      }
    (newState, Update(deaths.toVector, births.toVector, changes.toSeq))
  }

  def merge(oldState: State, update: Update): State = {
    var newState: State = oldState
    for (Death(agent) <- update.deaths)
      newState -= agent
    for (Birth(agent, values) <- update.births)
      newState += agent -> values
    for ((agent, changes) <- update.changes)
      newState += agent -> mergeValues(oldState(agent), changes)
    newState
  }

  private def mergeValues(oldValues: Seq[AnyRef], changes: Seq[Change]): Seq[AnyRef] = {
    // Important: make sure newValues is a *copy* of oldValues.
    // (Using oldValues.toArray instead of Array(oldValues: _*),
    // for example, would return a reference to it and the process
    // would mess up the old values.) NP 2013-01-08.
    val newValues = Array(oldValues: _*)
    for (Change(variable, value) <- changes)
      newValues(variable) = value
    newValues.toSeq
  }

}
