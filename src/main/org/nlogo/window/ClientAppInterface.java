// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.CompilerServices;

public interface ClientAppInterface {
  void startup(final EditorFactory editorFactory, final String userid, final String hostip,
               final int port, final boolean isLocal, final boolean isRobo, final long waitTime,
               final CompilerServices workspace);
}
