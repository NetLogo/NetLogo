package org.nlogo.prim

import org.nlogo.nvm.{Command, Context}

class _enterscope extends Command {
  override def perform(context: Context): Unit = perform_1(context)

  def perform_1(context: Context): Unit = {
    context.activation.binding = context.activation.binding.enterScope
    context.ip += 1
  }
}

class _exitscope extends Command {
  override def perform(context: Context): Unit = perform_1(context)

  def perform_1(context: Context): Unit = {
    context.activation.binding = context.activation.binding.exitScope
    context.ip += 1
  }
}
