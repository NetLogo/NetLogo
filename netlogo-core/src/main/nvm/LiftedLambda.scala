// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.core.{ ClosedVariable, Let, StructureDeclarations, Token, prim => coreprim }

final class LiftedLambda(
  name:          String,
  nameToken:     Token,
  val parent:        Procedure,
  val lambdaFormals: Seq[Let],
  val closedLets:    Set[Let]
  ) extends Procedure(false, name, nameToken, Seq.empty[Token], null) {

    val lambdaFormalsArray: Array[Let] = lambdaFormals.toArray[Let]

    override val isLambda = true

    def getLambdaFormal(name: String): Option[Let] =
      lambdaFormals.find(_.name == name) orElse (parent match {
        case parentLambda: LiftedLambda => parentLambda.getLambdaFormal(name)
        case _ => None
      })

    override lazy val displayName = {
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
