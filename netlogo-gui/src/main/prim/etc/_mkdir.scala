// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.etc

import org.nlogo.nvm.{ Command, Context }

class _mkdir extends Command {
  override def perform(c: Context): Unit = {
    // leaving this double-underscored for now since it isn't relative to the model's location, like
    // it ought to be - ST 2/7/11
    // I made this relative to the model's location, like it ought to have been.  I did not make
    // it a "regular" prim because 1) that's too much work for why I'm doing it (language tests in
    // extensions) and 2) I'm not sure file-based prims like this one should live in the core language
    // (as opposed to extensions).  -Jeremy B April 2022
    val filePath     = argEvalString(c, 0)
    val absolutePath = workspace.fileManager.attachPrefix(filePath)
    val directory    = new java.io.File(absolutePath)
    directory.mkdirs()
    c.ip = next
  }
}
