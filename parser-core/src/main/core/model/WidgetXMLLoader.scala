// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core.model

import org.nlogo.core.{ AgentKind, Button, ChooseableBoolean, ChooseableDouble, ChooseableList, ChooseableString,
                        Chooser, Horizontal, InputBox, LogoList, Monitor, NumericInput, Output, Pen, Plot, Slider,
                        StringInput, Switch, TextBox, UpdateMode, Vertical, View, Widget, WorldDimensions,
                        WorldDimensions3D }

object WidgetXMLLoader {
  def readWidget(element: XMLElement): Option[Widget] = {
    // alternative to toInt that provides a more descriptive error message on failure (Isaac B 7/6/25)
    def toInt(element: XMLElement, property: String): Int = {
      element(property).toIntOption.getOrElse {
        throw new Exception(s"Property \"$property\" in element \"${element.name}\" must be an integer.")
      }
    }

    // alternative to toDouble that provides a more descriptive error message on failure (Isaac B 7/6/25)
    def toDouble(element: XMLElement, property: String): Double = {
      element(property).toDoubleOption.getOrElse {
        throw new Exception(s"Property \"$property\" in element \"${element.name}\" must be a floating point number.")
      }
    }

    // alternative to toBoolean that provides a more descriptive error message on failure (Isaac B 7/6/25)
    def toBoolean(element: XMLElement, property: String): Boolean = {
      element(property).toBooleanOption.getOrElse {
        throw new Exception(s"Property \"$property\" in element \"${element.name}\" must be \"true\" or \"false\".")
      }
    }

    element.name match {

      case "button" =>
        val source =
          if (element.text.isEmpty)
            None
          else
            Some(element.text)
        val kind =
          element("kind") match {
            case "Observer" => AgentKind.Observer
            case "Patch"    => AgentKind.Patch
            case "Turtle"   => AgentKind.Turtle
            case "Link"     => AgentKind.Link
          }
        Some(Button( source, toInt(element, "x"), toInt(element, "y"), toInt(element, "width")
                   , toInt(element, "height"), element("sizeVersion", "1").toInt == 0, element.get("display")
                   , toBoolean(element, "forever"), kind, element.get("actionKey").map(_.head)
                   , toBoolean(element, "disableUntilTicks")
                   ))

      case "slider" =>
        val direction =
          element("direction") match {
            case "Horizontal" => Horizontal
            case "Vertical"   => Vertical
          }
        Some(Slider( element.get("variable"), toInt(element, "x"), toInt(element, "y"), toInt(element, "width")
                   , toInt(element, "height"), element("sizeVersion", "1").toInt == 0, element.get("display")
                   , element("min"), element("max"), toDouble(element, "default"), element("step")
                   , element.get("units"), direction
                   ))

      case "view" =>
        val dims =
          new WorldDimensions( toInt(element, "minPxcor"), toInt(element, "maxPxcor"), toInt(element, "minPycor")
                             , toInt(element, "maxPycor"), toDouble(element, "patchSize")
                             , toBoolean(element, "wrappingAllowedX"), toBoolean(element, "wrappingAllowedY")
                             )
        Some(View( toInt(element, "x"), toInt(element, "y"), toInt(element, "width"), toInt(element, "height"), dims
                 , toInt(element, "fontSize"), UpdateMode.load(toInt(element, "updateMode"))
                 , toBoolean(element, "showTickCounter"), element.get("tickCounterLabel")
                 , toDouble(element, "frameRate")
                 ))

      case "view3d" =>
        val dims =
          new WorldDimensions3D( toInt(element, "minPxcor"), toInt(element, "maxPxcor")
                               , toInt(element, "minPycor"), toInt(element, "maxPycor")
                               , toInt(element, "minPzcor"), toInt(element, "maxPzcor")
                               , toDouble(element, "patchSize"), toBoolean(element, "wrappingAllowedX")
                               , toBoolean(element, "wrappingAllowedY"), toBoolean(element, "wrappingAllowedZ")
                               )
        Some(View( toInt(element, "x"), toInt(element, "y"), toInt(element, "width"), toInt(element, "height"), dims
                 , toInt(element, "fontSize"), UpdateMode.load(toInt(element, "updateMode"))
                 , toBoolean(element, "showTickCounter"), element.get("tickCounterLabel")
                 , toDouble(element, "frameRate")
                 ))

      case "monitor" =>
        Some(Monitor( Some(element.text), toInt(element, "x"), toInt(element, "y")
                    , toInt(element, "width"), toInt(element, "height"), element("sizeVersion", "1").toInt == 0
                    , element.get("display"), toInt(element, "precision"), toInt(element, "fontSize")
                    , element.get("units")
                    ))

      case "switch" =>
        Some(Switch( element.get("variable"), toInt(element, "x"), toInt(element, "y"), toInt(element, "width")
                   , toInt(element, "height"), element("sizeVersion", "1").toInt == 0, element.get("display")
                   , toBoolean(element, "on")
                   ))

      case "plot" =>
        val pens =
          element.getChildren("pen").map(
            el =>
               Pen( el("display"), el("interval").toDouble, el("mode").toInt, el("color").toInt
                  , el("legend").toBoolean, el.getChild("setup").text, el.getChild("update").text
                  )
          ).toList
        Some(Plot( element.get("display"), toInt(element, "x"), toInt(element, "y"), toInt(element, "width")
                 , toInt(element, "height"), element("sizeVersion", "1").toInt == 0, element.get("xAxis")
                 , element.get("yAxis"), toDouble(element, "xMin"), toDouble(element, "xMax")
                 , toDouble(element, "yMin"), toDouble(element, "yMax"), toBoolean(element, "autoPlotX")
                 , toBoolean(element, "autoPlotY"), toBoolean(element, "legend"), element.getChild("setup").text
                 , element.getChild("update").text, pens
                 ))

      case "chooser" =>
        val children =
          element.getChildren("choice").map(
            el =>
              el("type") match {
                case "string" =>
                  ChooseableString(el("value"))
                case "double" =>
                  ChooseableDouble(el("value").toDouble)
                case "boolean" =>
                  ChooseableBoolean(el("value").toBoolean)
                case "list" =>
                  ChooseableList(LogoList.fromList(el.getChildren("value").map(_("value")).toList))
              }
          )
        Some(Chooser( element.get("variable"), toInt(element, "x"), toInt(element, "y"), toInt(element, "width")
                   , toInt(element, "height"), element("sizeVersion", "1").toInt == 0, element.get("display")
                   , children.toList, toInt(element, "current")
                   ))

      case "output" =>
        Some(Output( toInt(element, "x"), toInt(element, "y"), toInt(element, "width"), toInt(element, "height")
                   , toInt(element, "fontSize")
                   ))

      case "input" =>
        val input =
          element("type") match {
            case "number" =>
              NumericInput(element.text.toDouble, NumericInput.NumberLabel)
            case "color" =>
              NumericInput(element.text.toDouble, NumericInput.ColorLabel)
            case "string" =>
              StringInput(element.text, StringInput.StringLabel, toBoolean(element, "multiline"))
            case "reporter" =>
              StringInput(element.text, StringInput.ReporterLabel, toBoolean(element, "multiline"))
            case "command" =>
              StringInput(element.text, StringInput.CommandLabel, toBoolean(element, "multiline"))
          }
        Some(InputBox( element.get("variable"), toInt(element, "x"), toInt(element, "y"), toInt(element, "width")
                     , toInt(element, "height"), element("sizeVersion", "1").toInt == 0, input
                     ))

      case "note" =>
        Some(TextBox( Some(element.text), toInt(element, "x"), toInt(element, "y"), toInt(element, "width")
                    , toInt(element, "height"), toInt(element, "fontSize"), toBoolean(element, "markdown")
                    , element.get("textColorLight").map(_.toInt), element.get("textColorDark").map(_.toInt)
                    , element.get("backgroundLight").map(_.toInt), element.get("backgroundDark").map(_.toInt)
                    ))

      case _ => None // ignore other widgets for compatibility with other versions in the future (Isaac B 2/12/25)

    }
  }

