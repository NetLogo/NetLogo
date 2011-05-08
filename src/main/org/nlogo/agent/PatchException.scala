package org.nlogo.agent

class PatchException(val patch: Patch) extends Exception(patch.toString)
