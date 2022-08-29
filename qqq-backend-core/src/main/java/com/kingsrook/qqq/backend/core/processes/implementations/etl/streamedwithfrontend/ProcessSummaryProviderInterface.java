package com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend;


import java.util.ArrayList;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;


/*******************************************************************************
 **
 *******************************************************************************/
public interface ProcessSummaryProviderInterface
{

   /*******************************************************************************
    **
    *******************************************************************************/
   ArrayList<ProcessSummaryLine> getProcessSummary(boolean isForResultScreen);

}
