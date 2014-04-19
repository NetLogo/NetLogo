// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
  * Like source owners, job owners are usually parts of the UI.  Jobs are initiated by
  * by buttons, by monitors, by the Command Center, and so on.
  *
  * The (assorted, disparate) methods here in JobOwner have mainly to do with runtime behavior
  * and/or runtime error handling.  (SourceOwner is more about compilation and compile-time errors.)
  */

trait JobOwner extends SourceOwner {
  /** for error reporting purposes */
  def displayName: String
  /** so we know whether the job "takes turns" with other buttons */
  def isButton: Boolean
  /** so we know whether newly created turtles should join the job */
  def isTurtleForeverButton: Boolean
  /** so we know whether newly created links should join the job */
  def isLinkForeverButton: Boolean
  /** determines whether job "breathes"; also affects error handling */
  def ownsPrimaryJobs: Boolean
  /** command centers get special treatment here and there */
  def isCommandCenter: Boolean
  /** typically mainRNG or auxRNG */
  def random: MersenneTwisterFast
}
