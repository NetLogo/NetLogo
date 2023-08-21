package org.nlogo.lab

trait PostProcessor
{
  type Data
  def extractData(): Option[Data]
  def process(): Unit
}