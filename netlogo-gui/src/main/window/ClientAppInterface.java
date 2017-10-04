// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window;

import org.nlogo.api.CompilerServices;
import org.nlogo.nvm.PresentationCompilerInterface;

public interface ClientAppInterface {
  void startup(final String userid, final String hostip,
               final int port, final boolean isLocal, final boolean isRobo, final long waitTime,
               final PresentationCompilerInterface workspace);
}
