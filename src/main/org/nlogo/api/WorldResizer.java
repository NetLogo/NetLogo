package org.nlogo.api;

/// used by Importer for resizing the world during an import.
/// also used by BehaviorSpace when the experiment varies the world size

public interface WorldResizer {
  void resizeView();

  void patchSize(double patchSize);

  void setDimensions(WorldDimensions dim);

  void setDimensions(WorldDimensions dim, double patchSize);
}
