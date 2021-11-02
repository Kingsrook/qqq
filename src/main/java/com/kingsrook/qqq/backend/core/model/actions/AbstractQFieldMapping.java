package com.kingsrook.qqq.backend.core.model.actions;


/*******************************************************************************
 ** For bulk-loads, define where a QField comes from in an input data source.
 **
 *******************************************************************************/
public abstract class AbstractQFieldMapping<T>
{

   /*******************************************************************************
    ** Enum to define the types of sources a mapping may use
    *******************************************************************************/
   @SuppressWarnings("rawtypes")
   public enum SourceType
   {
      KEY(String.class),
      INDEX(Integer.class);

      private Class sourceClass;



      /*******************************************************************************
       ** enum constructor
       *******************************************************************************/
      SourceType(Class sourceClass)
      {
         this.sourceClass = sourceClass;
      }



      /*******************************************************************************
       ** Getter for sourceClass
       **
       *******************************************************************************/
      public Class getSourceClass()
      {
         return sourceClass;
      }
   }



   /*******************************************************************************
    ** For a given field, return its source - a key (e.g., from a json object or csv
    ** with a header row) or an index (for a csv w/o a header)
    *******************************************************************************/
   public abstract T getFieldSource(String fieldName);


   /*******************************************************************************
    ** for a mapping instance, get what its source-type is
    *******************************************************************************/
   public abstract SourceType getSourceType();
}
