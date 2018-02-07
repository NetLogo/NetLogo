// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet.client

import org.nlogo.plot.PlotManager
import org.nlogo.api.{ EditorCompiler, RandomServices, MersenneTwisterFast }
import org.nlogo.window.{ButtonWidget,ChooserWidget,InterfacePanelLite}

import javax.swing.border.{BevelBorder,EmptyBorder}
import javax.swing.{Box,JScrollPane,JLabel,JTextArea,JPanel,BoxLayout}
import java.awt.{Font,BorderLayout,Color,Insets}

// The layout for the hubnet client. Holds the interface panel and the message text field.
class ClientGUI(editorFactory: org.nlogo.window.EditorFactory,clientView: ClientView,
                plotManager: PlotManager,compiler: EditorCompiler) extends JPanel {

  private val statusPanel = new StatusPanel()
  private val messagePanel = new MessagePanel(new JTextArea(4,3))
  private val interfacePanel = new InterfacePanelLite(clientView,compiler,new DummyRandomServices(),plotManager,editorFactory) {
    sliderEventOnReleaseOnly(true)

    class ClientGUIButtonKeyAdapter extends ButtonKeyAdapter {
      val map = new collection.mutable.HashMap[ButtonWidget,Long]
      override def buttonKeyed(button: ButtonWidget) {
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

  locally {
    setBackground(Color.white)
    setLayout(new BorderLayout())
    add(interfacePanel,BorderLayout.NORTH)
    add(new JPanel(new BorderLayout()){
      setBackground(Color.white)
      add(statusPanel,BorderLayout.NORTH)
      add(messagePanel,BorderLayout.CENTER)
      },BorderLayout.CENTER
    )
  }

  override def getInsets = new Insets(5,5,5,5)
  override def requestFocus() { if (interfacePanel != null) interfacePanel.requestFocus() }
  def getInterfaceComponents = interfacePanel.getComponents
  def setStatus(username: String, activity: String, server: String, port: Int) {
    statusPanel.setStatus(username, activity, server, port)
  }
  def addMessage(message: String) {messagePanel.addMessage(message)}
  def clearMessages() {messagePanel.clear()}
  def setChoices(chooserChoices: Map[String, org.nlogo.core.LogoList]) {
    def getWidget(name: String): ChooserWidget = {
      getInterfaceComponents.collect{case w:ChooserWidget => w}.find(_.displayName == name) match {
        case Some(w) => w
        case _ => throw new IllegalStateException("couldn't find widget " + name)
      }
    }
    for ((k,v) <- chooserChoices){ getWidget(k).setChoices(v) }
  }

  private class MessagePanel(messageTextArea: JTextArea) extends JScrollPane(messageTextArea) {
    locally {
      messageTextArea.setDragEnabled(false)
      messageTextArea.setEditable(false)
      messageTextArea.setForeground(Color.darkGray)
      setBorder(new BevelBorder(BevelBorder.LOWERED))
      setVisible(false)
    }
    def addMessage(message: String) {
      setVisible(true)
      messageTextArea.append(message + "\n")
      // windows seems to need this to scroll to the end
      messageTextArea.setCaretPosition(messageTextArea.getDocument.getLength)
      val frame = org.nlogo.awt.Hierarchy.getFrame(this)
      // frame might be null in the applet ev 1/23/09
      if (frame != null) frame.pack()
    }
    def clear() {messageTextArea.setText("")}
  }
  private class StatusPanel extends JPanel {
    private val List(username, server, port) = List("User name", "Server", "Port").map(new StatusField(_, ""))
    locally {
      setBackground(java.awt.Color.white)
      setLayout(new BoxLayout(this,BoxLayout.X_AXIS))
      add(username); add(Box.createHorizontalGlue); add(server); add(port)
    }
    def setStatus(u:String, activity: String, s:String, p:Int) {
      username setText u; server setText s; port setText p.toString
      org.nlogo.awt.Hierarchy.getFrame(ClientGUI.this).setTitle("HubNet: " + activity)
    }
  }
  // Component shows label,value pair. For the status bar.
  private class StatusField(labelStr: String,valueStr: String) extends JPanel {
    private val value = new JLabel(valueStr) {
      setFont(getFont.deriveFont((getFont.getSize - 2.0).asInstanceOf[Float]))
    }
    locally {
      setBackground(Color.white)
      setLayout(new BoxLayout(this,BoxLayout.X_AXIS))
      setBorder(new EmptyBorder(4,4,4,4))
      add(new JLabel(labelStr + ": ") {
        setFont(getFont.deriveFont((getFont.getSize - 2.0).toFloat))
        setFont(getFont.deriveFont(Font.BOLD))
      })
      add(value)
    }
    def setText(text: String) {value.setText(text)}
  }
  private class DummyRandomServices extends RandomServices {
    def auxRNG: MersenneTwisterFast = null
    def mainRNG: MersenneTwisterFast = null
    def seedRNGs(seed: Int): Unit = {}
  }
}
