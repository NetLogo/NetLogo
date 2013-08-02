package org.nlogo.engine

// I don't care what anyone says; `Overlord` and `Vassal` are awesome! --JAB (8/2/13)
trait Vassal {

  def id: ID

  protected def updateType: Overlord.UpdateType
  protected def companion:  VassalCompanion

  protected def registerUpdate(changePairs: (String, JSW)*): Unit = {
    val filteredPairs = changePairs filter { case (k, v) => companion.trackedKeys(k) }
    Overlord.registerUpdate(id, updateType, filteredPairs: _*)
  }

}

trait VassalCompanion {
  def trackedKeys: Set[String]
}
