// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc

import java.awt.{ FileDialog => AWTFileDialog, Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.image.BufferedImage
import java.io.{ ByteArrayOutputStream, File }
import java.net.URI
import javax.imageio.ImageIO
import javax.swing.{ Box, BoxLayout, ButtonGroup, JDialog, JLabel, JPanel }
import javax.swing.event.{ DocumentEvent, DocumentListener }

import org.nlogo.analytics.Analytics
import org.nlogo.api.Workspace
import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.swing.{ BrowserLauncher, Button, ButtonPanel, ComboBox, DialogButton, FileDialog, OptionPane,
                         Positioning, RadioButton, TextField, Transparent }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }
import org.nlogo.workspace.{ PreviewCommandsRunner, WorkspaceFactory }

import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

import sttp.client4.Backend
import sttp.client4.quick.multipart

class UploadDialog(parent: Frame, backend: Backend[Future], loginInfo: LoginInfo, modelBody: String,
                   workspace: Workspace, workspaceFactory: WorkspaceFactory)
  extends JDialog(parent, I18N.gui.get("dialog.mc.upload"), true) with ThemeSync {

  private val greeting = new JLabel(I18N.gui.getN("dialog.mc.upload.hello", loginInfo.first))
  private val uploadAs = new JLabel(I18N.gui.get("dialog.mc.upload.uploadAs"))

  private val newModelButton = new RadioButton(I18N.gui.get("dialog.mc.upload.newModel"), updateOptionVisibility)
  private val childModelButton = new RadioButton(I18N.gui.get("dialog.mc.upload.childModel"), updateOptionVisibility)
  private val newVersionButton = new RadioButton(I18N.gui.get("dialog.mc.upload.newVersion"), updateOptionVisibility)

  private val newName = new JLabel(I18N.gui.get("dialog.mc.upload.newName"))
  private val newNameField = new TextField

  private val existingName = new JLabel(I18N.gui.get("dialog.mc.upload.existingName"))

  private val existingNameField = new TextField {
    getDocument.addDocumentListener(new DocumentListener {
      override def changedUpdate(e: DocumentEvent): Unit = {
        executeModelSearch()
      }

      override def insertUpdate(e: DocumentEvent): Unit = {
        executeModelSearch()
      }

      override def removeUpdate(e: DocumentEvent): Unit = {
        executeModelSearch()
      }
    })
  }

  private val existingNameDropdown: ComboBox[ModelEntry] = new ComboBox(Seq(ModelEntry(-1, " ")))

  private val comment = new JLabel(I18N.gui.get("dialog.mc.upload.comment"))
  private val commentField = new TextField

  private val group = new JLabel(I18N.gui.get("dialog.mc.upload.group"))
  private val groupDropdown = new ComboBox[Group](Group(-1, " ") +: loginInfo.groups)

  private val visible = new JLabel(I18N.gui.get("dialog.mc.upload.visible"))
  private val visibleDropdown = new ComboBox[Permissions](Seq(Everyone, OnlyGroup, OnlyYou))

  private val changeable = new JLabel(I18N.gui.get("dialog.mc.upload.changeable"))
  private val changeableDropdown = new ComboBox[Permissions](Seq(Everyone, OnlyGroup, OnlyYou))

  private val preview = new JLabel(I18N.gui.get("dialog.mc.upload.image"))

  private val currentImageButton = new RadioButton(I18N.gui.get("dialog.mc.upload.currentImage"),
                                                   updateSelectFileVisibility)

  private val generateImageButton = new RadioButton(I18N.gui.get("dialog.mc.upload.generateImage"),
                                                    updateSelectFileVisibility)

  private val fileImageButton = new RadioButton(I18N.gui.get("dialog.mc.upload.fileImage"), updateSelectFileVisibility)
  private val noImageButton = new RadioButton(I18N.gui.get("dialog.mc.upload.noImage"), updateSelectFileVisibility)

  private var selectedFile: Option[File] = None

  private val selectFileButton = new Button(I18N.gui.get("dialog.mc.upload.selectFile"), () => {
    val file: File = {
      try {
        new File(FileDialog.showFiles(parent, I18N.gui.get("dialog.mc.upload.selectFile"), AWTFileDialog.LOAD))
      } catch {
        case _: UserCancelException => null
      }
    }

    if (file != null && file.exists) {
      selectedFile = Some(file)

      fileLabel.setText(file.getName)
    }
  })

  private val fileLabel = new JLabel(I18N.gui.get("dialog.mc.upload.noFile"))

  private val logoutButton = new DialogButton(false, I18N.gui.get("dialog.mc.upload.logout"), () => {
    if (ModelingCommons.logout(parent))
      setVisible(false)
  })

  private val cancelButton = new DialogButton(false, I18N.gui.get("common.buttons.cancel"), () => {
    setVisible(false)
  })

  private val uploadButton = new DialogButton(true, I18N.gui.get("dialog.mc.upload.upload"), () => upload())

  locally {
    setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.gridx = 0
    c.anchor = GridBagConstraints.WEST
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(12, 12, 12, 12)

    add(greeting, c)

    c.insets = new Insets(0, 12, 12, 12)

    add(new JPanel(new GridBagLayout) with Transparent {
      locally {
        val c = new GridBagConstraints

        c.gridx = 0
        c.anchor = GridBagConstraints.NORTHWEST
        c.insets = new Insets(0, 0, 6, 6)

        add(uploadAs, c)
        add(newName, c)
        add(existingName, c)
        add(comment, c)
        add(group, c)
        add(visible, c)
        add(changeable, c)
        add(preview, c)

        c.gridx = 1
        c.fill = GridBagConstraints.HORIZONTAL
        c.weightx = 1
        c.insets = new Insets(0, 0, 6, 0)

        add(new JPanel(new GridBagLayout) with Transparent {
          locally {
            val c = new GridBagConstraints

            c.gridx = 0
            c.anchor = GridBagConstraints.WEST
            c.weightx = 1

            add(newModelButton, c)
            add(childModelButton, c)
            add(newVersionButton, c)

            val buttons = new ButtonGroup

            buttons.add(newModelButton)
            buttons.add(childModelButton)
            buttons.add(newVersionButton)
          }
        }, c)

        add(newNameField, c)

        add(new JPanel with Transparent {
          setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))

          add(existingNameField)
          add(Box.createVerticalStrut(6))
          add(existingNameDropdown)
        }, c)

        add(commentField, c)
        add(groupDropdown, c)
        add(visibleDropdown, c)
        add(changeableDropdown, c)

        add(new JPanel(new GridBagLayout) with Transparent {
          locally {
            val c = new GridBagConstraints

            c.gridx = 0
            c.anchor = GridBagConstraints.WEST
            c.weightx = 1

            add(currentImageButton, c)
            add(generateImageButton, c)

            add(new JPanel with Transparent {
              setLayout(new BoxLayout(this, BoxLayout.X_AXIS))

              add(fileImageButton)
              add(Box.createHorizontalStrut(12))
              add(selectFileButton)
              add(Box.createHorizontalStrut(6))
              add(fileLabel)
            }, c)

            add(noImageButton, c)

            val buttons = new ButtonGroup

            buttons.add(currentImageButton)
            buttons.add(generateImageButton)
            buttons.add(fileImageButton)
            buttons.add(noImageButton)
          }
        }, c)
      }
    }, c)

    add(new JPanel(new GridBagLayout) with Transparent {
      locally {
        val c = new GridBagConstraints

        c.gridy = 0
        c.anchor = GridBagConstraints.WEST
        c.weightx = 1

        add(logoutButton, c)

        c.weightx = 0

        add(new ButtonPanel(Seq(uploadButton, cancelButton)), c)
      }
    }, c)

    newModelButton.setSelected(true)
    currentImageButton.setSelected(true)

    updateOptionVisibility()

    syncTheme()
    pack()

    Positioning.center(this, parent)

    setResizable(false)
    setVisible(true)
  }

  private def updateOptionVisibility(): Unit = {
    newName.setEnabled(!newVersionButton.isSelected)
    newNameField.setEnabled(!newVersionButton.isSelected)

    existingName.setEnabled(!newModelButton.isSelected)
    existingNameField.setEnabled(!newModelButton.isSelected)
    existingNameDropdown.setEnabled(!newModelButton.isSelected)

    comment.setEnabled(!newModelButton.isSelected)
    commentField.setEnabled(!newModelButton.isSelected)

    group.setEnabled(newModelButton.isSelected)
    groupDropdown.setEnabled(newModelButton.isSelected)

    visible.setEnabled(newModelButton.isSelected)
    visibleDropdown.setEnabled(newModelButton.isSelected)

    changeable.setEnabled(newModelButton.isSelected)
    changeableDropdown.setEnabled(newModelButton.isSelected)

    preview.setEnabled(newModelButton.isSelected)
    currentImageButton.setEnabled(newModelButton.isSelected)
    generateImageButton.setEnabled(newModelButton.isSelected)
    fileImageButton.setEnabled(newModelButton.isSelected)
    noImageButton.setEnabled(newModelButton.isSelected)

    updateSelectFileVisibility()
  }

  private def updateSelectFileVisibility(): Unit = {
    selectFileButton.setEnabled(fileImageButton.isSelected && newModelButton.isSelected)
    fileLabel.setEnabled(fileImageButton.isSelected && newModelButton.isSelected)
  }

  private def executeModelSearch(): Unit = {
    existingNameDropdown.setItems(Seq(ModelEntry(-1, " ")))

    val query = existingNameField.getText.trim

    if (query.isEmpty) {
      existingNameDropdown.hidePopup()
    } else {
      JsonRequest("/account/models", {
        if (newVersionButton.isSelected) {
          Map("query" -> query, "count" -> "10", "changeability" -> "changeability")
        } else {
          Map("query" -> query, "count" -> "10")
        }
      }, loginInfo.cookies, backend).flatMap {
        case JsonResponse(json, _) => Try {
          val models = json("models").arr.map(obj => ModelEntry(obj("id").num.toInt, obj("name").str)).toSeq

          if (models.nonEmpty)
            existingNameDropdown.setItems(models)

          val fieldFocus = existingNameField.hasFocus

          existingNameDropdown.showPopup()

          if (fieldFocus)
            existingNameField.requestFocus()
        }
      }.recover(ModelingCommons.handleError(this, "upload.searchFailed"))
    }
  }

  private def upload(): Unit = {
    if (newModelButton.isSelected) {
      val name = newNameField.getText.trim

      if (name.isEmpty) {
        ModelingCommons.displayError(this, "invalid", "upload.noName")
      } else if (fileImageButton.isSelected && selectedFile.isEmpty) {
        ModelingCommons.displayError(this, "invalid", "upload.needFile")
      } else {
        val image: Option[BufferedImage] = {
          if (currentImageButton.isSelected) {
            Option(workspace.exportView)
          } else if (generateImageButton.isSelected) {
            PreviewCommandsRunner.fromFactory(workspaceFactory).previewImage match {
              case Success(image) =>
                Option(image)

              case Failure(_) =>
                ModelingCommons.displayError(this, "upload.failed", "upload.generateFailed")

                return
            }
          } else if (fileImageButton.isSelected) {
            selectedFile.map(ImageIO.read)
          } else {
            None
          }
        }

        JsonRequest("/upload/create_model", Seq(
          multipart("new_model[name]", name),
          multipart("new_model[uploaded_body]", modelBody).fileName(s"$name.nlogox"),
          multipart("read_permission", visibleDropdown.getSelectedItem.fold("")(_.id)),
          multipart("write_permission", changeableDropdown.getSelectedItem.fold("")(_.id))
        ) ++ groupDropdown.getSelectedItem.collect {
          case group if group.id != -1 =>
            multipart("group_id", group.id.toString)
        } ++ image.map { img =>
          val stream = new ByteArrayOutputStream

          ImageIO.write(img, "png", stream)

          multipart("new_model[uploaded_preview]", stream.toByteArray).fileName(s"$name.png")
        }, loginInfo.cookies, backend).flatMap {
          case JsonResponse(json, _) => Try {
            if (json("status").str == "SUCCESS_PREVIEW_NOT_SAVED") {
              new OptionPane(this, I18N.gui.get("common.messages.warning"),
                             I18N.gui.get("dialog.mc.upload.previewNotSaved"), OptionPane.Options.Ok,
                             OptionPane.Icons.Warning)
            } else if (json("status").str != "SUCCESS") {
              throw new ServerException
            }

            uploadSucceeded(json("model")("url").str)
          }
        }.recover(ModelingCommons.handleError(this, "upload.failed"))
      }
    } else if (childModelButton.isSelected) {
      val name = newNameField.getText.trim
      val comment = commentField.getText.trim
      val model: Option[ModelEntry] = existingNameDropdown.getSelectedItem

      if (name.isEmpty) {
        ModelingCommons.displayError(this, "invalid", "upload.noName")
      } else if (comment.isEmpty) {
        ModelingCommons.displayError(this, "invalid", "upload.noComment")
      } else if (!model.exists(_.name.trim.nonEmpty)) {
        ModelingCommons.displayError(this, "invalid", "upload.noModel")
      } else {
        JsonRequest("/upload/update_model", Seq(
          multipart("new_version[name]", name),
          multipart("new_version[description]", comment),
          multipart("new_version[node_id]", model.fold("-1")(_.id.toString)),
          multipart("fork", "child"),
          multipart("new_version[uploaded_body]", modelBody).fileName(s"$name.nlogox")
        ), loginInfo.cookies, backend).flatMap {
          case JsonResponse(json, _) => Try {
            if (json("status").str != "SUCCESS")
              throw new ServerException

            uploadSucceeded(json("model")("url").str)
          }
        }.recover(ModelingCommons.handleError(this, "upload.failed"))
      }
    } else if (newVersionButton.isSelected) {
      val comment = commentField.getText.trim
      val model: Option[ModelEntry] = existingNameDropdown.getSelectedItem

      if (comment.isEmpty) {
        ModelingCommons.displayError(this, "invalid", "upload.noComment")
      } else if (!model.exists(_.name.trim.nonEmpty)) {
        ModelingCommons.displayError(this, "invalid", "upload.noModel")
      } else {
        val id = model.fold("-1")(_.id.toString)

        JsonRequest("/upload/update_model", Seq(
          multipart("new_version[description]", comment),
          multipart("new_version[node_id]", id),
          multipart("fork", "overwrite"),
          multipart("new_version[uploaded_body]", modelBody).fileName(s"${id}_new.nlogox")
        ), loginInfo.cookies, backend).flatMap {
          case JsonResponse(json, _) => Try {
            if (json("status").str != "SUCCESS")
              throw new ServerException

            uploadSucceeded(json("model")("url").str)
          }
        }.recover(ModelingCommons.handleError(this, "upload.failed"))
      }
    }
  }

  private def uploadSucceeded(url: String): Unit = {
    Analytics.modelingCommonsUpload()

    if (new OptionPane(this, I18N.gui.get("dialog.mc.upload.succeeded"),
                       I18N.gui.get("dialog.mc.upload.succeeded.message"), Seq(
                         I18N.gui.get("common.buttons.ok"),
                         I18N.gui.get("dialog.mc.upload.openPage")
                       )).getSelectedIndex == 1) {
      BrowserLauncher.openURI(UploadDialog.this, new URI(url))
    }

    setVisible(false)
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())

    greeting.setForeground(InterfaceColors.dialogText())
    uploadAs.setForeground(InterfaceColors.dialogText())
    newName.setForeground(InterfaceColors.dialogText())
    existingName.setForeground(InterfaceColors.dialogText())
    comment.setForeground(InterfaceColors.dialogText())
    group.setForeground(InterfaceColors.dialogText())
    visible.setForeground(InterfaceColors.dialogText())
    changeable.setForeground(InterfaceColors.dialogText())
    preview.setForeground(InterfaceColors.dialogText())
    fileLabel.setForeground(InterfaceColors.dialogText())

    newModelButton.syncTheme()
    childModelButton.syncTheme()
    newVersionButton.syncTheme()
    newNameField.syncTheme()
    existingNameField.syncTheme()
    existingNameDropdown.syncTheme()
    commentField.syncTheme()
    groupDropdown.syncTheme()
    visibleDropdown.syncTheme()
    changeableDropdown.syncTheme()
    currentImageButton.syncTheme()
    generateImageButton.syncTheme()
    fileImageButton.syncTheme()
    noImageButton.syncTheme()
    selectFileButton.syncTheme()
    logoutButton.syncTheme()
    cancelButton.syncTheme()
    uploadButton.syncTheme()
  }
}
