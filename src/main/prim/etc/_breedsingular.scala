// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.core.{ Syntax, SyntaxJ }
import org.nlogo.api.Nobody
import org.nlogo.nvm.{ Reporter, Context, EngineException }

class _breedsingular(_breedName: String) extends Reporter {

  override def syntax =
    SyntaxJ.reporterSyntax(
      Array(Syntax.NumberType),
      Syntax.TurtleType | Syntax.NobodyType)

  override def toString =
    super.toString + ":" + _breedName

  override def report(context: Context): AnyRef =
    report_1(context, argEvalDoubleValue(context, 0))

  def report_1(context: Context, idDouble: Double): AnyRef = {
    val id = validLong(idDouble)
    if (id != idDouble)
      throw new EngineException(
        context, this, idDouble + " is not an integer")
    val turtle = world.getTurtle(id)
    if (turtle == null)
      Nobody
    else {
      val breed = world.getBreed(_breedName)
      if (!breed.contains(turtle))
        throw new EngineException(
          context, this,
          s"$turtle is not a ${world.getBreedSingular(breed)}")
      turtle
    }
  }

  // MethodRipper won't let us call a public method from perform_1() - FD 10/10/13
  def breedName: String = _breedName
}
