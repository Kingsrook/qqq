/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.interfaces.mock;


import com.kingsrook.qqq.backend.core.interfaces.FunctionBody;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;


/*******************************************************************************
 ** Mock implementation of a FunctionBody.
 **
 ** Basically just passes data from the request to the response.
 *******************************************************************************/
public class MockFunctionBody implements FunctionBody
{
   @Override
   public void run(RunFunctionRequest runFunctionRequest, RunFunctionResult runFunctionResult)
   {
      runFunctionResult.getRecords().forEach(r -> r.setValue("mockValue", "Ha ha!"));

      runFunctionResult.setValues(runFunctionRequest.getValues());
      runFunctionResult.addValue("mockValue", "You so silly");
   }
}
