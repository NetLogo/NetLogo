// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

/**
 * This class's purpose is to catch one specific inefficient pattern of bytecode that the inliner
 * generates -- the needless GOTO [next instruction].  When it finds this pattern:
 *  GOTO labelA
 *  (labelB)     // optional extra label
 *  labelA
 * it removes the GOTO. ~Forrest (6/19/2006), ST 2/25/10
 */

import org.objectweb.asm.Opcodes.GOTO
import org.objectweb.asm.{ Label, MethodVisitor }

class PeepholeOptimizer1(mv: MethodVisitor) extends AbstractPeepholeOptimizer(mv) {

  private var goto: Option[Label] = None
  private var extra: Option[Label] = None

  // when match fails, flush saved pieces of the pattern before moving on
  override def restartMatch() {
    goto.foreach(mv.visitJumpInsn(GOTO, _))
    extra.foreach(mv.visitLabel(_))
    goto = None
    extra = None
  }

  override def visitJumpInsn(opcode: Int, label: Label) {
    if (goto.isDefined)
      restartMatch()
    opcode match {
      case GOTO =>
        goto = Some(label)
      case _ =>
        mv.visitJumpInsn(opcode, label)
    }
  }

  override def visitLabel(label: Label) {
    (goto, extra) match {
      case (Some(`label`), _) =>
        // omit the goto, keep the label
        goto = None
        restartMatch()
        mv.visitLabel(label)
      case (Some(_), None) =>
        extra = Some(label)
      case _ =>
        restartMatch()
        mv.visitLabel(label)
    }
  }

}
