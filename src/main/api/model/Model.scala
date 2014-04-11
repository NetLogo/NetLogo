// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api.model
import org.nlogo.util.Utils.getResourceLines

case class Model(code: String = "", widgets: List[Widget] = Nil, info: String = "", version: String = "NetLogo 5.0",
  turtleShapes: List[String] = Nil, behaviorSpace: List[String] = Nil, linkShapes: List[String] = Nil,
  previewCommands: List[String] = Nil) {

  def view: View = widgets.collectFirst({case (w: View) => w}).get
  def plots: List[Plot] = widgets.collect({case (w: Plot) => w})
}

object Model {
  lazy val defaultShapes: Seq[String] =
    getResourceLines("/system/defaultShapes.txt").toSeq
  lazy val defaultLinkShapes: Seq[String] =
    getResourceLines("/system/defaultLinkShapes.txt").toSeq
}

object ModelReader {

  val SEPARATOR = "@#$#@#$#@"

  def parseModel(model: String): Model = {
    var sections = Vector[Vector[String]]()
    var sectionContents = Vector[String]()
    def sectionDone() {
      sections :+= sectionContents
      sectionContents = Vector()
    }
    for(line <- io.Source.fromString(model).getLines)
      if(line.startsWith(SEPARATOR))
        sectionDone()
      else
        sectionContents :+= line
    sectionDone()

    val Vector(code, interface, info, turtleShapes, version, previewCommands, systemDynamics,
             behaviorSpace, hubNetClient, linkShapes, modelSettings, deltaTick) = sections
    new Model(code.mkString("\n"), WidgetReader.readInterface(interface.toList), info.mkString("\n"), version.head,
              turtleShapes.toList, behaviorSpace.toList, linkShapes.toList, previewCommands.toList)
  }

/*
  val modelSuffix = "nlogo"
  val emptyModelPath = "/system/empty." + modelSuffix

  def parseVersion(map: ModelMap): String =
    map(ModelSection.Version).head

  def parseWidgets(lines: Seq[String]): Seq[Seq[String]] = {
    var widgets = Vector[Vector[String]]()
    var widget = Vector[String]()
    for(line <- lines)
      if(line.nonEmpty)
        widget :+= line
      else {
        if(!widget.forall(_.isEmpty))
          widgets :+= widget
        widget = Vector()
      }
    if(!widget.isEmpty)
      widgets :+= widget
    widgets
  }

  def restoreLines(s: String): String = {
    @scala.annotation.tailrec
    def loop(acc: Vector[Char], rest: String): Vector[Char] = {
      if (rest.size < 2)
        acc ++ rest
      else if (rest.head == '\\')
        rest.tail.head match {
          case 'n'  => loop(acc :+ '\n', rest.tail.tail)
          case '\\' => loop(acc :+ '\\', rest.tail.tail)
          case '"'  => loop(acc :+ '"', rest.tail.tail)
          case _    => sys.error("invalid escape sequence in \"" + s + "\"")
        }
      else loop(acc :+ rest.head, rest.tail)
    }
    loop(Vector(), s).mkString
  }*/

}
/*
  val sliders = widgets.filter(_.isInstanceOf[Slider])
  val switches = widgets.filter(_.isInstanceOf[Switch])
  val choosers = widgets.filter(_.isInstanceOf[Chooser])
  val plots = widgets.filter(_.isInstanceOf[Plot])
  val inputBoxes = widgets.filter(_.isInstanceOf[InputBox[_]])

  override def toString =
    template.replace("<<CODE SECTION>>", code).
             replace("<<SLIDER SECTION>>\n",  sliders.mkString("\n\n") + "\n").
             replace("<<CHOOSER SECTION>>\n", choosers.mkString("\n\n") + "\n").
             replace("<<SWITCH SECTION>>\n",  switches.mkString("\n\n") + "\n").
             replace("<<INPUTBOX SECTION>>\n",  inputBoxes.mkString("\n\n") + "\n").
             replace("<<PLOT SECTION>>\n",    plots.mkString("\n\n") + "\n").
             replace("<<PREVIEW SECTION>>", previewCode).
             replace("<<TURTLE SHAPES SECTION>>", api.ModelReader.defaultShapes.mkString("\n")).
             replace("<<LINK SHAPES SECTION>>", api.ModelReader.defaultLinkShapes.mkString("\n")).
             replace("<<WRAPPING-ALLOWED-IN-X>>", (if (dimensions.wrappingAllowedInX) "1" else "0")).
             replace("<<WRAPPING-ALLOWED-IN-Y>>", (if (dimensions.wrappingAllowedInY) "1" else "0")).
             replace("<<MAX-PXCOR-OR-MINUS-ONE>>",
               (if (dimensions.minPxcor == -dimensions.maxPxcor)
                  dimensions.maxPxcor else -1).toString).
             replace("<<MAX-PYCOR-OR-MINUS-ONE>>",
               (if (dimensions.minPycor == -dimensions.maxPycor)
                  dimensions.maxPycor else -1).toString).
             replace("<<MIN-PXCOR>>", dimensions.minPxcor.toString).
             replace("<<MAX-PXCOR>>", dimensions.maxPxcor.toString).
             replace("<<MIN-PYCOR>>", dimensions.minPycor.toString).
             replace("<<MAX-PYCOR>>", dimensions.maxPycor.toString)
}*/
