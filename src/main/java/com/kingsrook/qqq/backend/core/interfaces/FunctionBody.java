/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.interfaces;


import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunFunctionResult;


/*******************************************************************************
 ** TODO - document!
 *******************************************************************************/
public interface FunctionBody
{
   /*******************************************************************************
    ** TODO - document!
    *******************************************************************************/
   void run(RunFunctionRequest runFunctionRequest, RunFunctionResult runFunctionResult);
}
