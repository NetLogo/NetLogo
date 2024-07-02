// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import java.io.{ File, PrintWriter, StringReader, StringWriter, Writer }
import java.net.URI
import javax.xml.stream.{ XMLInputFactory, XMLOutputFactory, XMLStreamConstants }

import org.nlogo.core.{ AgentKind, Button, ChooseableBoolean, ChooseableDouble, ChooseableList, ChooseableString,
                        Chooser, Horizontal, InputBox, LogoList, Model, Monitor, NumericInput, OptionalSection, Output,
                        Pen, Plot, ShapeXMLLoader, Slider, StringInput, Switch, UpdateMode, Vertical, View, Widget,
                        WorldDimensions, XMLElement }
import org.nlogo.core.Shape.{ LinkShape, VectorShape }

import scala.io.Source
import scala.util.{ Failure, Success, Try }

// figure out where to use editNames based on LabLoader
class NLogoXMLLoader(editNames: Boolean) extends GenericModelLoader {
  lazy private val defaultInfo: String = FileIO.url2String("/system/empty-info.md")

  private def isCompatible(extension: String): Boolean =
    extension == "nlogo" || extension == "nlogo3d"

  private def isCompatible(uri: URI): Boolean = {
    val extension = GenericModelLoader.getURIExtension(uri)

    extension.isDefined && isCompatible(extension.get)
  }

  def readModel(uri: URI): Try[Model] = {
    readModel(if (uri.getScheme == "jar") {
                Source.fromInputStream(uri.toURL.openStream).mkString
              } else {
                Source.fromURI(uri).mkString
              }, GenericModelLoader.getURIExtension(uri).getOrElse(""))
  }
  
