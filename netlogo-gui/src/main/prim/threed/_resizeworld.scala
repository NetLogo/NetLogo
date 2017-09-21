// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.threed

import org.nlogo.api.WorldResizer
import org.nlogo.agent.World3D
import org.nlogo.nvm.{ Command, Context, RuntimePrimitiveException }

class _resizeworld extends Command {

  switches = true



  override def perform(context: Context) {

    val newMinX = argEvalIntValue(context, 0)
    val newMaxX = argEvalIntValue(context, 1)
    val newMinY = argEvalIntValue(context, 2)
    val newMaxY = argEvalIntValue(context, 3)
    val newMinZ = argEvalIntValue(context, 4)
    val newMaxZ = argEvalIntValue(context, 5)

    val oldMinX = workspace.world.minPxcor
    val oldMaxX = workspace.world.maxPxcor
    val oldMinY = workspace.world.minPycor
    val oldMaxY = workspace.world.maxPycor
    val oldMinZ = workspace.world.asInstanceOf[World3D].minPzcor
    val oldMaxZ = workspace.world.asInstanceOf[World3D].maxPzcor

    if (newMinX > 0 || newMaxX < 0 || newMinY > 0 || newMaxY < 0 || newMinZ > 0 || newMaxZ < 0)
      throw new RuntimePrimitiveException(
        context, this,
        "You must include the point (0, 0, 0) in the world.")
    if (oldMinX != newMinX || oldMaxX != newMaxX ||
        oldMinY != newMinY || oldMaxY != newMaxY ||
        oldMinZ != newMinZ || oldMaxZ != newMaxZ) {
      val dimensions =
        new org.nlogo.api.WorldDimensions3D(newMinX, newMaxX,
                                            newMinY, newMaxY,
                                            newMinZ, newMaxZ,
                                            world.patchSize);
      workspace.waitFor(
        new org.nlogo.api.CommandRunnable {
          override def run(): Unit = {
            workspace.setDimensions(dimensions, true, WorldResizer.StopNonObserverJobs)
          }
        })
    }
    context.ip = next
  }
}
