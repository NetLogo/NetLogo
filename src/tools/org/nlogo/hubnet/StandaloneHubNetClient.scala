package org.nlogo.hubnet

import java.awt.event.{WindowAdapter, WindowEvent, ActionEvent, ActionListener}
import javax.swing.{JFrame, JButton, JLabel, JTextField, BoxLayout, JPanel}
import org.nlogo.hubnet.protocol.TestClient

object StandaloneHubNetClient{

  def main(args:Array[String]){ new ConnectionGUI().go() }

  class ConnectionGUI extends JFrame {
    getContentPane.add(new ConnectionPanel)
    this.addWindowListener(new WindowAdapter() {
      override def windowClosing(e: WindowEvent) {ConnectionGUI.this.dispose()}
    })
    def go() { pack(); setVisible(true) }
    class ConnectionPanel extends JPanel {
      private val name = new JTextField(20){ setText("robot") }
      private val ip = new JTextField(20){ setText("localhost") }
      private val port = new JTextField(20){ setText("9173") }
      setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
      def makePanel(name: String, tf: JTextField) = new JPanel() {add(new JLabel(name)); add(tf)}
      add(makePanel("Name:", name))
      add(makePanel("IP:  ", ip))
      add(makePanel("Port:", port))
      add(new JButton("Connect") {
        addActionListener(new ActionListener() {
          def actionPerformed(actionEvent: ActionEvent) {
            try {
              new ClientGUI(new TestClient(name.getText, "ANDROID", ip.getText, port.getText.toInt)).run()
              ConnectionGUI.this.dispose
            }
            catch { case e: Exception => e.printStackTrace; false }
          }
        })
      })
    }
  }

  class ClientGUI(connection:TestClient) {
    def run() = { new ClientFrame().go() }
    class ClientFrame extends JFrame {
      getContentPane.add(new ConnectionPanel)
      this.addWindowListener(new WindowAdapter() {
        override def windowClosing(e: WindowEvent) {
          connection.close("Window closed.")
          ClientFrame.this.dispose()
        }
      })
      def go() { pack(); setVisible(true) }
      class ConnectionPanel extends JPanel {
        class MessageButton(message: String) extends JButton(message) {
          addActionListener(new ActionListener() {
            def actionPerformed(actionEvent: ActionEvent) {sendMessage(message)}
          })
        }
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
