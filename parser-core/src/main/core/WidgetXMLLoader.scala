// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object WidgetXMLLoader {
  def readWidget(element: XMLElement, makeDimensions3D: (WorldDimensions, Int, Int, Boolean) =>
                 WorldDimensions): Widget = {

    element.name match {

      case "button" =>
        val kind =
          element("kind") match {
            case "Observer" => AgentKind.Observer
            case "Patch"    => AgentKind.Patch
            case "Turtle"   => AgentKind.Turtle
            case "Link"     => AgentKind.Link
          }
        Button( element.getOptionalChild("source").map(_.text), element("left").toInt, element("top").toInt
              , element("right").toInt, element("bottom").toInt, element.get("display"), element("forever").toBoolean
              , kind, element.get("actionKey").map(_.head), element("disableUntilTicks").toBoolean
              )

      case "slider" =>
        val direction =
          element("direction") match {
            case "Horizontal" => Horizontal
            case "Vertical"   => Vertical
          }
        Slider( element.get("variable"), element("left").toInt, element("top").toInt, element("right").toInt
              , element("bottom").toInt, element.get("display"), element("min"), element("max")
              , element("default").toDouble, element("step"), element.get("units"), direction
              )

      case "view" =>
        val dims =
          new WorldDimensions( element("minPxcor").toInt, element("maxPxcor").toInt, element("minPycor").toInt
                             , element("maxPycor").toInt, element("patchSize").toDouble
                             , element("wrappingAllowedX").toBoolean, element("wrappingAllowedY").toBoolean
                             )
        View( element("left").toInt, element("top").toInt, element("right").toInt, element("bottom").toInt, dims
            , element("fontSize").toInt, UpdateMode.load(element("updateMode").toInt)
            , element("showTickCounter").toBoolean, element.get("tickCounterLabel"), element("frameRate").toDouble
            )

      case "view3d" =>
        val dims =
          new WorldDimensions( element("minPxcor").toInt, element("maxPxcor").toInt
                             , element("minPycor").toInt, element("maxPycor").toInt
                             , element("patchSize").toDouble, element("wrappingAllowedX").toBoolean
                             , element("wrappingAllowedY").toBoolean
                             )
        val dims3D =
          makeDimensions3D(dims, element("minPzcor").toInt, element("maxPzcor").toInt
                          , element("wrappingAllowedZ").toBoolean
                          )
        View( element("left").toInt, element("top").toInt, element("right").toInt, element("bottom").toInt, dims3D
            , element("fontSize").toInt, UpdateMode.load(element("updateMode").toInt)
            , element("showTickCounter").toBoolean, element.get("tickCounterLabel"), element("frameRate").toDouble
            )

      case "monitor" =>
        Monitor( element.getOptionalChild("source").map(_.text), element("left").toInt, element("top").toInt
               , element("right").toInt, element("bottom").toInt, element.get("display"), element("precision").toInt
               , element("fontSize").toInt
               )

      case "switch" =>
        Switch( element.get("variable"), element("left").toInt, element("top").toInt, element("right").toInt
              , element("bottom").toInt, element.get("display"), element("on").toBoolean
              )

      case "plot" =>
        val pens =
          element.getChildren("pen").map(
            el =>
               Pen( el("display"), el("interval").toDouble, el("mode").toInt, el("color").toInt
                  , el("legend").toBoolean, el.getChild("setup").text, el.getChild("update").text
                  )
          ).toList
        Plot( element.get("display"), element("left").toInt, element("top").toInt, element("right").toInt
            , element("bottom").toInt, element.get("xAxis"), element.get("yAxis"), element("xMin").toDouble
            , element("xMax").toDouble, element("yMin").toDouble, element("yMax").toDouble
            , element("autoplot").toBoolean, element("legend").toBoolean, element.getChild("setup").text
            , element.getChild("update").text, pens
            )

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
        Chooser( element.get("variable"), element("left").toInt, element("top").toInt, element("right").toInt
               , element("bottom").toInt, element.get("display"), children.toList, element("current").toInt
               )

      case "output" =>
        Output( element("left").toInt, element("top").toInt, element("right").toInt, element("bottom").toInt
              , element("fontSize").toInt
              )

      case "input" =>
        val value = element.getChild("value").text
        val input =
          element("type") match {
            case "number" =>
              NumericInput(value.toDouble, NumericInput.NumberLabel)
            case "color" =>
              NumericInput(value.toDouble, NumericInput.ColorLabel)
            case "string" =>
              StringInput(value, StringInput.StringLabel, element("multiline").toBoolean)
            case "reporter" =>
              StringInput(value, StringInput.ReporterLabel, element("multiline").toBoolean)
            case "command" =>
              StringInput(value, StringInput.CommandLabel, element("multiline").toBoolean)
          }
        InputBox( element.get("variable"), element("left").toInt, element("top").toInt, element("right").toInt
                , element("bottom").toInt, input
                )

      case "image" =>
        Image( element("left").toInt, element("top").toInt, element("right").toInt, element("bottom").toInt
             , element("image"), element("preserveAspect").toBoolean
             )

    }
  }

  def writeWidget(widget: Widget): XMLElement = {

    def ifDefined[T <: Widget](w: T)(key: String, f: (T) => Option[Any]): Map[String, String] =
      if (f(w).isDefined) Map(key -> f(w).get.toString) else Map()

    widget match {

      case button: Button =>
        val attributes =
          Map( "left"              -> button.left.toString
             , "top"               -> button.top.toString
             , "right"             -> button.right.toString
             , "bottom"            -> button.bottom.toString
             , "forever"           -> button.forever.toString
             , "kind"              -> button.buttonKind.toString
             , "disableUntilTicks" -> button.disableUntilTicksStart.toString
             ) ++
          ifDefined(button)(  "display",   _.display) ++
          ifDefined(button)("actionKey", _.actionKey)

        val children =
          if (button.source.isDefined)
            Seq(XMLElement("source", Map(), button.source.get, Seq()))
          else
            Seq()

        XMLElement("button", attributes, "", children)

      case slider: Slider =>
        val attributes =
          Map( "left"      -> slider.left.toString
             , "top"       -> slider.top.toString
             , "right"     -> slider.right.toString
             , "bottom"    -> slider.bottom.toString
             , "min"       -> slider.min
             , "max"       -> slider.max
             , "default"   -> slider.default.toString
             , "step"      -> slider.step
             , "direction" -> slider.direction.toString
             ) ++
          ifDefined(slider)( "display",  _.display) ++
          ifDefined(slider)("variable", _.variable) ++
          ifDefined(slider)(   "units",    _.units)
        XMLElement("slider", attributes, "", Seq())

      case view: View =>

        val extraVars = view.dimensions.extras

        val baseAttributes =
          Map( "left"             -> view.left.toString
             , "top"              -> view.top.toString
             , "right"            -> view.right.toString
             , "bottom"           -> view.bottom.toString
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
             )

        val attributes =
          baseAttributes ++
            extraVars.get(          "minPzcor").map((z)   => "minPzcor"         ->   z.asInstanceOf[ String].toString).toMap ++
            extraVars.get(          "maxPzcor").map((z)   => "maxPzcor"         ->   z.asInstanceOf[ String].toString).toMap ++
            extraVars.get("wrappingAllowedInZ").map((isW) => "wrappingAllowedZ" -> isW.asInstanceOf[Boolean].toString).toMap ++
            ifDefined(view)("tickCounterLabel", _.tickCounterLabel)

        val nodeType = if (!extraVars.contains("minPzcor")) "view" else "view3d"

        XMLElement(nodeType, attributes, "", Seq())

      case monitor: Monitor =>
        val attributes =
          Map( "left"      -> monitor.left.toString
             , "top"       -> monitor.top.toString
             , "right"     -> monitor.right.toString
             , "bottom"    -> monitor.bottom.toString
             , "precision" -> monitor.precision.toString
             , "fontSize"  -> monitor.fontSize.toString
             ) ++
          ifDefined(monitor)("display", _.display)

        val children =
          if (monitor.source.isDefined)
            List(XMLElement("source", Map(), monitor.source.get, Seq()))
          else
            Seq()

        XMLElement("monitor", attributes, "", children)

      case switch: Switch =>
        val attributes =
          Map( "left"   -> switch.left.toString
             , "top"    -> switch.top.toString
             , "right"  -> switch.right.toString
             , "bottom" -> switch.bottom.toString
             , "on"     -> switch.on.toString
             ) ++
          ifDefined(switch)( "display",  _.display) ++
          ifDefined(switch)("variable", _.variable)

        XMLElement("switch", attributes, "", Seq())

      case plot: Plot =>

        val attributes =
          Map( "left"     -> plot.left.toString
             , "top"      -> plot.top.toString
             , "right"    -> plot.right.toString
             , "bottom"   -> plot.bottom.toString
             , "xMin"     -> plot.xmin.toString
             , "xMax"     -> plot.xmax.toString
             , "yMin"     -> plot.ymin.toString
             , "yMax"     -> plot.ymax.toString
             , "autoplot" -> plot.autoPlotOn.toString
             , "legend"   -> plot.legendOn.toString
             ) ++
          ifDefined(plot)("display", _.display) ++
          ifDefined(plot)(  "xAxis",   _.xAxis) ++
          ifDefined(plot)(  "yAxis",   _.yAxis)

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
          Map( "left"    -> chooser.left.toString
             , "top"     -> chooser.top.toString
             , "right"   -> chooser.right.toString
             , "bottom"  -> chooser.bottom.toString
             , "current" -> chooser.currentChoice.toString
             ) ++
          ifDefined(chooser)( "display",  _.display) ++
          ifDefined(chooser)("variable", _.variable)

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
          Map( "left"     -> output.left.toString
             , "top"      -> output.top.toString
             , "right"    -> output.right.toString
             , "bottom"   -> output.bottom.toString
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
          Map( "left"      -> input.left.toString
             , "top"       -> input.top.toString
             , "right"     -> input.right.toString
             , "bottom"    -> input.bottom.toString
             , "multiline" -> input.multiline.toString
             , "type"      -> typeName
             ) ++
          ifDefined(input)("variable", _.variable)

        val children = List(XMLElement("value", Map(), input.boxedValue.asString, Seq()))

        XMLElement("input", attributes, "", children)

      case image: Image =>
        val attributes =
          Map( "left"           -> image.left.toString
             , "top"            -> image.top.toString
             , "right"          -> image.right.toString
             , "bottom"         -> image.bottom.toString
             , "image"          -> image.image
             , "preserveAspect" -> image.preserveAspect.toString
          )

        XMLElement("image", attributes, "", Seq())

    }

  }

}
