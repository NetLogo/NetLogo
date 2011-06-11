package org.nlogo.api

import org.nlogo.api.File.{restoreLines, stripLines}
import org.nlogo.api.StringUtils.unEscapeString
import collection.mutable.ArrayBuffer
import java.io.Serializable

// TODO handle default values better. sometimes we are using .get on an
// option, but maybe we should get rid of the option and set the default value here instead.
// im not sure yet the best way to handle this.

object WidgetIO {

  object Loc{
    def apply(strings: Array[String]): Loc = {
      val List(x1, y1, x2, y2) = strings.drop(1).take(4).map(_.toInt).toList
      Loc(x1, y1, x2, y2)
    }
  }
  case class Loc(x1: Int, y1: Int, x2: Int, y2: Int) extends Serializable {
    def toStringArray = Array(x1.toString, y1.toString, x2.toString, y2.toString)
  }

  sealed trait WidgetSpec extends Serializable{
    val loc: Loc
    def width = loc.x2 - loc.x1
    def height = loc.y2 - loc.y1
  }

  sealed trait InterfaceGlobalWidgetSpec {
    def name: String
  }

  case class ButtonSpec(loc: Loc, displayName: Option[String], source: String, forever: Boolean,
                        agentType: String, actionKey: Option[Char], goTime: Boolean) extends WidgetSpec

  case class SliderSpec(loc: Loc, name: String, min: String, max: String,
                        value: Double, increment: String, units: Option[String], vertical: Boolean)
          extends WidgetSpec with InterfaceGlobalWidgetSpec

  case class MonitorSpec(loc: Loc, displayName: Option[String], source: Option[String],
                         decimalPlaces: Option[Int], fontSize: Option[Int]) extends WidgetSpec
  // for MonitorWidget.java
  def monitorOption(loc: Loc, displayName: String, source: String,
                    decimalPlaces: Int, fontSize: Int):Option[WidgetSpec] =
    Some(MonitorSpec(loc,
      if(displayName.trim == "") None else Some(displayName),
      Some(source), Some(decimalPlaces), Some(fontSize)))

  case class SwitchSpec(loc: Loc, name: String, isOn: Boolean) extends WidgetSpec with InterfaceGlobalWidgetSpec
  // for SwitchWidget.java
  def switchOption(loc: Loc, name: String, isOn: Boolean):Option[WidgetSpec] = Some(SwitchSpec(loc, name, isOn))

  // TODO: zzz turn choices into a List[String] or List[Any]
  case class ChooserSpec(loc: Loc, name: String, choices: String, index: Int) extends WidgetSpec with InterfaceGlobalWidgetSpec

  case class InputBoxSpec(loc: Loc, name: String, contents: String,
                          multiLine: Boolean, inputType: String) extends WidgetSpec with InterfaceGlobalWidgetSpec

  case class OutputSpec(loc: Loc, fontSize: Option[Int]) extends WidgetSpec

  case class NoteSpec(loc: Loc, text: String, fontSize: Option[Int],
                      color: Option[Double], transparency: Boolean) extends WidgetSpec
  // for WorldViewSettings.java
  def noteOption(loc: Loc, text: String, fontSize: Int,
                 color: Double, transparency: Boolean):Option[WidgetSpec] =
    Some(NoteSpec(loc, text, Some(fontSize), Some(color), transparency))

  case class PlotSpec(loc: Loc, name: String, xLabel: String, yLabel: String,
                      defaultXMin: Double, defaultXMax: Double, defaultYMin: Double, defaultYMax: Double,
                      autoScaleOn: Boolean, legendOn: Boolean,
                      setupCode: Option[String], updateCode: Option[String],
                      pens: List[PlotPenSpec]) extends WidgetSpec

  case class PlotPenSpec(name: String, interval: Double, mode: Int, color: Int, inLegend: Boolean,
                         setupCode: Option[String], updateCode: Option[String]) extends Serializable

  // View stuff is such a disaster that I can't even begin to disentangle it now.
  // We we just have to stick with the raw strings. This is, of course, very telling
  // that the code is in rough shape here. But there's nothing we can do about that now.
  // JC - 2/24/11
  case class ViewSpec(strings:Array[String]) extends WidgetSpec{
    val loc = Loc(strings)
    override def toString = "ViewSpec(" + strings.mkString(",") + ")"
  }
  object ViewSpec {
    def parse(strings: Array[String]) = ViewSpec(strings)
    def toArray(spec:ViewSpec) = spec.strings
  }
  // for WorldViewSettings.java
  def viewOption(strings:Array[String]):Option[WidgetSpec] = Some(ViewSpec(strings))

