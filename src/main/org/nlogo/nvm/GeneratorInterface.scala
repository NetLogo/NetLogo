package org.nlogo.nvm

trait GeneratorInterface {
  def generate(): Array[Command]
}
