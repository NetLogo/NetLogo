// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.{ LogoException, Version }

object EngineException {
  def rethrow(ex: LogoException, context: Context, instruction: Instruction) {
    ex match {
      case ee: EngineException =>
        if (ee.context == null)
          ee.context = context
        if (ee.instruction == null)
          ee.instruction = instruction
        if (!ee.hasBeenResolved)
          ee.resolveErrorInstruction()
        throw ee
      // previously this flattened all LogoExceptions into EngineExceptions however, we definitely don't
      // want to flatten halt exceptions since we handle them differently later.  ev 8/5/05
      case he: HaltException =>
        throw he
      case _ =>
        val newEx = new EngineException(context, instruction, ex.getMessage)
        newEx.resolveErrorInstruction()
        throw newEx
    }
  }
}

// We need Scala's rather obscure "early initializer" syntax here because fillInStackTrace gets called
// during construction, so we need to make sure the cached message var has been initialized to
// None already. - ST 1/19/12
class EngineException(var context: Context, var instruction: Instruction, val message: String)
extends {
  var cachedRuntimeErrorMessage: Option[String] = None
} with LogoException(message, null) {

  def this(context: Context, message: String) =
    this(context, null, message)

  private var hasBeenResolved = false

  // With the new bytecode generation compiler stuff, we need be able to create EngineExceptions
  // without an instruction.  Later, when we catch the exception, we can use line number information
  // to figure out what token the error happened on.
  require(Version.useGenerator || instruction != null)

  // GeneratedInstructions have multiple Instructions stored inside them and we need to resolve
  // which instruction was actually executing when the error occurred.  ~Forrest (10/24/2006)
  private def resolveErrorInstruction() {
    require(!hasBeenResolved,
            "An EngineException must only be 'resolved' once")
    hasBeenResolved = true
    instruction = instruction.extractErrorInstruction(this)
  }

  override def fillInStackTrace = {
    super.fillInStackTrace()
    require(cachedRuntimeErrorMessage != null)
    if(context != null && cachedRuntimeErrorMessage == None)
      cachedRuntimeErrorMessage = Some(
        context.buildRuntimeErrorMessage(instruction, this, message))
    this
  }

}
