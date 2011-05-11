package org.nlogo.editor;

public interface IndenterInterface {
  void handleTab();

  void handleCloseBracket();

  void handleInsertion(String text);

  void handleEnter();
}
