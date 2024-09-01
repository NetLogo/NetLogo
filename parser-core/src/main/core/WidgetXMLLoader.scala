// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

object WidgetXMLLoader {
  def readWidget(element: XMLElement, makeDimensions3D: (WorldDimensions, Int, Int, Boolean) =>
                 WorldDimensions): Widget = {
    element.name match {
      case "button" =>
        Button(element.attributes.get("source"), element.attributes("left").toInt,
               element.attributes("top").toInt, element.attributes("right").toInt,
               element.attributes("bottom").toInt, element.attributes.get("display"),
               element.attributes("forever").toBoolean, element.attributes("kind") match {
                                                          case "Observer" => AgentKind.Observer
                                                          case "Patch" => AgentKind.Patch
                                                          case "Turtle" => AgentKind.Turtle
                                                          case "Link" => AgentKind.Link
                                                        },
               element.attributes.get("actionKey").map(x => x(0)),
               element.attributes("disableUntilTicks").toBoolean)

      case "slider" =>
        Slider(element.attributes.get("variable"), element.attributes("left").toInt,
               element.attributes("top").toInt, element.attributes("right").toInt,
               element.attributes("bottom").toInt, element.attributes.get("display"),
               element.attributes("min"), element.attributes("max"),
               element.attributes("default").toDouble, element.attributes("step"),
               element.attributes.get("units"), element.attributes("direction") match {
                                                  case "Horizontal" => Horizontal
                                                  case "Vertical" => Vertical
                                                })

      case "view" =>
        View(element.attributes("left").toInt, element.attributes("top").toInt,
             element.attributes("right").toInt, element.attributes("bottom").toInt,
             new WorldDimensions(element.attributes("minPxcor").toInt, element.attributes("maxPxcor").toInt,
                                 element.attributes("minPycor").toInt, element.attributes("maxPycor").toInt,
                                 element.attributes("patchSize").toDouble,
                                 element.attributes("wrappingAllowedX").toBoolean,
                                 element.attributes("wrappingAllowedY").toBoolean),
             element.attributes("fontSize").toInt, UpdateMode.load(element.attributes("updateMode").toInt),
             element.attributes("showTickCounter").toBoolean, element.attributes.get("tickCounterLabel"),
             element.attributes("frameRate").toDouble)
      
      case "view3d" =>
        View(element.attributes("left").toInt, element.attributes("top").toInt,
             element.attributes("right").toInt, element.attributes("bottom").toInt,
             makeDimensions3D(new WorldDimensions(element.attributes("minPxcor").toInt,
                                                  element.attributes("maxPxcor").toInt,
                                                  element.attributes("minPycor").toInt,
                                                  element.attributes("maxPycor").toInt,
                                                  element.attributes("patchSize").toDouble,
                                                  element.attributes("wrappingAllowedX").toBoolean,
                                                  element.attributes("wrappingAllowedY").toBoolean),
                              element.attributes("minPzcor").toInt, element.attributes("maxPzcor").toInt,
                              element.attributes("wrappingAllowedZ").toBoolean),
             element.attributes("fontSize").toInt, UpdateMode.load(element.attributes("updateMode").toInt),
             element.attributes("showTickCounter").toBoolean, element.attributes.get("tickCounterLabel"),
             element.attributes("frameRate").toDouble)

      case "monitor" =>
        Monitor(element.attributes.get("source"), element.attributes("left").toInt,
                element.attributes("top").toInt, element.attributes("right").toInt,
                element.attributes("bottom").toInt, element.attributes.get("display"),
                element.attributes("precision").toInt, element.attributes("fontSize").toInt)

      case "switch" =>
        Switch(element.attributes.get("variable"), element.attributes("left").toInt,
               element.attributes("top").toInt, element.attributes("right").toInt,
               element.attributes("bottom").toInt, element.attributes.get("display"),
               element.attributes("on").toBoolean)

      case "plot" =>
        Plot(element.attributes.get("display"), element.attributes("left").toInt, element.attributes("top").toInt,
             element.attributes("right").toInt, element.attributes("bottom").toInt, element.attributes.get("xAxis"),
             element.attributes.get("yAxis"), element.attributes("xMin").toDouble,
             element.attributes("xMax").toDouble, element.attributes("yMin").toDouble,
             element.attributes("yMax").toDouble, element.attributes("autoplot").toBoolean,
             element.attributes("legend").toBoolean, element.attributes("setup"), element.attributes("update"),
             for (element <- element.children if element.name == "pen")
               yield Pen(element.attributes("display"), element.attributes("interval").toDouble,
                         element.attributes("mode").toInt, element.attributes("color").toInt,
                         element.attributes("legend").toBoolean, element.attributes("setup"),
                         element.attributes("update")))

      case "chooser" =>
        Chooser(element.attributes.get("variable"), element.attributes("left").toInt,
                element.attributes("top").toInt, element.attributes("right").toInt,
                element.attributes("bottom").toInt, element.attributes.get("display"),
                for (element <- element.children if element.name == "choice") yield {
                  element.attributes("type") match {
                    case "string" =>
                      ChooseableString(element.attributes("value"))
                    
                    case "double" =>
                      ChooseableDouble(element.attributes("value").toDouble)
                    
                    case "boolean" =>
                      ChooseableBoolean(element.attributes("value").toBoolean)
                    
                    case "list" =>
                      ChooseableList(LogoList.fromList(for (element <- element.children if element.name == "value")
                                                         yield element.attributes("value")))

                  }
                }, element.attributes("current").toInt)

      case "output" =>
        Output(element.attributes("left").toInt, element.attributes("top").toInt, element.attributes("right").toInt,
               element.attributes("bottom").toInt, element.attributes("fontSize").toInt)
      
      case "input" =>
        InputBox(element.attributes.get("variable"), element.attributes("left").toInt,
                 element.attributes("top").toInt, element.attributes("right").toInt,
                 element.attributes("bottom").toInt,
                 element.attributes("type") match {
                   case "number" =>
                     NumericInput(element.attributes("value").toDouble, NumericInput.NumberLabel)
                  
                   case "color" =>
                     NumericInput(element.attributes("value").toDouble, NumericInput.ColorLabel)
                  
                   case "string" =>
                     StringInput(element.attributes("value"), StringInput.StringLabel,
                                 element.attributes("multiline").toBoolean)
                  
                   case "reporter" =>
                     StringInput(element.attributes("value"), StringInput.ReporterLabel,
                                 element.attributes("multiline").toBoolean)
                  
                   case "command" =>
                     StringInput(element.attributes("value"), StringInput.CommandLabel,
                                 element.attributes("multiline").toBoolean)

                 })

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
        
        if (button.source.isDefined)
          attributes += (("source", button.source.get))
        
        if (button.actionKey.isDefined)
          attributes += (("actionKey", button.actionKey.get.toString))

        XMLElement("button", attributes, "", Nil)
      
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
        
        if (monitor.source.isDefined)
          attributes += (("source", monitor.source.get))

        XMLElement("monitor", attributes, "", Nil)
      
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
          ("legend", plot.legendOn.toString),
          ("setup", plot.setupCode),
          ("update", plot.updateCode)
        )

        if (plot.display.isDefined)
          attributes += (("display", plot.display.get))
        
        if (plot.xAxis.isDefined)
          attributes += (("xAxis", plot.xAxis.get))
        
        if (plot.yAxis.isDefined)
          attributes += (("yAxis", plot.yAxis.get))
        
        val children =
          for (pen <- plot.pens) yield {
            val attributes = Map[String, String](
              ("display", pen.display),
              ("interval", pen.interval.toString),
              ("mode", pen.mode.toString),
              ("color", pen.color.toString),
              ("legend", pen.inLegend.toString),
              ("setup", pen.setupCode),
              ("update", pen.updateCode)
            )

            XMLElement("pen", attributes, "", Nil)
          }

        XMLElement("plot", attributes, "", children)

      case chooser: Chooser =>
        var attributes = Map[String, String](
          ("left", chooser.left.toString),
          ("top", chooser.top.toString),
          ("right", chooser.right.toString),
          ("bottom", chooser.right.toString),
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
          ("multiline", input.multiline.toString),
          ("value", input.boxedValue.asString)
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

        XMLElement("input", attributes, "", Nil)

    }
  }
}
