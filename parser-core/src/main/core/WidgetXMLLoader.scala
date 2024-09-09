// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object WidgetXMLLoader {
  def readWidget(element: XMLElement, makeDimensions3D: (WorldDimensions, Int, Int, Boolean) =>
                 WorldDimensions): Widget = {
    element.name match {
      case "button" =>
        Button(element.getOptionalChild("source").map(_.text), element("left").toInt, element("top").toInt,
               element("right").toInt, element("bottom").toInt, element.get("display"), element("forever").toBoolean,
               element("kind") match {
                 case "Observer" => AgentKind.Observer
                 case "Patch" => AgentKind.Patch
                 case "Turtle" => AgentKind.Turtle
                 case "Link" => AgentKind.Link
               }, element.get("actionKey").map(x => x(0)), element("disableUntilTicks").toBoolean)

      case "slider" =>
        Slider(element.get("variable"), element("left").toInt, element("top").toInt, element("right").toInt,
               element("bottom").toInt, element.get("display"), element("min"), element("max"),
               element("default").toDouble, element("step"), element.get("units"),
               element("direction") match {
                 case "Horizontal" => Horizontal
                 case "Vertical" => Vertical
               })

      case "view" =>
        View(element("left").toInt, element("top").toInt, element("right").toInt, element("bottom").toInt,
             new WorldDimensions(element("minPxcor").toInt, element("maxPxcor").toInt, element("minPycor").toInt,
                                 element("maxPycor").toInt, element("patchSize").toDouble,
                                 element("wrappingAllowedX").toBoolean, element("wrappingAllowedY").toBoolean),
             element("fontSize").toInt, UpdateMode.load(element("updateMode").toInt),
             element("showTickCounter").toBoolean, element.get("tickCounterLabel"), element("frameRate").toDouble)
      
      case "view3d" =>
        View(element("left").toInt, element("top").toInt, element("right").toInt, element("bottom").toInt,
             makeDimensions3D(new WorldDimensions(element("minPxcor").toInt, element("maxPxcor").toInt,
                                                  element("minPycor").toInt, element("maxPycor").toInt,
                                                  element("patchSize").toDouble, element("wrappingAllowedX").toBoolean,
                                                  element("wrappingAllowedY").toBoolean),
                              element("minPzcor").toInt, element("maxPzcor").toInt,
                              element("wrappingAllowedZ").toBoolean),
             element("fontSize").toInt, UpdateMode.load(element("updateMode").toInt),
             element("showTickCounter").toBoolean, element.get("tickCounterLabel"), element("frameRate").toDouble)

      case "monitor" =>
        Monitor(element.getOptionalChild("source").map(_.text), element("left").toInt, element("top").toInt,
                element("right").toInt, element("bottom").toInt, element.get("display"), element("precision").toInt,
                element("fontSize").toInt)

      case "switch" =>
        Switch(element.get("variable"), element("left").toInt, element("top").toInt, element("right").toInt,
               element("bottom").toInt, element.get("display"), element("on").toBoolean)

      case "plot" =>
        Plot(element.get("display"), element("left").toInt, element("top").toInt, element("right").toInt,
             element("bottom").toInt, element.get("xAxis"), element.get("yAxis"), element("xMin").toDouble,
             element("xMax").toDouble, element("yMin").toDouble, element("yMax").toDouble,
             element("autoplot").toBoolean, element("legend").toBoolean, element.getChild("setup").text,
             element.getChild("update").text,
             element.getChildren("pen").map(element =>
               Pen(element("display"), element("interval").toDouble, element("mode").toInt, element("color").toInt,
                   element("legend").toBoolean, element.getChild("setup").text, element.getChild("update").text)))

      case "chooser" =>
        Chooser(element.get("variable"), element("left").toInt, element("top").toInt, element("right").toInt,
                element("bottom").toInt, element.get("display"),
                element.getChildren("choice").map(element => {
                  element("type") match {
                    case "string" =>
                      ChooseableString(element("value"))
                    
                    case "double" =>
                      ChooseableDouble(element("value").toDouble)
                    
                    case "boolean" =>
                      ChooseableBoolean(element("value").toBoolean)
                    
                    case "list" =>
                      ChooseableList(LogoList.fromList(element.getChildren("value").map(element => element("value"))))

                  }
                }), element("current").toInt)

      case "output" =>
        Output(element("left").toInt, element("top").toInt, element("right").toInt, element("bottom").toInt,
               element("fontSize").toInt)
      
      case "input" =>
        InputBox(element.get("variable"), element("left").toInt, element("top").toInt, element("right").toInt,
                 element("bottom").toInt,
                 element("type") match {
                   case "number" =>
                     NumericInput(element.getChild("value").text.toDouble, NumericInput.NumberLabel)
                  
                   case "color" =>
                     NumericInput(element.getChild("value").text.toDouble, NumericInput.ColorLabel)
                  
                   case "string" =>
                     StringInput(element.getChild("value").text, StringInput.StringLabel,
                                 element("multiline").toBoolean)
                  
                   case "reporter" =>
                     StringInput(element.getChild("value").text, StringInput.ReporterLabel,
                                 element("multiline").toBoolean)
                  
                   case "command" =>
                     StringInput(element.getChild("value").text, StringInput.CommandLabel,
                                 element("multiline").toBoolean)

                 })

      case "image" =>
        Image(element("left").toInt, element("top").toInt, element("right").toInt, element("bottom").toInt,
              element("image"))

    }
  }

  def writeWidget(widget: Widget): XMLElement = {
    widget match {
      case button: Button =>
        var attributes = Map[String, String](
          ("left", button.left.toString),
          ("top", button.top.toString),
          ("right", button.right.toString),
          ("bottom", button.bottom.toString),
          ("forever", button.forever.toString),
          ("kind", button.buttonKind.toString),
          ("disableUntilTicks", button.disableUntilTicksStart.toString)
        )

        if (button.display.isDefined)
          attributes += (("display", button.display.get))
        
        if (button.actionKey.isDefined)
          attributes += (("actionKey", button.actionKey.get.toString))
        
        val children =
          if (button.source.isDefined)
            List(XMLElement("source", Map(), button.source.get, Nil))
          else
            Nil

        XMLElement("button", attributes, "", children)
      
      case slider: Slider =>
        var attributes = Map[String, String](
          ("left", slider.left.toString),
          ("top", slider.top.toString),
          ("right", slider.right.toString),
          ("bottom", slider.bottom.toString),
          ("min", slider.min),
          ("max", slider.max),
          ("default", slider.default.toString),
          ("step", slider.step),
          ("direction", slider.direction.toString)
        )

        if (slider.display.isDefined)
          attributes += (("display", slider.display.get))
        
        if (slider.variable.isDefined)
          attributes += (("variable", slider.variable.get))
        
        if (slider.units.isDefined)
          attributes += (("units", slider.units.get))

        XMLElement("slider", attributes, "", Nil)

      case view: View =>
        view.dimensions.get3D match {
          case Some((minPzcor, maxPzcor, wrappingAllowedInZ)) =>
            var attributes = Map[String, String](
              ("left", view.left.toString),
              ("top", view.top.toString),
              ("right", view.right.toString),
              ("bottom", view.bottom.toString),
              ("minPxcor", view.dimensions.minPxcor.toString),
              ("maxPxcor", view.dimensions.maxPxcor.toString),
              ("minPycor", view.dimensions.minPycor.toString),
              ("maxPycor", view.dimensions.maxPycor.toString),
              ("minPzcor", minPzcor.toString),
              ("maxPzcor", maxPzcor.toString),
              ("patchSize", view.dimensions.patchSize.toString),
              ("wrappingAllowedX", view.dimensions.wrappingAllowedInX.toString),
              ("wrappingAllowedY", view.dimensions.wrappingAllowedInY.toString),
              ("wrappingAllowedZ", wrappingAllowedInZ.toString),
              ("fontSize", view.fontSize.toString),
              ("updateMode", view.updateMode.save.toString),
              ("showTickCounter", view.showTickCounter.toString),
              ("frameRate", view.frameRate.toString)
            )

            if (view.tickCounterLabel.isDefined)
              attributes += (("tickCounterLabel", view.tickCounterLabel.get))

            XMLElement("view3d", attributes, "", Nil)
          
          case None =>
            var attributes = Map[String, String](
              ("left", view.left.toString),
              ("top", view.top.toString),
              ("right", view.right.toString),
              ("bottom", view.bottom.toString),
              ("minPxcor", view.dimensions.minPxcor.toString),
              ("maxPxcor", view.dimensions.maxPxcor.toString),
              ("minPycor", view.dimensions.minPycor.toString),
              ("maxPycor", view.dimensions.maxPycor.toString),
              ("patchSize", view.dimensions.patchSize.toString),
              ("wrappingAllowedX", view.dimensions.wrappingAllowedInX.toString),
              ("wrappingAllowedY", view.dimensions.wrappingAllowedInY.toString),
              ("fontSize", view.fontSize.toString),
              ("updateMode", view.updateMode.save.toString),
              ("showTickCounter", view.showTickCounter.toString),
              ("frameRate", view.frameRate.toString)
            )

            if (view.tickCounterLabel.isDefined)
              attributes += (("tickCounterLabel", view.tickCounterLabel.get))

            XMLElement("view", attributes, "", Nil)
        }

      case monitor: Monitor =>
        var attributes = Map[String, String](
          ("left", monitor.left.toString),
          ("top", monitor.top.toString),
          ("right", monitor.right.toString),
          ("bottom", monitor.bottom.toString),
          ("precision", monitor.precision.toString),
          ("fontSize", monitor.fontSize.toString)
        )

        if (monitor.display.isDefined)
          attributes += (("display", monitor.display.get))
        
        val children =
          if (monitor.source.isDefined)
            List(XMLElement("source", Map(), monitor.source.get, Nil))
          else
            Nil

        XMLElement("monitor", attributes, "", children)
      
      case switch: Switch =>
        var attributes = Map[String, String](
          ("left", switch.left.toString),
          ("top", switch.top.toString),
          ("right", switch.right.toString),
          ("bottom", switch.bottom.toString),
          ("on", switch.on.toString)
        )

        if (switch.display.isDefined)
          attributes += (("display", switch.display.get))
        
        if (switch.variable.isDefined)
          attributes += (("variable", switch.variable.get))

        XMLElement("switch", attributes, "", Nil)
      
      case plot: Plot =>
        var attributes = Map[String, String](
          ("left", plot.left.toString),
          ("top", plot.top.toString),
          ("right", plot.right.toString),
          ("bottom", plot.bottom.toString),
          ("xMin", plot.xmin.toString),
          ("xMax", plot.xmax.toString),
          ("yMin", plot.ymin.toString),
          ("yMax", plot.ymax.toString),
          ("autoplot", plot.autoPlotOn.toString),
          ("legend", plot.legendOn.toString)
        )

        if (plot.display.isDefined)
          attributes += (("display", plot.display.get))
        
        if (plot.xAxis.isDefined)
          attributes += (("xAxis", plot.xAxis.get))
        
        if (plot.yAxis.isDefined)
          attributes += (("yAxis", plot.yAxis.get))
        
        
        var children = List(
          XMLElement("setup", Map(), plot.setupCode, Nil),
          XMLElement("update", Map(), plot.updateCode, Nil)
        )

        for (pen <- plot.pens) {
          val attributes = Map[String, String](
            ("display", pen.display),
            ("interval", pen.interval.toString),
            ("mode", pen.mode.toString),
            ("color", pen.color.toString),
            ("legend", pen.inLegend.toString)
          )

          val penChildren = List(
            XMLElement("setup", Map(), pen.setupCode, Nil),
            XMLElement("update", Map(), pen.updateCode, Nil)
          )

          children = children :+ XMLElement("pen", attributes, "", penChildren)
        }

        XMLElement("plot", attributes, "", children)

      case chooser: Chooser =>
        var attributes = Map[String, String](
          ("left", chooser.left.toString),
          ("top", chooser.top.toString),
          ("right", chooser.right.toString),
          ("bottom", chooser.bottom.toString),
          ("current", chooser.currentChoice.toString)
        )

        if (chooser.display.isDefined)
          attributes += (("display", chooser.display.get))
        
        if (chooser.variable.isDefined)
          attributes += (("variable", chooser.variable.get))
        
        val children =
          for (choice <- chooser.choices) yield {
            choice match {
              case ChooseableString(string) =>
                val attributes = Map[String, String](
                  ("type", "string"),
                  ("value", string)
                )

                XMLElement("choice", attributes, "", Nil)
              
              case ChooseableDouble(double) =>
                val attributes = Map[String, String](
                  ("type", "double"),
                  ("value", double.toString)
                )
                
                XMLElement("choice", attributes, "", Nil)
              
              case ChooseableBoolean(boolean) =>
                val attributes = Map[String, String](
                  ("type", "boolean"),
                  ("value", boolean.toString)
                )

                XMLElement("choice", attributes, "", Nil)
              
              case ChooseableList(list) =>
                val attributes = Map[String, String](
                  ("type", "list")
                )

                val children =
                  for (value <- list) yield {
                    val attributes = Map[String, String](
                      ("value", value.toString)
                    )

                    XMLElement("value", attributes, "", Nil)
                  }
                
                XMLElement("choice", attributes, "", children.toList)

            }
          }

        XMLElement("chooser", attributes, "", children)
      
      case output: Output =>
        val attributes = Map[String, String](
          ("left", output.left.toString),
          ("top", output.top.toString),
          ("right", output.right.toString),
          ("bottom", output.bottom.toString),
          ("fontSize", output.fontSize.toString)
        )

        XMLElement("output", attributes, "", Nil)
      
      case input: InputBox =>
        var attributes = Map[String, String](
          ("left", input.left.toString),
          ("top", input.top.toString),
          ("right", input.right.toString),
          ("bottom", input.bottom.toString),
          ("multiline", input.multiline.toString)
        )

        if (input.variable.isDefined)
          attributes += (("variable", input.variable.get))
        
        input.boxedValue match {
          case NumericInput(_, label) =>
            label match {
              case NumericInput.NumberLabel =>
                attributes += (("type", "number"))
              
              case NumericInput.ColorLabel =>
                attributes += (("type", "color"))

            }

          case input: StringInput =>
            input.label match {
              case StringInput.StringLabel =>
                attributes += (("type", "string"))
              
              case StringInput.ReporterLabel =>
                attributes += (("type", "reporter"))
              
              case StringInput.CommandLabel =>
                attributes += (("type", "command"))

            }

        }

        val children = List(XMLElement("value", Map(), input.boxedValue.asString, Nil))

        XMLElement("input", attributes, "", children)
      
      case image: Image =>
        val attributes = Map(
          ("left", image.left.toString),
          ("top", image.top.toString),
          ("right", image.right.toString),
          ("bottom", image.bottom.toString),
          ("image", image.image)
        )

        XMLElement("image", attributes, "", Nil)

    }
  }
}
