// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api
import scala.math.floor

object LabDefaultThreads {
 val RATIO = 0.75
 // Determines the number of threads in BehaviorSpace if the user has not specified a value
 def getLabDefaultThreads :Int  = { floor(Runtime.getRuntime.availableProcessors * RATIO).toInt }
}