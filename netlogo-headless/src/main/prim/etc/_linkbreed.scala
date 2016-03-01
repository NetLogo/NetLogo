// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.agent.AgentSet
import org.nlogo.nvm.{ Context, Reporter }

class _linkbreed(breedName: String) extends Reporter {
  override def toString =
    super.toString + ":" + breedName
  override def report(context: Context): AgentSet =
    report_1(context)
  def report_1(context: Context): AgentSet =
    world.getLinkBreed(breedName)
  def getBreedName = breedName
}
