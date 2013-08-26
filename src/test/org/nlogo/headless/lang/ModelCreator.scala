// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.headless
package lang

object ModelCreator {

  private val template = """|<<CODE SECTION>>
                            |@#$#@#$#@
                            |GRAPHICS-WINDOW
                            |0
                            |0
                            |0
                            |0
                            |10
                            |10
                            |1.0
                            |0
                            |10
                            |1
                            |1
                            |1
                            |-16
                            |16
                            |-16
                            |16
                            |
                            |<<SLIDER SECTION>>
                            |<<SWITCH SECTION>>
                            |<<CHOOSER SECTION>>
                            |<<INPUTBOX SECTION>>
                            |<<PLOT SECTION>>
                            |
                            |@#$#@#$#@
                            |@#$#@#$#@
                            |@#$#@#$#@
                            |NetLogo 5.0
                            |@#$#@#$#@
                            |<<PREVIEW SECTION>>
                            |@#$#@#$#@
                            |@#$#@#$#@""".stripMargin

  //
  // Code to create models
  //

  // a can have N widgets. plots, sliders, etc are widgets.
  trait Widget

  case class Model(code: String = "", previewCode: String = "", widgets: List[Widget] = Nil) {
    val sliders = widgets.filter(_.isInstanceOf[Slider])
    val switches = widgets.filter(_.isInstanceOf[Switch])
    val choosers = widgets.filter(_.isInstanceOf[Chooser])
    val plots = widgets.filter(_.isInstanceOf[Plot])
    val inputBoxes = widgets.filter(_.isInstanceOf[InputBox[_]])

    override def toString = {
      template.replace("<<CODE SECTION>>", code).
               replace("<<SLIDER SECTION>>\n",  sliders.mkString("\n\n") + "\n").
               replace("<<CHOOSER SECTION>>\n", choosers.mkString("\n\n") + "\n").
               replace("<<SWITCH SECTION>>\n",  switches.mkString("\n\n") + "\n").
               replace("<<INPUTBOX SECTION>>\n",  inputBoxes.mkString("\n\n") + "\n").
               replace("<<PLOT SECTION>>\n",    plots.mkString("\n\n") + "\n").
               replace("<<PREVIEW SECTION>>", previewCode)
    }
  }

  val counter = Iterator.from(0)
  def quoted(s:String) = '"' + s + '"'

  //
  // Code to create plots
  //

  def Pens(pens: Pen*) = pens.toList

  case class Plot(name: String = "Plot" + counter.next,
                  setupCode: String = "", updateCode: String = "", pens: List[Pen] = Nil) extends Widget{
    override def toString =
      "PLOT\n5\n5\n5\n5\n" + name + "\ntime\nnum of turtles\n0.0\n10.0\n0.0\n10.0\ntrue\nfalse\n" +
      quoted(setupCode) + " " + quoted(updateCode) + "\nPENS\n" + pens.mkString("\n")
  }

  case class Pen(name:String = "Pen" + counter.next, setupCode:String = "", updateCode: String = ""){
    override def toString =
      quoted(name) + " 1.0 0 -16777216 true " + quoted(setupCode) + " " + quoted(updateCode)
  }

  //
  // Code to create sliders
  //

  trait Direction
  case object HORIZONTAL extends Direction
  case object VERTICAL extends Direction

  case class Slider(name: String = "Slider" + counter.next,
                    // all of these should be something other than string. however,
                    // constraints can be foo + 30, so we need to allow for that.
                    // so we currently, clients use "10" even though they want just 10.
                    // this is something we should work out in the future. josh - 3/5/10
                    min: String = "0", max: String = "100", current: String = "50", inc: String = "1",
                    units:String = "NIL", direction: Direction = HORIZONTAL) extends Widget{
    override def toString = "SLIDER\n5\n5\n5\n5\n" + name + "\n" + name + "\n" +
      min + "\n" + max + "\n" + current + "\n" + inc + "\n1\n" + units + "\n" + direction
  }

  //
  // Code to create switches
  //

  case class Switch(name:String = "Switch" + counter.next, on: Boolean = true) extends Widget {
    override def toString = "SWITCH\n5\n5\n5\n5\n" + name + "\n" + name + "\n" +
     (if(on) "0" else "1") + "\n1\n1000"
  }


  //
  // Code to create choosers
  //

  case class Chooser(name:String = "Chooser" + counter.next, choices:List[Any] = Nil, index:Int = 0) extends Widget {
    def dump(o:Any) = org.nlogo.api.Dump.logoObject(org.nlogo.api.ScalaConversions.toLogoObject(o), true, false)
    override def toString =
      "CHOOSER\n5\n5\n5\n5\n" + name + "\n" + name + "\n" + choices.map(dump).mkString(" ") + "\n" + index + "\n"
  }

  object InputBoxTypes{
    abstract class InputBoxType(val name:String)
    case object Num extends InputBoxType("Number")
    case object Str extends InputBoxType("String")
    case object StrReporter extends InputBoxType("String (reporter)")
    case object StrCommand extends InputBoxType("String (commands)")
    case object Col extends InputBoxType("Color")
  }

  case class InputBox[T](name:String = "InputBox" + counter.next, multiline: Boolean = false,
          value: T = null, typ: InputBoxTypes.InputBoxType) extends Widget{
    override def toString =
      "INPUTBOX\n5\n5\n5\n5\n" + name + "\n" + value +
      "\n1\n" + (if(multiline) 1 else 0) + "\n" + typ.name + "\n"
  }

}
