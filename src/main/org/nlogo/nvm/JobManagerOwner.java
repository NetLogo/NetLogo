package org.nlogo.nvm;

import org.nlogo.api.JobOwner;

public interface JobManagerOwner {
  void runtimeError(JobOwner owner, Context context,
                    Instruction instruction, Exception ex);

  void ownerFinished(JobOwner owner);

  void updateDisplay(boolean haveWorldLockAlready);

  void periodicUpdate();
}
