package com.kingsrook.qqq.api;


import java.math.BigDecimal;
import com.kingsrook.qqq.backend.core.actions.processes.BackendStep;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepOutput;


/*******************************************************************************
 **
 *******************************************************************************/
public class GetPersonInfoStep implements BackendStep
{
   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run(RunBackendStepInput runBackendStepInput, RunBackendStepOutput runBackendStepOutput) throws QException
   {
      runBackendStepOutput.addValue("density", new BigDecimal("3.50"));
      runBackendStepOutput.addValue("daysOld", runBackendStepInput.getValueInteger("age") * 365);
      runBackendStepOutput.addValue("nickname", "Guy from " + runBackendStepInput.getValueString("homeTown"));
   }

}
