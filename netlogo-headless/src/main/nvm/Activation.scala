// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.{ Activation => ApiActivation }

object Activation {
  private val NoArgs = Array[AnyRef]()
}

class Activation(
  val procedure: Procedure,
  _parent: Activation,
  private[nlogo] val args: Array[AnyRef],
  val returnAddress: Int) extends ApiActivation {

  def this(procedure: Procedure, parent: Activation, returnAddress: Int) = {
    this(procedure, parent, {
      val size = procedure.size
      if (size > 0)
        new Array[AnyRef](size)
      else
        Activation.NoArgs
    }, returnAddress)
  }


  def parent: Option[Activation] =
    Option(_parent)

  def setUpArgsForRunOrRunresult() {
    // if there's a reason we copy instead of using the original, I don't remember it - ST 2/6/11
    System.arraycopy(_parent.args, 0, args, 0, _parent.procedure.args.size)
  }

  override def toString =
    super.toString + ":" + procedure.name + "(" + args.size + " args" +
      ", return address = " + returnAddress + ")\n" +
      args.zipWithIndex
        .map{case (a, i) => "  arg " + i + " = " + a}
        .mkString("\n")

  def nonLambdaActivation: Activation = {
    if (procedure.isLambda)
      parent.map(_.nonLambdaActivation).getOrElse(this)
    else
      this
  }
}
