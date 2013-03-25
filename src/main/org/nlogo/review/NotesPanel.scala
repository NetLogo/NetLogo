package org.nlogo.review

import java.awt.BorderLayout

import javax.swing.{ JLabel, JPanel, JScrollPane, JTextArea }

class NotesPanel(tabState: ReviewTabState) extends JPanel {
  setLayout(new BorderLayout)
  val notesArea = new NotesArea(tabState)
  add(new JLabel("General notes on current run"), BorderLayout.NORTH)
  add(new JScrollPane(notesArea), BorderLayout.CENTER)
}

class NotesArea(tabState: ReviewTabState) extends JTextArea("") {
  setLineWrap(true)
  setRows(3)
  override def setText(text: String) {
    for {
      run <- tabState.currentRun
      if getText != run.generalNotes
    } super.setText(text)
  }
}