  object ButtonSpec {
    // 0:  BUTTON
    // 1-4: x1, y1, x2, y2
    // 5:  name
    // 6:  source
    // 7:  forever (T or NIL)
    // 8:  unused
    // 9:  show display name, though, it is now automatic.
    // 10: agent type
    // 11: unused : former autoUpdate flag
    // 12: action key
    // 13: unused : intermediateupdates were optional for a short time
    // 14: unused : being affected by the speed slider was optional for a short time
    // 15: goTime (disable until after setup)

    // takes the old style button lines and builds a ButtonSpec
    def parse(strings: Array[String]) = {
      val name = if (strings(5) != "NIL") Some(strings(5)) else None
      val source = restoreLines(strings(6))
      val forever = strings(7) == "T"
      val agentType = strings(10)
      val actionKey = if( strings.length > 12 && strings(12) != "NIL" ) Some(strings(12).charAt(0)) else None
      val goTime = if( strings.length > 15) strings(15) == "0" else false
      ButtonSpec(Loc(strings), name, source, forever, agentType, actionKey, goTime)
    }

    def toArray(spec:ButtonSpec) = {
      val s = ArrayBuffer[String]()
      s+="BUTTON"
      s++=spec.loc.toStringArray
      s+=spec.displayName.getOrElse("NIL")
      if(spec.source.trim != "") s+=stripLines(spec.source) else s+="NIL"
      s+=(if(spec.forever) "T" else "NIL")
      s+="1" // for compatability
      s+="T" // show display name
      s+=spec.agentType
      s+="NIL" // former autoUpdate flag
      s+=spec.actionKey.map(_.toString).getOrElse("NIL")
      s+="NIL" // intermediateupdates were optional for a short time
      s+="NIL" // being affected by the speed slider was optional for a short time
      s+=(if(spec.goTime) "0" else "1")
      s.toArray
    }

    def toString(spec:ButtonSpec) = toArray(spec).mkString("\n") + "\n"
  }

  object SliderSpec {
    // 0:  SLIDER
    // 1-4: x1, y1, x2, y2: Int(s)
    // 5:  name: String
    // 6:  name... yeah thats weird.
    // 7:  min: String
    // 8:  max: String
    // 9:  value: Double
    // 10: increment: String
    // 11: unused
    // 12: units: NIL or user defined String
    // 13: vertical: VERTICAL or HORIZONTAL

    // takes the old style slider lines and builds a SliderSpec
    def parse(strings: Array[String]) = {
      val (name, min, max, value, increment, units, vertical) =
        (strings(6),strings(7),strings(8),strings(9),strings(10), strings(12), strings(13) == "VERTICAL")
      SliderSpec(Loc(strings), name, min, max, value.toDouble, increment,
        if(units == "NIL") None else Some(units), vertical)
    }

    def toArray(spec:SliderSpec) = {
      val s = ArrayBuffer[String]()
      s+="SLIDER"
      s++=spec.loc.toStringArray
      s+=spec.name
      s+=spec.name
      s+=stripLines(spec.min)
      s+=stripLines(spec.max)
      s+=Dump.number(spec.value)
      s+=stripLines(spec.increment)
      s+="1" // unused
      s+=spec.units.getOrElse("NIL")
      s+=(if(spec.vertical) "VERTICAL" else "HORIZONTAL")
      s.toArray
    }
  }

  object MonitorSpec{
    // takes the old style monitor lines and builds a MonitorSpec
    def parse(strings: Array[String]) = {
      // 0:  MONITOR
      // 1-4: x1, y1, x2, y2: Int(s)
      // 5:  displayName: String (maybe NIL)
      // 6:  source: String (maybe NIL...but I don't see how)
      // 7:  decimalPlaces: Int
      // 8:  unused
      // 9:  fontSize: Int
      val displayName = strings(5)
      val source = restoreLines(strings(6))
      val decimalPlaces = if (strings.length > 7) Some(strings(7).toInt) else None
      val fontSize = if (strings.length > 9) Some(strings(9).toInt) else None
      MonitorSpec(Loc(strings),
        if(displayName!="NIL") Some(displayName) else None,
        if(source!="NIL") Some(source) else None,
        decimalPlaces, fontSize)
    }

