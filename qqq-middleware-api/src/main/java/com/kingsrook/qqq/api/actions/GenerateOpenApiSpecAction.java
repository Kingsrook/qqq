/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
 * 651 N Broad St Ste 205 # 6917 | Middletown DE 19709 | United States
 * contact@kingsrook.com
 * https://github.com/Kingsrook/
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.kingsrook.qqq.api.actions;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.api.model.actions.GenerateOpenApiSpecInput;
import com.kingsrook.qqq.api.model.actions.GenerateOpenApiSpecOutput;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.model.openapi.Components;
import com.kingsrook.qqq.api.model.openapi.Contact;
import com.kingsrook.qqq.api.model.openapi.Content;
import com.kingsrook.qqq.api.model.openapi.Example;
import com.kingsrook.qqq.api.model.openapi.ExampleWithListValue;
import com.kingsrook.qqq.api.model.openapi.ExampleWithSingleValue;
import com.kingsrook.qqq.api.model.openapi.Info;
import com.kingsrook.qqq.api.model.openapi.Method;
import com.kingsrook.qqq.api.model.openapi.OAuth2;
import com.kingsrook.qqq.api.model.openapi.OAuth2Flow;
import com.kingsrook.qqq.api.model.openapi.OpenAPI;
import com.kingsrook.qqq.api.model.openapi.Parameter;
import com.kingsrook.qqq.api.model.openapi.Path;
import com.kingsrook.qqq.api.model.openapi.RequestBody;
import com.kingsrook.qqq.api.model.openapi.Response;
import com.kingsrook.qqq.api.model.openapi.Schema;
import com.kingsrook.qqq.api.model.openapi.SecurityScheme;
import com.kingsrook.qqq.api.model.openapi.Server;
import com.kingsrook.qqq.api.model.openapi.Tag;
import com.kingsrook.qqq.backend.core.actions.AbstractQActionFunction;
import com.kingsrook.qqq.backend.core.actions.permissions.PermissionsHelper;
import com.kingsrook.qqq.backend.core.actions.permissions.TablePermissionSubType;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.YamlUtils;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;


/*******************************************************************************
 **
 *******************************************************************************/
public class GenerateOpenApiSpecAction extends AbstractQActionFunction<GenerateOpenApiSpecInput, GenerateOpenApiSpecOutput>
{

