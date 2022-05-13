// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm;

import org.nlogo.api.SourceOwner;
import scala.collection.mutable.Buffer;

abstract class ProcedureJ {
  Command[] _code = new Command[0];
}
