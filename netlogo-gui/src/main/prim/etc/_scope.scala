package org.nlogo.prim.etc

import org.nlogo.nvm.{Command, Context}

class _enterscope extends Command {
  override def perform(context: Context): Unit = {
    context.activation.binding = context.activation.binding.enterScope
    context.ip += 1
  }
}

class _exitscope extends Command {
  override def perform(context: Context): Unit = {
    context.activation.binding = context.activation.binding.exitScope
    context.ip += 1
  }
}
