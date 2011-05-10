package org.nlogo.api;

public interface SourceOwner {
  String classDisplayName();

  String headerSource();

  String innerSource();

  String source();

  void innerSource(String s);

  Class<?> agentClass();
}
