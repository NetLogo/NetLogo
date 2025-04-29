// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import java.awt.{ BorderLayout, Font, Insets }
import javax.swing.{ Box, BoxLayout, JLabel, JPanel }
import javax.swing.border.{ BevelBorder, EmptyBorder, LineBorder }

import org.nlogo.api.{ CompilerServices, MersenneTwisterFast, RandomServices }
import org.nlogo.awt.Hierarchy
import org.nlogo.plot.PlotManager
import org.nlogo.swing.{ ScrollPane, TextArea, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.window.{ ButtonWidget, ChooserWidget, EditorFactory, InterfacePanelLite }

// The layout for the hubnet client. Holds the interface panel and the message text field.
class ClientGUI(editorFactory: EditorFactory, clientView: ClientView, plotManager: PlotManager,
                compiler: CompilerServices) extends JPanel(new BorderLayout) with Transparent with ThemeSync {

  private val statusPanel = new StatusPanel()
  private val messagePanel = new MessagePanel(new TextArea(4, 3))
  private val interfacePanel = new InterfacePanelLite(clientView, compiler, new DummyRandomServices(), plotManager, editorFactory) {
    sliderEventOnReleaseOnly(true)

    class ClientGUIButtonKeyAdapter extends ButtonKeyAdapter {
      val map = new collection.mutable.HashMap[ButtonWidget,Long]
      override def buttonKeyed(button: ButtonWidget): Unit = {
        val currentTime = System.currentTimeMillis
        val lastMessageTime = map.getOrElse(button, 0L)
        // only send the message for this button if it hasnt been pressed in the last 100 ms.
        if (currentTime - lastMessageTime > 100) {
          map(button) = currentTime
          button.keyTriggered()
        }
      }
    }

    // override in order to throttle messages when a hubnet client is holding down a key
    override def getKeyAdapter = new ClientGUIButtonKeyAdapter
  }

  add(interfacePanel, BorderLayout.NORTH)
  add(new JPanel(new BorderLayout) with Transparent {
    add(statusPanel, BorderLayout.NORTH)
    add(messagePanel, BorderLayout.CENTER)
  }, BorderLayout.CENTER)

  syncTheme()

  override def getInsets = new Insets(5, 5, 5, 5)
  override def requestFocus(): Unit = { if (interfacePanel != null) interfacePanel.requestFocus() }
  def getInterfaceComponents = interfacePanel.getComponents
  def setStatus(username: String, activity: String, server: String, port: Int): Unit = {
    statusPanel.setStatus(username, activity, server, port)
  }
  def addMessage(message: String): Unit = {messagePanel.addMessage(message)}
  def clearMessages(): Unit = {messagePanel.clear()}
  def setChoices(chooserChoices: Map[String, org.nlogo.core.LogoList]): Unit = {
    def getWidget(name: String): ChooserWidget = {
      getInterfaceComponents.collect{case w:ChooserWidget => w}.find(_.displayName == name) match {
        case Some(w) => w
        case _ => throw new IllegalStateException("couldn't find widget " + name)
      }
    }
    for ((k,v) <- chooserChoices){ getWidget(k).setChoices(v) }
  }

  private class MessagePanel(messageTextArea: TextArea) extends ScrollPane(messageTextArea) with ThemeSync {
    setBorder(new BevelBorder(BevelBorder.LOWERED))
    setVisible(false)

    messageTextArea.setDragEnabled(false)
    messageTextArea.setEditable(false)

    def addMessage(message: String): Unit = {
      setVisible(true)
      messageTextArea.append(message + "\n")
      // windows seems to need this to scroll to the end
      messageTextArea.setCaretPosition(messageTextArea.getDocument.getLength)
      val frame = org.nlogo.awt.Hierarchy.getFrame(this)
      // frame might be null in the applet ev 1/23/09
      if (frame != null) frame.pack()
    }

    def clear(): Unit = {
      messageTextArea.setText("")
    }

    override def syncTheme(): Unit = {
      setBorder(new LineBorder(InterfaceColors.textAreaBorderNoneditable()))
      setBackground(InterfaceColors.textAreaBackground())

      messageTextArea.syncTheme()
    }
  }

  private class StatusPanel extends JPanel with Transparent with ThemeSync {
    private val username = new StatusField("User name", "")
    private val server = new StatusField("Server", "")
    private val port = new StatusField("Port", "")

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

    add(username)
    add(Box.createHorizontalGlue)
    add(server)
    add(port)

    def setStatus(u: String, activity: String, s: String, p: Int): Unit = {
      username.setText(u)
      server.setText(s)
      port.setText(p.toString)

      Hierarchy.getFrame(ClientGUI.this).setTitle("HubNet: " + activity)
    }

    override def syncTheme(): Unit = {
      username.syncTheme()
      server.syncTheme()
      port.syncTheme()
    }
  }

  // Component shows label,value pair. For the status bar.
  private class StatusField(labelStr: String, valueStr: String) extends JPanel with Transparent with ThemeSync {
    private val label = new JLabel(labelStr + ": ") {
      setFont(getFont.deriveFont((getFont.getSize - 2.0).toFloat).deriveFont(Font.BOLD))
    }

    private val value = new JLabel(valueStr) {
      setFont(getFont.deriveFont((getFont.getSize - 2.0).asInstanceOf[Float]))
    }

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS))
    setBorder(new EmptyBorder(4, 4, 4, 4))

    add(label)
    add(value)

    def setText(text: String): Unit = {
      value.setText(text)
    }

    override def syncTheme(): Unit = {
      label.setForeground(InterfaceColors.dialogText())
      value.setForeground(InterfaceColors.dialogText())
    }
  }

  private class DummyRandomServices extends RandomServices {
    def auxRNG: MersenneTwisterFast = null
    def mainRNG: MersenneTwisterFast = null
    def seedRNGs(seed: Int): Unit = {}
  }

  override def syncTheme(): Unit = {
    statusPanel.syncTheme()
    messagePanel.syncTheme()
    interfacePanel.syncTheme()

    repaint()
  }
}
