/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions.processes.person.addtopeoplesage;


import com.kingsrook.qqq.backend.core.interfaces.FunctionBody;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public class AddAge implements FunctionBody
{
   @Override
   public void run(RunFunctionRequest runFunctionRequest, RunFunctionResult runFunctionResult)
   {
      int totalYearsAdded = 0;
      Integer yearsToAdd = runFunctionRequest.getValueInteger("yearsToAdd");
      for(QRecord record : runFunctionRequest.getRecords())
      {
         Integer age = record.getValueInteger("age");
         age += yearsToAdd;
         totalYearsAdded += yearsToAdd;
         record.setValue("age", age);
      }
      runFunctionResult.addValue("totalYearsAdded", totalYearsAdded);
   }
}
