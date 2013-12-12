// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.review

import org.nlogo.mirror.ModelRun
import org.nlogo.mirror.AgentKey
import org.nlogo.mirror.WidgetKinds
import org.nlogo.mirror.Kind

trait MirroredWidget {
  val run: ModelRun
  val index: Int
  val kind: Kind
  lazy val agentKey = AgentKey(kind, index)

  def mirroredVar[T](variableId: Int): Option[T] =
    for {
      frame <- run.currentFrame
      variables <- frame.mirroredState.get(agentKey)
    } yield variables(variableId).asInstanceOf[T]
}