  def writeWidget(widget: Widget): XMLElement = {

    def ifDefined[T <: Widget](w: T)(key: String, f: (T) => Option[Any]): Map[String, String] =
      if (f(w).isDefined) Map(key -> f(w).get.toString) else Map()

    widget match {

      case button: Button =>
        val attributes =
          Map( "x"              -> button.x.toString
             , "y"               -> button.y.toString
             , "width"             -> button.width.toString
             , "height"            -> button.height.toString
             , "forever"           -> button.forever.toString
             , "kind"              -> button.buttonKind.toString
             , "disableUntilTicks" -> button.disableUntilTicksStart.toString
             ) ++
          ifDefined(button)(  "display",   _.display) ++
          ifDefined(button)("actionKey", _.actionKey) ++
          (if (button.oldSize) Map("sizeVersion" -> "0") else Map())

        XMLElement("button", attributes, button.source.getOrElse(""), Seq())

      case slider: Slider =>
        val attributes =
          Map( "x"      -> slider.x.toString
             , "y"       -> slider.y.toString
             , "width"     -> slider.width.toString
             , "height"    -> slider.height.toString
             , "min"       -> slider.min
             , "max"       -> slider.max
             , "default"   -> slider.default.toString
             , "step"      -> slider.step
             , "direction" -> slider.direction.toString
             ) ++
          ifDefined(slider)( "display",  _.display) ++
          ifDefined(slider)("variable", _.variable) ++
          ifDefined(slider)(   "units",    _.units) ++
          (if (slider.oldSize) Map("sizeVersion" -> "0") else Map())
        XMLElement("slider", attributes, "", Seq())

      case view: View =>

        val baseAttributes =
          Map( "x"             -> view.x.toString
             , "y"              -> view.y.toString
             , "width"            -> view.width.toString
             , "height"           -> view.height.toString
             , "minPxcor"         -> view.dimensions.minPxcor.toString
             , "maxPxcor"         -> view.dimensions.maxPxcor.toString
             , "minPycor"         -> view.dimensions.minPycor.toString
             , "maxPycor"         -> view.dimensions.maxPycor.toString
             , "patchSize"        -> view.dimensions.patchSize.toString
             , "wrappingAllowedX" -> view.dimensions.wrappingAllowedInX.toString
             , "wrappingAllowedY" -> view.dimensions.wrappingAllowedInY.toString
             , "fontSize"         -> view.fontSize.toString
             , "updateMode"       -> view.updateMode.save.toString
             , "showTickCounter"  -> view.showTickCounter.toString
             , "frameRate"        -> view.frameRate.toString
             ) ++ ifDefined(view)("tickCounterLabel", _.tickCounterLabel)

        view.dimensions match {
          case d3d: WorldDimensions3D =>
            XMLElement("view3d", baseAttributes ++ Map(
                                   ("minPzcor" -> d3d.minPzcor.toString),
                                   ("maxPzcor" -> d3d.maxPzcor.toString),
                                   ("wrappingAllowedZ" -> d3d.wrappingAllowedInZ.toString)
                                 ), "", Seq())
          case d: WorldDimensions =>
            XMLElement("view", baseAttributes, "", Seq())
        }

      case monitor: Monitor =>
        val attributes =
          Map( "x"      -> monitor.x.toString
             , "y"       -> monitor.y.toString
             , "width"     -> monitor.width.toString
             , "height"    -> monitor.height.toString
             , "precision" -> monitor.precision.toString
             , "fontSize"  -> monitor.fontSize.toString
             ) ++
          ifDefined(monitor)("display", _.display) ++
          ifDefined(monitor)("units", _.units) ++
          (if (monitor.oldSize) Map("sizeVersion" -> "0") else Map())

        XMLElement("monitor", attributes, monitor.source.getOrElse(""), Seq())

      case switch: Switch =>
        val attributes =
          Map( "x"   -> switch.x.toString
             , "y"    -> switch.y.toString
             , "width"  -> switch.width.toString
             , "height" -> switch.height.toString
             , "on"     -> switch.on.toString
             ) ++
          ifDefined(switch)( "display",  _.display) ++
          ifDefined(switch)("variable", _.variable) ++
          (if (switch.oldSize) Map("sizeVersion" -> "0") else Map())

        XMLElement("switch", attributes, "", Seq())

      case plot: Plot =>

        val attributes =
          Map( "x"     -> plot.x.toString
             , "y"      -> plot.y.toString
             , "width"    -> plot.width.toString
             , "height"   -> plot.height.toString
             , "xMin"     -> plot.xmin.toString
             , "xMax"     -> plot.xmax.toString
             , "yMin"     -> plot.ymin.toString
             , "yMax"     -> plot.ymax.toString
             , "autoPlotX" -> plot.autoPlotX.toString
             , "autoPlotY" -> plot.autoPlotY.toString
             , "legend"   -> plot.legendOn.toString
             ) ++
          ifDefined(plot)("display", _.display) ++
          ifDefined(plot)(  "xAxis",   _.xAxis) ++
          ifDefined(plot)(  "yAxis",   _.yAxis) ++
          (if (plot.oldSize) Map("sizeVersion" -> "0") else Map())

        val children =
          Seq( XMLElement( "setup", Map(), plot. setupCode, Seq())
             , XMLElement("update", Map(), plot.updateCode, Seq())
             )

        val pens =
          plot.pens.map {
            pen =>

              val attrs =
                Map( "display"  -> pen.display
                   , "interval" -> pen.interval.toString
                   , "mode"     -> pen.mode.toString
                   , "color"    -> pen.color.toString
                   , "legend"   -> pen.inLegend.toString
                   )

              val kids =
                Seq( XMLElement( "setup", Map(), pen. setupCode, Seq())
                   , XMLElement("update", Map(), pen.updateCode, Seq())
                   )

              XMLElement("pen", attrs, "", kids)

          }

        XMLElement("plot", attributes, "", children ++ pens)

      case chooser: Chooser =>

        val attributes =
          Map( "x"    -> chooser.x.toString
             , "y"     -> chooser.y.toString
             , "width"   -> chooser.width.toString
             , "height"  -> chooser.height.toString
             , "current" -> chooser.currentChoice.toString
             ) ++
          ifDefined(chooser)( "display",  _.display) ++
          ifDefined(chooser)("variable", _.variable) ++
          (if (chooser.oldSize) Map("sizeVersion" -> "0") else Map())

        val children =
          chooser.choices.map {
            case ChooseableString(string) =>
              XMLElement("choice", Map("type" ->  "string", "value" ->           string), "", Seq())
            case ChooseableDouble(double) =>
              XMLElement("choice", Map("type" ->  "double", "value" ->  double.toString), "", Seq())
            case ChooseableBoolean(boolean) =>
              XMLElement("choice", Map("type" -> "boolean", "value" -> boolean.toString), "", Seq())
            case ChooseableList(list) =>
              val children = list.map((x) => XMLElement("value", Map("value" -> x.toString), "", Seq()))
              XMLElement("choice", Map("type" -> "list"), "", children)
          }

        XMLElement("chooser", attributes, "", children)

      case output: Output =>
        val attributes =
          Map( "x"     -> output.x.toString
             , "y"      -> output.y.toString
             , "width"    -> output.width.toString
             , "height"   -> output.height.toString
             , "fontSize" -> output.fontSize.toString
             )
        XMLElement("output", attributes, "", Seq())

      case input: InputBox =>

        val typeName =
          input.boxedValue match {
            case NumericInput(_, label) =>
              label match {
                case NumericInput.NumberLabel => "number"
                case NumericInput.ColorLabel  => "color"
              }
            case input: StringInput =>
              input.label match {
                case StringInput.StringLabel   => "string"
                case StringInput.ReporterLabel => "reporter"
                case StringInput.CommandLabel  => "command"
              }
          }

        val attributes =
          Map( "x"      -> input.x.toString
             , "y"       -> input.y.toString
             , "width"     -> input.width.toString
             , "height"    -> input.height.toString
             , "multiline" -> input.multiline.toString
             , "type"      -> typeName
             ) ++
          ifDefined(input)("variable", _.variable) ++
          (if (input.oldSize) Map("sizeVersion" -> "0") else Map())

        XMLElement("input", attributes, input.boxedValue.asString, Seq())

      case note: TextBox =>
        val attributes =
          Map( "x" -> note.x.toString
             , "y"  -> note.y.toString
             , "width" -> note.width.toString
             , "height" -> note.height.toString
             , "fontSize" -> note.fontSize.toString
             , "markdown" -> note.markdown.toString
             ) ++
          ifDefined(note)("textColorLight",   _.textColorLight) ++
          ifDefined(note)("textColorDark",     _.textColorDark) ++
          ifDefined(note)("backgroundLight", _.backgroundLight) ++
          ifDefined(note)("backgroundDark",   _.backgroundDark)

        XMLElement("note", attributes, note.display.getOrElse(""), Seq())

      case _ =>
        throw new IllegalStateException

    }

  }

}
