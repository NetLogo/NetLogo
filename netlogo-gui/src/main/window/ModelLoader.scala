// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.swing.BrowserLauncher
import java.awt.Container
import org.nlogo.window.Events._
import org.nlogo.api.{ CompilerServices, ModelType, ModelReader, ModelSection, Version}
import org.nlogo.core.I18N
import org.nlogo.core.model.WidgetReader
import org.nlogo.fileformat

object ModelLoader {
  val TRANSITION_URL = "http://ccl.northwestern.edu/netlogo/5.0/docs/transition.html"

  @throws(classOf[InvalidVersionException])
  def load(linkParent: Container, modelPath: String,
           modelType: ModelType, map: java.util.Map[ModelSection, Array[String]],
           compilerServices: CompilerServices) {
    Loader(linkParent).loadHelper(modelPath, modelType, map, compilerServices)
  }

  @throws(classOf[InvalidVersionException])
  def load(linkParent: Container, modelPath: String, modelType: ModelType, source: String, compilerServices: CompilerServices) {
    Loader(linkParent).loadHelper(modelPath, modelType, ModelReader.parseModel(source), compilerServices)
  }

  private case class Loader(linkParent: Container) extends org.nlogo.window.Event.LinkChild {
    def getLinkParent = linkParent

    @throws(classOf[InvalidVersionException])
    def loadHelper(modelPath: String, modelType: ModelType, map: java.util.Map[ModelSection, Array[String]], compilerServices: CompilerServices) {
      if (map == null) throw new InvalidVersionException()
      val version = ModelReader.parseVersion(map)
      if (version == null || !version.startsWith("NetLogo")) throw new InvalidVersionException()

      def decideToContinueBasedOnVersion() = {
        // we don't want to show this warning if we're opening a 2D model in
        // 3D or vice versa because we have different, better warnings. ev 11/1/07
        val needsWarning = Version.is3D == Version.is3D(version) && !Version.compatibleVersion(version)
        type Decision = Boolean
        val Continue = true
        val Cancel = false
        if (needsWarning) {
          def showVersionWarningAndGetResponse(): Decision = {
            val response = {
              val message = "This model was created in " + version +
                      ". You may need to make changes for it to work in " +
                      org.nlogo.api.Version.version + "."
              val options = Array[Object](I18N.gui.get("common.buttons.continue"), "Open Transition Guide", I18N.gui.get("common.buttons.cancel"))
              org.nlogo.swing.OptionDialog.show(linkParent, I18N.gui.get("common.messages.warning"), message, options)
            }
            response match {
              case 0 => Continue
              case 1 =>
                // Open Transition Guide
                BrowserLauncher.openURL(linkParent, TRANSITION_URL, false)
                showVersionWarningAndGetResponse()
              case _ => Cancel
            }
          }
          showVersionWarningAndGetResponse()
        } else Continue
      }
      // if we need to show a the version warning, show it, and get their response.
      // if they want to proceed anyway, then allow them to. if not, dont fire the load events.
      // if we dont need to show the version warning, great, just proceed.
      if (decideToContinueBasedOnVersion()) {
        val beforeEvents = List(
          new BeforeLoadEvent(modelPath, modelType),
          new LoadBeginEvent())

        val sectionTypes = List(
          ModelSection.PreviewCommands,
          ModelSection.Code,
          ModelSection.Info,
          ModelSection.Interface,
          ModelSection.SystemDynamics,
          ModelSection.TurtleShapes,
          ModelSection.BehaviorSpace,
          ModelSection.HubNetClient,
          ModelSection.LinkShapes,
          ModelSection.ModelSettings)  // snap to grid

        val loadSectionEvents = sectionTypes.map { section => // kludgey - ST 2/11/08
          (section, map.get(section).length) match {
            // Kludge: If the shapes section is empty, give it the default shapes on open
            // - ST 9/2/03
            case (ModelSection.TurtleShapes, 0) =>
              val lines = ModelReader.defaultShapes
              new LoadSectionEvent(version, section, lines, lines.mkString("\n"))
            case (ModelSection.LinkShapes, 0) =>
              val lines = ModelReader.defaultLinkShapes
              new LoadSectionEvent(version, section, lines, lines.mkString("\n"))
            // Kludge: pre-4.1 model files have org.nlogo.aggregate.gui
            // instead of org.nlogo.sdm.gui, translate on the fly - ST 2/18/08
            case (ModelSection.SystemDynamics, _) =>
              val newLines =
                map.get(section).map(_.replaceAll("org.nlogo.aggregate.gui", "org.nlogo.sdm.gui"))
              new LoadSectionEvent(version, section, newLines, newLines.mkString("\n"))
            case (ModelSection.Interface, _) =>
              val lines = map.get(section)
              val additionalReaders = fileformat.nlogoReaders(Version.is3D(version))
              val widgets = WidgetReader.readInterface(lines.toList, compilerServices, additionalReaders, compilerServices.autoConvert(version))
              new LoadWidgetsEvent(widgets)
            case (ModelSection.HubNetClient, l) if l > 0 =>
              val lines = map.get(section)
              val hnWidgets = WidgetReader.readInterface(lines.toList, compilerServices, fileformat.hubNetReaders, compilerServices.autoConvert(version))
              new LoadHubNetInterfaceEvent(hnWidgets)
            case _ =>
              val lines = map.get(section)
              new LoadSectionEvent(version, section, lines, lines.mkString("\n"))
          }
        }

        val afterEvents = List(new LoadEndEvent(), new AfterLoadEvent())

        // fire missles! (actually, just fire the events...)
        for (e <- beforeEvents ::: loadSectionEvents ::: afterEvents) e.raise(this)
      }
    }
  }
}
