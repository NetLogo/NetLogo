package org.nlogo.nvm

import org.nlogo.api.LogoException

class HaltException(val haltAll: Boolean)
  extends LogoException("model halted by user")
