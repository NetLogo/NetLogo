// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.api.{ TokenizerInterface  }
import org.nlogo.core.Program
import org.nlogo.core.{ DummyExtensionManager, DummyCompilationEnvironment, Token }
import org.nlogo.nvm.Procedure

object TestHelper {
  private[compiler] def structureParse(source: String, program: Program, is3D: Boolean = false): StructureParser.Results = {
    implicit val tokenizer = if (is3D) Compiler.Tokenizer3D else Compiler.Tokenizer2D
    new StructureParser(tokenizer.tokenize(source), None, program,
      java.util.Collections.emptyMap[String, Procedure], new DummyExtensionManager,
      new DummyCompilationEnvironment()).parse(false)
  }

  private[compiler] def structureParse(tokens: Seq[Token], program: Program)(implicit tokenizer: TokenizerInterface): StructureParser.Results = {
    new StructureParser(tokens, None, program,
      java.util.Collections.emptyMap[String, Procedure], new DummyExtensionManager,
      new DummyCompilationEnvironment()).parse(false)
  }
}
