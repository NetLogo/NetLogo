// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import org.nlogo.internalapi.{ CompiledModel, CompiledWidget, CompiledButton, NonCompiledWidget }
import org.nlogo.api.NetLogoLegacyDialect
import org.nlogo.core.{ AgentKind, Button => CoreButton, CompilerException,
  Model, Program, Widget }
import org.nlogo.nvm.CompilerResults
import org.nlogo.workspace.AbstractWorkspace

object CompileAll {
  def apply(model: Model, workspace: AbstractWorkspace): CompiledModel = {
    //TODO: We're forcing this to be a 2D Program
    val program = Program.fromDialect(NetLogoLegacyDialect).copy(interfaceGlobals = model.interfaceGlobals)
    try {
      val results =
        workspace.compiler.compileProgram(model.code, Seq(), program,
          workspace.getExtensionManager,
          workspace.getCompilationEnvironment)
      workspace.setProcedures(results.proceduresMap)
      workspace.init()
      workspace.world.program(results.program)
      val compiledWidgets = model.widgets.map(compileWidget(results, workspace))
      CompiledModel(model, compiledWidgets, Right(results.program))
    } catch {
      case e: CompilerException =>
        CompiledModel(model, Seq(), Left(e))
      case e: Exception =>
        println("exception!")
        throw e
      case s: scala.NotImplementedError =>
        s.printStackTrace()
        throw s
    }
  }

  def compileWidget(results: CompilerResults, workspace: AbstractWorkspace)(widget: Widget): CompiledWidget = {
    widget match {
      case b: CoreButton =>
        b.source map { buttonSource =>
          val headerCode = b.buttonKind match {
            case AgentKind.Observer => "__observercode"
            case AgentKind.Turtle => "__turtlecode"
            case AgentKind.Patch => "__patchcode"
            case AgentKind.Link => "__linkcode"
          }
          val (repeatStart, repeatEnd) = if (b.forever) ("loop [", "__foreverbuttonend ]") else ("", "__done")
          val tag = s" __button-" + b.hashCode
          val source = s"to $tag [] $headerCode $repeatStart \n $buttonSource \n $repeatEnd end"
          val displayName = b.display.getOrElse(buttonSource.trim.replaceAll("\\s+", " "))

          try {
            val buttonResults =
              workspace.compiler.compileMoreCode(source, Some(displayName),
                results.program, results.proceduresMap,
                workspace.getExtensionManager, workspace.getCompilationEnvironment)
            buttonResults.head.init(workspace)
            CompiledButton(b, None, tag)
          } catch {
            case e: CompilerException =>
              CompiledButton(b, Some(e), "")
          }
        } getOrElse NonCompiledWidget(widget)

      case _ => NonCompiledWidget(widget)
    }
  }
}
