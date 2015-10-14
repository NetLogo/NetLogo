// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.swing.BrowserLauncher
import java.awt.Container
import org.nlogo.window.Events._
import org.nlogo.api.{I18N, ModelType, ModelReader, ModelSection, Version}

object ModelLoader {
  val TRANSITION_URL = "http://ccl.northwestern.edu/netlogo/5.0/docs/transition.html"

  @throws(classOf[InvalidVersionException])
  def load(linkParent: Container, modelPath: String,
           modelType: ModelType, map: java.util.Map[ModelSection, Array[String]]) {
    Loader(linkParent).loadHelper(modelPath, modelType, map)
  }
  @throws(classOf[InvalidVersionException])
  def load(linkParent: Container, modelPath: String, modelType: ModelType, source: String) {
    Loader(linkParent).loadHelper(modelPath, modelType, ModelReader.parseModel(source))
  }

  private case class Loader(linkParent: Container) extends org.nlogo.window.Event.LinkChild {
    def getLinkParent = linkParent

    @throws(classOf[InvalidVersionException])
    def loadHelper(modelPath: String, modelType: ModelType, map: java.util.Map[ModelSection, Array[String]]) {
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
          ModelSection.ModelSettings)

        val loadSectionEvents = sectionTypes.map { section => // kludgey - ST 2/11/08
          val lines = (section, map.get(section).length) match {
            // Kludge: If the shapes section is empty, then this is an unconverted pre-Beta4 model,
            // so the default shapes must be loaded -- or maybe it's a model (such as the
            // default model) that was hand-edited to have no shapes in it, so it always gets
            // the default shapes when opened. - ST 9/2/03
            case (ModelSection.TurtleShapes, 0) => ModelReader.defaultShapes
            case (ModelSection.LinkShapes, 0) => ModelReader.defaultLinkShapes
            // Another kludge: pre-4.1 model files have
            // org.nlogo.aggregate.gui in them instead of org.nlogo.sdm.gui,
            // so translate on the fly - ST 2/18/08
            case (ModelSection.SystemDynamics, _) =>
              map.get(section).map(_.replaceAll("org.nlogo.aggregate.gui", "org.nlogo.sdm.gui"))
            case _ => map.get(section)
          }
          new LoadSectionEvent(version, section, lines, lines.mkString("\n"))
        }

        val afterEvents = List(new LoadEndEvent(), new AfterLoadEvent())

        // fire missles! (actually, just fire the events...)
        for (e <- beforeEvents ::: loadSectionEvents ::: afterEvents) e.raise(this)
      }
    }
  }
}
