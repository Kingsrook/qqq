package com.kingsrook.qqq.api.model.metadata.processes;


/*******************************************************************************
 **
 *******************************************************************************/
public class ApiProcessInput
{
   private ApiProcessInputFieldsContainer queryStringParams;
   private ApiProcessInputFieldsContainer formParams;
   private ApiProcessInputFieldsContainer recordBodyParams;



   /*******************************************************************************
    **
    *******************************************************************************/
   public String getRecordIdsParamName()
   {
      if(queryStringParams != null && queryStringParams.getRecordIdsField() != null)
      {
         return (queryStringParams.getRecordIdsField().getName());
      }

      if(formParams != null && formParams.getRecordIdsField() != null)
      {
         return (formParams.getRecordIdsField().getName());
      }

      if(recordBodyParams != null && recordBodyParams.getRecordIdsField() != null)
      {
         return (recordBodyParams.getRecordIdsField().getName());
      }

      return (null);
   }



   /*******************************************************************************
    ** Getter for queryStringParams
    *******************************************************************************/
   public ApiProcessInputFieldsContainer getQueryStringParams()
   {
      return (this.queryStringParams);
   }



   /*******************************************************************************
    ** Setter for queryStringParams
    *******************************************************************************/
   public void setQueryStringParams(ApiProcessInputFieldsContainer queryStringParams)
   {
      this.queryStringParams = queryStringParams;
   }



   /*******************************************************************************
    ** Fluent setter for queryStringParams
    *******************************************************************************/
   public ApiProcessInput withQueryStringParams(ApiProcessInputFieldsContainer queryStringParams)
   {
      this.queryStringParams = queryStringParams;
      return (this);
   }



   /*******************************************************************************
    ** Getter for formParams
    *******************************************************************************/
   public ApiProcessInputFieldsContainer getFormParams()
   {
      return (this.formParams);
   }



   /*******************************************************************************
    ** Setter for formParams
    *******************************************************************************/
   public void setFormParams(ApiProcessInputFieldsContainer formParams)
   {
      this.formParams = formParams;
   }



   /*******************************************************************************
    ** Fluent setter for formParams
    *******************************************************************************/
   public ApiProcessInput withFormParams(ApiProcessInputFieldsContainer formParams)
   {
      this.formParams = formParams;
      return (this);
   }



   /*******************************************************************************
    ** Getter for recordBodyParams
    *******************************************************************************/
   public ApiProcessInputFieldsContainer getObjectBodyParams()
   {
      return (this.recordBodyParams);
   }



   /*******************************************************************************
    ** Setter for recordBodyParams
    *******************************************************************************/
   public void setRecordBodyParams(ApiProcessInputFieldsContainer recordBodyParams)
   {
      this.recordBodyParams = recordBodyParams;
   }



   /*******************************************************************************
    ** Fluent setter for recordBodyParams
    *******************************************************************************/
   public ApiProcessInput withRecordBodyParams(ApiProcessInputFieldsContainer recordBodyParams)
   {
      this.recordBodyParams = recordBodyParams;
      return (this);
   }
}
