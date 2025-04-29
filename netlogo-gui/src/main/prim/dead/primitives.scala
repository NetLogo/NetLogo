// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.dead

import org.nlogo.nvm.{ Command, Context, Reporter }

trait DeadCommand extends Command {
  override def perform(context: Context): Unit = {
    throw new IllegalStateException
  }
}

trait DeadReporter extends Reporter {
  override def report(context: Context) =
    throw new IllegalStateException
}

/**
 * This isn't in the language anymore, but in order to auto-translate it to HISTOGRAM + OF, we need
 * to have a class for it so the tokenizer and parser can parse it.
 */
class _histogramfrom extends DeadCommand {}

/**
 * This exists only to support parsing of old models by AutoConverter; in older NetLogos we allowed
 * + on types other than numbers.
 */
class _pluswildcard extends DeadReporter {}

/**
 * This primitive never existed; we convert "random" (in very, very old models) or
 * "random-int-or-float" (in more recent models) to this to force the user to change it; we need to
 * have a class for it so the tokenizer and parser can parse it.
 */
class _randomorrandomfloat extends DeadReporter {}
