// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import
  scala.io.Source

import
  org.nlogo.util.Femto,
  org.nlogo.{ api, nvm, workspace },
    api.{ ModelReader, ModelSection, WorldDimensions },
    workspace.WidgetParser

object CompilerService {

  def main(args: Array[String]) {

    val source =
      args match {
        case Array(nlogoPath) => Source.fromFile(nlogoPath)
        case _                => Source.fromInputStream(System.in)
      }

    val contents = source.mkString
    source.close()

    val modelMap  = ModelReader.parseModel(contents)
    val interface = modelMap(ModelSection.Interface)
    val nlogo     = modelMap(ModelSection.Code).mkString("\n")

    val frontEnd: nvm.FrontEndInterface =
      Femto.scalaSingleton("org.nlogo.compile.front.FrontEnd")

    val (iGlobals, _, _, _, iGlobalCmds) =
      new WidgetParser(new nvm.DefaultParserServices(frontEnd))
        .parseWidgets(interface)

    val patchSize = interface(7).toDouble
    val Seq(wrapX, wrapY, _, minX, maxX, minY, maxY) =
      14 to 20 map { x => interface(x).toInt }
    val dimensions = WorldDimensions(
      minX, maxX, minY, maxY, patchSize, wrapX==0, wrapY==0)

    val (js, _, _) = Compiler.compileProcedures(nlogo, iGlobals, iGlobalCmds.toString, dimensions)

    System.out.println(js)

    System.exit(0)

  }

}
