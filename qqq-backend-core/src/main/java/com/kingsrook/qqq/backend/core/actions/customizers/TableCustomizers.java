package com.kingsrook.qqq.backend.core.actions.customizers;


import java.util.function.Function;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 ** Enum definition of possible table customizers - "roles" for custom code that
 ** can be applied to tables.
 **
 ** Works with TableCustomizer (singular version of this name) objects, during
 ** instance validation, to provide validation of the referenced code (and to
 ** make such validation from sub-backend-modules possible in the future).
 **
 ** The idea of the 3rd argument here is to provide a way that we can enforce
 ** the type-parameters for the custom code.  E.g., if it's a Function - how
 ** can we check at run-time that the type-params are correct?  We couldn't find
 ** how to do this "reflectively", so we can instead try to run the custom code,
 ** passing it objects of the type that this customizer expects, and a validation
 ** error will raise upon ClassCastException...  This maybe could improve!
 *******************************************************************************/
public enum TableCustomizers
{
   POST_QUERY_RECORD(new TableCustomizer("postQueryRecord", Function.class, ((Object x) ->
   {
      @SuppressWarnings("unchecked")
      Function<QRecord, QRecord> function = (Function<QRecord, QRecord>) x;
      QRecord                    output   = function.apply(new QRecord());
   })));


   private final TableCustomizer tableCustomizer;



   /*******************************************************************************
    **
    *******************************************************************************/
   TableCustomizers(TableCustomizer tableCustomizer)
   {
      this.tableCustomizer = tableCustomizer;
   }



   /*******************************************************************************
    ** Get the TableCustomer for a given role (e.g., the role used in meta-data, not
    ** the enum-constant name).
    *******************************************************************************/
   public static TableCustomizers forRole(String name)
   {
      for(TableCustomizers value : values())
      {
         if(value.tableCustomizer.getRole().equals(name))
         {
            return (value);
         }
      }

      return (null);
   }



   /*******************************************************************************
    ** Getter for tableCustomizer
    **
    *******************************************************************************/
   public TableCustomizer getTableCustomizer()
   {
      return tableCustomizer;
   }



   /*******************************************************************************
    ** get the role from the tableCustomizer
    **
    *******************************************************************************/
   public String getRole()
   {
      return (tableCustomizer.getRole());
   }

}
