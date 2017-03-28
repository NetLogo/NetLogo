// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.internalapi

import java.util.concurrent.atomic.AtomicReference

sealed trait ModelAction

case class UpdateInterfaceGlobal(name: String, value: AtomicReference[_ <: AnyRef]) extends ModelAction
case class AddProcedureRun(widgetTag: String, isForever: Boolean) extends ModelAction
// this may need to contain data on a suspended run, a procedure and forever flag may be insufficient
case class StopProcedure(jobTag: String) extends ModelAction

case class TaggedAction(action: ModelAction, tag: String)

sealed trait ModelUpdate

case class ActionCompleted(tag: String) extends ModelUpdate
case class JobDone(jobTag: String) extends ModelUpdate // done could mean halted, stopped, or completed
case class JobErrored(jobTag: String, error: Exception) extends ModelUpdate
