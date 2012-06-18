// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

object Activation {
  private val NoArgs = Array[AnyRef]()
}

class Activation(val procedure: Procedure, val parent: Activation, val returnAddress: Int) {

  // "var" so ReporterTask can swap in the definition-site args - ST 2/5/11
  var args: Array[AnyRef] = {
    val size = procedure.size
    if (size > 0)
      new Array[AnyRef](size)
    else
      Activation.NoArgs
  }

  def setUpArgsForRunOrRunresult() {
    // if there's a reason we copy instead of using the original, I don't remember it - ST 2/6/11
    System.arraycopy(parent.args, 0, args, 0, parent.procedure.args.size)
  }

  override def toString =
    super.toString + ":" + procedure.name + "(" + args.size + " args" +
      ", return address = " + returnAddress + ")\n" +
      args.zipWithIndex
        .map{case (a, i) => "  arg " + i + " = " + a}
        .mkString("\n")

}
