// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.hubnet

import java.awt.event.{ WindowAdapter, WindowEvent }
import javax.swing.{ BoxLayout, JFrame, JLabel, JPanel }

import org.nlogo.hubnet.protocol.TestClient
import org.nlogo.swing.{ Button, TextField, Transparent }
import org.nlogo.theme.InterfaceColors

object StandaloneHubNetClient {

  def main(args:Array[String]){ new ConnectionGUI().go() }

  class ConnectionGUI extends JFrame {
    getContentPane.setBackground(InterfaceColors.dialogBackground)
    getContentPane.add(new ConnectionPanel)
    addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent) {ConnectionGUI.this.dispose()}
    })
    def go() { pack(); setVisible(true) }
    class ConnectionPanel extends JPanel with Transparent {
      private val name = new TextField(20, "robot")
      private val ip = new TextField(20, "localhost")
      private val port = new TextField(20, "9173")
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      def makePanel(name: String, tf: TextField) = {
        new JPanel with Transparent {
          add(new JLabel(name) {
            setForeground(InterfaceColors.dialogText)
          })
          add(tf)
        }
      }
      add(makePanel("Name:", name))
      add(makePanel("IP:  ", ip))
      add(makePanel("Port:", port))
      add(new Button("Connect", () => {
        try {
          new ClientGUI(new TestClient(name.getText, "ANDROID", ip.getText, port.getText.toInt)).run()
          ConnectionGUI.this.dispose
        }
        catch { case e: Exception => e.printStackTrace }
      }))
    }
  }

  class ClientGUI(connection:TestClient) {
    def run() = { new ClientFrame().go() }
    class ClientFrame extends JFrame {
      getContentPane.setBackground(InterfaceColors.dialogBackground)
      getContentPane.add(new ConnectionPanel)
      addWindowListener(new WindowAdapter() {
        override def windowClosing(e: WindowEvent) {
          connection.close("Window closed.")
          ClientFrame.this.dispose()
        }
      })
      def go() { pack(); setVisible(true) }
      class ConnectionPanel extends JPanel with Transparent {
        class MessageButton(message: String) extends Button(message, () => sendMessage(message))
        add(new MessageButton("left"))
        add(new MessageButton("up"))
        add(new MessageButton("down"))
        add(new MessageButton("right"))
      }
    }
    def sendMessage(message: String) {
      connection.sendActivityCommand(message, false.asInstanceOf[AnyRef])
    }
  }
}
