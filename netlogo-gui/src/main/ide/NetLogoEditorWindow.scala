// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.ide

import java.awt.Dimension
import javax.swing.{ JEditorPane, JFrame, SwingUtilities }

import org.fife.ui.rtextarea.RTextScrollPane
import org.fife.ui.rsyntaxtextarea.{ folding, AbstractTokenMakerFactory, RSyntaxTextArea, SyntaxConstants, TokenMakerFactory },
  folding.FoldParserManager

// this is a harness for running a simple editor window
object NetLogoEditorWindow extends App {

  SwingUtilities.invokeLater(
    new Runnable {
      override def run(): Unit = {
        val frame = new JFrame("NetLogo IDE")
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        TokenMakerFactory.getDefaultInstance
          .asInstanceOf[AbstractTokenMakerFactory]
          .putMapping("netlogo", "org.nlogo.ide.NetLogoTokenMaker")

        FoldParserManager.get.addFoldParserMapping("netlogo", new NetLogoFoldParser())

        val textArea = new RSyntaxTextArea(20, 80)
        textArea.setSyntaxEditingStyle("netlogo")
        textArea.setCodeFoldingEnabled(true)

        val scrollPane = new RTextScrollPane(textArea)
        frame.add(scrollPane)
        frame.setSize(new Dimension(640, 480))
        frame.setVisible(true)
      }
    })
}
