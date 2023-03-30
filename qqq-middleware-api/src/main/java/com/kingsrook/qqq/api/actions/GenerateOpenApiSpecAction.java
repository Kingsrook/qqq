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


import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Capability;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
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
            .withDescription("This server")
            .withUrl("/api/" + version)
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
         .withExamples(getComponentExamples())
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

      LinkedHashMap<String, String> scopes = new LinkedHashMap<>();
      // todo, or not todo? .withScopes(scopes)
      securitySchemes.put("OAuth2", new OAuth2()
         .withFlows(MapBuilder.of("clientCredentials", new OAuth2Flow()
            .withTokenUrl("/api/oauth/token")
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
      List<QTableMetaData> tables = new ArrayList<>(qInstance.getTables().values());
      tables.sort(Comparator.comparing(t -> t.getLabel()));
      for(QTableMetaData table : tables)
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
         boolean          countCapability  = table.isCapabilityEnabled(tableBackend, Capability.TABLE_COUNT); // todo - look at this - if table doesn't have count, don't include it in its input/output, etc

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

         ///////////////////////////////
         // permissions for the table //
         ///////////////////////////////
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
         LinkedHashMap<String, Schema> tableFields = new LinkedHashMap<>();
         Schema tableSchema = new Schema()
            .withType("object")
            .withProperties(tableFields);
         componentSchemas.put(tableApiName, tableSchema);

         for(QFieldMetaData field : tableApiFields)
         {
            Schema fieldSchema = getFieldSchema(table, field);
            tableFields.put(ApiFieldMetaData.getEffectiveApiFieldName(field), fieldSchema);
         }

         //////////////////////////////////
         // recursively add associations //
         //////////////////////////////////
         addAssociations(table, tableSchema);

         //////////////////////////////////////////////////////////////////////////////
         // table as a search result (the base search result, plus the table itself) //
         //////////////////////////////////////////////////////////////////////////////
         componentSchemas.put(tableApiName + "SearchResult", new Schema()
            .withType("object")
            .withAllOf(ListBuilder.of(new Schema().withRef("#/components/schemas/baseSearchResultFields")))
            .withProperties(MapBuilder.of(
               "records", new Schema()
                  .withType("array")
                  .withItems(new Schema()
                     .withAllOf(ListBuilder.of(
                        new Schema().withRef("#/components/schemas/" + tableApiName)))))));

         //////////////////////////////////////
         // paths and methods for this table //
         //////////////////////////////////////
         Method queryGet = new Method()
            .withSummary("Search for " + tableLabel + " records by query string")
            .withDescription("""
               Execute a query on this table, using query criteria as specified in query string parameters.
                              
               * Pagination is managed via the `pageNo` & `pageSize` query string parameters.  pageNo starts at 1.  pageSize defaults to 50.
               * By default, the response includes the total count of records that match the query criteria.  The count can be omitted by specifying `includeCount=false`
               * By default, results are sorted by the table's primary key, descending.  This can be changed by specifying the `orderBy` query string parameter, following SQL ORDER BY syntax (e.g., `fieldName1 ASC, fieldName2 DESC`)
               * By default, all given query criteria are combined using logical AND.  This can be changed by specifying the query string parameter `booleanOperator=OR`.
               * Each field on the table can be used as a query criteria.  Each query criteria field can be specified on the query string any number of times.
               * By default, all criteria use the equals operator (e.g., `myField=value` means records will be returned where myField equals value).  Alternative operators can be used as follows:
                 * Equals: `myField=value`
                 * Not Equals: `myField=!value`
                 * Less Than: `myField=&lt;value`
                 * Greater Than: `myField=&gt;value`
                 * Less Than or Equals: `myField=&lt;=value`
                 * Greater Than or Equals: `myField=&gt;=value`
                 * Empty (or null): `myField=EMPTY`
                 * Not Empty: `myField=!EMPTY`
                 * Between: `myField=BETWEEN value1,value2` (two values must be given, separated by commas)
                 * Not Between: `myField=!BETWEEN value1,value2` (two values must be given, separated by commas)
                 * In: `myField=IN value1,value2,...,valueN` (one or more values must be given, separated by commas)
                 * Not In: `myField=!IN value1,value2,...,valueN` (one or more values must be given, separated by commas)
                 * Like: `myField=LIKE value` (using standard SQL % and _ wildcards)
                 * Not Like: `myField=!LIKE value` (using standard SQL % and _ wildcards)
               """)
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
                  .withDescription("Max number of records to include in a page.  Defaults to 50.  Must be between 1 and 1000.")
                  .withIn("query")
                  .withSchema(new Schema().withType("integer")),
               new Parameter()
                  .withName("includeCount")
                  .withDescription("Whether or not to include the count (total matching records) in the result. Default is true.")
                  .withIn("query")
                  .withSchema(new Schema().withType("boolean").withEnumValues(ListBuilder.of("true", "false"))),
               new Parameter()
                  .withName("orderBy")
                  .withDescription("How the results of the query should be sorted. SQL-style, comma-separated list of field names, each optionally followed by ASC or DESC (defaults to ASC).")
                  .withIn("query")
                  .withSchema(new Schema().withType("string"))
                  .withExamples(buildOrderByExamples(primaryKeyApiName, tableApiFields)),
               new Parameter()
                  .withName("booleanOperator")
                  .withDescription("Whether to combine query field as an AND or an OR.  Default is AND.")
                  .withIn("query")
                  .withSchema(new Schema().withType("string").withEnumValues(ListBuilder.of("AND", "OR")))))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.OK.getCode(), new Response()
               .withDescription("Successfully searched the " + tableLabel + " table (though may have found 0 records).")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema().withRef("#/components/schemas/" + tableApiName + "SearchResult")))))
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
               .withExamples(getCriteriaExamples(openAPI.getComponents().getExamples(), tableApiField)));
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
            .withDescription("""
               Get one record from this table, by specifying its primary key as a path parameter.
               """)
            .withOperationId("get" + tableApiNameUcFirst)
            .withTags(ListBuilder.of(tableLabel))
            .withParameters(ListBuilder.of(
               new Parameter()
                  .withName(primaryKeyApiName)
                  .withDescription(primaryKeyLabel + " of the " + tableLabel + " to get.")
                  .withIn("path")
                  .withRequired(true)
                  .withSchema(new Schema().withType(getFieldType(primaryKeyField)))))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.NOT_FOUND.getCode(), buildStandardErrorResponse("The requested " + tableLabel + " record was not found.", "Could not find " + tableLabel + " with " + primaryKeyLabel + " of 47."))
            .withResponse(HttpStatus.OK.getCode(), new Response()
               .withDescription("Successfully got the requested " + tableLabel)
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema().withRef("#/components/schemas/" + tableApiName)))))
            .withSecurity(getSecurity(tableReadPermissionName));

         Method idPatch = new Method()
            .withSummary("Update one " + tableLabel)
            .withDescription("""
               Update one record in this table, by specifying its primary key as a path parameter, and by supplying values to be updated in the request body.
                              
               * Only the fields provided in the request body will be updated.
               * To remove a value from a field, supply the key for the field, with a null value.
               * The request body does not need to contain all fields from the table.  Rather, only the fields to be updated should be supplied.
               * Note that if the request body includes the primary key, it will be ignored.  Only the primary key value path parameter will be used.
                              
               Upon success, a status code of 204 (`No Content`) is returned, with no response body.
               """)
            .withOperationId("update" + tableApiNameUcFirst)
            .withTags(ListBuilder.of(tableLabel))
            .withParameters(ListBuilder.of(
               new Parameter()
                  .withName(primaryKeyApiName)
                  .withDescription(primaryKeyLabel + " of the " + tableLabel + " to update.")
                  .withIn("path")
                  .withRequired(true)
                  .withSchema(new Schema().withType(getFieldType(primaryKeyField)))))
            .withRequestBody(new RequestBody()
               .withRequired(true)
               .withDescription("Field values to update in the " + tableLabel + " record.")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema().withRef("#/components/schemas/" + tableApiName)))))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.NOT_FOUND.getCode(), buildStandardErrorResponse("The requested " + tableLabel + " record was not found.", "Could not find " + tableLabel + " with " + primaryKeyLabel + " of 47."))
            .withResponse(HttpStatus.NO_CONTENT.getCode(), new Response().withDescription("Successfully updated the requested " + tableLabel))
            .withSecurity(getSecurity(tableUpdatePermissionName));

         Method idDelete = new Method()
            .withSummary("Delete one " + tableLabel)
            .withDescription("""
               Delete one record from this table, by specifying its primary key as a path parameter.
                              
               Upon success, a status code of 204 (`No Content`) is returned, with no response body.
               """)
            .withOperationId("delete" + tableApiNameUcFirst)
            .withTags(ListBuilder.of(tableLabel))
            .withParameters(ListBuilder.of(
               new Parameter()
                  .withName(primaryKeyApiName)
                  .withDescription(primaryKeyLabel + " of the " + tableLabel + " to delete.")
                  .withIn("path")
                  .withRequired(true)
                  .withSchema(new Schema().withType(getFieldType(primaryKeyField)))))
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
            .withSummary("Create one " + tableLabel)
            .withDescription("""
               Insert one record into this table by supplying the values to be inserted in the request body.
               * The request body should not include a value for the table's primary key.  Rather, a value will be generated and returned in a successful response's body.
                              
               Upon success, a status code of 201 (`Created`) is returned, and the generated value for the primary key will be returned in the response body object.
               """)
            .withRequestBody(new RequestBody()
               .withRequired(true)
               .withDescription("Values for the " + tableLabel + " record to create.")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema().withRef("#/components/schemas/" + tableApiName))
               )))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.CREATED.getCode(), new Response()
               .withDescription("Successfully created the requested " + tableLabel)
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema()
                     .withType("object")
                     .withProperties(MapBuilder.of(primaryKeyApiName, new Schema()
                        .withType(getFieldType(primaryKeyField))
                        .withExample("47")))))))
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
            .withSummary("Create multiple " + tableLabel + " records")
            .withDescription("""
               Insert one or more records into this table by supplying array of records with values to be inserted, in the request body.
               * The objects in the request body should not include a value for the table's primary key.  Rather, a value will be generated and returned in a successful response's body
                              
               An HTTP 207 (`Multi-Status`) code is generally returned, with an array of objects giving the individual sub-status codes for each record in the request body.
               * The 1st record in the request will have its response in the 1st object in the response, and so-forth.
               * For sub-status codes of 201 (`Created`), and the generated value for the primary key will be returned in the response body object.
               """)
            .withRequestBody(new RequestBody()
               .withRequired(true)
               .withDescription("Values for the " + tableLabel + " records to create.")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema()
                     .withType("array")
                     .withItems(new Schema().withRef("#/components/schemas/" + tableApiName))))))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.MULTI_STATUS.getCode(), buildMultiStatusResponse(tableLabel, primaryKeyApiName, primaryKeyField, "post"))
            .withTags(ListBuilder.of(tableLabel))
            .withSecurity(getSecurity(tableInsertPermissionName));

         Method bulkPatch = new Method()
            .withSummary("Update multiple " + tableLabel + " records")
            .withDescription("""
               Update one or more records in this table, by supplying an array of records, with primary keys and values to be updated, in the request body.
               * Only the fields provided in the request body will be updated.
               * To remove a value from a field, supply the key for the field, with a null value.
               * The request body does not need to contain all fields from the table.  Rather, only the fields to be updated should be supplied.
                             
               An HTTP 207 (`Multi-Status`) code is generally returned, with an array of objects giving the individual sub-status codes for each record in the request body.
               * The 1st record in the request will have its response in the 1st object in the response, and so-forth.
               """)
            .withRequestBody(new RequestBody()
               .withRequired(true)
               .withDescription("Values for the " + tableLabel + " records to update.")
               .withContent(MapBuilder.of("application/json", new Content()
                  .withSchema(new Schema()
                     .withType("array")
                     .withItems(new Schema()
                        .withAllOf(ListBuilder.of(new Schema().withRef("#/components/schemas/" + tableApiName)))
                        .withProperties(MapBuilder.of(primaryKeyApiName, new Schema()
                           .withType(getFieldType(primaryKeyField))
                           .withReadOnly(false)
                           .withNullable(false)
                           .withExample("47"))))))))
            .withResponses(buildStandardErrorResponses())
            .withResponse(HttpStatus.MULTI_STATUS.getCode(), buildMultiStatusResponse(tableLabel, primaryKeyApiName, primaryKeyField, "patch"))
            .withTags(ListBuilder.of(tableLabel))
            .withSecurity(getSecurity(tableUpdatePermissionName));

         Method bulkDelete = new Method()
            .withSummary("Delete multiple " + tableLabel + " records")
            .withDescription("""
               Delete one or more records from this table, by supplying an array of primary key values in the request body.
                              
               An HTTP 207 (`Multi-Status`) code is generally returned, with an array of objects giving the individual sub-status codes for each record in the request body.
               * The 1st primary key in the request will have its response in the 1st object in the response, and so-forth.
               """)
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
      componentResponses.put("error" + HttpStatus.INTERNAL_SERVER_ERROR.getCode(), buildStandardErrorResponse("Internal Server Error.  An error occurred in the server while processing the request.", "Database connection error.  Try again later."));

      GenerateOpenApiSpecOutput output = new GenerateOpenApiSpecOutput();
      output.setOpenAPI(openAPI);
      output.setYaml(YamlUtils.toYaml(openAPI));
      output.setJson(JsonUtils.toPrettyJson(openAPI));
      return (output);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Map<String, Example> getComponentExamples()
   {
      Map<String, Example> rs = new LinkedHashMap<>();
      rs.put("criteriaNotQueried", new ExampleWithListValue().withSummary("no query on this field").withValue(ListBuilder.of("")));

      rs.put("criteriaNumberEquals", new ExampleWithListValue().withSummary("equal to 47").withValue(ListBuilder.of("47")));
      rs.put("criteriaNumberNotEquals", new ExampleWithListValue().withSummary("not equal to 47").withValue(ListBuilder.of("!47")));
      rs.put("criteriaNumberLessThan", new ExampleWithListValue().withSummary("less than 47").withValue(ListBuilder.of("<47")));
      rs.put("criteriaNumberGreaterThan", new ExampleWithListValue().withSummary("greater than 47").withValue(ListBuilder.of(">47")));
      rs.put("criteriaNumberLessThanOrEquals", new ExampleWithListValue().withSummary("less than or equal to 47").withValue(ListBuilder.of("<=47")));
      rs.put("criteriaNumberGreaterThanOrEquals", new ExampleWithListValue().withSummary("greater than or equal to 47").withValue(ListBuilder.of(">=47")));
      rs.put("criteriaNumberEmpty", new ExampleWithListValue().withSummary("null value").withValue(ListBuilder.of("EMPTY")));
      rs.put("criteriaNumberNotEmpty", new ExampleWithListValue().withSummary("non-null value").withValue(ListBuilder.of("!EMPTY")));
      rs.put("criteriaNumberBetween", new ExampleWithListValue().withSummary("between 42 and 47").withValue(ListBuilder.of("BETWEEN 42,47")));
      rs.put("criteriaNumberNotBetween", new ExampleWithListValue().withSummary("not between 42 and 47").withValue(ListBuilder.of("!BETWEEN 42,47")));
      rs.put("criteriaNumberIn", new ExampleWithListValue().withSummary("any of 1701, 74205, or 74656").withValue(ListBuilder.of("IN 1701,74205,74656")));
      rs.put("criteriaNumberNotIn", new ExampleWithListValue().withSummary("not any of 1701, 74205, or 74656").withValue(ListBuilder.of("!IN 1701,74205,74656")));
      rs.put("criteriaNumberMultiple", new ExampleWithListValue().withSummary("multiple criteria: between 42 and 47 and not equal to 45").withValue(ListBuilder.of("BETWEEN 42,47", "!45")));

      rs.put("criteriaBooleanEquals", new ExampleWithListValue().withSummary("equal to true").withValue(ListBuilder.of("true")));
      rs.put("criteriaBooleanNotEquals", new ExampleWithListValue().withSummary("not equal to true").withValue(ListBuilder.of("!true")));
      rs.put("criteriaBooleanEmpty", new ExampleWithListValue().withSummary("null value").withValue(ListBuilder.of("EMPTY")));
      rs.put("criteriaBooleanNotEmpty", new ExampleWithListValue().withSummary("non-null value").withValue(ListBuilder.of("!EMPTY")));

      String now  = Instant.now().truncatedTo(ChronoUnit.SECONDS).toString();
      String then = Instant.now().minus(90, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS).toString();
      String when = Instant.now().plus(90, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS).toString();
      rs.put("criteriaDateTimeEquals", new ExampleWithListValue().withSummary("equal to " + now).withValue(ListBuilder.of(now)));
      rs.put("criteriaDateTimeNotEquals", new ExampleWithListValue().withSummary("not equal to " + now).withValue(ListBuilder.of("!" + now)));
      rs.put("criteriaDateTimeLessThan", new ExampleWithListValue().withSummary("less than " + now).withValue(ListBuilder.of("<" + now)));
      rs.put("criteriaDateTimeGreaterThan", new ExampleWithListValue().withSummary("greater than " + now).withValue(ListBuilder.of(">" + now)));
      rs.put("criteriaDateTimeLessThanOrEquals", new ExampleWithListValue().withSummary("less than or equal to " + now).withValue(ListBuilder.of("<=" + now)));
      rs.put("criteriaDateTimeGreaterThanOrEquals", new ExampleWithListValue().withSummary("greater than or equal to " + now).withValue(ListBuilder.of(">=" + now)));
      rs.put("criteriaDateTimeEmpty", new ExampleWithListValue().withSummary("null value").withValue(ListBuilder.of("EMPTY")));
      rs.put("criteriaDateTimeNotEmpty", new ExampleWithListValue().withSummary("non-null value").withValue(ListBuilder.of("!EMPTY")));
      rs.put("criteriaDateTimeBetween", new ExampleWithListValue().withSummary("between " + then + " and " + now).withValue(ListBuilder.of("BETWEEN " + then + "," + now)));
      rs.put("criteriaDateTimeNotBetween", new ExampleWithListValue().withSummary("not between " + then + " and " + now).withValue(ListBuilder.of("!BETWEEN " + then + "," + now)));
      rs.put("criteriaDateTimeIn", new ExampleWithListValue().withSummary("any of " + then + ", " + now + ", or " + when).withValue(ListBuilder.of("IN " + then + "," + now + "," + when)));
      rs.put("criteriaDateTimeNotIn", new ExampleWithListValue().withSummary("not any of " + then + ", " + now + ", or " + when).withValue(ListBuilder.of("!IN " + then + "," + now + "," + when)));
      rs.put("criteriaDateTimeMultiple", new ExampleWithListValue().withSummary("multiple criteria: between " + then + " and " + when + " and not equal to " + now).withValue(ListBuilder.of("BETWEEN " + then + "," + when, "!" + now)));

      now = LocalDate.now().toString();
      then = LocalDate.now().minus(90, ChronoUnit.DAYS).toString();
      when = LocalDate.now().plus(90, ChronoUnit.DAYS).toString();
      rs.put("criteriaDateEquals", new ExampleWithListValue().withSummary("equal to " + now).withValue(ListBuilder.of(now)));
      rs.put("criteriaDateNotEquals", new ExampleWithListValue().withSummary("not equal to " + now).withValue(ListBuilder.of("!" + now)));
      rs.put("criteriaDateLessThan", new ExampleWithListValue().withSummary("less than " + now).withValue(ListBuilder.of("<" + now)));
      rs.put("criteriaDateGreaterThan", new ExampleWithListValue().withSummary("greater than " + now).withValue(ListBuilder.of(">" + now)));
      rs.put("criteriaDateLessThanOrEquals", new ExampleWithListValue().withSummary("less than or equal to " + now).withValue(ListBuilder.of("<=" + now)));
      rs.put("criteriaDateGreaterThanOrEquals", new ExampleWithListValue().withSummary("greater than or equal to " + now).withValue(ListBuilder.of(">=" + now)));
      rs.put("criteriaDateEmpty", new ExampleWithListValue().withSummary("null value").withValue(ListBuilder.of("EMPTY")));
      rs.put("criteriaDateNotEmpty", new ExampleWithListValue().withSummary("non-null value").withValue(ListBuilder.of("!EMPTY")));
      rs.put("criteriaDateBetween", new ExampleWithListValue().withSummary("between " + then + " and " + now).withValue(ListBuilder.of("BETWEEN " + then + "," + now)));
      rs.put("criteriaDateNotBetween", new ExampleWithListValue().withSummary("not between " + then + " and " + now).withValue(ListBuilder.of("!BETWEEN " + then + "," + now)));
      rs.put("criteriaDateIn", new ExampleWithListValue().withSummary("any of " + then + ", " + now + ", or " + when).withValue(ListBuilder.of("IN " + then + "," + now + "," + when)));
      rs.put("criteriaDateNotIn", new ExampleWithListValue().withSummary("not any of " + then + ", " + now + ", or " + when).withValue(ListBuilder.of("!IN " + then + "," + now + "," + when)));
      rs.put("criteriaDateMultiple", new ExampleWithListValue().withSummary("multiple criteria: between " + then + " and " + when + " and not equal to " + now).withValue(ListBuilder.of("BETWEEN " + then + "," + when, "!" + now)));

      rs.put("criteriaStringEquals", new ExampleWithListValue().withSummary("equal to foo").withValue(ListBuilder.of("foo")));
      rs.put("criteriaStringNotEquals", new ExampleWithListValue().withSummary("not equal to foo").withValue(ListBuilder.of("!foo")));
      rs.put("criteriaStringLessThan", new ExampleWithListValue().withSummary("less than foo").withValue(ListBuilder.of("<foo")));
      rs.put("criteriaStringGreaterThan", new ExampleWithListValue().withSummary("greater than foo").withValue(ListBuilder.of(">foo")));
      rs.put("criteriaStringLessThanOrEquals", new ExampleWithListValue().withSummary("less than or equal to foo").withValue(ListBuilder.of("<=foo")));
      rs.put("criteriaStringGreaterThanOrEquals", new ExampleWithListValue().withSummary("greater than or equal to foo").withValue(ListBuilder.of(">=foo")));
      rs.put("criteriaStringEmpty", new ExampleWithListValue().withSummary("null value").withValue(ListBuilder.of("EMPTY")));
      rs.put("criteriaStringNotEmpty", new ExampleWithListValue().withSummary("non-null value").withValue(ListBuilder.of("!EMPTY")));
      rs.put("criteriaStringBetween", new ExampleWithListValue().withSummary("between bar and foo").withValue(ListBuilder.of("BETWEEN bar,foo")));
      rs.put("criteriaStringNotBetween", new ExampleWithListValue().withSummary("not between bar and foo").withValue(ListBuilder.of("!BETWEEN bar,foo")));
      rs.put("criteriaStringIn", new ExampleWithListValue().withSummary("any of foo, bar, or baz").withValue(ListBuilder.of("IN foo,bar,baz")));
      rs.put("criteriaStringNotIn", new ExampleWithListValue().withSummary("not any of foo, bar, or baz").withValue(ListBuilder.of("!IN foo,bar,baz")));
      rs.put("criteriaStringLike", new ExampleWithListValue().withSummary("starting with f").withValue(ListBuilder.of("LIKE f%")));
      rs.put("criteriaStringNotLike", new ExampleWithListValue().withSummary("not starting with f").withValue(ListBuilder.of("!LIKE f%")));
      rs.put("criteriaStringMultiple", new ExampleWithListValue().withSummary("multiple criteria: between bar and foo and not equal to baz").withValue(ListBuilder.of("BETWEEN bar,foo", "!baz")));

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Map<String, Example> getCriteriaExamples(Map<String, Example> componentExamples, QFieldMetaData tableApiField)
   {
      List<String> exampleRefs = new ArrayList<>();
      exampleRefs.add("criteriaNotQueried");

      if(tableApiField.getType().isStringLike())
      {
         componentExamples.keySet().stream().filter(s -> s.startsWith("criteriaString")).forEach(exampleRefs::add);
      }
      else if(tableApiField.getType().isNumeric())
      {
         componentExamples.keySet().stream().filter(s -> s.startsWith("criteriaNumber")).forEach(exampleRefs::add);
      }
      else if(tableApiField.getType().equals(QFieldType.DATE_TIME))
      {
         componentExamples.keySet().stream().filter(s -> s.startsWith("criteriaDateTime")).forEach(exampleRefs::add);
      }
      else if(tableApiField.getType().equals(QFieldType.DATE))
      {
         componentExamples.keySet().stream().filter(s -> s.startsWith("criteriaDate") && !s.startsWith("criteriaDateTime")).forEach(exampleRefs::add);
      }
      else if(tableApiField.getType().equals(QFieldType.BOOLEAN))
      {
         componentExamples.keySet().stream().filter(s -> s.startsWith("criteriaBoolean")).forEach(exampleRefs::add);
      }

      Map<String, Example> rs = new LinkedHashMap<>();

      for(String exampleRef : exampleRefs)
      {
         rs.put(exampleRef, new Example().withRef("#components/examples/" + exampleRef));
      }

      return (rs);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static void addAssociations(QTableMetaData table, Schema tableSchema)
   {
      for(Association association : CollectionUtils.nonNullList(table.getAssociations()))
      {
         String           associatedTableName        = association.getAssociatedTableName();
         QTableMetaData   associatedTable            = QContext.getQInstance().getTable(associatedTableName);
         ApiTableMetaData associatedApiTableMetaData = Objects.requireNonNullElse(ApiTableMetaData.of(associatedTable), new ApiTableMetaData());
         String           associatedTableApiName     = StringUtils.hasContent(associatedApiTableMetaData.getApiTableName()) ? associatedApiTableMetaData.getApiTableName() : associatedTableName;

         tableSchema.getProperties().put(association.getName(), new Schema()
            .withType("array")
            .withItems(new Schema().withRef("#/components/schemas/" + associatedTableApiName)));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private Schema getFieldSchema(QTableMetaData table, QFieldMetaData field)
   {
      Schema fieldSchema = new Schema()
         .withType(getFieldType(table.getField(field.getName())))
         .withFormat(getFieldFormat(table.getField(field.getName())))
         .withDescription(field.getLabel() + " for the " + table.getLabel() + ".");

      if(!field.getIsEditable())
      {
         fieldSchema.setReadOnly(true);
      }

      if(!field.getIsRequired())
      {
         fieldSchema.setNullable(true);
      }

      if(field.getType().isStringLike() && field.getMaxLength() != null)
      {
         fieldSchema.setMaxLength(field.getMaxLength());
      }

      if(StringUtils.hasContent(field.getPossibleValueSourceName()))
      {
         QPossibleValueSource possibleValueSource = QContext.getQInstance().getPossibleValueSource(field.getPossibleValueSourceName());
         if(QPossibleValueSourceType.ENUM.equals(possibleValueSource.getType()))
         {
            List<String> enumValues  = new ArrayList<>();
            List<String> enumMapping = new ArrayList<>();
            for(QPossibleValue<?> enumValue : possibleValueSource.getEnumValues())
            {
               enumValues.add(String.valueOf(enumValue.getId()));
               enumMapping.add(enumValue.getId() + "=" + enumValue.getLabel());
            }
            fieldSchema.setEnumValues(enumValues);
            fieldSchema.setDescription(fieldSchema.getDescription() + "  Value definitions are: " + StringUtils.joinWithCommasAndAnd(enumMapping));
         }
         else if(QPossibleValueSourceType.TABLE.equals(possibleValueSource.getType()))
         {
            QTableMetaData sourceTable = QContext.getQInstance().getTable(possibleValueSource.getTableName());
            fieldSchema.setDescription(fieldSchema.getDescription() + "  Values in this field come from the primary key of the " + sourceTable.getLabel() + " table");
         }
      }
      return fieldSchema;
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
      properties.put("statusCode", new Schema().withType("integer"));
      properties.put("statusText", new Schema().withType("string"));
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

      rs.put("default", new ExampleWithListValue()
         .withSummary("default:  order by " + primaryKeyApiName + " descending")
         .withValue(ListBuilder.of("")));

      rs.put(primaryKeyApiName, new ExampleWithSingleValue()
         .withSummary("order by " + primaryKeyApiName + " (ascending, since ASC/DESC was not specified)")
         .withValue("id"));

      rs.put(primaryKeyApiName + "Desc", new ExampleWithSingleValue()
         .withSummary("order by " + primaryKeyApiName + " descending")
         .withValue("id DESC"));

      rs.put(primaryKeyApiName + "Asc", new ExampleWithSingleValue()
         .withSummary("order by " + primaryKeyApiName + " ascending")
         .withValue("id ASC"));

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
            .withValue(a + " DESC, " + b + " ASC, " + c));
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