   /*******************************************************************************
    **
    *******************************************************************************/
   public GenerateOpenApiSpecOutput execute(GenerateOpenApiSpecInput input) throws QException
   {
      String version = input.getVersion();

      QInstance qInstance = QContext.getQInstance();

      OpenAPI openAPI = new OpenAPI()
         .withVersion("3.0.3")
         .withInfo(new Info()
            .withTitle("QQQ API")
            .withDescription("This is an openAPI built by QQQ")
            .withContact(new Contact()
               .withEmail("contact@kingsrook.com")
            )
            .withVersion(version)
         )
         .withServers(ListBuilder.of(new Server()
            .withDescription("Localhost development")
            .withUrl("http://localhost:8000/api")
         ));

      openAPI.setTags(new ArrayList<>());
      openAPI.setPaths(new LinkedHashMap<>());

      LinkedHashMap<Integer, Response>      componentResponses = new LinkedHashMap<>();
      LinkedHashMap<String, Schema>         componentSchemas   = new LinkedHashMap<>();
      LinkedHashMap<String, SecurityScheme> securitySchemes    = new LinkedHashMap<>();
      openAPI.setComponents(new Components()
         .withSchemas(componentSchemas)
         .withResponses(componentResponses)
         .withSecuritySchemes(securitySchemes)
      );

      LinkedHashMap<String, String> scopes = new LinkedHashMap<>();
      securitySchemes.put("OAuth2", new OAuth2()
         .withFlows(MapBuilder.of("authorizationCode", new OAuth2Flow()
            .withAuthorizationUrl("https://nutrifresh-one-development.us.auth0.com/authorize")
            .withTokenUrl("https://nutrifresh-one-development.us.auth0.com/oauth/token")
            .withScopes(scopes)
         ))
      );

      componentSchemas.put("baseSearchResultFields", new Schema()
         .withType("object")
         .withProperties(MapBuilder.of(
            "count", new Schema()
               .withType("integer")
               .withDescription("Number of records that matched the search criteria"),
            "pageNo", new Schema()
               .withType("integer")
               .withDescription("Requested result page number"),
            "pageSize", new Schema()
               .withType("integer")
               .withDescription("Requested result page size")
         ))
      );

      ///////////////////
      // foreach table //
      ///////////////////
      for(QTableMetaData table : qInstance.getTables().values())
      {
         if(table.getIsHidden())
         {
            continue;
         }

         String         tableName        = table.getName();
         String         tableNameUcFirst = StringUtils.ucFirst(table.getName());
         String         tableLabel       = table.getLabel();
         String         primaryKeyName   = table.getPrimaryKeyField();
         QFieldMetaData primaryKeyField  = table.getField(table.getPrimaryKeyField());
         String         primaryKeyLabel  = primaryKeyField.getLabel();

         List<? extends QFieldMetaData> tableApiFields = new GetTableApiFieldsAction().execute(new GetTableApiFieldsInput().withTableName(tableName).withVersion(version)).getFields();

         String tableReadPermissionName = PermissionsHelper.getTablePermissionName(tableName, TablePermissionSubType.READ);
         if(StringUtils.hasContent(tableReadPermissionName))
         {
            scopes.put(tableReadPermissionName, "Permission to read the " + tableLabel + " table");
         }

         String tableUpdatePermissionName = PermissionsHelper.getTablePermissionName(tableName, TablePermissionSubType.EDIT);
         if(StringUtils.hasContent(tableUpdatePermissionName))
         {
            scopes.put(tableUpdatePermissionName, "Permission to update records in the " + tableLabel + " table");
         }

         String tableInsertPermissionName = PermissionsHelper.getTablePermissionName(tableName, TablePermissionSubType.EDIT);
         if(StringUtils.hasContent(tableInsertPermissionName))
         {
            scopes.put(tableInsertPermissionName, "Permission to insert records in the " + tableLabel + " table");
         }

         String tableDeletePermissionName = PermissionsHelper.getTablePermissionName(tableName, TablePermissionSubType.EDIT);
         if(StringUtils.hasContent(tableDeletePermissionName))
         {
            scopes.put(tableDeletePermissionName, "Permission to delete records in the " + tableLabel + " table");
         }

         //////////////////////////////////////////////////////////////////////////////////////////////////
         // todo - handle non read/edit/insert/delete tables (e.g., w/ just 1 permission, or read/write) //
         //////////////////////////////////////////////////////////////////////////////////////////////////

         ////////////////////////
         // tag for this table //
         ////////////////////////
         openAPI.getTags().add(new Tag()
            .withName(tableLabel)
            .withDescription("Operations on the " + tableLabel + " table."));

         //////////////////////////////////////
         // build the schemas for this table //
         //////////////////////////////////////
         LinkedHashMap<String, Schema> tableFieldsWithoutPrimaryKey = new LinkedHashMap<>();
         componentSchemas.put(tableName + "WithoutPrimaryKey", new Schema()
            .withType("object")
            .withProperties(tableFieldsWithoutPrimaryKey));

         for(QFieldMetaData tableApiField : tableApiFields)
         {
            if(primaryKeyName.equals(tableApiField.getName()))
            {
               continue;
            }

            tableFieldsWithoutPrimaryKey.put(tableApiField.getName(), new Schema()
               .withType(getFieldType(table.getField(tableApiField.getName())))
               .withFormat(getFieldFormat(table.getField(tableApiField.getName())))
               .withDescription(tableApiField.getLabel() + " for the " + tableLabel + ".")
            );
         }

         componentSchemas.put(tableName, new Schema()
            .withType("object")
            .withAllOf(ListBuilder.of(new Schema().withRef("#/components/schemas/" + tableName + "WithoutPrimaryKey")))
            .withProperties(MapBuilder.of(
               primaryKeyName, new Schema()
                  .withType(getFieldType(table.getField(primaryKeyName)))
                  .withFormat(getFieldFormat(table.getField(primaryKeyName)))
                  .withDescription(primaryKeyLabel + " for the " + tableLabel + ".  Primary Key.")
            ))
         );

         componentSchemas.put(tableName + "SearchResult", new Schema()
            .withType("object")
            .withAllOf(ListBuilder.of(new Schema().withRef("#/components/schemas/baseSearchResultFields")))
            .withProperties(MapBuilder.of(
               "records", new Schema()
                  .withType("array")
                  .withItems(new Schema()
                     .withAllOf(ListBuilder.of(
                        new Schema().withRef("#/components/schemas/" + tableName),
                        new Schema().withRef("#/components/schemas/" + tableName + "WithoutPrimaryKey")
                     ))
                  )
            ))
         );

         //////////////////////////////////////
         // paths and methods for this table //
         //////////////////////////////////////
         Method queryGet = new Method()
            .withSummary("Search the " + tableLabel + " table using multiple query string fields.")
            .withDescription("TODO")
            .withOperationId("query" + tableNameUcFirst)
            .withTags(ListBuilder.of(tableLabel))
            .withParameters(ListBuilder.of(
               new Parameter()
                  .withName("pageNo")
                  .withDescription("Which page of results to return.  Starts at 1.")
                  .withIn("query")
                  .withSchema(new Schema().withType("integer")),
               new Parameter()
                  .withName("pageSize")
                  .withDescription("Max number of records to include in a page.  Defaults to 50.")
                  .withIn("query")
                  .withSchema(new Schema().withType("integer")),
               new Parameter()
                  .withName("includeCount")
                  .withDescription("Whether or not to include the count (total matching records) in the result. Default is true.")
                  .withIn("query")
                  .withSchema(new Schema().withType("boolean")),
               new Parameter()
                  .withName("orderBy")
                  .withDescription("""
                     How the results of the query should be sorted.<br/>
                     SQL-style, comma-separated list of field names, each optionally followed by ASC or DESC (defaults to ASC).
                     """)
                  .withIn("query")
                  .withSchema(new Schema().withType("string"))
                  .withExamples(buildOrderByExamples(primaryKeyName, tableApiFields)),
               new Parameter()
                  .withName("booleanOperator")
                  .withDescription("Whether to combine query field as an AND or an OR.  Default is AND.")
                  .withIn("query")
                  .withSchema(new Schema().withType("string").withEnumValues(ListBuilder.of("AND", "OR")))
            ))
            .withResponses(buildStandardErrorResponses())
            .withResponse(200, new Response()
               .withDescription("Successfully searched the " + tableLabel + " table (though may have found 0 records).")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema().withRef("#/components/schemas/" + tableName + "SearchResult"))
               )))
            .withSecurity(ListBuilder.of(MapBuilder.of("OAuth2", List.of(tableReadPermissionName))));

         for(QFieldMetaData tableApiField : tableApiFields)
         {
            queryGet.getParameters().add(new Parameter()
               .withName(tableApiField.getName())
               .withDescription("Query on the " + tableApiField.getLabel() + " field.  Can prefix value with an operator, else defaults to = (equals).")
               .withIn("query")
               .withExplode(true)
               .withSchema(new Schema()
                  .withType("array")
                  .withItems(new Schema().withType("string")))
               .withExamples(MapBuilder.of(
                  // todo - multiple examples, and different per-type, and as components
                  "notQueried", new ExampleWithListValue().withSummary("no query on this field").withValue(ListBuilder.of("")),
                  "equals", new ExampleWithListValue().withSummary("equal to 47").withValue(ListBuilder.of("47")),
                  "complex", new ExampleWithListValue().withSummary("between 42 and 47 and not equal to 45").withValue(ListBuilder.of("BETWEEN 42,47", "!=45"))
               ))
            );
         }

         Method queryPost = new Method()
            .withSummary("Search the " + tableLabel + " table by posting a QueryFilter object.")
            .withTags(ListBuilder.of(tableLabel))
            .withSecurity(ListBuilder.of(MapBuilder.of("OAuth2", List.of(tableReadPermissionName))));

         openAPI.getPaths().put("/" + tableName + "/query", new Path()
            .withGet(queryGet)
            .withPost(queryPost)
         );

         Method idGet = new Method()
            .withSummary("Get one " + tableLabel + " by " + primaryKeyLabel)
            .withDescription("TODO")
            .withOperationId("get" + tableNameUcFirst)
            .withTags(ListBuilder.of(tableLabel))
            .withParameters(ListBuilder.of(
               new Parameter()
                  .withName(primaryKeyName)
                  .withDescription(primaryKeyLabel + " of the " + tableLabel + " to get.")
                  .withIn("path")
                  .withSchema(new Schema().withType(getFieldType(primaryKeyField)))
            ))
            .withResponses(buildStandardErrorResponses())
            .withResponse(404, buildStandardErrorResponse("The requested " + tableLabel + " record was not found.", "Could not find " + tableLabel + " with " + primaryKeyLabel + " of 47."))
            .withResponse(200, new Response()
               .withDescription("Successfully got the requested " + tableLabel)
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema().withRef("#/components/schemas/" + tableName))
               )))
            .withSecurity(ListBuilder.of(MapBuilder.of("OAuth2", List.of(tableReadPermissionName))));

         Method idPatch = new Method()
            .withSummary("Update one " + tableLabel + ".")
            .withDescription("TODO")
            .withOperationId("update" + tableNameUcFirst)
            .withTags(ListBuilder.of(tableLabel))
            .withParameters(ListBuilder.of(
               new Parameter()
                  .withName(primaryKeyName)
                  .withDescription(primaryKeyLabel + " of the " + tableLabel + " to update.")
                  .withIn("path")
                  .withSchema(new Schema().withType(getFieldType(primaryKeyField)))
            ))
            .withRequestBody(new RequestBody()
               .withRequired(true)
               .withDescription("Field values to update in the " + tableLabel + " record.")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema().withRef("#/components/schemas/" + tableName))
               )))
            .withResponses(buildStandardErrorResponses())
            .withResponse(404, buildStandardErrorResponse("The requested " + tableLabel + " record was not found.", "Could not find " + tableLabel + " with " + primaryKeyLabel + " of 47.")) // todo - 404 on update?
            .withResponse(200, new Response().withDescription("Successfully updated the requested " + tableLabel))
            .withSecurity(ListBuilder.of(MapBuilder.of("OAuth2", List.of(tableUpdatePermissionName))));

         Method idDelete = new Method()
            .withSummary("Delete one " + tableLabel + ".")
            .withDescription("TODO")
            .withOperationId("delete" + tableNameUcFirst)
            .withTags(ListBuilder.of(tableLabel))
            .withParameters(ListBuilder.of(
               new Parameter()
                  .withName(primaryKeyName)
                  .withDescription(primaryKeyLabel + " of the " + tableLabel + " to delete.")
                  .withIn("path")
                  .withSchema(new Schema().withType(getFieldType(primaryKeyField)))
            ))
            .withResponses(buildStandardErrorResponses())
            .withResponse(404, buildStandardErrorResponse("The requested " + tableLabel + " record was not found.", "Could not find " + tableLabel + " with " + primaryKeyLabel + " of 47.")) // todo - 404 on update?
            .withResponse(200, new Response().withDescription("Successfully deleted the requested " + tableLabel))
            .withSecurity(ListBuilder.of(MapBuilder.of("OAuth2", List.of(tableDeletePermissionName))));

         openAPI.getPaths().put("/" + tableName + "/{" + primaryKeyName + "}", new Path()
            .withGet(idGet)
            .withPatch(idPatch)
            .withDelete(idDelete)
         );

         Method slashPost = new Method()
            .withSummary("Create one " + tableLabel + " record.")
            .withRequestBody(new RequestBody()
               .withRequired(true)
               .withDescription("Values for the " + tableLabel + " record to create.")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema().withRef("#/components/schemas/" + tableName + "WithoutPrimaryKey"))
               )))
            .withResponses(buildStandardErrorResponses())
            .withResponse(201, new Response()
               .withDescription("Successfully created the requested " + tableLabel)
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema()
                     .withType("object")
                     .withProperties(MapBuilder.of(primaryKeyName, new Schema()
                        .withType(getFieldType(primaryKeyField))
                        .withExample("47")
                     ))
                  )
               )))
            .withTags(ListBuilder.of(tableLabel))
            .withSecurity(ListBuilder.of(MapBuilder.of("OAuth2", List.of(tableInsertPermissionName))));

         openAPI.getPaths().put("/" + tableName + "/", new Path()
            .withPost(slashPost)
         );

         /*
         openAPI.getPaths().put("/" + tableName + "/bulk", new Path()
            .withPatch(new Method()
               .withSummary("Update multiple " + tableLabel + " records.")
               .withTags(ListBuilder.of(tableLabel))
               .withResponses(buildStandardErrorResponses())
            )
            .withDelete(new Method()
               .withSummary("Delete multiple " + tableLabel + " records.")
               .withTags(ListBuilder.of(tableLabel))
               .withResponses(buildStandardErrorResponses())
            )
            .withPost(new Method()
               .withSummary("Create multiple  " + tableLabel + " records.")
               .withTags(ListBuilder.of(tableLabel))
               .withResponses(buildStandardErrorResponses())
            )
         );
         */
      }

      componentResponses.put(400, buildStandardErrorResponse("Bad Request.  Some portion of the request's content was not acceptable to the server.  See error message in body for details.", "Parameter id should be given an integer value, but received string: \"Foo\""));
      componentResponses.put(401, buildStandardErrorResponse("Unauthorized.  The required authentication credentials were missing or invalid.", "The required authentication credentials were missing or invalid."));
      componentResponses.put(403, buildStandardErrorResponse("Forbidden.  You do not have permission to access the requested resource.", "You do not have permission to access the requested resource."));
      componentResponses.put(500, buildStandardErrorResponse("Internal Server Error.  An error occurred in the server processing the request.", "Database connection error.  Try again later."));

      GenerateOpenApiSpecOutput output = new GenerateOpenApiSpecOutput();
      output.setOpenAPI(openAPI);
      output.setYaml(YamlUtils.toYaml(openAPI));
      output.setJson(JsonUtils.toJson(openAPI));
      return (output);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Map<String, Example> buildOrderByExamples(String primaryKeyName, List<? extends QFieldMetaData> tableApiFields)
   {
      Map<String, Example> rs = new LinkedHashMap<>();

      List<String> fieldsForExample4 = new ArrayList<>();
      List<String> fieldsForExample5 = new ArrayList<>();
      for(QFieldMetaData tableApiField : tableApiFields)
      {
         String name = tableApiField.getName();
         if(primaryKeyName.equals(name) || fieldsForExample4.contains(name) || fieldsForExample5.contains(name))
         {
            continue;
         }

         if(!fieldsForExample4.contains(name) && fieldsForExample4.size() < 2)
         {
            fieldsForExample4.add(name);
         }
         else if(!fieldsForExample5.contains(name) && fieldsForExample5.size() < 3)
         {
            fieldsForExample5.add(name);
         }

         if(fieldsForExample5.size() == 3)
         {
            break;
         }
      }

      rs.put(primaryKeyName, new ExampleWithSingleValue()
         .withSummary("order by " + primaryKeyName + " (by default is ascending)")
         .withValue("id"));

      rs.put(primaryKeyName + "Desc", new ExampleWithSingleValue()
         .withSummary("order by " + primaryKeyName + " (descending)")
         .withValue("id desc"));

      rs.put(primaryKeyName + "Asc", new ExampleWithSingleValue()
         .withSummary("order by " + primaryKeyName + " (explicitly ascending)")
         .withValue("id asc"));

      if(fieldsForExample4.size() == 2)
      {
         String a = fieldsForExample4.get(0);
         String b = fieldsForExample4.get(1);

         String name = a + "And" + StringUtils.ucFirst(b);
         rs.put(name, new ExampleWithSingleValue()
            .withSummary("order by " + a + ", then by " + b + " (both ascending)")
            .withValue(a + ", " + b));
      }

      if(fieldsForExample5.size() == 3)
      {
         String a = fieldsForExample5.get(0);
         String b = fieldsForExample5.get(1);
         String c = fieldsForExample5.get(2);

         String name = a + "And" + StringUtils.ucFirst(b) + "And" + StringUtils.ucFirst(c);
         rs.put(name, new ExampleWithSingleValue()
            .withSummary("order by " + a + " descending, then by " + b + " ascending, then by " + c)
            .withValue(a + " desc, " + b + " asc, " + c));
      }

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getFieldType(QFieldMetaData field)
   {
      return (getFieldType(field.getType()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:indentation")
   private String getFieldType(QFieldType type)
   {
      return switch(type)
         {
            case STRING, DATE, TIME, DATE_TIME, TEXT, HTML, PASSWORD, BLOB -> "string";
            case INTEGER -> "integer";
            case DECIMAL -> "number";
            case BOOLEAN -> "boolean";
         };
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private String getFieldFormat(QFieldMetaData field)
   {
      return (getFieldFormat(field.getType()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:indentation")
   private String getFieldFormat(QFieldType type)
   {
      return switch(type)
         {
            case DATE -> "date";
            case TIME -> "time"; // non-standard format...
            case DATE_TIME -> "date-time";
            case PASSWORD -> "password";
            case BLOB -> "byte"; // base-64-encoded, per https://swagger.io/docs/specification/data-models/data-types/#file
            default -> null;
         };
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Map<Integer, Response> buildStandardErrorResponses()
   {
      return MapBuilder.of(
         400, new Response().withRef("#/components/responses/400"),
         401, new Response().withRef("#/components/responses/401"),
         403, new Response().withRef("#/components/responses/403"),
         500, new Response().withRef("#/components/responses/500")
      );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Response buildStandardErrorResponse(String description)
   {
      return buildStandardErrorResponse(description, description);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Response buildStandardErrorResponse(String description, String example)
   {
      return new Response()
         .withDescription(description)
         .withContent(MapBuilder.of("application/json", new Content()
            .withSchema(new Schema()
               .withType("object")
               .withProperties(MapBuilder.of("error", new Schema()
                  .withType("string")
                  .withExample(example)
               ))
            )
         ));
   }

}