    def toArray(spec:MonitorSpec) = {
      val s = ArrayBuffer[String]()
      s+="MONITOR"
      s++=spec.loc.toStringArray
      s+=spec.displayName.getOrElse("NIL")
      s+=spec.source.map(stripLines).getOrElse("NIL")
      s+=spec.decimalPlaces.getOrElse(17).toString // 17 is the default
      s+="1"
      s+=spec.fontSize.getOrElse(11).toString // 11 is the default
      s.toArray
    }
  }

  object SwitchSpec {
    // 0:  SWITCH
    // 1-4: x1, y1, x2, y2: Int(s)
    // 5:  displayName: String (maybe NIL) .... but this is not used.
    // 6:  name: String
    // 7:  isOn: 1 for off, 0 for on. really.
    // 8: unused
    // 9: unused

    // takes the old style switch lines and builds a SwitchSpec
    def parse(strings: Array[String]) = {
      val (name, on) = (restoreLines(strings(6)),strings(7).toDouble == 0)
      SwitchSpec(Loc(strings), name, on)
    }

    def toArray(spec:SwitchSpec) = {
      val s = ArrayBuffer[String]()
      s+="SWITCH"
      s++=spec.loc.toStringArray
      s+="NIL" // unused display name
      s+=spec.name
      s+=(if(spec.isOn) "0" else "1")
      s+="1" // for compatibility
      s+="-1000" // for compatibility
      s.toArray
    }
  }

  object ChooserSpec {
    // 0:  CHOOSER or CHOICE
    // 1-4: x1, y1, x2, y2: Int(s)
    // 5:  name: String
    // 6:  name... yeah thats weird.
    // 7:  choices: String
    // 8:  index: Int

    // takes the old style chooser lines and builds a ChooserSpec
    def parse(strings: Array[String]) = {
      val (name, choices, index) = (restoreLines(strings(5)),strings(7), strings(8).toInt)
      ChooserSpec(Loc(strings), name, choices, index)
    }

    def toArray(spec:ChooserSpec) = {
      val s = ArrayBuffer[String]()
      s+="CHOOSER"
      s++=spec.loc.toStringArray
      s+=spec.name
      s+=spec.name
      s+=spec.choices
      s+=spec.index.toString
      s.toArray
    }
  }

  object InputBoxSpec {
    // 0:  INPUTBOX
    // 1-4: x1, y1, x2, y2: Int(s)
    // 5:  name: String
    // 6:  contents: String
    // 7:  unused
    // 8:  multiline: 1 (true) or 0 (false)
    // 9:  inputType: String

    // takes the old style input box lines and builds a InputBoxSpec
    def parse(strings:Array[String]) = {
      val (name, contents) = (strings(5),restoreLines(strings(6)))
      val multiLine = if(strings.length > 9) strings(8) == "1" else false
      // input box defaults to "String" type
      val inputType = if(strings.length > 9) strings(9) else "String"
      InputBoxSpec(Loc(strings), name, contents, multiLine, inputType)
    }

    def toArray(spec:InputBoxSpec) = {
      val s = ArrayBuffer[String]()
      s+="INPUTBOX"
      s++=spec.loc.toStringArray
      s+=spec.name
      s+=spec.contents // this one is funny. see InputBox.saveSpec
      s+="1"
      s+=(if(spec.multiLine) "1" else "0")
      s+=spec.inputType
      s.toArray
    }
  }

  object OutputSpec {
    // 0:  OUTPUT
    // 1-4: x1, y1, x2, y2: Int(s)
    // 5:  font size: Int (optional)

    // takes the old style output widget lines and builds a OutputSpec
    def parse(strings:Array[String]) = {
      val fontSize = if(strings.length > 5) Some(strings(5).toInt) else None
      OutputSpec(Loc(strings), fontSize)
    }
    
    def toArray(spec:OutputSpec) = {
      val s = ArrayBuffer[String]()
      s+="OUTPUT"
      s++=spec.loc.toStringArray
      s+=spec.fontSize.getOrElse(12).toString
      s.toArray
    }
  }

  object NoteSpec {
    // 0:  TEXTBOX
    // g1-4: x1, y1, x2, y2: Int(s)
    // 5:  text: String (maybe NIL)
    // 6:  font size: Int (optional)
    // 7:  color: Double (optional)
    // 8:  transparency: Boolean

    def parse(strings:Array[String]) = {
      val text = if (strings(5) != "NIL") strings(5) else ""
      val fontSize = if(strings.length > 6) Some(strings(6).toInt) else None
      val color = if(strings.length > 7) Some(strings(7).toDouble) else None
      val transparency = if(strings.length > 8) strings(8) != 0 else false
      NoteSpec(Loc(strings), text, fontSize, color, transparency)
    }

