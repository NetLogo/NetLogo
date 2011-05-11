package org.nlogo.window;

public interface InterfaceGlobalWidget {
  String name();

  String classDisplayName();

  Object valueObject();

  void valueObject(Object value);

  void updateConstraints();
}
