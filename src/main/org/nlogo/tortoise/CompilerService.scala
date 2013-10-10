// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.tortoise

import
  scala.io.Source

import
  org.nlogo.{ api, headless },
    api.{ ModelReader, ModelSection, WorldDimensions },
    headless.{ HeadlessWorkspace, WidgetParser }

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

    val (iGlobals, _, _, _, iGlobalCmds) = new WidgetParser(HeadlessWorkspace.newInstance).parseWidgets(interface)

    val Seq(minX, maxX, minY, maxY) = 17 to 20 map { x => interface(x).toInt }
    val dimensions = WorldDimensions(minX, maxX, minY, maxY)

    val (js, _, _) = Compiler.compileProcedures(nlogo, iGlobals, iGlobalCmds.toString, dimensions)

    System.out.println(js)

    System.exit(0)

  }

}

