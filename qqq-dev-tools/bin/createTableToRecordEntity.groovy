#!/usr/bin/env groovy

/*******************************************************************************
 ** Script to convert a CREATE TABLE statement  to fields for a QRecordEntity
 *******************************************************************************/

if (args.length < 1)
{
   System.out.println("Usage: ${this.class.getSimpleName()} EntityClassName [writeWholeClass] [writeTableMetaData]")
   System.exit(1);
}

String className = args[0]
boolean writeWholeClass = args.length > 1 ? args[1] : false;
boolean writeTableMetaData = args.length > 2 ? args[2] : false;

println("Please paste in a CREATE TABLE statement (then a newline and an end-of-file (CTRL-D))")
def reader = new BufferedReader(new InputStreamReader(System.in))
String line
String allFieldNames = ""
String dataFieldNames = ""
List<String> fieldNameList = new ArrayList<>();
List<String> fieldTypeList = new ArrayList<>();

StringBuilder output = new StringBuilder();
if(writeWholeClass)
{
   String classNameLcFirst = className.substring(0, 1).toLowerCase() + className.substring(1);
   output.append("""
/*******************************************************************************
 ** Entity bean for %s table
 *******************************************************************************/
public class %s extends QRecordEntity
{
      public static final String TABLE_NAME = "%s";
   """.formatted(className, className, classNameLcFirst));
}

String tableName = "";
while((line = reader.readLine()) != null)
{
   line = line.trim();
   String[] words = line.split("\\s+");
   if(words.length < 2)
   {
      continue;
   }
   if(words[0].equalsIgnoreCase("create"))
   {
      if(words.length >= 2)
      {
         tableName = words[2]
      }
      continue;
   }

   String columnName = words[0];
   String columnType = words[1];

   String fieldName = columNameToFieldName(columnName)
   String fieldType = columTypeToFieldType(columnType)
   String attributes = getFieldAttributes(fieldName);

   fieldNameList.add(fieldName);
   fieldTypeList.add(fieldType);

   output.append("""
      @QField(%s)
      private %s %s;
   """.formatted(attributes, fieldType, fieldName))

   allFieldNames += '"' + fieldName + '",'
   if(!fieldName.equals("id") && !fieldName.equals("createDate") && !fieldName.equals("modifyDate"))
   {
      dataFieldNames += '"' + fieldName + '",'
   }
}

if(dataFieldNames.length() > 0)
{
   dataFieldNames = dataFieldNames.substring(0, dataFieldNames.length() - 1);
}

if(writeWholeClass)
{
   output.append("""


   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public {className}()
   {
   }



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public {className}(QRecord qRecord) throws QException
   {
      populateFromQRecord(qRecord);
   }

   """.replaceAll("\\{className}", className));
}

for(int i = 0; i < fieldNameList.size(); i++)
{
   String fieldName = fieldNameList.get(i);
   String fieldType = fieldTypeList.get(i);
   String fieldNameUcFirst = fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

   output.append("""

   /*******************************************************************************
    ** Getter for {fieldName}
    **
    *******************************************************************************/
   public {fieldType} get{fieldNameUcFirst}()
   {
      return {fieldName};
   }
   
   
   
   /*******************************************************************************
    ** Setter for {fieldName}
    **
    *******************************************************************************/
   public void set{fieldNameUcFirst}({fieldType} {fieldName})
   {
      this.{fieldName} = {fieldName};
   }
   
   
   /*******************************************************************************
    ** Fluent setter for {fieldName}
    **
    *******************************************************************************/
   public {className} with{fieldNameUcFirst}({fieldType} {fieldName})
   {
      this.{fieldName} = {fieldName};
      return (this);
   }
   
   """
      .replaceAll("\\{fieldNameUcFirst}", fieldNameUcFirst)
      .replaceAll("\\{fieldName}", fieldName)
      .replaceAll("\\{fieldType}", fieldType)
      .replaceAll("\\{className}", className)
   );
}

if(writeWholeClass)
{
   output.append("\n}");
}

if(writeTableMetaData)
{
   output.append("""



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QTableMetaData defineTable{className}() throws QException
   {
      QTableMetaData qTableMetaData = new QTableMetaData()
         .withName({className}.TABLE_NAME)
         .withRecordLabelFormat("%s")
         .withRecordLabelFields("TODO")
         .withBackendName(TODO)
         .withPrimaryKeyField("id")
         .withFieldsFromEntity({className}.class)
         .withBackendDetails(new RDBMSTableBackendDetails()
            .withTableName("{tableName}")
         )
         .withSection(new QFieldSection("identity", new QIcon().withName("badge"), Tier.T1, List.of("id")))
         .withSection(new QFieldSection("data", new QIcon().withName("text_snippet"), Tier.T2, List.of({dataFieldNames})))
         .withSection(new QFieldSection("dates", new QIcon().withName("calendar_month"), Tier.T3, List.of("createDate", "modifyDate")));
         
      QInstanceEnricher.setInferredFieldBackendNames(qTableMetaData);
         
      return (qTableMetaData);
   }

   """
      .replaceAll("\\{className}", className)
      .replaceAll("\\{tableName}", tableName)
      .replaceAll("\\{dataFieldNames}", dataFieldNames)
   );
}

println(output);
println()

if(!writeTableMetaData)
{
   println("All field names (e.g., for sections):")
   allFieldNames = allFieldNames.substring(0, allFieldNames.length() - 1);
   println(allFieldNames);
}
println();



private static String columNameToFieldName(String columnName)
{
   String fieldName = "";
   char lastChar = 0;
   for(int i = 0; i < columnName.length(); i++)
   {
      char c = columnName.charAt(i);
      if(c == '_')
      {
         // noop
      } else if(lastChar == '_')
      {
         fieldName += new String(c).toUpperCase();
      } else
      {
         fieldName += c;
      }

      lastChar = c;
   }
   fieldName
}



private static String columTypeToFieldType(String columnType)
{
   if(columnType.toUpperCase().startsWith("INT"))
   {
      return ("Integer");
   }
   if(columnType.toUpperCase().startsWith("VARCHAR") || columnType.toUpperCase().startsWith("TEXT"))
   {
      return ("String");
   }
   if(columnType.toUpperCase().startsWith("TIMESTAMP"))
   {
      return ("Instant");
   }
   if(columnType.toUpperCase().startsWith("DATE"))
   {
      return ("LocalDate");
   }
   if(columnType.toUpperCase().startsWith("DECIMAL"))
   {
      return ("BigDecimal");
   }
   if(columnType.toUpperCase().startsWith("BOOL"))
   {
      return ("Boolean");
   }
   return ("FixMeUnknownType");
}



private static String getFieldAttributes(String s)
{
   StringBuilder rs = new StringBuilder("");
   if(s.equals("id") || s.equals("createDate") || s.equals("modifyDate"))
   {
      rs.append("isEditable = false");
   }
   return (rs.toString());
}