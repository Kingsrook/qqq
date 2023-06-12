package com.kingsrook.qqq.api.model.metadata.processes;


import java.util.ArrayList;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QStepMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiProcessInputFieldsContainer
{
   private QFieldMetaData       recordIdsField;
   private List<QFieldMetaData> fields;



   /*******************************************************************************
    **
    *******************************************************************************/
   public ApiProcessInputFieldsContainer withInferredInputFields(QProcessMetaData processMetaData)
   {
      fields = new ArrayList<>();
      for(QStepMetaData stepMetaData : CollectionUtils.nonNullList(processMetaData.getStepList()))
      {
         if(stepMetaData instanceof QFrontendStepMetaData frontendStep)
         {
            fields.addAll(frontendStep.getInputFields());
         }
      }

      return (this);
   }



   /*******************************************************************************
    ** Getter for recordIdsField
    *******************************************************************************/
   public QFieldMetaData getRecordIdsField()
   {
      return (this.recordIdsField);
   }



   /*******************************************************************************
    ** Setter for recordIdsField
    *******************************************************************************/
   public void setRecordIdsField(QFieldMetaData recordIdsField)
   {
      this.recordIdsField = recordIdsField;
   }



   /*******************************************************************************
    ** Fluent setter for recordIdsField
    *******************************************************************************/
   public ApiProcessInputFieldsContainer withRecordIdsField(QFieldMetaData recordIdsField)
   {
      this.recordIdsField = recordIdsField;
      return (this);
   }



   /*******************************************************************************
    ** Getter for fields
    *******************************************************************************/
   public List<QFieldMetaData> getFields()
   {
      return (this.fields);
   }



   /*******************************************************************************
    ** Setter for fields
    *******************************************************************************/
   public void setFields(List<QFieldMetaData> fields)
   {
      this.fields = fields;
   }



   /*******************************************************************************
    ** Fluent setter for fields
    *******************************************************************************/
   public ApiProcessInputFieldsContainer withField(QFieldMetaData field)
   {
      if(this.fields == null)
      {
         this.fields = new ArrayList<>();
      }
      this.fields.add(field);
      return (this);
   }



   /*******************************************************************************
    ** Fluent setter for fields
    *******************************************************************************/
   public ApiProcessInputFieldsContainer withFields(List<QFieldMetaData> fields)
   {
      this.fields = fields;
      return (this);
   }
}
