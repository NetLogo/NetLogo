package org.nlogo.swing

import java.beans.PropertyChangeListener

import javax.swing.event.SwingPropertyChangeSupport

trait HasPropertyChangeSupport {
  def isNotifyOnEDT = true
  val propertyChangeSupport = new SwingPropertyChangeSupport(this, isNotifyOnEDT)
  def addPropertyChangeListener(propertyName: String, listener: PropertyChangeListener): Unit = {
    propertyChangeSupport.addPropertyChangeListener(propertyName, listener)
  }
  def removePropertyChangeListener(propertyName: String, listener: PropertyChangeListener): Unit = {
    propertyChangeSupport.removePropertyChangeListener(propertyName, listener)
  }
  def addPropertyChangeListener(listener: PropertyChangeListener): Unit = {
    propertyChangeSupport.addPropertyChangeListener(listener)
  }
  def removePropertyChangeListener(listener: PropertyChangeListener): Unit = {
    propertyChangeSupport.removePropertyChangeListener(listener)
  }
}
