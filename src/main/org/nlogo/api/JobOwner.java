package org.nlogo.api;

public interface JobOwner
    extends SourceOwner {
  String displayName();

  boolean isButton();

  boolean isTurtleForeverButton();

  boolean isLinkForeverButton();

  boolean ownsPrimaryJobs();

  boolean isCommandCenter();

  org.nlogo.util.MersenneTwisterFast random();
}

