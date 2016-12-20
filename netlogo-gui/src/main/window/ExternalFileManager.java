// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

public interface ExternalFileManager {
  scala.Option<String> getSource(String filename);
}
