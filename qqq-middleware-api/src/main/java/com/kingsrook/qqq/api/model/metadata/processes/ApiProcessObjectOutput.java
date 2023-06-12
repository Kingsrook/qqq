package com.kingsrook.qqq.api.model.metadata.processes;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessInput;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunProcessOutput;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiProcessObjectOutput implements ApiProcessOutputInterface
{
   private List<QFieldMetaData> outputFields;



   /*******************************************************************************
    **
    ******************************************************************************/
   @Override
   public Serializable getOutputForProcess(RunProcessInput runProcessInput, RunProcessOutput runProcessOutput)
   {
      LinkedHashMap<String, Serializable> outputMap = new LinkedHashMap<>();

      for(QFieldMetaData outputField : CollectionUtils.nonNullList(getOutputFields()))
      {
         outputMap.put(outputField.getName(), runProcessOutput.getValues().get(outputField.getName()));
      }

      return (outputMap);
   }



   /*******************************************************************************
    ** Getter for outputFields
    *******************************************************************************/
   public List<QFieldMetaData> getOutputFields()
   {
      return (this.outputFields);
   }



   /*******************************************************************************
    ** Setter for outputFields
    *******************************************************************************/
   public void setOutputFields(List<QFieldMetaData> outputFields)
   {
      this.outputFields = outputFields;
   }



   /*******************************************************************************
    ** Fluent setter for outputFields
    *******************************************************************************/
   public ApiProcessObjectOutput withOutputFields(List<QFieldMetaData> outputFields)
   {
      this.outputFields = outputFields;
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for a single outputField
    *******************************************************************************/
   public ApiProcessObjectOutput withOutputField(QFieldMetaData outputField)
   {
      if(this.outputFields == null)
      {
         this.outputFields = new ArrayList<>();
      }
      this.outputFields.add(outputField);
      return (this);
   }

}
