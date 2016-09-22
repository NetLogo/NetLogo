// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.{ ClosedVariable, Let, Token, prim => coreprim }

class LiftedLambda(
  isReporter:          Boolean,
  nameToken:           Token,
  name:                String,
  _displayName:        Option[String],
  override val parent: Procedure,
  argTokens:           Seq[Token]     = Seq(),
  initialArgs:         Vector[String] = Vector[String](),
  override val lambdaFormals:   Array[Let]     = Array(),
  val closedLets:      Set[Let]       = Set()) extends Procedure(
    isReporter, name, nameToken, argTokens, _displayName, null, null, lambdaFormals) {

    override def isLambda = true

    override def getLambdaFormal(name: String): Option[Let] =
      lambdaFormals.find(_.name == name) orElse (parent match {
        case parentLambda: LiftedLambda => parentLambda.getLambdaFormal(name)
        case _ => None
      })

    override protected def buildDisplayName(displayName: Option[String]): String = {
      def topParent(p: Procedure): Procedure =
        p match {
          case ll: LiftedLambda => topParent(ll.parent)
          case _ => p
        }

      val sourceCode = code.map(_.fullSource).filterNot(_ == null).mkString("[", " ", "]")
      "(anonymous command from: " + topParent(parent).displayName + ": " + sourceCode + ")"
    }


  override def dump: String = {
    val buf = new StringBuilder
    var displayArgs = args.mkString("[", " ", "]")
    val titleMargin = if (isReporter) "   reporter " else "   "
    buf ++= s"$titleMargin$displayName:${parent.displayName}${displayArgs}{$agentClassString}:\n"
    for (i <- code.indices) {
      buf ++= s"   [$i]${code(i).dump(6)}\n"
    }
    for (p <- children) {
      buf ++= "\n"
      buf ++= p.dump
    }
    buf.toString
  }
}
