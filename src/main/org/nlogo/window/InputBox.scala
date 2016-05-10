// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.agent.InputBoxConstraint
import org.nlogo.editor.AbstractEditorArea
import org.nlogo.api.Approximate.approximate
import org.nlogo.api.Color.{getColor, getColorNameByIndex, modulateDouble}
import org.nlogo.api.ModelReader.stripLines
import org.nlogo.swing.ButtonPanel
import org.nlogo.awt.Fonts.platformMonospacedFont
import org.nlogo.awt.Fonts.platformFont
import org.nlogo.swing.Implicits._
import org.nlogo.api.{Options, I18N, ValueConstraint, CompilerException, LogoException, CompilerServices, Dump, Editable}
import java.awt.{Color, Frame, Dimension, Font, Component}
import java.awt.event.{ActionListener, WindowEvent, WindowAdapter, FocusListener, FocusEvent, ActionEvent, KeyEvent}
import javax.swing.text.EditorKit
import javax.swing.KeyStroke.getKeyStroke
import javax.swing.{JDialog, JOptionPane, AbstractAction, ScrollPaneConstants, JScrollPane, JButton, JLabel}
import javax.swing.plaf.basic.BasicButtonUI
import java.util.prefs.Preferences

abstract class InputBox(textArea:AbstractEditorArea, editDialogTextArea:AbstractEditorArea,
            compiler:CompilerServices, nextComponent:Component)
  extends SingleErrorWidget with Editable with Events.InputBoxLoseFocusEvent.Handler {

  object Prefs {
    private val prefs = Preferences.userNodeForPackage(InputBox.this.getClass)
    def inputTypeName = prefs.get("inputTypeName", "Number")
    def updateTo(inputTypeName: String): Unit = {
      prefs.put("inputTypeName", inputTypeName)
    }
  }
   val MIN_WIDTH = 50
   val MIN_HEIGHT = 60

  /// be editable
  override def classDisplayName = I18N.gui.get("tabs.run.widgets.input")
  protected val widgetLabel = new JLabel()
  protected var dialog: InputDialog = null
  private var _hasFocus = false
  // grab the current editor kit from the editor area
  // everyone but string will use it but we need to
  // keep it around so we know what to set it to.
  private val codeEditorKit: EditorKit = textArea.getEditorKit
  protected var inputType: InputType = InputType.create(Prefs.inputTypeName)
  private val constraint: InputBoxConstraint = new InputBoxConstraint(inputType.baseName, inputType.defaultValue)
  protected val changeButton: JButton = new NLButton("Change") {
    addActionListener(new EditActionListener())
  }
  protected val colorSwatch: JButton = new JButton("black"){
    setFont(javax.swing.UIManager.getFont("Label.font").deriveFont(9.0f))
    setBorder(widgetBorder)
    addActionListener(new SelectColorActionListener())
    // on winXP if we don't set this the color in the button doesn't show up ev 2/15/08
    setContentAreaFilled(false)
    setOpaque(true)
  }

  private val scroller: JScrollPane = new JScrollPane(textArea,
    ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)

  // most of the time text and value will be exactly the same
  // however, for numbers there will be a Double rather than
  // a String in the value field.  ev 8/13/06
  protected var text = ""
  protected var value: AnyRef = ""
  def valueObject = value
  def valueObject(value: AnyRef) {valueObject(value, false)}
  def valueObject(value: AnyRef, raiseEvent: Boolean) {
    text = Dump.logoObject(value)
    this.value = value
    if (text != textArea.getText) textArea.setText(text)
  }
  protected def inputText(input: Object) {
    if (input != null) valueObject(input, true)
  }
  // multiline property
  protected var multiline = false
  def multiline(multiline: Boolean) {
    this.multiline = multiline
    textArea.setEditable(!inputType.changeVisible)
    changeButton.setVisible(inputType.changeVisible)
    editing = false
  }

  var typeOptions = new org.nlogo.api.Options[InputType]()
  var name = ""
  var nameChanged = false

  /// name needs a wrapper because we don't want to recompile until editFinished()
  def name(name: String) {this.name(name, true)}
  def name(name: String, sendEvent: Boolean) {
    this.name = name
    // I don't think anyone ever uses the display name, but let's keep it in sync
    // with the real name, just in case - ST 6/3/02
    displayName(name)
    widgetLabel.setText(name)
  }

  def nameWrapper = name
  def nameWrapper(name: String) {
    nameChanged = name != this.name || nameChanged
    this.name(name, false)
  }

  protected var editing = false
  protected def stopEdit() {
    editing = false
    transferFocus()
    nextComponent.requestFocus()
  }

  locally {
    InputType.addTypeOptions(typeOptions)
    typeOptions.selectValue(inputType)
    textArea.setEditorKit(inputType.getEditorKit)
    textArea.setFont(inputType.getFont)
    textArea.enableBracketMatcher(inputType.enableBracketMatcher)

    multiline(multiline)

    setBackground(InterfaceColors.SLIDER_BACKGROUND)
    setBorder(widgetBorder)
    setOpaque(true)
    org.nlogo.awt.Fonts.adjustDefaultFont(this)

    val layout = new java.awt.GridBagLayout()
    setLayout(layout)
    val c = new java.awt.GridBagConstraints()

    c.gridx = 0
    c.gridy = 0
    c.weightx = 1
    c.anchor = java.awt.GridBagConstraints.WEST
    c.insets = new java.awt.Insets(3, 3, 3, 3)

    layout.setConstraints(widgetLabel, c)
    add(widgetLabel)

    widgetLabel.setFont(new Font(platformFont, Font.PLAIN, 10))

    c.gridx = 1
    c.weightx = 0
    c.anchor = java.awt.GridBagConstraints.EAST
    layout.setConstraints(changeButton, c)
    add(changeButton, c)

    c.gridx = 0
    c.gridy += 1
    c.weighty = 1
    c.weightx = 1
    c.gridwidth = java.awt.GridBagConstraints.REMAINDER
    c.fill = java.awt.GridBagConstraints.BOTH
    c.anchor = java.awt.GridBagConstraints.WEST

    layout.setConstraints(scroller, c)
    add(scroller)

    layout.setConstraints(colorSwatch, c)
    add(colorSwatch, c)
    colorSwatch.setVisible(false)

    // focus listener for in place editing
    textArea.addFocusListener(
      new FocusListener() {
        def focusGained(e: FocusEvent) {
          _hasFocus = true
          if (!multiline) editing = true
        }
        def focusLost(e: FocusEvent) {
          _hasFocus = false
          if (editing) {
            try inputText(inputType.readValue(InputBox.this.textArea.getText))
            catch {
              case ex@(_:LogoException|_:CompilerException|_:ValueConstraint.Violation) =>
                showError(ex.asInstanceOf[Exception])
                org.nlogo.awt.EventQueue.invokeLater(() => InputBox.this.textArea.requestFocus())
            }
            editing = false
          }
        }
      })

    textArea.getInputMap.put(getKeyStroke(KeyEvent.VK_ESCAPE, 0), new CancelAction())
    textArea.getInputMap.put(getKeyStroke(KeyEvent.VK_ENTER, 0), new TransferFocusAction())
    textArea.getInputMap.put(getKeyStroke(KeyEvent.VK_TAB, 0), new TransferFocusAction())
  }

  private class EditActionListener extends ActionListener {
    def actionPerformed(e: ActionEvent) {
      if (!editing) {
        editing = true
        dialog = new InputDialog(org.nlogo.awt.Hierarchy.getFrame(InputBox.this), name, `inputType`, editDialogTextArea)
        dialog.setVisible(true)
        editDialogTextArea.setText(textArea.getText)
        editDialogTextArea.selectAll()
      }
    }
  }

  private class SelectColorActionListener extends ActionListener {
    def actionPerformed(e: ActionEvent) {
      val colorDialog = new ColorDialog(org.nlogo.awt.Hierarchy.getFrame(InputBox.this), true)
      valueObject(colorDialog.showInputBoxDialog(
        if (value.isInstanceOf[Double])
          org.nlogo.api.Color.modulateDouble(value.asInstanceOf[Double])
        else 0d
       ).asInstanceOf[AnyRef], true)
    }
  }

  private def showError(ex: Exception) {
    val frame = org.nlogo.awt.Hierarchy.getFrame(this)
    if (frame != null) {
      var msg = ex.getMessage
      if (msg.startsWith("REPORT expected 1 input."))
        msg = "Expected reporter."
      org.nlogo.swing.OptionDialog.show(frame, "Invalid input for a " + inputType,
        msg, Array(I18N.gui.get("common.buttons.ok")))
    }
  }

  def handle(e:Events.InputBoxLoseFocusEvent){
    if(_hasFocus) transferFocus()
  }

  private class CancelAction extends AbstractAction {
    def actionPerformed(e:ActionEvent){
      textArea.setText(text)
      stopEdit()
    }
  }

  private class TransferFocusAction extends AbstractAction {
    def actionPerformed(e:ActionEvent) {
      transferFocus()
      nextComponent.requestFocus()
    }
  }

  override def updateConstraints() {
    if (name.length > 0) new org.nlogo.window.Events.AddInputBoxConstraintEvent(name, constraint).raise(this)
  }

  override def editFinished() = {
    super.editFinished()
    name(this.name, nameChanged)
    nameChanged = false
    try inputText(constraint.coerceValue(inputType.readValue(text)))
    catch {
      case ex: LogoException => throw new IllegalStateException(ex)
      case ex@(_: CompilerException | _: ValueConstraint.Violation) => showError(ex.asInstanceOf[Exception])
    }
    true
  }

  def typeOptions(typeOptions: org.nlogo.api.Options[InputType]) {
    this.typeOptions = typeOptions
    if (inputType.displayName != typeOptions.chosenValue.displayName) {
      inputType = typeOptions.chosenValue
      constraint.setType(inputType.baseName, inputType.defaultValue)
      // if the current value doesn't comply with the new constraint
      // set it to a default value ev 12/14/06
      try constraint.assertConstraint(value)
      catch {
        case v: ValueConstraint.Violation => valueObject(inputType.defaultValue, true)
      }
      textArea.setEditorKit(inputType.getEditorKit)
      textArea.setFont(inputType.getFont)
      textArea.enableBracketMatcher(inputType.enableBracketMatcher)
      changeButton.setVisible(inputType.changeVisible)
      inputType.colorPanel(colorSwatch)
      Prefs.updateTo(typeOptions.chosenName)
    }
    scroller.setHorizontalScrollBarPolicy(
      if (inputType.multiline) ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED
      else ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER)
    multiline(inputType.multiline)
    if (dialog != null) {
      dialog.dispose()
      dialog = null
    }
  }

  override def getMinimumSize = new Dimension(MIN_WIDTH, MIN_HEIGHT)
  override def getPreferredSize(font: Font) = {
    val result = super.getPreferredSize(font)
    val insets = getInsets
    // add 4 because apparently we need a few extra pixels to make sure
    // that we don't get a horizontal scroll bar at the default size. ev 9/28/06
    result.width =
            textArea.getPreferredSize.width + insets.left + insets.right +
            textArea.getInsets.right + textArea.getInsets.left + 4
    new Dimension(StrictMath.max(MIN_WIDTH, result.width), StrictMath.max(MIN_HEIGHT, result.height))
  }

  override def save = {
    val s = new StringBuilder()
    s.append("INPUTBOX\n")
    s.append(getBoundsString)
    if((null != name) && (name.trim != "")) s.append(name + "\n")
    else s.append("NIL\n")
    val nilValue = value == null || (value.isInstanceOf[String] && value.asInstanceOf[String].trim == "")
    if(! nilValue) s.append(stripLines(Dump.logoObject(value).replaceAll("\r\n", "\n")) + "\n")
    else s.append("NIL\n")
    s.append("1\n")  //7
    s.append((if(multiline) "1" else "0") + "\n")  //8
    s.append(inputType.saveName + "\n")  //9
    s.toString
  }

  override def load(strings:Array[String], helper:Widget.LoadHelper) = {
    val displayName = strings(5)
    if(displayName ==  "NIL") name("") else name(displayName)
    var contents = org.nlogo.api.ModelReader.restoreLines(strings(6))
    if(contents == "NIL") contents = ""
    if(strings.length > 8) multiline(strings(8) == "1")

    def setType(i: String) {
      this.inputType = InputType.create(i match{
        case "Reporter" => "String (reporter)"
        case "Commands" => "String (commands)"
        case _ => i
      })
      textArea.setEditorKit(this.inputType.getEditorKit)
      textArea.setFont(this.inputType.getFont)
      textArea.enableBracketMatcher(this.inputType.enableBracketMatcher)
      typeOptions.selectByName(inputType.displayName)
      constraint.setType(this.inputType.baseName, this.inputType.defaultValue)
      changeButton.setVisible(this.inputType.changeVisible)
    }
    if(strings.length > 9) setType(strings(9)) else setType("String")

    try valueObject(inputType.readValue(contents), true)
    catch{
      case e@(_:CompilerException|_:ValueConstraint.Violation|_:LogoException) =>
        valueObject(inputType.defaultValue, true)
    }
    val List(x1,y1,x2,y2) = strings.drop(1).take(4).map(_.toInt).toList
    setSize(x2 - x1, y2 - y1)
    this
  }

  override def needsPreferredWidthFudgeFactor = false
  override def exportable = true
  override def getDefaultExportName = "export.txt"
  override def hasContextMenu = true
  override def zoomSubcomponents = true
  override def getMaximumSize = null

  override def export(exportPath: String) {
    try org.nlogo.api.FileIO.writeFile(exportPath, text, true)
    catch {
      case ex: java.io.IOException =>
        JOptionPane.showMessageDialog(this,
          "Export failed.  Error:\n" + ex.getMessage, "Export Failed", JOptionPane.ERROR_MESSAGE)
    }
  }

  // based on MoreButton in ViewControlStrip.java
  protected class NLButton(title:String) extends JButton(title) {
    setFont(new Font(platformFont,Font.PLAIN, 10))
    setBackground(InterfaceColors.GRAPHICS_BACKGROUND)
    setBorder(org.nlogo.swing.Utils.createWidgetBorder)
    setFocusable(false)
    setOpaque(false)
    setFont(new Font(platformFont, Font.PLAIN, 10))
    // without this it looks funny on Windows - ST 9/18/03
    override def updateUI() { setUI(new BasicButtonUI()) }
  }

  protected class InputDialog(parent: Frame, title: String, inputType: InputType,
                              textArea: AbstractEditorArea) extends JDialog(parent, title) {
    private val textArea1: AbstractEditorArea = textArea
    private val okAction = new AbstractAction(I18N.gui.get("common.buttons.ok")) {
      def actionPerformed(e: ActionEvent) {
        try{
          val value = inputType.readValue(textArea1.getText)
          inputText(value)
          editing = false
          dispose()
          dialog = null
        }
        catch {
          case ex@(_:LogoException | _:CompilerException | _:ValueConstraint.Violation) =>
            showError(ex.asInstanceOf[Exception])
        }
      }
    }

    private val cancelAction = new AbstractAction(I18N.gui.get("common.buttons.cancel")) {
      def actionPerformed(e: ActionEvent) {
        dispose()
        editing = false
        dialog = null
      }
    }

    private val applyAction =
    new AbstractAction("Apply") {
      def actionPerformed(e: ActionEvent) {
        try inputText(inputType.readValue(textArea1.getText))
        catch {
          case ex@(_:LogoException | _:CompilerException | _:ValueConstraint.Violation) =>
            showError(ex.asInstanceOf[Exception])
        }
      }
    }
    locally {
      setResizable(true)
      textArea.setEditorKit(inputType.getEditorKit)
      textArea.setFont(inputType.getFont)
      textArea.enableBracketMatcher(inputType.enableBracketMatcher)

      val layout = new java.awt.GridBagLayout()
      getContentPane.setLayout(layout)
      val c = new java.awt.GridBagConstraints()
      c.insets = new java.awt.Insets(3, 3, 3, 3)
      c.gridwidth = java.awt.GridBagConstraints.REMAINDER
      c.anchor = java.awt.GridBagConstraints.WEST
      val label = new JLabel(inputType.toString)
      layout.setConstraints(label, c)
      getContentPane.add(label)
      c.weightx = 1
      c.weighty = 1
      c.fill = java.awt.GridBagConstraints.BOTH
      val scroller = new JScrollPane(textArea)
      layout.setConstraints(scroller, c)
      getContentPane.add(scroller)
      val buttonPanel = new ButtonPanel(Array(new JButton(okAction), new JButton(applyAction), new JButton(cancelAction)))
      c.gridy = 2
      c.anchor = java.awt.GridBagConstraints.EAST
      c.weightx = 0
      c.weighty = 0
      layout.setConstraints(buttonPanel, c)
      getContentPane.add(buttonPanel)

      org.nlogo.swing.Utils.addEscKeyAction(this, cancelAction)

      pack()
      org.nlogo.awt.Positioning.center(this, parent)
      addWindowListener(new WindowAdapter() {
        override def windowClosing(e: WindowEvent) {
          dispose()
          editing = false
          dialog = null
        }
      })
    }
    def setText(text: String) {
      textArea1.setText(text)
      textArea1.selectAll()
    }
  }

  object InputType {
    val baseNames = List("Number", "String", "Color", "String (reporter)", "String (commands)")
    def create(inputType: String) = {
      if (inputType == "Number") new NumberInputType(codeEditorKit)
      else if (inputType == "Color") new ColorInputType(codeEditorKit)
      else if (inputType == "String (reporter)") new ReporterInputType(codeEditorKit)
      else if (inputType == "String (commands)") new CommandInputType(codeEditorKit)
      else new StringInputType()
    }

    def addTypeOptions(options:Options[InputType]){
      baseNames.map{ name =>
        val t = create(name)
        typeOptions.addOption(t.displayName, t)
      }
    }
  }

  case class InputType(baseName: String, i18nKey: String, editorKit: EditorKit, font: Font) {
    def defaultValue: AnyRef = ""
    def multiline = InputBox.this.multiline
    def multiline(newMultiline: Boolean) {InputBox.this.multiline(newMultiline)}
    override def toString = displayName
    def saveName = baseName
    def displayName = I18N.gui.get("edit.input.type." + i18nKey)
    def getEditorKit = editorKit
    def getFont = font
    def colorPanel(panel: JButton) {
      panel.setVisible(false)
      scroller.setVisible(true)
    }
    @throws(classOf[ValueConstraint.Violation])
    @throws(classOf[LogoException])
    @throws(classOf[CompilerException])
    def readValue(text: String): Object = {
      constraint.assertConstraint(text)
      return text
    }
    def changeVisible = multiline
    def enableMultiline = true
    def enableBracketMatcher = false
    override def equals(a:Any) = { a match {
      case it@InputType(bn, _, _, _) => bn == baseName
      case _ => false
    }}
  }

  private class StringInputType extends InputType(
    "String", "string",
    textArea.getEditorKitForContentType("String"),
    javax.swing.UIManager.getFont("Label.font").deriveFont(12.0f)){
  }

  def plainFont = new Font(platformMonospacedFont, Font.PLAIN, 12)
  private class ReporterInputType(kit: EditorKit) extends InputType("String (reporter)", "string.reporter", kit, plainFont) {
    override def defaultValue = "0"
    override def enableBracketMatcher = true
    @throws(classOf[ValueConstraint.Violation])
    @throws(classOf[CompilerException])
    override def readValue(text: String) = {
      constraint.assertConstraint(text)
      compiler.checkReporterSyntax(text)
      text
    }
  }

  private class CommandInputType(kit: EditorKit) extends InputType("String (commands)", "string.commands", kit, plainFont) {
    override def enableBracketMatcher = true
    @throws(classOf[ValueConstraint.Violation])
    @throws(classOf[CompilerException])
    override def readValue(text: String) = {
      constraint.assertConstraint(text)
      compiler.checkCommandSyntax(text)
      text
    }
  }

  private class NumberInputType(kit: EditorKit) extends InputType("Number", "number", kit, plainFont) {
    @throws(classOf[CompilerException])
    override def readValue(text: String) = compiler.readNumberFromString(text)
    override def enableMultiline = false
    override def defaultValue = org.nlogo.agent.World.ZERO
  }

  private class ColorInputType(kit: EditorKit) extends InputType("Color", "color", kit, plainFont) {
    @throws(classOf[CompilerException])
    override def readValue(text: String) = compiler.readNumberFromString(text)
    override def colorPanel(panel: JButton) {
      panel.setVisible(true)
      scroller.setVisible(false)
      panel.setOpaque(true)

      val (colorval, c) =
        if (value.isInstanceOf[Double]) {
          val cv = modulateDouble(value.asInstanceOf[Double]): java.lang.Double
          (cv, getColor(cv))
        }
        else (0d: java.lang.Double, Color.BLACK)

      panel.setBackground(c)
      panel.setForeground(if ((colorval % 10) > 5) Color.BLACK else Color.WHITE)
      panel.setText(colorval match {
        // this logic is duplicated in ColorEditor; black and white are special cases
        case d: java.lang.Double if d.doubleValue == 0.0 => "0 (black)"
        case d: java.lang.Double if d.doubleValue == 9.9 => "9.9 (white)"
        case c =>
          val index = (c / 10).toInt
          val baseColor = index * 10 + 5
          Dump.number(c) + " (" + getColorNameByIndex(index) + (
            if (c > baseColor) {" + " + Dump.number(approximate(c - baseColor, 1))}
            else if (c < baseColor) {" - "} + Dump.number(approximate(baseColor - c, 1))
            else ""
          ) + ")"
      })
    }
    override def changeVisible = false
    override def enableMultiline = false
    override def defaultValue = org.nlogo.agent.World.ZERO
  }
}
