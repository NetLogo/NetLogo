// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.nvm.{ Instruction => NvmInstruction }
import org.nlogo.prim._letvariable
import org.nlogo.core.Let
import org.nlogo.core.prim.Lambda

object LambdaArgsHandler {

  def createInstruction(arguments: Lambda.Arguments, instruction: NvmInstruction): LambdaArgsHandler.Instruction = {
    if (arguments.isVariadic) {
      return new ConciseVariadic(instruction)
    } else {
      return new Static
    }
  }

  def createCommand(arguments: Lambda.Arguments, procedure: Procedure): LambdaArgsHandler.Instruction = {
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
    def updateArgs(formals: Array[Let]): Unit = {}
  }

  case class ConciseVariadic(instruction: NvmInstruction) extends LambdaArgsHandler.Instruction {
    def updateRuntimeArgs(formals: Array[Let], args: Array[AnyRef]): Array[Let] = {
      if (formals.length < args.length) {
        val runtimeFormals = (0 until args.length).map( (i) => new Let(s"__$i|concise")).toArray
        instruction.args = runtimeFormals.map( (l) => new _letvariable(l) )
        runtimeFormals
      } else {
        formals
      }
    }
  }

}
