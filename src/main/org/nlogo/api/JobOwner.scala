package org.nlogo.api

import org.nlogo.util.MersenneTwisterFast 

trait JobOwner extends SourceOwner {
  def displayName: String
  def isButton: Boolean
  def isTurtleForeverButton: Boolean
  def isLinkForeverButton: Boolean
  def ownsPrimaryJobs: Boolean
  def isCommandCenter: Boolean
  def random: MersenneTwisterFast
}
