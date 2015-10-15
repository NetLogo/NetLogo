// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.generate

import
  org.objectweb.asm.{ Label, MethodVisitor, Opcodes },
    Opcodes._

/**
 * This class serves as a peep-hole optimizer.  Its purpose is just to catch one specific
 * inefficient pattern of bytecode that the inliner generates -- the needless STORE / LOAD
 * combination.
 *
 * IMPORTANT: This class is tricky, because the inefficient pattern we want to replace is not
 * always safe to replace.
 *
 * Even if DLOAD is called right after DSTORE, there may be another DLOAD later in the method body.
 * The only way we can safely use this peep-hole optimizer is if we could guarantee that every
 * argument to a report_* method was only referenced once in the bytecode of the method body.
 * PeepholeSafeChecker checks for this.  If a report_* or perform_* method is "PeepholeSafe", then a
 * special label (PEEPHOLE_SAFE_FLAG) is inserted into the bytecode by the InstructionGenerator,
 * which this class looks for, to make sure it is safe to perform the transformation.
 *
 *   ~Forrest (6/19/2006)
 *
 * It's looking for patterns like this:
 *    DSTORE 4
 *  (L-special) // PEEPHOLE_SAFE_FLAG label
 *   L2
 *    LINENUMBER 2 L2
 *  (L3)  // extra junk label
 *    DLOAD 4
 * And replacing them with just:
 *   L2
 *    LINENUMBER 2 L2
 *  (L3)
 *  ~Forrest (6/19/2006)
 */
object PeepholeOptimizer3 {
  val PEEPHOLE_FLAG_LABEL = new Label
}
class PeepholeOptimizer3(mv: MethodVisitor) extends AbstractPeepholeOptimizer(mv) {

  /* 0 = looking for pattern to start
   * 1 = found *STORE B  (opcode stored in A)
   * 2 = found special Label PEEPHOLE_FLAG_LABEL
   * 3 = found Label C
   * 4 = found LineNumber D
   * 5 = found Label F  (junk label...)
   * finished = found *LOAD B  -- (opcode stored in E)
   */
  private var state = 0

  private var storeOpcodeA = 0
  private var localVarB = 0
  private var labelC: Label = null
  private var lineNumberD = 0
  private var labelE: Label = null

  // When match fails, flush saved pieces of pattern before moving on
  def restartMatch() {
    state match {
      case 0 => // do nothing, because nothing to flush
      case 1 => mv.visitVarInsn(storeOpcodeA, localVarB)
      case 2 =>
        mv.visitVarInsn(storeOpcodeA, localVarB)
        mv.visitLabel(PeepholeOptimizer3.PEEPHOLE_FLAG_LABEL)
      case 3 =>
        mv.visitVarInsn(storeOpcodeA, localVarB)
        mv.visitLabel(PeepholeOptimizer3.PEEPHOLE_FLAG_LABEL)
        mv.visitLabel(labelC)
      case 4 =>
        mv.visitVarInsn(storeOpcodeA, localVarB)
        mv.visitLabel(PeepholeOptimizer3.PEEPHOLE_FLAG_LABEL)
        mv.visitLabel(labelC)
        mv.visitLineNumber(lineNumberD, labelC)
      case 5 =>
        mv.visitVarInsn(storeOpcodeA, localVarB)
        mv.visitLabel(PeepholeOptimizer3.PEEPHOLE_FLAG_LABEL)
        mv.visitLabel(labelC)
        mv.visitLineNumber(lineNumberD, labelC)
        mv.visitLabel(labelE)
    }
    state = 0
  }

  private def matchSucceeded() {
    mv.visitLabel(PeepholeOptimizer3.PEEPHOLE_FLAG_LABEL)
    mv.visitLabel(labelC)
    mv.visitLineNumber(lineNumberD, labelC)
    mv.visitLabel(labelE)
    state = 0
  }
  private def storeLoadOpcodesMatch(storeOpcode: Int, loadOpcode: Int): Boolean =
    (storeOpcode, loadOpcode) match {
      case (ISTORE, ILOAD) => true
      case (DSTORE, DLOAD) => true
      case (LSTORE, LLOAD) => true
      case (FSTORE, FLOAD) => true
      case (ASTORE, ALOAD) => true
      case _ => false
    }
  override def visitVarInsn(opcode: Int, variable: Int) {
    if (state == 0)
      if (List(ISTORE, DSTORE, LSTORE, FSTORE, ASTORE).contains(opcode)) {
        storeOpcodeA = opcode
        localVarB = variable
        state += 1
      } else mv.visitVarInsn(opcode, variable)
    else if (state == 5 && variable == localVarB && storeLoadOpcodesMatch(storeOpcodeA, opcode))
      matchSucceeded()
    else {
      restartMatch()
      // recurse, so we don't miss the start of a new pattern
      visitVarInsn(opcode, variable)
    }
  }
  override def visitLabel(label: Label) {
    (state, label) match {
      case (1, PeepholeOptimizer3.PEEPHOLE_FLAG_LABEL) => state += 1
      case (2, _) => labelC = label; state += 1
      case (4, _) => labelE = label; state += 1
      case _ =>
        restartMatch()
        mv.visitLabel(label)
    }
  }
  override def visitLineNumber(line: Int, start: Label) {
    if (state == 3 && start == labelC) {
      lineNumberD = line
      state += 1
    } else {
      restartMatch()
      mv.visitLineNumber(line, start)
    }
  }
}

/**
 * This class serves as a peep-hole optimizer.
 * Its purpose is just to clean up the PEEPHOLE_FLAG_LABELs
 * that PeepholeOptimizer3 leaves behind.
 * PeepholeOptimizer3 must ALWAYS be used in conjunction with this
 * class, because otherwise we'll end up placing the PEEPHOLE_FLAG_LABEL
 * in multiple places in the method body, which causes a verifier error.
 *
 * It's looking for patterns like this:
 *    Label PEEPHOLE_FLAG_LABEL
 * And replacing them with:
 *   (nothing)
 *
 *  ~Forrest (6/19/2006)
 */

class PeepholeOptimizer3B(mv: MethodVisitor) extends MethodVisitor(ASM5, mv) {
  override def visitLabel(label: Label) {
    if (label != PeepholeOptimizer3.PEEPHOLE_FLAG_LABEL)
      mv.visitLabel(label)
  }
}
