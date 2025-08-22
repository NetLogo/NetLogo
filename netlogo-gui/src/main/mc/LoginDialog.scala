// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc

import java.awt.{ Frame, GridBagConstraints, GridBagLayout, Insets }
import java.net.URI
import javax.swing.{ JDialog, JLabel, JPanel }

import org.nlogo.core.I18N
import org.nlogo.swing.{ BrowserLauncher, ButtonPanel, DialogButton, Positioning, TextField, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

import scala.concurrent.Future
import scala.util.Try

import sttp.client4.Backend

class LoginDialog(parent: Frame, backend: Backend[Future])
  extends JDialog(parent, I18N.gui.get("dialog.mc.login"), true) with ThemeSync {

  private val message = new JLabel(I18N.gui.get("dialog.mc.login.message"))

  private val email = new JLabel(I18N.gui.get("dialog.mc.login.email"))
  private val password = new JLabel(I18N.gui.get("dialog.mc.login.password"))

  private val emailField = new TextField
  private val passwordField = new TextField

  private val createButton = new DialogButton(false, I18N.gui.get("dialog.mc.login.create"), () => {
    BrowserLauncher.openURI(LoginDialog.this, new URI("https://modelingcommons.org/account/new"))
  })

  private val cancelButton = new DialogButton(false, I18N.gui.get("common.buttons.cancel"), () => {
    setVisible(false)
  })

  private val loginButton = new DialogButton(true, I18N.gui.get("dialog.mc.login.login"), () => login())

  private var loginInfo: Option[LoginInfo] = None

  locally {
    setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.gridx = 0
    c.insets = new Insets(12, 12, 24, 12)

    add(message, c)

    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(0, 12, 24, 12)

    add(new JPanel(new GridBagLayout) with Transparent {
      locally {
        val c = new GridBagConstraints

        c.gridx = 0
        c.anchor = GridBagConstraints.EAST
        c.insets = new Insets(0, 0, 6, 6)

        add(email, c)
        add(password, c)

        c.gridx = 1
        c.fill = GridBagConstraints.HORIZONTAL
        c.weightx = 1
        c.insets = new Insets(0, 0, 6, 0)

        add(emailField, c)
        add(passwordField, c)
      }
    }, c)

    c.insets = new Insets(0, 12, 12, 12)

    add(new JPanel(new GridBagLayout) with Transparent {
      locally {
        val c = new GridBagConstraints

        c.gridy = 0
        c.anchor = GridBagConstraints.WEST
        c.weightx = 1

        add(createButton, c)

        c.weightx = 0

        add(new ButtonPanel(Seq(loginButton, cancelButton)), c)
      }
    }, c)

    getRootPane.setDefaultButton(loginButton)

    syncTheme()
    pack()

    Positioning.center(this, parent)

    setResizable(false)
    setVisible(true)
  }

  def getLoginInfo: Option[LoginInfo] =
    loginInfo

  private def login(): Unit = {
    val emailText = emailField.getText.trim
    val passwordText = passwordField.getText.trim

    if (emailText.nonEmpty && passwordText.nonEmpty) {
      JsonRequest("/account/login_action", Map("email_address" -> emailText, "password" -> passwordText),
                  backend).flatMap {
        case JsonResponse(json, cookies) => Try {
          if (json("status").str == "INVALID_CREDENTIALS") {
            throw new CredentialsException
          } else if (json("status").str != "SUCCESS") {
            throw new ServerException
          }

          val person = json("person")

          // no need to handle here if anything goes wrong during this request or the parsing of its response,
          // the error will propagate to the call to recover below (Isaac B 8/22/25)
          val groups = JsonRequest("/account/list_groups", cookies, backend)
                         .get.json("groups").arr.map(obj => Group(obj("id").num.toInt, obj("name").str)).toSeq

          loginInfo = Some(LoginInfo(person("first_name").str, person("last_name").str, person("id").num.toInt,
                                     person("avatar").str, person("email_address").str, groups, cookies))

          setVisible(false)
        }
      }.recover(ModelingCommons.handleError(this, "login.failed"))
    } else {
      ModelingCommons.displayError(this, "invalid", "login.invalid")
    }
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())

    message.setForeground(InterfaceColors.dialogText())
    email.setForeground(InterfaceColors.dialogText())
    password.setForeground(InterfaceColors.dialogText())

    emailField.syncTheme()
    passwordField.syncTheme()
    createButton.syncTheme()
    cancelButton.syncTheme()
    loginButton.syncTheme()
  }
}
