package org.nlogo.tortoise.engine

// I don't care what anyone says; `Overlord` and `Vassal` are awesome! --JAB (8/2/13)
trait Vassal {

  def id: ID

  protected def updateType: Overlord.UpdateType
  protected def companion:  VassalCompanion

  protected def registerUpdate(changePairs: (String, JSW)*): Unit = {
//    val filteredPairs = changePairs filter { case (k, v) => companion.trackedKeys(k) } //@ Make this work quickly (probably delegate into `Overlord`, doing it all at once) --JAB (9/11/13)
    Overlord.registerUpdate(id, updateType, changePairs: _*)
  }

}

trait VassalCompanion {
  def trackedKeys: Set[String]
}
