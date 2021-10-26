package com.kingsrook.qqq.backend.core.model.actions;


/*******************************************************************************
 **
 *******************************************************************************/
public abstract class AbstractQFieldMapping<T>
{
   /*******************************************************************************
    ** Returns the fieldName for the input 'key' or 'index'.
    *******************************************************************************/
   public abstract String getMappedField(T t);
}
