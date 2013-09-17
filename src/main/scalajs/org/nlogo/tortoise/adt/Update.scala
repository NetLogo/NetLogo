package org.nlogo.tortoise.adt

trait Update {
  type MapT
  def turtles: MapT
  def patches: MapT
  def toDictionary: Dictionary
}

trait UpdateCompanion {
  type UpdateT <: Update
  def apply(turtles: UpdateT#MapT, patches: UpdateT#MapT): UpdateT
}

class UpdateJS(override val turtles: Dictionary, override val patches: Dictionary) extends Update {
  override type MapT = Dictionary
  override def toDictionary =  Dictionary("turtles" -> turtles, "patches" -> patches)
}

object UpdateJS extends UpdateCompanion {
  override type UpdateT = UpdateJS
  override def apply(turtles: UpdateT#MapT = Dictionary(), patches: UpdateT#MapT = Dictionary()) = new UpdateJS(turtles, patches)
}
