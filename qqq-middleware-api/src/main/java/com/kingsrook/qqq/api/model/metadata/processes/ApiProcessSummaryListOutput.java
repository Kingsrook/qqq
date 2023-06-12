package com.kingsrook.qqq.api.model.metadata.processes;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryFilterLink;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLine;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryLineInterface;
import com.kingsrook.qqq.backend.core.model.actions.processes.ProcessSummaryRecordLink;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jetty.http.HttpStatus;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiProcessSummaryListOutput implements ApiProcessOutputInterface
{
   private static final QLogger LOG = QLogger.getLogger(ApiProcessSummaryListOutput.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public HttpStatus.Code getSuccessStatusCode(RunProcessInput runProcessInput, RunProcessOutput runProcessOutput)
   {
      List<ProcessSummaryLineInterface> processSummaryLineInterfaces = (List<ProcessSummaryLineInterface>) runProcessOutput.getValues().get("processResults");
      if(processSummaryLineInterfaces.isEmpty())
      {
         //////////////////////////////////////////////////////////////////////////
         // if there are no summary lines, all we can return is 204 - no content //
         //////////////////////////////////////////////////////////////////////////
         return (HttpStatus.Code.NO_CONTENT);
      }
      else
      {
         ///////////////////////////////////////////////////////////////////////////////////
         // else if there are summary lines, we'll represent them as a 207 - multi-status //
         ///////////////////////////////////////////////////////////////////////////////////
         return (HttpStatus.Code.MULTI_STATUS);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public Serializable getOutputForProcess(RunProcessInput runProcessInput, RunProcessOutput runProcessOutput) throws QException
   {
      try
      {
         ArrayList<Serializable>           apiOutput                    = new ArrayList<>();
         List<ProcessSummaryLineInterface> processSummaryLineInterfaces = (List<ProcessSummaryLineInterface>) runProcessOutput.getValues().get("processResults");
         for(ProcessSummaryLineInterface processSummaryLineInterface : processSummaryLineInterfaces)
         {
            if(processSummaryLineInterface instanceof ProcessSummaryLine processSummaryLine)
            {
               processSummaryLine.setCount(1);
               processSummaryLine.prepareForFrontend(true);

               List<Serializable> primaryKeys = processSummaryLine.getPrimaryKeys();
               if(CollectionUtils.nullSafeHasContents(primaryKeys))
               {
                  for(Serializable primaryKey : primaryKeys)
                  {
                     HashMap<String, Serializable> map = toMap(processSummaryLine);
                     map.put("id", primaryKey);
                     apiOutput.add(map);
                  }
               }
               else
               {
                  apiOutput.add(toMap(processSummaryLine));
               }
            }
            else if(processSummaryLineInterface instanceof ProcessSummaryRecordLink processSummaryRecordLink)
            {
               throw new NotImplementedException("ProcessSummaryRecordLink handling");
            }
            else if(processSummaryLineInterface instanceof ProcessSummaryFilterLink processSummaryFilterLink)
            {
               throw new NotImplementedException("ProcessSummaryFilterLink handling");
            }
            else
            {
               throw new NotImplementedException("Unknown ProcessSummaryLineInterface handling");
            }
         }

         return (apiOutput);
      }
      catch(Exception e)
      {
         LOG.warn("Error getting api output for process", e);
         throw (new QException("Error generating process output", e));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:indentation")
   private static HashMap<String, Serializable> toMap(ProcessSummaryLine processSummaryLine)
   {
      HashMap<String, Serializable> map = new HashMap<>();
      HttpStatus.Code code = switch(processSummaryLine.getStatus())
      {
         case OK, WARNING, INFO -> HttpStatus.Code.OK;
         case ERROR -> HttpStatus.Code.INTERNAL_SERVER_ERROR;
      };

      String messagePrefix = switch(processSummaryLine.getStatus())
      {
         case OK, INFO, ERROR -> "";
         case WARNING -> "Warning: ";
      };

      map.put("statusCode", code.getCode());
      map.put("statusText", code.getMessage());
      map.put("message", messagePrefix + processSummaryLine.getMessage());

      return (map);
   }

}
