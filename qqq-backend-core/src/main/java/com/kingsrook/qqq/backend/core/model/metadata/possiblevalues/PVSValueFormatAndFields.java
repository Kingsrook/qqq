package com.kingsrook.qqq.backend.core.model.metadata.possiblevalues;


import java.util.List;


/*******************************************************************************
 ** Define some standard ways to format the value portion of a PossibleValueSource.
 **
 ** Can be passed to short-cut {set,with}ValueFormatAndFields methods in QPossibleValueSource
 ** class, or the format & field properties can be extracted and passed to regular field-level setters.
 *******************************************************************************/
public enum PVSValueFormatAndFields
{
   LABEL_ONLY("%s", "label"),
   LABEL_PARENS_ID("%s (%s)", "label", "id"),
   ID_COLON_LABEL("%s: %s", "id", "label");


   private final String       format;
   private final List<String> fields;



   /*******************************************************************************
    **
    *******************************************************************************/
   PVSValueFormatAndFields(String format, String... fields)
   {
      this.format = format;
      this.fields = List.of(fields);
   }



   /*******************************************************************************
    ** Getter for format
    **
    *******************************************************************************/
   public String getFormat()
   {
      return format;
   }



   /*******************************************************************************
    ** Getter for fields
    **
    *******************************************************************************/
   public List<String> getFields()
   {
      return fields;
   }
}
