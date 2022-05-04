/*
 * Copyright © 2021-2022. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.actions.processes.person.addtopeoplesage;


import java.time.LocalDate;
import java.time.Period;
import com.kingsrook.qqq.backend.core.interfaces.FunctionBody;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public class GetAgeStatistics implements FunctionBody
{
   @Override
   public void run(RunFunctionRequest runFunctionRequest, RunFunctionResult runFunctionResult)
   {
      Integer min = null;
      Integer max = null;
      LocalDate now = LocalDate.now();
      for(QRecord record : runFunctionRequest.getRecords())
      {
         LocalDate birthDate = record.getValueDate("birthDate");
         Period until = birthDate.until(now);
         int age = until.getYears();
         record.setValue("age", age);
         System.out.println(birthDate + " -> " + age);
         min = (min == null || age < min) ? age : min;
         max = (max == null || age > max) ? age : max;
      }

      runFunctionResult.addValue("minAge", min);
      runFunctionResult.addValue("maxAge", max);
   }
}
