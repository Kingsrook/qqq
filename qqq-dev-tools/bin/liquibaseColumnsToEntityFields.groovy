#!/usr/bin/env groovy

/*******************************************************************************
 ** Script to convert a list of columnNames from liquibase XML (on stdin)
 ** to fields for a QRecordEntity (on stdout)
 *******************************************************************************/

def reader = new BufferedReader(new InputStreamReader(System.in))
String line
String allFieldNames = ""
while ((line = reader.readLine()) != null)
{
   line = line.trim();
   if (!line.matches(".* name=\".*") || !line.matches(".* type=\".*") )
   {
      continue;
   }

   String columnName = line.replaceFirst(".* name=\"", "").replaceFirst("\".*", "");
   String columnType = line.replaceFirst(".* type=\"", "").replaceFirst("\".*", "");

   String fieldName = columNameToFieldName(columnName)
   String fieldType = columTypeToFieldType(columnType)

   printf("""
      @QField()
      private %s %s;
   """, fieldType, fieldName)

   allFieldNames += '"' + fieldName + '",'
}

println()
println("All field names:")
println(allFieldNames);

private static String columNameToFieldName(String columnName)
{
   String fieldName = "";
   char lastChar = 0;
   for (int i = 0; i < columnName.length(); i++)
   {
      char c = columnName.charAt(i);
      if (c == '_')
      {
         // noop
      }
      else if (lastChar == '_')
      {
         fieldName += new String(c).toUpperCase();
      }
      else
      {
         fieldName += c;
      }

      lastChar = c;
   }
   fieldName
}


private static String columTypeToFieldType(String columnType)
{
   if (columnType.toUpperCase().startsWith("INT"))
   {
      return ("Integer");
   }
   if (columnType.toUpperCase().startsWith("VARCHAR"))
   {
      return ("String");
   }
   if (columnType.toUpperCase().startsWith("TIMESTAMP"))
   {
      return ("Instant");
   }
   if (columnType.toUpperCase().startsWith("DATE"))
   {
      return ("LocalDate");
   }
   if (columnType.toUpperCase().startsWith("DECIMAL"))
   {
      return ("BigDecimal");
   }
   if (columnType.toUpperCase().startsWith("BOOL"))
   {
      return ("Boolean");
   }
   return ("FixMeUnknownType");
}