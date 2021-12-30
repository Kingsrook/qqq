/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.model.metadata.processes;


import com.kingsrook.qqq.backend.core.model.metadata.QCodeReference;


/*******************************************************************************
 ** Meta-Data to define a function in a QQQ instance.
 **
 *******************************************************************************/
public class QFunctionMetaData
{
   private String name;
   private String label;
   private QFunctionInputMetaData inputMetaData;
   private QFunctionOutputMetaData outputMetaData;
   private QCodeReference code;
   private QOutputView outputView;



   /*******************************************************************************
    ** Getter for name
    **
    *******************************************************************************/
   public String getName()
   {
      return name;
   }



   /*******************************************************************************
    ** Setter for name
    **
    *******************************************************************************/
   public void setName(String name)
   {
      this.name = name;
   }



   /*******************************************************************************
    ** Setter for name
    **
    *******************************************************************************/
   public QFunctionMetaData withName(String name)
   {
      this.name = name;
      return (this);
   }



   /*******************************************************************************
    ** Getter for label
    **
    *******************************************************************************/
   public String getLabel()
   {
      return label;
   }



   /*******************************************************************************
    ** Setter for label
    **
    *******************************************************************************/
   public void setLabel(String label)
   {
      this.label = label;
   }



   /*******************************************************************************
    ** Setter for label
    **
    *******************************************************************************/
   public QFunctionMetaData withLabel(String label)
   {
      this.label = label;
      return (this);
   }



   /*******************************************************************************
    ** Getter for inputData
    **
    *******************************************************************************/
   public QFunctionInputMetaData getInputMetaData()
   {
      return inputMetaData;
   }



   /*******************************************************************************
    ** Setter for inputData
    **
    *******************************************************************************/
   public void setInputMetaData(QFunctionInputMetaData inputMetaData)
   {
      this.inputMetaData = inputMetaData;
   }



   /*******************************************************************************
    ** Setter for inputData
    **
    *******************************************************************************/
   public QFunctionMetaData withInputData(QFunctionInputMetaData inputData)
   {
      this.inputMetaData = inputData;
      return (this);
   }



   /*******************************************************************************
    ** Getter for outputData
    **
    *******************************************************************************/
   public QFunctionOutputMetaData getOutputMetaData()
   {
      return outputMetaData;
   }



   /*******************************************************************************
    ** Setter for outputData
    **
    *******************************************************************************/
   public void setOutputMetaData(QFunctionOutputMetaData outputMetaData)
   {
      this.outputMetaData = outputMetaData;
   }



   /*******************************************************************************
    ** Setter for outputData
    **
    *******************************************************************************/
   public QFunctionMetaData withOutputMetaData(QFunctionOutputMetaData outputMetaData)
   {
      this.outputMetaData = outputMetaData;
      return(this);
   }



   /*******************************************************************************
    ** Getter for code
    **
    *******************************************************************************/
   public QCodeReference getCode()
   {
      return code;
   }



   /*******************************************************************************
    ** Setter for code
    **
    *******************************************************************************/
   public void setCode(QCodeReference code)
   {
      this.code = code;
   }



   /*******************************************************************************
    ** Setter for code
    **
    *******************************************************************************/
   public QFunctionMetaData withCode(QCodeReference code)
   {
      this.code = code;
      return (this);
   }



   /*******************************************************************************
    ** Getter for outputView
    **
    *******************************************************************************/
   public QOutputView getOutputView()
   {
      return outputView;
   }



   /*******************************************************************************
    ** Setter for outputView
    **
    *******************************************************************************/
   public void setOutputView(QOutputView outputView)
   {
      this.outputView = outputView;
   }



   /*******************************************************************************
    ** Setter for outputView
    **
    *******************************************************************************/
   public QFunctionMetaData withOutputView(QOutputView outputView)
   {
      this.outputView = outputView;
      return (this);
   }
}
