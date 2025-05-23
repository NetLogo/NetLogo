// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.agent

import org.nlogo.core

trait Neighbors {

  def getPN (source: Patch): Patch
  def getPE (source: Patch): Patch
  def getPS (source: Patch): Patch
  def getPW (source: Patch): Patch
  def getPNE(source: Patch): Patch
  def getPSE(source: Patch): Patch
  def getPSW(source: Patch): Patch
  def getPNW(source: Patch): Patch

  private val getters4 = Seq(getPN, getPE, getPS, getPW)
  private val getters8 = getters4 ++ Seq(getPNE, getPSE, getPSW, getPNW)

  // this is very general and therefore has poor performance,
  // but we don't care because Patch caches the results - ST 3/29/13
  private def fromGetters(source: Patch, getters: Seq[Patch => Patch]): IndexedAgentSet =
    AgentSet.fromArray(core.AgentKind.Patch,
      getters.map(_(source))
        .filter(p => p != null && (p ne source))
        .distinct
        .toArray[Agent])

  def getNeighbors(source: Patch): IndexedAgentSet =
    fromGetters(source, getters8)
  def getNeighbors4(source: Patch): IndexedAgentSet =
    fromGetters(source, getters4)
}