    // TODO zzz
    def toArray(spec:NoteSpec) = {
      val s = ArrayBuffer[String]()
      s+="TEXTBOX"
      s++=spec.loc.toStringArray
      s+=(if(spec.text.trim=="") "NIL" else stripLines(spec.text))
      s+=spec.fontSize.get.toString // NoteWidget will always give us back a fontSize
      s+=spec.color.get.toString // NoteWidget will always give us back a color
      s+=(if(spec.transparency) "1" else "0")
      s.toArray
    }

    /**
    s.append( org.nlogo.api.Color.getClosestColorNumberByARGB( color.getRGB() )  + "\n" ) ;
     */
  }

  object PlotSpec {
    object PlotParser {
      // 0: PLOT
      // 1-4: x1, y1, x2, y2: Int(s)
      // 5:  name: String
      // 6, 7: xLabel, yLabel
      // 8, 9, 10, 11: defaultXMin, defaultXMax, defaultYMin, defaultYMax: Double (optional)
      // 12: defaultAutoScaleOn: true/false
      // 13: legendOn: true/false
      // 14: setup and update code
      // 15: PENS
      // 16+: pen lines
      // note, its possible that the pen lines start earlier than 15, in older versions.
      
      def parse(strings: Array[String]): PlotSpec = {
        val (plotLines, penLines) = strings.toList.span(_ != "PENS")
        val (plotName, xLabel, yLabel)= (plotLines(5), plotLines(6), plotLines(7))
        val List(defaultXMin, defaultXMax, defaultYMin, defaultYMax) =
          if (11 < plotLines.length) plotLines.drop(8).take(4).map(_.toDouble)
          else List(0.0, 10.0, 0.0, 10.0)

        val defaultAutoScaleOn = if (12 < plotLines.length) plotLines(12).toBoolean else true
        val legendOn = if (13 < plotLines.length) plotLines(13).toBoolean else true

        val (setupCode, updateCode) =
          if (14 < plotLines.length) {
            parseStringLiterals(plotLines(14)) match {
              case setup :: update :: Nil => (setup, update) // the correct amount of plot code.
              case Nil => (None, None)// old style model, no new plot code. this is ok.
              case _ =>
                // 1, or 3 or more bits of code...error.
                error("Plot '" + plotName + "' contains invalid setup and/or update code: " + plotLines(14))
            }
          } else (None, None)

        // some models might not have a PENS line with any pens underneath.
        // deal with that here.
        val doubleCheckedPenLines = penLines match {
          case "PENS" :: xs => xs
          case _ => Nil
        }

        val pens = doubleCheckedPenLines.map(parsePen)

        PlotSpec(Loc(strings), plotName, xLabel, yLabel,
                 defaultXMin, defaultXMax, defaultYMin, defaultYMax,
                 defaultAutoScaleOn, legendOn, setupCode, updateCode, pens)
      }

//      def toArray(spec: PlotSpec) = {
//        val s = ArrayBuffer[String]()
//        s += "PLOT"
////        s ++= spec.loc.toStringArray
////        s += (if (spec.text.trim == "") "NIL" else stripLines(spec.text))
////        s += spec.fontSize.get.toString // NoteWidget will always give us back a fontSize
////        s += spec.color.get.toString // NoteWidget will always give us back a color
////        s += (if (spec.transparency) "1" else "0")
//        s.toArray
//      }


      // example pen line: "My Pen" 1.0 0 -16777216 true
      // name, default interval, mode, color, legend

      private def parsePen(s: String): PlotPenSpec = {
        // the drop(1) skips the opening quote
        val tokens = tokenize(s.drop(1)).toList

        // this is a bit messy. span() puts the last part of the name in the wrong
        // part of the result, so we have to shuffle a token from one list to the other
        val (nameTokens, moreTokens) =
        spanPlusOne(tokens)(tok => !tok.endsWith("\"") || tok.endsWith("\\\""))

        // dropRight drops the closing quote
        val name = unEscapeString(nameTokens.mkString.dropRight(1))

        // the rest of the line is easy to handle
        val (interval, mode, color, inLegend) =
        moreTokens.filter(_.trim.nonEmpty) match {
          case List(interval, mode, color, inLegend, _*) =>
            (interval.toDouble, mode.toInt, color.toInt, inLegend.toBoolean)
          case _ => error("bad line: \"" + s + "\"")
        }

        val codeString = moreTokens.dropWhile(!_.startsWith("\"")).mkString.trim
        parseStringLiterals(codeString) match {
          case List(setup, update) => PlotPenSpec(name, interval, mode, color, inLegend, setup, update)
          case _ => PlotPenSpec(name, interval, mode, color, inLegend, None, None)
        }
      }

