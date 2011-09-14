package org.nlogo.nvm

import org.nlogo.api.Let

private[nvm] case class LetBinding(let: Let, var value: AnyRef)
