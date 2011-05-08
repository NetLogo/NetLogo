package org.nlogo.plot

import org.nlogo.api.LogoException

class PlotException(message: String, cause: Throwable=null)
        extends LogoException(message, cause)
