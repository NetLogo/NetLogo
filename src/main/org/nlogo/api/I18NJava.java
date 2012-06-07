// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api;

/**
 * The Scala compiler won't generate a Java varargs forwarder for a Scala varargs
 * method unless the method comes from a Java class or interface.  So this is in
 * Java to be implemented from Scala by I18N.BundleKind.
 */
public interface I18NJava {
  String get(String key);
  String getN(String key, Object... args);
  scala.Function1<String, String> fn();
}
