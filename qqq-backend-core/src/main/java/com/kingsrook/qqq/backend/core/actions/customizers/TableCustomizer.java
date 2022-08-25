package com.kingsrook.qqq.backend.core.actions.customizers;


import java.util.function.Consumer;


/*******************************************************************************
 ** Object used by TableCustomizers enum (and similar enums in backend modules)
 ** to assist with definition and validation of Customizers applied to tables.
 *******************************************************************************/
public class TableCustomizer
{
   private final String           role;
   private final Class<?>         expectedType;
   private final Consumer<Object> validationFunction;



   /*******************************************************************************
    **
    *******************************************************************************/
   public TableCustomizer(String role, Class<?> expectedType, Consumer<Object> validationFunction)
   {
      this.role = role;
      this.expectedType = expectedType;
      this.validationFunction = validationFunction;
   }



   /*******************************************************************************
    ** Getter for role
    **
    *******************************************************************************/
   public String getRole()
   {
      return role;
   }



   /*******************************************************************************
    ** Getter for expectedType
    **
    *******************************************************************************/
   public Class<?> getExpectedType()
   {
      return expectedType;
   }



   /*******************************************************************************
    ** Getter for validationFunction
    **
    *******************************************************************************/
   public Consumer<Object> getValidationFunction()
   {
      return validationFunction;
   }
}