  def readModel(source: String, extension: String): Try[Model] = {
    if (isCompatible(extension)) {
      val reader = XMLInputFactory.newFactory.createXMLStreamReader(new StringReader(source))

      def readXMLElement(): XMLElement = {
        val name = reader.getLocalName
        val attributes = (for (i <- 0 until reader.getAttributeCount) yield
                            ((reader.getAttributeLocalName(i), reader.getAttributeValue(i)))).toMap
        var text = ""
        var children = List[XMLElement]()

        var end = false

        while (reader.hasNext && !end) {
          reader.next match {
            case XMLStreamConstants.START_ELEMENT =>
              children = children :+ readXMLElement

            case XMLStreamConstants.END_ELEMENT =>
              end = true
            
            case XMLStreamConstants.CHARACTERS =>
              text = reader.getText
            
            case _ =>
          }
        }

        XMLElement(name, attributes, text, children)
      }

      def readWidget(element: XMLElement): Widget = {
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
                  yield Pen(element.attributes("display"), element.attributes("interval").toInt,
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

      def readValueSet(element: XMLElement): RefValueSet = {
        element.name match {
          case "steppedValueSet" =>
            SteppedValueSet(element.attributes("variable"), element.attributes("first").toDouble,
                            element.attributes("step").toDouble, element.attributes("last").toDouble)

          case "enumeratedValueSet" =>
            RefEnumeratedValueSet(element.attributes("variable"), for (element <- element.children
                                                                       if element.name == "value")
                                                                    yield element.attributes("value").
                                                                            toDouble.asInstanceOf[AnyRef])

        }
      }

      var code: Option[String] = None
      var widgets = List[Widget]()
      var info: Option[String] = None
      var version = ""
      var turtleShapes: Option[List[VectorShape]] = None
      var linkShapes: Option[List[LinkShape]] = None
      var optionalSections = List[OptionalSection[_]]()

      reader.next

      val element = readXMLElement

      element.name match {
        case "model" =>
          version = element.attributes("version")

          for (element <- element.children) {
            element.name match {
              case "widgets" =>
                widgets = element.children.map(readWidget)
              
              case "info" =>
                info = Some(element.text)

              case "code" =>
                code = Some(element.text)
              
              case "turtleShapes" =>
                turtleShapes = Some(for (element <- element.children if element.name == "shape")
                                      yield ShapeXMLLoader.readShape(element))
              
              case "linkShapes" =>
                linkShapes = Some(for (element <- element.children if element.name == "shape")
                                    yield ShapeXMLLoader.readLinkShape(element))
              
              case "previewCommands" =>
                optionalSections = optionalSections :+
                  new OptionalSection[PreviewCommands]("org.nlogo.modelsection.previewcommands",
                                                       Some(PreviewCommands.Custom(element.text)),
                                                       PreviewCommands.Default)

              case "systemDynamics" =>
                throw new Exception("NetLogo XML does not yet support System Dynamics.")

              case "experiments" =>
                val experiments =
                  for (element <- element.children if element.name == "experiment") yield {
                    var preExperiment = ""
                    var setup = ""
                    var go = ""
                    var postRun = ""
                    var postExperiment = ""
                    var runMetricsCondition = ""
                    var exitCondition = ""
                    var metrics = List[String]()
                    var constants = List[RefValueSet]()
                    var subExperiments = List[List[RefValueSet]]()

                    for (element <- element.children) {
                      element.name match {
                        case "preExperiment" =>
                          preExperiment = element.text
                        
                        case "setup" =>
                          setup = element.text
                        
                        case "go" =>
                          go = element.text
                        
                        case "postRun" =>
                          postRun = element.text
                        
                        case "postExperiment" =>
                          postExperiment = element.text
                        
                        case "runMetricsCondition" =>
                          runMetricsCondition = element.text
                        
                        case "exitCondition" =>
                          exitCondition = element.text
                        
                        case "metrics" =>
                          metrics = for (element <- element.children if element.name == "metric") yield element.text
                        
                        case "constants" =>
                          constants = element.children.map(readValueSet)
                        
                        case "subExperiments" =>
                          subExperiments = for (element <- element.children if element.name == "subExperiment")
                                              yield element.children.map(readValueSet)

                      }
                    }

                    LabProtocol(element.attributes("name"), preExperiment, setup, go, postRun, postExperiment,
                                element.attributes("repetitions").toInt,
                                element.attributes("sequentialRunOrder").toBoolean,
                                element.attributes("runMetricsEveryStep").toBoolean, runMetricsCondition,
                                element.attributes.getOrElse("timeLimit", "0").toInt, exitCondition, metrics,
                                constants, subExperiments)
                  }

                optionalSections = optionalSections :+
                  new OptionalSection[Seq[LabProtocol]]("org.nlogo.modelsection.behaviorspace", Some(experiments),
                                                        Seq[LabProtocol]())

              case "hubNetClient" =>
                optionalSections = optionalSections :+
                  new OptionalSection[Seq[Widget]]("org.nlogo.modelsection.hubnetclient",
                                                   Some(element.children.map(readWidget)), Seq[Widget]())

              case "settings" =>
                optionalSections = optionalSections :+
                  new OptionalSection[ModelSettings]("org.nlogo.modelsection.modelsettings",
                                                     Some(ModelSettings(element.attributes("snapToGrid").toBoolean)),
                                                     ModelSettings(false))

              case "deltaTick" =>
                // not sure what this is

            }
          }

      }

      reader.close

      Success(Model(code.getOrElse(Model.defaultCode), widgets, info.getOrElse(defaultInfo), version,
                    turtleShapes.getOrElse(Model.defaultShapes), linkShapes.getOrElse(Model.defaultLinkShapes),
                    optionalSections))
    }

    else
      Failure(new Exception("Unable to open model with format \"" + extension + "\"."))
  }


  def saveToWriter(model: Model, destWriter: Writer) {
    val writer = XMLOutputFactory.newFactory.createXMLStreamWriter(destWriter)

    def writeWidget(widget: Widget) {
      widget match {
        case button: Button =>
          writer.writeStartElement("button")

          writer.writeAttribute("left", button.left.toString)
          writer.writeAttribute("top", button.top.toString)
          writer.writeAttribute("right", button.right.toString)
          writer.writeAttribute("bottom", button.bottom.toString)

          if (button.display.isDefined)
            writer.writeAttribute("display", button.display.get)
          
          if (button.source.isDefined)
            writer.writeAttribute("source", button.source.get)
          
          writer.writeAttribute("forever", button.forever.toString)
          writer.writeAttribute("kind", button.buttonKind.toString)
          
          if (button.actionKey.isDefined)
            writer.writeAttribute("actionKey", button.actionKey.get.toString)
          
          writer.writeAttribute("disableUntilTicks", button.disableUntilTicksStart.toString)

          writer.writeEndElement
        
        case slider: Slider =>
          writer.writeStartElement("slider")

          writer.writeAttribute("left", slider.left.toString)
          writer.writeAttribute("top", slider.top.toString)
          writer.writeAttribute("right", slider.right.toString)
          writer.writeAttribute("bottom", slider.bottom.toString)

          if (slider.display.isDefined)
            writer.writeAttribute("display", slider.display.get)
          
          if (slider.variable.isDefined)
            writer.writeAttribute("variable", slider.variable.get)
          
          writer.writeAttribute("min", slider.min)
          writer.writeAttribute("max", slider.max)
          writer.writeAttribute("default", slider.default.toString)
          writer.writeAttribute("step", slider.step)

          if (slider.units.isDefined)
            writer.writeAttribute("units", slider.units.get)
          
          writer.writeAttribute("direction", slider.direction.toString)

          writer.writeEndElement

        case view: View =>
          writer.writeStartElement("view")

          writer.writeAttribute("left", view.left.toString)
          writer.writeAttribute("top", view.top.toString)
          writer.writeAttribute("right", view.right.toString)
          writer.writeAttribute("bottom", view.bottom.toString)
          writer.writeAttribute("minPxcor", view.dimensions.minPxcor.toString)
          writer.writeAttribute("maxPxcor", view.dimensions.maxPxcor.toString)
          writer.writeAttribute("minPycor", view.dimensions.minPycor.toString)
          writer.writeAttribute("maxPycor", view.dimensions.maxPycor.toString)
          writer.writeAttribute("patchSize", view.dimensions.patchSize.toString)
          writer.writeAttribute("wrappingAllowedX", view.dimensions.wrappingAllowedInX.toString)
          writer.writeAttribute("wrappingAllowedY", view.dimensions.wrappingAllowedInY.toString)
          writer.writeAttribute("fontSize", view.fontSize.toString)
          writer.writeAttribute("updateMode", view.updateMode.save.toString)
          writer.writeAttribute("showTickCounter", view.showTickCounter.toString)

          if (view.tickCounterLabel.isDefined)
            writer.writeAttribute("tickCounterLabel", view.tickCounterLabel.get)

          writer.writeAttribute("frameRate", view.frameRate.toString)

          writer.writeEndElement

        case monitor: Monitor =>
          writer.writeStartElement("monitor")

          writer.writeAttribute("left", monitor.left.toString)
          writer.writeAttribute("top", monitor.top.toString)
          writer.writeAttribute("right", monitor.right.toString)
          writer.writeAttribute("bottom", monitor.bottom.toString)

          if (monitor.display.isDefined)
            writer.writeAttribute("display", monitor.display.get)
          
          if (monitor.source.isDefined)
            writer.writeAttribute("source", monitor.source.get)
          
          writer.writeAttribute("precision", monitor.precision.toString)
          writer.writeAttribute("fontSize", monitor.fontSize.toString)

          writer.writeEndElement
        
        case switch: Switch =>
          writer.writeStartElement("switch")

          writer.writeAttribute("left", switch.left.toString)
          writer.writeAttribute("top", switch.top.toString)
          writer.writeAttribute("right", switch.right.toString)
          writer.writeAttribute("bottom", switch.bottom.toString)

          if (switch.display.isDefined)
            writer.writeAttribute("display", switch.display.get)
          
          if (switch.variable.isDefined)
            writer.writeAttribute("variable", switch.variable.get)
          
          writer.writeAttribute("on", switch.on.toString)

          writer.writeEndElement
        
        case plot: Plot =>
          writer.writeStartElement("plot")

          writer.writeAttribute("left", plot.left.toString)
          writer.writeAttribute("top", plot.top.toString)
          writer.writeAttribute("right", plot.right.toString)
          writer.writeAttribute("bottom", plot.bottom.toString)

          if (plot.display.isDefined)
            writer.writeAttribute("display", plot.display.get)
          
          if (plot.xAxis.isDefined)
            writer.writeAttribute("xAxis", plot.xAxis.get)
          
          if (plot.yAxis.isDefined)
            writer.writeAttribute("yAxis", plot.yAxis.get)
          
          writer.writeAttribute("xMin", plot.xmin.toString)
          writer.writeAttribute("xMax", plot.xmax.toString)
          writer.writeAttribute("yMin", plot.ymin.toString)
          writer.writeAttribute("yMax", plot.ymax.toString)
          writer.writeAttribute("autoplot", plot.autoPlotOn.toString)
          writer.writeAttribute("legend", plot.legendOn.toString)
          writer.writeAttribute("setup", plot.setupCode)
          writer.writeAttribute("update", plot.updateCode)
          
          for (pen <- plot.pens) {
            writer.writeStartElement("pen")

            writer.writeAttribute("display", pen.display)
            writer.writeAttribute("interval", pen.interval.toString)
            writer.writeAttribute("mode", pen.mode.toString)
            writer.writeAttribute("color", pen.color.toString)
            writer.writeAttribute("legend", pen.inLegend.toString)
            writer.writeAttribute("setup", pen.setupCode)
            writer.writeAttribute("update", pen.updateCode)

            writer.writeEndElement
          }

          writer.writeEndElement

        case chooser: Chooser =>
          writer.writeStartElement("chooser")

          writer.writeAttribute("left", chooser.left.toString)
          writer.writeAttribute("top", chooser.top.toString)
          writer.writeAttribute("right", chooser.right.toString)
          writer.writeAttribute("bottom", chooser.right.toString)

          if (chooser.display.isDefined)
            writer.writeAttribute("display", chooser.display.get)
          
          if (chooser.variable.isDefined)
            writer.writeAttribute("variable", chooser.variable.get)

          writer.writeAttribute("current", chooser.currentChoice.toString)
          
          for (choice <- chooser.choices) {
            writer.writeStartElement("choice")

            choice match {
              case ChooseableString(string) =>
                writer.writeAttribute("type", "string")
                writer.writeAttribute("value", string)
              
              case ChooseableDouble(double) =>
                writer.writeAttribute("type", "double")
                writer.writeAttribute("value", double.toString)
              
              case ChooseableBoolean(boolean) =>
                writer.writeAttribute("type", "boolean")
                writer.writeAttribute("value", boolean.toString)
              
              case ChooseableList(list) =>
                writer.writeAttribute("type", "list")

                for (value <- list) {
                  writer.writeStartElement("value")
                  writer.writeAttribute("value", value.toString)
                  writer.writeEndElement
                }
            }

            writer.writeEndElement
          }

          writer.writeEndElement
        
        case output: Output =>
          writer.writeStartElement("output")

          writer.writeAttribute("left", output.left.toString)
          writer.writeAttribute("top", output.top.toString)
          writer.writeAttribute("right", output.right.toString)
          writer.writeAttribute("bottom", output.bottom.toString)
          writer.writeAttribute("fontSize", output.fontSize.toString)

          writer.writeEndElement
        
        case input: InputBox =>
          writer.writeStartElement("input")

          writer.writeAttribute("left", input.left.toString)
          writer.writeAttribute("top", input.top.toString)
          writer.writeAttribute("right", input.right.toString)
          writer.writeAttribute("bottom", input.bottom.toString)

          if (input.variable.isDefined)
            writer.writeAttribute("variable", input.variable.get)
          
          input.boxedValue match {
            case NumericInput(_, label) =>
              label match {
                case NumericInput.NumberLabel =>
                  writer.writeAttribute("type", "number")
                
                case NumericInput.ColorLabel =>
                  writer.writeAttribute("type", "color")

              }

            case input: StringInput =>
              input.label match {
                case StringInput.StringLabel =>
                  writer.writeAttribute("type", "string")
                
                case StringInput.ReporterLabel =>
                  writer.writeAttribute("type", "reporter")
                
                case StringInput.CommandLabel =>
                  writer.writeAttribute("type", "command")

              }

              writer.writeAttribute("multiline", input.multiline.toString)

          }

          writer.writeAttribute("value", input.boxedValue.asString)

          writer.writeEndElement

      }
    }

    def writeValueSet(valueSet: RefValueSet) {
      valueSet match {
        case stepped: SteppedValueSet =>
          writer.writeStartElement("steppedValueSet")

          writer.writeAttribute("variable", stepped.variableName)
          writer.writeAttribute("first", stepped.firstValue.toString)
          writer.writeAttribute("step", stepped.step.toString)
          writer.writeAttribute("last", stepped.lastValue.toString)

          writer.writeEndElement
        
        case enumerated: RefEnumeratedValueSet =>
          writer.writeStartElement("enumeratedValueSet")

          writer.writeAttribute("variable", enumerated.variableName)

          for (value <- enumerated) {
            writer.writeStartElement("value")
            writer.writeAttribute("value", value.toString)
            writer.writeEndElement
          }

          writer.writeEndElement
        
      }
    }

    writer.writeStartDocument

    writer.writeStartElement("model")

    writer.writeAttribute("version", model.version)

    writer.writeStartElement("widgets")

    for (widget <- model.widgets) {
      writeWidget(widget)
    }

    writer.writeEndElement

    if (model.info != defaultInfo) {
      writer.writeStartElement("info")
      writer.writeCData(model.info)
      writer.writeEndElement
    }

    if (model.code != Model.defaultCode) {
      writer.writeStartElement("code")
      writer.writeCData(model.code)
      writer.writeEndElement
    }

    if (model.turtleShapes != Model.defaultShapes) {
      writer.writeStartElement("turtleShapes")

      model.turtleShapes.foreach(shape => ShapeXMLLoader.writeShape(writer, shape))

      writer.writeEndElement
    }

    if (model.linkShapes != Model.defaultLinkShapes) {
      writer.writeStartElement("linkShapes")

      model.linkShapes.foreach(shape => ShapeXMLLoader.writeLinkShape(writer, shape))

      writer.writeEndElement
    }

    for (section <- model.optionalSections) {
      section.key match {
        case "org.nlogo.modelsection.previewcommands" =>
          val commands = section.get.get.asInstanceOf[PreviewCommands]

          if (commands != PreviewCommands.Default) {
            writer.writeStartElement("previewCommands")
            writer.writeCData(commands.source)
            writer.writeEndElement
          }

        case "org.nlogo.modelsection.systemdynamics.gui" =>
          throw new Exception("NetLogo XML does not yet support System Dynamics.")
        
        case "org.nlogo.modelsection.systemdynamics" =>
          // ignore, duplicate of previous case

        case "org.nlogo.modelsection.behaviorspace" =>
          val experiments = section.get.get.asInstanceOf[Seq[LabProtocol]]

          if (experiments.nonEmpty) {
            writer.writeStartElement("experiments")

            for (experiment <- experiments) {
              writer.writeStartElement("experiment")

              writer.writeAttribute("name", experiment.name)
              writer.writeAttribute("repetitions", experiment.repetitions.toString)
              writer.writeAttribute("sequentialRunOrder", experiment.sequentialRunOrder.toString)
              writer.writeAttribute("runMetricsEveryStep", experiment.runMetricsEveryStep.toString)

              if (experiment.timeLimit != 0) {
                writer.writeAttribute("timeLimit", experiment.timeLimit.toString)
              }

              if (experiment.preExperimentCommands.trim.nonEmpty) {
                writer.writeStartElement("preExperiment")
                writer.writeCData(experiment.preExperimentCommands.trim)
                writer.writeEndElement
              }

              if (experiment.setupCommands.trim.nonEmpty) {
                writer.writeStartElement("setup")
                writer.writeCData(experiment.setupCommands.trim)
                writer.writeEndElement
              }

              if (experiment.goCommands.trim.nonEmpty) {
                writer.writeStartElement("go")
                writer.writeCData(experiment.goCommands.trim)
                writer.writeEndElement
              }

              if (experiment.postRunCommands.trim.nonEmpty) {
                writer.writeStartElement("postRun")
                writer.writeCData(experiment.postRunCommands.trim)
                writer.writeEndElement
              }

              if (experiment.postExperimentCommands.trim.nonEmpty) {
                writer.writeStartElement("postExperiment")
                writer.writeCData(experiment.postExperimentCommands.trim)
                writer.writeEndElement
              }

              if (experiment.exitCondition.trim.nonEmpty) {
                writer.writeStartElement("exitCondition")
                writer.writeCData(experiment.exitCondition.trim)
                writer.writeEndElement
              }

              if (experiment.runMetricsCondition.trim.nonEmpty) {
                writer.writeStartElement("runMetricsCondition")
                writer.writeCData(experiment.runMetricsCondition.trim)
                writer.writeEndElement
              }

              if (experiment.metrics.nonEmpty) {
                writer.writeStartElement("metrics")

                for (metric <- experiment.metrics) {
                  writer.writeStartElement("metric")
                  writer.writeCData(metric)
                  writer.writeEndElement
                }

                writer.writeEndElement
              }

              if (experiment.constants.nonEmpty) {
                writer.writeStartElement("constants")

                experiment.constants.foreach(writeValueSet)

                writer.writeEndElement
              }

              for (subExperiment <- experiment.subExperiments) {
                writer.writeStartElement("subExperiment")

                subExperiment.foreach(writeValueSet)

                writer.writeEndElement
              }

              writer.writeEndElement
            }

            writer.writeEndElement
          }

        case "org.nlogo.modelsection.hubnetclient" =>
          val widgets = section.get.get.asInstanceOf[Seq[Widget]]

          if (widgets.nonEmpty) {
            writer.writeStartElement("hubNetClient")

            widgets.foreach(writeWidget)

            writer.writeEndElement
          }

        case "org.nlogo.modelsection.modelsettings" =>
          val settings = section.get.get.asInstanceOf[ModelSettings]

          if (settings.snapToGrid) {
            writer.writeStartElement("settings")
            writer.writeAttribute("snapToGrid", settings.snapToGrid.toString)
            writer.writeEndElement
          }

        case "org.nlogo.modelsection.deltatick" =>
          // not sure what this is

      }
    }

    writer.writeEndElement

    writer.writeEndDocument

    writer.close
  }

  def save(model: Model, uri: URI): Try[URI] = {
    if (isCompatible(uri)) {
      saveToWriter(model, new PrintWriter(new File(uri)))

      Success(uri)
    }

    else
      Failure(new Exception("Unable to save model with format \"" + GenericModelLoader.getURIExtension(uri) + "\"."))
  }

  def sourceString(model: Model, extension: String): Try[String] = {
    if (isCompatible(extension)) {
      val writer = new StringWriter

      saveToWriter(model, writer)

      Success(writer.toString)
    }

    else
      Failure(new Exception("Unable to create source string for model with format \"" + extension + "\"."))
  }

  def emptyModel(extension: String): Model = {
    if (isCompatible(extension)) {
      Model(Model.defaultCode, List(View(left = 210, top = 10, right = 649, bottom = 470,
                                   dimensions = WorldDimensions(-16, 16, -16, 16, 13.0), fontSize = 10,
                                   updateMode = UpdateMode.Continuous, showTickCounter = true, frameRate = 30)),
            defaultInfo, "NetLogo 6.4.0", Model.defaultShapes, Model.defaultLinkShapes)
    }

    else
      throw new Exception("Unable to create empty model with format \"" + extension + "\".")
  }
}
