// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.api.{HaltSignal, LogoException}

class HaltException(val haltAll: Boolean)
  extends LogoException("model halted by user") with HaltSignal
