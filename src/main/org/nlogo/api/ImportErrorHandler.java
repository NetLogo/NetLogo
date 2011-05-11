package org.nlogo.api;

public interface ImportErrorHandler {
  void showError(String title, String message, String defaultAction);
}