      // Used to parse a line that may contain multiple string literals, surrounded by double quotes
      // and separated by spaces.  This is tricky because the string literals may contain escaped
      // double quotes, so it's nontrivial to figure out where one literal ends and the next starts.
      // (right now this doesn't fail properly when the first quote in the code string is missing.
      // it just thinks there is no code, and returns None. not sure if it's worth fixing.)
      private def parseStringLiterals(s: String): List[Option[String]] = {
        def toCodeOption(s: String) = {
          val code = unEscapeString(s.trim.drop(1).dropRight(1))
          if (code.nonEmpty) Some(code)
          else None
        }
        def isCloseQuote(tok: String) =
          tok.endsWith("\"") && !tok.endsWith("\\\"")
        def recurse(toks: List[String]): List[String] =
          if(toks.isEmpty) Nil
          else {
            val (xs, more) = spanPlusOne(toks)(!isCloseQuote(_))
            xs.mkString :: recurse(more)
          }
        val tokens = tokenize(s).toList
        if (tokens.isEmpty) Nil
        else recurse(tokens).map(toCodeOption(_))
      }

      // encapsulate ugly StringTokenizer
      private def tokenize(s: String): Iterator[String] = {
        import java.util.StringTokenizer
        val tokenizer = new StringTokenizer(s, " ", true)
        new Iterator[String]() {
          def hasNext = tokenizer.hasMoreTokens
          def next() = tokenizer.nextToken()
        }
      }

      // like span, but keeps the first failing item
      private def spanPlusOne[T](list: List[T])(pred: T => Boolean) =
        list.span(pred) match {
          case (xs, y :: ys) => (xs :+ y, ys)
          case (xs, Nil) => (xs, Nil)
        }
    }
  }

  // simply takes the pre 4.2 widget lines and turns them into WidgetSpecs
  // those specs can later be turned into XML.
  def parseWidgets(lines: Iterable[String]): Iterable[WidgetSpec] = {
    val widgetStrings: Seq[Seq[String]] = {
      val widgets = new collection.mutable.ListBuffer[List[String]]
      val widget = new collection.mutable.ListBuffer[String]
      for (line <- lines)
        if (line.nonEmpty) widget += line
        else {
          if (!widget.forall(_.isEmpty)) widgets += widget.toList
          widget.clear()
        }
      if (!widget.isEmpty) widgets += widget.toList
      widgets
    }

    val widgetSpecs = for (w <- widgetStrings; strings = w.toList.toArray) yield w(0) match {
      case "BUTTON"  =>  Some(ButtonSpec.parse(strings))
      case "SLIDER"  =>  Some(SliderSpec.parse(strings))
      case "MONITOR" =>  Some(MonitorSpec.parse(strings))
      case "SWITCH"  =>  Some(SwitchSpec.parse(strings))
      case "CHOOSER" | "CHOICE"  =>  Some(ChooserSpec.parse(strings))
      case "OUTPUT"  =>  Some(OutputSpec.parse(strings))
      case "INPUTBOX"  =>  Some(InputBoxSpec.parse(strings))
      case "TEXTBOX" =>  Some(NoteSpec.parse(strings))
      case "PLOT"  =>  Some(PlotSpec.PlotParser.parse(strings))
      case "GRAPHICS-WINDOW" | "VIEW" => Some(ViewSpec.parse(strings))
      case _ => None
    }
    widgetSpecs.flatten
  }

  def dumpWidgets(widgetSpecs:Iterable[WidgetSpec]): Array[String] = {
    (for (w <- widgetSpecs.toList) yield w match {
      case b:ButtonSpec   => ButtonSpec.toArray(b)
      case s:SliderSpec   => SliderSpec.toArray(s)
      case m:MonitorSpec  => MonitorSpec.toArray(m)
      case s:SwitchSpec   => SwitchSpec.toArray(s)
      case c:ChooserSpec  => ChooserSpec.toArray(c)
      case o:OutputSpec   => OutputSpec.toArray(o)
      case i:InputBoxSpec => InputBoxSpec.toArray(i)
      case n:NoteSpec     => NoteSpec.toArray(n)
      case p:PlotSpec     => Array[String]()
      case v:ViewSpec     => ViewSpec.toArray(v)
    }).filterNot(_.isEmpty).map(_:+"").flatten.toArray
  }
}