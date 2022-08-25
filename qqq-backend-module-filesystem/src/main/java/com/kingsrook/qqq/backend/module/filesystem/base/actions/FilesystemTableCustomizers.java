package com.kingsrook.qqq.backend.module.filesystem.base.actions;


import java.util.function.Function;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizer;


/*******************************************************************************
 **
 *******************************************************************************/
public enum FilesystemTableCustomizers
{
   POST_READ_FILE(new TableCustomizer("postReadFile", Function.class, ((Object x) ->
   {
      Function<String, String> function = (Function<String, String>) x;
      String                   output   = function.apply(new String());
   })));

   private final TableCustomizer tableCustomizer;



   /*******************************************************************************
    **
    *******************************************************************************/
   FilesystemTableCustomizers(TableCustomizer tableCustomizer)
   {
      this.tableCustomizer = tableCustomizer;
   }



   /*******************************************************************************
    ** Get the FilesystemTableCustomer for a given role (e.g., the role used in meta-data, not
    ** the enum-constant name).
    *******************************************************************************/
   public static FilesystemTableCustomizers forRole(String name)
   {
      for(FilesystemTableCustomizers value : values())
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
