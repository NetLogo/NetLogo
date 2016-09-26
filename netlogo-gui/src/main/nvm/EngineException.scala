// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.{ Context => ApiContext, ExtensionException, LogoException, Version }

object EngineException {
  def rethrow(ex: LogoException, context: ApiContext, instruction: Instruction): Unit = {
    ex match {
      case ee: EngineException => throw ee
      case he: HaltException   => throw he
      case _                   =>
        throw new WrappedLogoException(ex, context, instruction, ex.getMessage)
    }
  }
}

// abstract class for java interop, probably ought to be expressed as a trait
abstract class EngineException(_context: ApiContext, message: String, cause: Exception) extends LogoException(message, cause) {
  val context: ApiContext = _context match {
    case nvmContext: Context => nvmContext.copy // copy context in case it's later mutated
    case _ => _context
  }
  def responsibleInstruction: Option[Instruction]

  def computeRuntimeErrorMessage(ctx: ApiContext, instruction: Option[Instruction], cause: Option[Exception], message: String): String

  lazy val runtimeErrorMessage: String =
    computeRuntimeErrorMessage(context, responsibleInstruction, Option(cause), message)

  // strictly for java interop. Do not use from scala.
  def responsibleInstructionOrNull: Instruction = responsibleInstruction.orNull
}

trait StackTraceBuilt { this: EngineException =>
  def computeRuntimeErrorMessage(ctx: ApiContext, instruction: Option[Instruction], cause: Option[Exception], message: String) = {
    StackTraceBuilder.build(ctx.activation, ctx.getAgent, instruction.orNull, Some(this), message)
  }

  val responsibleInstruction = Some(baseInstruction.extractErrorInstruction(this))

  protected def baseInstruction: Instruction
}

class WrappedLogoException(
  logoException: LogoException, ctx: ApiContext,
  protected val baseInstruction: Instruction, val message: String)
  extends EngineException(ctx, message, logoException)
  with StackTraceBuilt

class RuntimePrimitiveException(ctx: ApiContext, protected val baseInstruction: Instruction, val message: String)
  extends EngineException(ctx, message, null)
  with StackTraceBuilt

class WrappedExtensionException(ctx: ApiContext, protected val baseInstruction: Instruction, val message: String, extensionException: ExtensionException)
  extends EngineException(ctx, message, extensionException)
  with StackTraceBuilt

class NetLogoStackOverflow(ctx: ApiContext, baseInstruction: Instruction, overflow: StackOverflowError) extends EngineException(ctx, "stack overflow (recursion too deep)", null) {
  val responsibleInstruction: Option[Instruction] = Option(baseInstruction)
  def computeRuntimeErrorMessage(ctx: ApiContext, instruction: Option[Instruction], cause: Option[Exception], message: String): String = {
    message + "\n  " + StackTraceBuilder.build(ctx.activation, ctx.getAgent, instruction.orNull, Some(overflow), message)
  }
}
