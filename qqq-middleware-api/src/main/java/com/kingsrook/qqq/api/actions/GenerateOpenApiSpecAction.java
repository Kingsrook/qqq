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
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.APIVersionRange;
import com.kingsrook.qqq.api.model.actions.GenerateOpenApiSpecInput;
import com.kingsrook.qqq.api.model.actions.GenerateOpenApiSpecOutput;
import com.kingsrook.qqq.api.model.actions.GetTableApiFieldsInput;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
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
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValue;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.JsonUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.YamlUtils;
import com.kingsrook.qqq.backend.core.utils.collections.ListBuilder;
import com.kingsrook.qqq.backend.core.utils.collections.MapBuilder;
import io.javalin.http.HttpStatus;
import org.apache.commons.lang.BooleanUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class GenerateOpenApiSpecAction extends AbstractQActionFunction<GenerateOpenApiSpecInput, GenerateOpenApiSpecOutput>
{
   private static final QLogger LOG = QLogger.getLogger(GenerateOpenApiSpecAction.class);



   /*******************************************************************************
    **
    *******************************************************************************/
   public GenerateOpenApiSpecOutput execute(GenerateOpenApiSpecInput input) throws QException
   {
      String version = input.getVersion();

      QInstance qInstance = QContext.getQInstance();

      ApiInstanceMetaData apiInstanceMetaData = ApiInstanceMetaData.of(qInstance);

      OpenAPI openAPI = new OpenAPI()
         .withVersion("3.0.3")
         .withInfo(new Info()
            .withTitle(apiInstanceMetaData.getName())
            .withDescription(apiInstanceMetaData.getDescription())
            .withContact(new Contact()
               .withEmail(apiInstanceMetaData.getContactEmail())
            )
            .withVersion(version)
         )
         .withServers(ListBuilder.of(new Server()
            .withDescription("Localhost development")
            .withUrl("http://localhost:8000/api/" + version)
         ));

      openAPI.setTags(new ArrayList<>());
      openAPI.setPaths(new LinkedHashMap<>());

      LinkedHashMap<String, Response>       componentResponses = new LinkedHashMap<>();
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
            // todo - get from auth metadata
            .withAuthorizationUrl("https://nutrifresh-one-development.us.auth0.com/authorize")
            .withTokenUrl("https://nutrifresh-one-development.us.auth0.com/oauth/token")
            .withScopes(scopes)
         ))
      );
      securitySchemes.put("bearerAuth", new SecurityScheme()
         .withType("http")
         .withScheme("bearer")
         .withBearerFormat("JWT")
      );

      securitySchemes.put("basicAuth", new SecurityScheme()
         .withType("http")
         .withScheme("basic")
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
         String tableName = table.getName();

         if(input.getTableName() != null && !input.getTableName().equals(tableName))
         {
            LOG.debug("Omitting table [" + tableName + "] because it is not the requested table [" + input.getTableName() + "]");
            continue;
         }

         if(table.getIsHidden())
         {
            LOG.debug("Omitting table [" + tableName + "] because it is marked as hidden");
            continue;
         }

         ApiTableMetaData apiTableMetaData = ApiTableMetaData.of(table);
         if(apiTableMetaData == null)
         {
            LOG.debug("Omitting table [" + tableName + "] because it does not have any apiTableMetaData");
            continue;
         }

         if(BooleanUtils.isTrue(apiTableMetaData.getIsExcluded()))
         {
            LOG.debug("Omitting table [" + tableName + "] because its apiTableMetaData marks it as excluded");
            continue;
         }

         APIVersionRange apiVersionRange = apiTableMetaData.getApiVersionRange();
         if(!apiVersionRange.includes(new APIVersion(version)))
         {
            LOG.debug("Omitting table [" + tableName + "] because its api version range [" + apiVersionRange + "] does not include this version [" + version + "]");
            continue;
         }

         QBackendMetaData tableBackend     = qInstance.getBackendForTable(tableName);
         boolean          queryCapability  = table.isCapabilityEnabled(tableBackend, Capability.TABLE_QUERY);
         boolean          getCapability    = table.isCapabilityEnabled(tableBackend, Capability.TABLE_GET);
         boolean          updateCapability = table.isCapabilityEnabled(tableBackend, Capability.TABLE_UPDATE);
         boolean          deleteCapability = table.isCapabilityEnabled(tableBackend, Capability.TABLE_DELETE);
         boolean          insertCapability = table.isCapabilityEnabled(tableBackend, Capability.TABLE_INSERT);
         boolean          countCapability  = table.isCapabilityEnabled(tableBackend, Capability.TABLE_COUNT);

         if(!queryCapability && !getCapability && !updateCapability && !deleteCapability && !insertCapability)
         {
            LOG.debug("Omitting table [" + tableName + "] because it does not have any supported capabilities");
            continue;
         }

         String               tableApiName        = StringUtils.hasContent(apiTableMetaData.getApiTableName()) ? apiTableMetaData.getApiTableName() : tableName;
         String               tableApiNameUcFirst = StringUtils.ucFirst(tableApiName);
         String               tableLabel          = table.getLabel();
         String               primaryKeyName      = table.getPrimaryKeyField();
         QFieldMetaData       primaryKeyField     = table.getField(table.getPrimaryKeyField());
         String               primaryKeyLabel     = primaryKeyField.getLabel();
         String               primaryKeyApiName   = ApiFieldMetaData.getEffectiveApiFieldName(primaryKeyField);
         List<QFieldMetaData> tableApiFields      = new GetTableApiFieldsAction().execute(new GetTableApiFieldsInput().withTableName(tableName).withVersion(version)).getFields();

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

         String tableInsertPermissionName = PermissionsHelper.getTablePermissionName(tableName, TablePermissionSubType.INSERT);
         if(StringUtils.hasContent(tableInsertPermissionName))
         {
            scopes.put(tableInsertPermissionName, "Permission to insert records in the " + tableLabel + " table");
         }

         String tableDeletePermissionName = PermissionsHelper.getTablePermissionName(tableName, TablePermissionSubType.DELETE);
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
         componentSchemas.put(tableApiName + "WithoutPrimaryKey", new Schema()
            .withType("object")
            .withProperties(tableFieldsWithoutPrimaryKey));

         for(QFieldMetaData field : tableApiFields)
         {
            if(primaryKeyName.equals(field.getName()))
            {
               continue;
            }

            String apiFieldName = ApiFieldMetaData.getEffectiveApiFieldName(field);

            Schema fieldSchema = new Schema()
               .withType(getFieldType(table.getField(field.getName())))
               .withFormat(getFieldFormat(table.getField(field.getName())))
               .withDescription(field.getLabel() + " for the " + tableLabel + ".");

            if(StringUtils.hasContent(field.getPossibleValueSourceName()))
            {
               QPossibleValueSource possibleValueSource = qInstance.getPossibleValueSource(field.getPossibleValueSourceName());
               if(QPossibleValueSourceType.ENUM.equals(possibleValueSource.getType()))
               {
                  List<String> enumValues = new ArrayList<>();
                  for(QPossibleValue<?> enumValue : possibleValueSource.getEnumValues())
                  {
                     enumValues.add(enumValue.getId() + "=" + enumValue.getLabel());
                  }
                  fieldSchema.setEnumValues(enumValues);
               }
               else if(QPossibleValueSourceType.TABLE.equals(possibleValueSource.getType()))
               {
                  QTableMetaData sourceTable = qInstance.getTable(possibleValueSource.getTableName());
                  fieldSchema.setDescription(fieldSchema.getDescription() + "  Values in this field come from the primary key of the " + sourceTable.getLabel() + " table");
               }
            }

            tableFieldsWithoutPrimaryKey.put(apiFieldName, fieldSchema);
         }

         componentSchemas.put(tableApiName, new Schema()
            .withType("object")
            .withAllOf(ListBuilder.of(new Schema().withRef("#/components/schemas/" + tableApiName + "WithoutPrimaryKey")))
            .withProperties(MapBuilder.of(
               primaryKeyApiName, new Schema()
                  .withType(getFieldType(table.getField(primaryKeyName)))
                  .withFormat(getFieldFormat(table.getField(primaryKeyName)))
                  .withDescription(primaryKeyLabel + " for the " + tableLabel + ".  Primary Key.")
            ))
         );

         componentSchemas.put(tableApiName + "SearchResult", new Schema()
            .withType("object")
            .withAllOf(ListBuilder.of(new Schema().withRef("#/components/schemas/baseSearchResultFields")))
            .withProperties(MapBuilder.of(
               "records", new Schema()
                  .withType("array")
                  .withItems(new Schema()
                     .withAllOf(ListBuilder.of(
                        new Schema().withRef("#/components/schemas/" + tableApiName),
                        new Schema().withRef("#/components/schemas/" + tableApiName + "WithoutPrimaryKey")
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
            .withOperationId("query" + tableApiNameUcFirst)
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
                  .withExamples(buildOrderByExamples(primaryKeyApiName, tableApiFields)),
               new Parameter()
                  .withName("booleanOperator")
                  .withDescription("Whether to combine query field as an AND or an OR.  Default is AND.")
                  .withIn("query")
                  .withSchema(new Schema().withType("string").withEnumValues(ListBuilder.of("AND", "OR")))
            ))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.OK.getCode(), new Response()
               .withDescription("Successfully searched the " + tableLabel + " table (though may have found 0 records).")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema().withRef("#/components/schemas/" + tableApiName + "SearchResult"))
               )))
            .withSecurity(getSecurity(tableReadPermissionName));

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
            .withSecurity(getSecurity(tableReadPermissionName));

         if(queryCapability)
         {
            openAPI.getPaths().put("/" + tableApiName + "/query", new Path()
               // todo!! .withPost(queryPost)
               .withGet(queryGet)
            );
         }

         Method idGet = new Method()
            .withSummary("Get one " + tableLabel + " by " + primaryKeyLabel)
            .withDescription("TODO")
            .withOperationId("get" + tableApiNameUcFirst)
            .withTags(ListBuilder.of(tableLabel))
            .withParameters(ListBuilder.of(
               new Parameter()
                  .withName(primaryKeyApiName)
                  .withDescription(primaryKeyLabel + " of the " + tableLabel + " to get.")
                  .withIn("path")
                  .withRequired(true)
                  .withSchema(new Schema().withType(getFieldType(primaryKeyField)))
            ))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.NOT_FOUND.getCode(), buildStandardErrorResponse("The requested " + tableLabel + " record was not found.", "Could not find " + tableLabel + " with " + primaryKeyLabel + " of 47."))
            .withResponse(HttpStatus.OK.getCode(), new Response()
               .withDescription("Successfully got the requested " + tableLabel)
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema().withRef("#/components/schemas/" + tableApiName))
               )))
            .withSecurity(getSecurity(tableReadPermissionName));

         Method idPatch = new Method()
            .withSummary("Update one " + tableLabel + ".")
            .withDescription("TODO")
            .withOperationId("update" + tableApiNameUcFirst)
            .withTags(ListBuilder.of(tableLabel))
            .withParameters(ListBuilder.of(
               new Parameter()
                  .withName(primaryKeyApiName)
                  .withDescription(primaryKeyLabel + " of the " + tableLabel + " to update.")
                  .withIn("path")
                  .withRequired(true)
                  .withSchema(new Schema().withType(getFieldType(primaryKeyField)))
            ))
            .withRequestBody(new RequestBody()
               .withRequired(true)
               .withDescription("Field values to update in the " + tableLabel + " record.")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema().withRef("#/components/schemas/" + tableApiName))
               )))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.NOT_FOUND.getCode(), buildStandardErrorResponse("The requested " + tableLabel + " record was not found.", "Could not find " + tableLabel + " with " + primaryKeyLabel + " of 47."))
            .withResponse(HttpStatus.NO_CONTENT.getCode(), new Response().withDescription("Successfully updated the requested " + tableLabel))
            .withSecurity(getSecurity(tableUpdatePermissionName));

         Method idDelete = new Method()
            .withSummary("Delete one " + tableLabel + ".")
            .withDescription("TODO")
            .withOperationId("delete" + tableApiNameUcFirst)
            .withTags(ListBuilder.of(tableLabel))
            .withParameters(ListBuilder.of(
               new Parameter()
                  .withName(primaryKeyApiName)
                  .withDescription(primaryKeyLabel + " of the " + tableLabel + " to delete.")
                  .withIn("path")
                  .withRequired(true)
                  .withSchema(new Schema().withType(getFieldType(primaryKeyField)))
            ))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.NOT_FOUND.getCode(), buildStandardErrorResponse("The requested " + tableLabel + " record was not found.", "Could not find " + tableLabel + " with " + primaryKeyLabel + " of 47."))
            .withResponse(HttpStatus.NO_CONTENT.getCode(), new Response().withDescription("Successfully deleted the requested " + tableLabel))
            .withSecurity(getSecurity(tableDeletePermissionName));

         if(getCapability || updateCapability || deleteCapability)
         {
            openAPI.getPaths().put("/" + tableApiName + "/{" + primaryKeyApiName + "}", new Path()
               .withGet(getCapability ? idGet : null)
               .withPatch(updateCapability ? idPatch : null)
               .withDelete(deleteCapability ? idDelete : null)
            );
         }

         Method slashPost = new Method()
            .withSummary("Create one " + tableLabel + " record.")
            .withRequestBody(new RequestBody()
               .withRequired(true)
               .withDescription("Values for the " + tableLabel + " record to create.")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema().withRef("#/components/schemas/" + tableApiName + "WithoutPrimaryKey"))
               )))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.CREATED.getCode(), new Response()
               .withDescription("Successfully created the requested " + tableLabel)
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema()
                     .withType("object")
                     .withProperties(MapBuilder.of(primaryKeyApiName, new Schema()
                        .withType(getFieldType(primaryKeyField))
                        .withExample("47")
                     ))
                  )
               )))
            .withTags(ListBuilder.of(tableLabel))
            .withSecurity(getSecurity(tableInsertPermissionName));

         if(insertCapability)
         {
            openAPI.getPaths().put("/" + tableApiName + "/", new Path()
               .withPost(slashPost)
            );
         }

         ////////////////
         // bulk paths //
         ////////////////
         Method bulkPost = new Method()
            .withSummary("Create multiple " + tableLabel + " records.")
            .withRequestBody(new RequestBody()
               .withRequired(true)
               .withDescription("Values for the " + tableLabel + " records to create.")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema()
                     .withType("array")
                     .withItems(new Schema().withRef("#/components/schemas/" + tableApiName + "WithoutPrimaryKey"))))))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.MULTI_STATUS.getCode(), buildMultiStatusResponse(tableLabel, primaryKeyApiName, primaryKeyField, "post"))
            .withTags(ListBuilder.of(tableLabel))
            .withSecurity(getSecurity(tableInsertPermissionName));

         Method bulkPatch = new Method()
            .withSummary("Update multiple " + tableLabel + " records.")
            .withRequestBody(new RequestBody()
               .withRequired(true)
               .withDescription("Values for the " + tableLabel + " records to update.")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema()
                     .withType("array")
                     .withItems(new Schema().withRef("#/components/schemas/" + tableApiName))))))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.MULTI_STATUS.getCode(), buildMultiStatusResponse(tableLabel, primaryKeyApiName, primaryKeyField, "patch"))
            .withTags(ListBuilder.of(tableLabel))
            .withSecurity(getSecurity(tableUpdatePermissionName));

         Method bulkDelete = new Method()
            .withSummary("Delete multiple " + tableLabel + " records.")
            .withRequestBody(new RequestBody()
               .withRequired(true)
               .withDescription(primaryKeyLabel + " values for the " + tableLabel + " records to delete.")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema()
                     .withType("array")
                     .withItems(new Schema().withType(getFieldType(primaryKeyField)))
                     .withExample(List.of(42, 47))))))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.MULTI_STATUS.getCode(), buildMultiStatusResponse(tableLabel, primaryKeyApiName, primaryKeyField, "delete"))
            .withTags(ListBuilder.of(tableLabel))
            .withSecurity(getSecurity(tableDeletePermissionName));

         if(insertCapability || updateCapability || deleteCapability)
         {
            openAPI.getPaths().put("/" + tableApiName + "/bulk", new Path()
               .withPost(insertCapability ? bulkPost : null)
               .withPatch(updateCapability ? bulkPatch : null)
               .withDelete(deleteCapability ? bulkDelete : null));
         }
      }

      componentResponses.put("error" + HttpStatus.BAD_REQUEST.getCode(), buildStandardErrorResponse("Bad Request.  Some portion of the request's content was not acceptable to the server.  See error message in body for details.", "Parameter id should be given an integer value, but received string: \"Foo\""));
      componentResponses.put("error" + HttpStatus.UNAUTHORIZED.getCode(), buildStandardErrorResponse("Unauthorized.  The required authentication credentials were missing or invalid.", "The required authentication credentials were missing or invalid."));
      componentResponses.put("error" + HttpStatus.FORBIDDEN.getCode(), buildStandardErrorResponse("Forbidden.  You do not have permission to access the requested resource.", "You do not have permission to access the requested resource."));
      componentResponses.put("error" + HttpStatus.INTERNAL_SERVER_ERROR.getCode(), buildStandardErrorResponse("Internal Server Error.  An error occurred in the server processing the request.", "Database connection error.  Try again later."));

      GenerateOpenApiSpecOutput output = new GenerateOpenApiSpecOutput();
      output.setOpenAPI(openAPI);
      output.setYaml(YamlUtils.toYaml(openAPI));
      output.setJson(JsonUtils.toJson(openAPI));
      return (output);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static List<Map<String, List<String>>> getSecurity(String permissionName)
   {
      return ListBuilder.of(
         MapBuilder.of("OAuth2", List.of(permissionName)),
         MapBuilder.of("bearerAuth", List.of(permissionName)),
         MapBuilder.of("basicAuth", List.of(permissionName))
      );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @SuppressWarnings("checkstyle:indentation")
   private Response buildMultiStatusResponse(String tableLabel, String primaryKeyApiName, QFieldMetaData primaryKeyField, String method)
   {
      List<Object> example = switch(method.toLowerCase())
         {
            case "post" -> ListBuilder.of(
               MapBuilder.of(LinkedHashMap::new)
                  .with("statusCode", HttpStatus.CREATED.getCode())
                  .with("statusText", HttpStatus.CREATED.getMessage())
                  .with(primaryKeyApiName, "47").build(),
               MapBuilder.of(LinkedHashMap::new)
                  .with("statusCode", HttpStatus.BAD_REQUEST.getCode())
                  .with("statusText", HttpStatus.BAD_REQUEST.getMessage())
                  .with("error", "Could not create " + tableLabel + ": Duplicate value in unique key field.").build()
            );
            case "patch" -> ListBuilder.of(
               MapBuilder.of(LinkedHashMap::new)
                  .with("statusCode", HttpStatus.NO_CONTENT.getCode())
                  .with("statusText", HttpStatus.NO_CONTENT.getMessage()).build(),
               MapBuilder.of(LinkedHashMap::new)
                  .with("statusCode", HttpStatus.BAD_REQUEST.getCode())
                  .with("statusText", HttpStatus.BAD_REQUEST.getMessage())
                  .with("error", "Could not update " + tableLabel + ": Duplicate value in unique key field.").build()
            );
            case "delete" -> ListBuilder.of(
               MapBuilder.of(LinkedHashMap::new)
                  .with("statusCode", HttpStatus.NO_CONTENT.getCode())
                  .with("statusText", HttpStatus.NO_CONTENT.getMessage()).build(),
               MapBuilder.of(LinkedHashMap::new)
                  .with("statusCode", HttpStatus.BAD_REQUEST.getCode())
                  .with("statusText", HttpStatus.BAD_REQUEST.getMessage())
                  .with("error", "Could not delete " + tableLabel + ": Foreign key constraint violation.").build()
            );
            default -> throw (new IllegalArgumentException("Unrecognized method: " + method));
         };

      Map<String, Schema> properties = new LinkedHashMap<>();
      properties.put("status", new Schema().withType("integer"));
      properties.put("error", new Schema().withType("string"));
      if(method.equalsIgnoreCase("post"))
      {
         properties.put(primaryKeyApiName, new Schema().withType(getFieldType(primaryKeyField)));
      }

      return new Response()
         .withDescription("Multiple statuses.  See body for details.")
         .withContent(MapBuilder.of("application/json", new Content()
            .withSchema(new Schema()
               .withType("array")
               .withItems(new Schema()
                  .withType("object")
                  .withProperties(properties))
               .withExample(example)
            )
         ));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Map<String, Example> buildOrderByExamples(String primaryKeyApiName, List<? extends QFieldMetaData> tableApiFields)
   {
      Map<String, Example> rs = new LinkedHashMap<>();

      List<String> fieldsForExample4 = new ArrayList<>();
      List<String> fieldsForExample5 = new ArrayList<>();
      for(QFieldMetaData tableApiField : tableApiFields)
      {
         String name = tableApiField.getName();
         if(primaryKeyApiName.equals(name) || fieldsForExample4.contains(name) || fieldsForExample5.contains(name))
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

      rs.put(primaryKeyApiName, new ExampleWithSingleValue()
         .withSummary("order by " + primaryKeyApiName + " (by default is ascending)")
         .withValue("id"));

      rs.put(primaryKeyApiName + "Desc", new ExampleWithSingleValue()
         .withSummary("order by " + primaryKeyApiName + " (descending)")
         .withValue("id desc"));

      rs.put(primaryKeyApiName + "Asc", new ExampleWithSingleValue()
         .withSummary("order by " + primaryKeyApiName + " (explicitly ascending)")
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
         HttpStatus.BAD_REQUEST.getCode(), new Response().withRef("#/components/responses/error" + HttpStatus.BAD_REQUEST.getCode()),
         HttpStatus.UNAUTHORIZED.getCode(), new Response().withRef("#/components/responses/error" + HttpStatus.UNAUTHORIZED.getCode()),
         HttpStatus.FORBIDDEN.getCode(), new Response().withRef("#/components/responses/error" + HttpStatus.FORBIDDEN.getCode()),
         HttpStatus.INTERNAL_SERVER_ERROR.getCode(), new Response().withRef("#/components/responses/error" + HttpStatus.INTERNAL_SERVER_ERROR.getCode())
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
