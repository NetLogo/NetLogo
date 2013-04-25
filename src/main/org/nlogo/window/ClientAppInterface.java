// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.ParserServices;

public interface ClientAppInterface {
  void startup(final EditorFactory editorFactory, final String userid, final String hostip,
               final int port, final boolean isLocal, final boolean isRobo, final long waitTime,
               final ParserServices workspace);
}
