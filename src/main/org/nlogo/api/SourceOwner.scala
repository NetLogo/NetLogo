package org.nlogo.api

trait SourceOwner {
  def classDisplayName: String
  def headerSource: String
  def innerSource: String
  def innerSource(s: String)
  def source: String
  def agentClass: Class[_]
}
