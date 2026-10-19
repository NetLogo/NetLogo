package org.nlogo.extensions.simpler

import java.awt.event.ActionEvent
import javax.swing.AbstractAction

import org.nlogo.api.{ ExtensionManager }
import org.nlogo.app.App
import org.nlogo.core.I18N
import org.nlogo.swing.{ MenuItem, OptionPane }

import org.nlogo.languagelibrary.config.{ Config, Menu }

trait SRMenu {

  protected type T

  protected var value: Option[T] = None

  protected def create(extMan: ExtensionManager, longName: String, binaryPath: String, config: Config): Option[T]

  def init(extMan: ExtensionManager, longName: String, binaryPath: String, config: Config): Unit = {
    value = create(extMan, longName, binaryPath, config)
    onInit()
  }

  def onInit(): Unit = ()

  def setup(): Unit = ()

  def teardown(): Unit = {
    value = None
  }

  def unload(): Unit = ()

  def showConsole(): Unit = ()

}

class SRMenuHeadless extends SRMenu {
  override protected type T = Unit
  override protected def create(extMan: ExtensionManager, longName: String, binaryPath: String, config: Config): Option[Unit] =
    None
}

class SRMenuGUI extends SRMenu {

  override protected type T = Menu

  override protected def create(extMan: ExtensionManager, longName: String, binaryPath: String, config: Config): Option[Menu] = {
    Menu.create(extMan, longName, binaryPath, config)
  }

  override def onInit(): Unit = {
    value.foreach {
      menu =>

        menu.addSeparator()
        menu.add(
          new MenuItem(
            new AbstractAction("Convert code from R extension") {
              override def actionPerformed(e: ActionEvent): Unit = {

                val tabManager = App.app.tabManager

                val anySetup = (tabManager.mainCodeTab +: tabManager.getExternalFileTabs).exists { tab =>
                  tab.innerSource = convertSource(tab.innerSource)

                  """(?i)(^|[^a-z0-9_\\-])sr:setup($|[^a-z0-9_\\-])""".r.findFirstIn(tab.innerSource).isDefined
                }

                if (!anySetup) {
                  new OptionPane(App.app.frame, I18N.gui.get("common.messages.warning"),
                                 """This model does not use the sr:setup primitive.
                                    You must call it before using any other Simple R primitives.""",
                                 OptionPane.Options.Ok, OptionPane.Icons.warning)
                }
              }
            }
          )
        )

    }
  }

  override def showConsole(): Unit = {
    value.foreach(_.showShellWindow())
  }

  override def setup(): Unit = {
    value.foreach(_.setup(SimpleRExtension.rProcess.evalStringified))
  }

  override def unload(): Unit = {
    value.foreach(_.unload())
  }

  private def convertSource(source: String): String = {
    val map = Map(
      "r:put" -> "sr:set",
      "r:get" -> "sr:runresult",
      "r:eval" -> "sr:run",
      "r:__evaldirect" -> "sr:run",
      "r:putlist" -> "sr:set-list",
      "r:putnamedlist" -> "sr:set-named-list",
      "r:putdataframe" -> "sr:set-data-frame",
      "r:putagent" -> "sr:set-agent",
      "r:putagentdf" -> "sr:set-agent-data-frame",
      "r:setplotdevice" -> "sr:set-plot-device",
      "r:interactiveshell" -> "sr:show-console",
      "r:clear" -> "sr:setup",
      "r:clearlocal" -> "sr:setup",
      "r:gc" -> "",
      "r:stop" -> ""
    )
    map.foldLeft(source) {
      case (str, (key, value)) => str.replaceAll(s"""(?i)(^|[^a-z0-9_\\-])$key($$|[^a-z0-9_\\-])""", s"$$1$value$$2")
    }
  }

}

