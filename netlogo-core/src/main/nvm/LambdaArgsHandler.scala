// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.nvm.{ Instruction => NvmInstruction }
import org.nlogo.core.Let

object LambdaArgsHandler {

  def createInstruction(arguments: LambdaArgs, instruction: NvmInstruction): LambdaArgsHandler.Instruction = {
    if (arguments.isVariadic) {
      return new ConciseVariadic(instruction)
    } else {
      return new Static
    }
  }

  def createCommand(arguments: LambdaArgs, procedure: Procedure): LambdaArgsHandler.Instruction = {
    // At the moment the only way an anonymous command can be variadic is by being concise.  So we're
    // Safe to assume `procedure.code(0)` is the actual, single command that'll be run.
    // -Jeremy B December 2021
    if (arguments.isVariadic) {
      return new ConciseVariadic(procedure.code(0))
    } else {
      return new Static
    }
  }

  sealed trait Instruction {
    def updateRuntimeArgs(formals: Array[Let], args: Array[AnyRef]): Array[Let]
  }

  case class Static() extends LambdaArgsHandler.Instruction {
    def updateRuntimeArgs(formals: Array[Let], args: Array[AnyRef]): Array[Let] = formals
  }

  case class ConciseVariadic(instruction: NvmInstruction) extends LambdaArgsHandler.Instruction {
    def updateRuntimeArgs(formals: Array[Let], args: Array[AnyRef]): Array[Let] = {
      if (formals.length < args.length) {
        val runtimeFormals = (0 until args.length).map( (i) => new Let(s"__$i|concise")).toArray
        // We make a quasi-letvariable here instead of just making a `prim._letvariable` instance to avoid
        // circular deps between the nvm and prim packages.
        // -Jeremy B January 2022
        instruction.args = runtimeFormals.map( (l) => new Reporter {
          override def toString(): String = s"${super.toString}(${l.name})"
          override def report(context: Context): AnyRef = {
            context.activation.binding.getLet(l)
          }
        })
        runtimeFormals
      } else {
        formals
      }
    }
  }

}
