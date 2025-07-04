// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

import java.util.Locale

trait Instruction extends TokenHolder {
  def syntax: Syntax
  var token: Token = null
  var agentClassString = syntax.agentClassString
  var blockAgentClassString = syntax.blockAgentClassString
  def displayName = token.text.toUpperCase(Locale.ENGLISH)

  // Unfortunately, token and instruction are recursively referential and token
  // is immutable, so we must have a bit of ugliness here RG 1/8/2015
  private[core] def copyInstruction[A <: Instruction](i: A): A = {
    i.token = token.refine(newPrim = i)
    i.agentClassString = agentClassString
    i
  }
}

trait Reporter extends Instruction

trait Command extends Instruction
