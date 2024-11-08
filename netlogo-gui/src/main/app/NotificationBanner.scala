package org.nlogo.app

import org.nlogo.theme.{InterfaceColors, ThemeSync}

import javax.swing._
import java.awt._
import java.awt.event.{ActionEvent, ActionListener, MouseAdapter, MouseEvent}

class NotificationBanner(initialMessage: String) extends JPanel with ThemeSync  {
  // Label to display notification messages
  private val messageLabel = new JLabel(" " + initialMessage)

  // Close button with an "X" icon
  private val closeButton = new JButton("\u2716") // Unicode character for "X"

  // Initialize the NotificationBanner panel
  initUI()
  def syncTheme(): Unit = {
    setBackground(InterfaceColors.MONITOR_BACKGROUND)
    messageLabel.setForeground(InterfaceColors.DISPLAY_AREA_TEXT)
    closeButton.setForeground(InterfaceColors.DISPLAY_AREA_TEXT)

  }

  private def initUI(): Unit = {
    // Set layout and appearance
    setLayout(new BorderLayout())
    setPreferredSize(new Dimension(400, 50))

    // Customize the message label
    messageLabel.setHorizontalAlignment(SwingConstants.LEFT)
    add(messageLabel, BorderLayout.CENTER)

    // Configure the close button
    closeButton.setBorderPainted(false)
    closeButton.setContentAreaFilled(false)
    closeButton.setFocusPainted(false)
    closeButton.addActionListener(new ActionListener {
      override def actionPerformed(e: ActionEvent): Unit = {
        setVisible(false)  // Hide the banner when the close button is pressed
      }
    })
    add(closeButton, BorderLayout.EAST)

    // Add a mouse listener to call showJsonInDialog when the banner is clicked
    addMouseListener(new MouseAdapter {
      override def mouseClicked(e: MouseEvent): Unit = {
        InAppAnnouncementsHelper.showJsonInDialog()  // Call the method on click
      }
    })
  }

  // Method to update the notification message
  def setMessage(message: String): Unit = {
    messageLabel.setText(" " + message)
    setVisible(true) // Ensure the banner is visible when updating the message
  }
}

// Testing process for dev:
// object NotificationBannerExample extends App {
//   val mainFrame = new JFrame("Main Application Window")
//   mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
//   mainFrame.setSize(600, 400)
//   mainFrame.setLayout(new BorderLayout())
//
//   val notificationBanner = new NotificationBanner("Hello from Bruce")
//   mainFrame.add(notificationBanner, BorderLayout.NORTH)
//
//   mainFrame.setVisible(true)
//   SwingUtilities.invokeLater(new Runnable {
//     override def run(): Unit = {
//       Thread.sleep(2000)
//       notificationBanner.setMessage("Updated Message")
//     }
//   })
// }
