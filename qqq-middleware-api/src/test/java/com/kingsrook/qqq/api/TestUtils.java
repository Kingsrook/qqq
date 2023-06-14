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

package com.kingsrook.qqq.api;


import java.util.List;
import java.util.function.Consumer;
import com.kingsrook.qqq.api.model.APIVersion;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaData;
import com.kingsrook.qqq.api.model.metadata.ApiInstanceMetaDataContainer;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaData;
import com.kingsrook.qqq.api.model.metadata.fields.ApiFieldMetaDataContainer;
import com.kingsrook.qqq.api.model.metadata.processes.ApiProcessInput;
import com.kingsrook.qqq.api.model.metadata.processes.ApiProcessInputFieldsContainer;
import com.kingsrook.qqq.api.model.metadata.processes.ApiProcessMetaData;
import com.kingsrook.qqq.api.model.metadata.processes.ApiProcessMetaDataContainer;
import com.kingsrook.qqq.api.model.metadata.processes.ApiProcessObjectOutput;
import com.kingsrook.qqq.api.model.metadata.processes.ApiProcessSummaryListOutput;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaData;
import com.kingsrook.qqq.api.model.metadata.tables.ApiTableMetaDataContainer;
import com.kingsrook.qqq.api.model.openapi.HttpMethod;
import com.kingsrook.qqq.backend.core.actions.customizers.AbstractPreDeleteCustomizer;
import com.kingsrook.qqq.backend.core.actions.customizers.AbstractPreInsertCustomizer;
import com.kingsrook.qqq.backend.core.actions.customizers.AbstractPreUpdateCustomizer;
import com.kingsrook.qqq.backend.core.actions.customizers.TableCustomizers;
import com.kingsrook.qqq.backend.core.actions.tables.InsertAction;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.insert.InsertInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterOrderBy;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationType;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.authentication.Auth0AuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.HtmlWrapper;
import com.kingsrook.qqq.backend.core.model.metadata.dashboard.nocode.WidgetHtmlLine;
import com.kingsrook.qqq.backend.core.model.metadata.fields.DisplayFormat;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinOn;
import com.kingsrook.qqq.backend.core.model.metadata.joins.JoinType;
import com.kingsrook.qqq.backend.core.model.metadata.joins.QJoinMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.PVSValueFormatAndFields;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSource;
import com.kingsrook.qqq.backend.core.model.metadata.possiblevalues.QPossibleValueSourceType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.NoCodeWidgetFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QBackendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QComponentType;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendComponentMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QFrontendStepMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.processes.QProcessMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.Association;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.tables.UniqueKey;
import com.kingsrook.qqq.backend.core.model.statusmessages.BadInputStatusMessage;
import com.kingsrook.qqq.backend.core.model.statusmessages.QWarningMessage;
import com.kingsrook.qqq.backend.core.model.statusmessages.SystemErrorStatusMessage;
import com.kingsrook.qqq.backend.core.modules.backend.implementations.memory.MemoryBackendModule;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaUpdateStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class TestUtils
{
   public static final String MEMORY_BACKEND_NAME = "memory";

   public static final String TABLE_NAME_PERSON              = "person";
   public static final String TABLE_NAME_ORDER               = "order";
   public static final String TABLE_NAME_LINE_ITEM           = "orderLine";
   public static final String TABLE_NAME_LINE_ITEM_EXTRINSIC = "orderLineExtrinsic";
   public static final String TABLE_NAME_ORDER_EXTRINSIC     = "orderExtrinsic";

   public static final String PROCESS_NAME_GET_PERSON_INFO  = "getPersonInfo";
   public static final String PROCESS_NAME_TRANSFORM_PEOPLE = "transformPeople";

   public static final String API_NAME             = "test-api";
   public static final String ALTERNATIVE_API_NAME = "person-api";

   public static final String V2022_Q4 = "2022.Q4";
   public static final String V2023_Q1 = "2023.Q1";
   public static final String V2023_Q2 = "2023.Q2";

   public static final String CURRENT_API_VERSION = V2023_Q1;



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QInstance defineInstance()
   {
      QInstance qInstance = new QInstance();

      qInstance.addBackend(defineMemoryBackend());
      qInstance.addTable(defineTablePerson());
      qInstance.addTable(defineTableOrder());
      qInstance.addTable(defineTableLineItem());
      qInstance.addTable(defineTableLineItemExtrinsic());
      qInstance.addTable(defineTableOrderExtrinsic());

      qInstance.addJoin(defineJoinOrderLineItem());
      qInstance.addJoin(defineJoinLineItemLineItemExtrinsic());
      qInstance.addJoin(defineJoinOrderOrderExtrinsic());

      qInstance.addPossibleValueSource(definePersonPossibleValueSource());
      qInstance.addProcess(defineProcessGetPersonInfo());
      qInstance.addProcess(defineProcessTransformPeople());

      qInstance.setAuthentication(new Auth0AuthenticationMetaData().withType(QAuthenticationType.FULLY_ANONYMOUS).withName("anonymous"));

      qInstance.withSupplementalMetaData(new ApiInstanceMetaDataContainer()
         .withApiInstanceMetaData(new ApiInstanceMetaData()
            .withName(API_NAME)
            .withPath("/api/")
            .withLabel("Test API")
            .withDescription("QQQ Test API")
            .withContactEmail("contact@kingsrook.com")
            .withCurrentVersion(new APIVersion(CURRENT_API_VERSION))
            .withSupportedVersions(List.of(new APIVersion(V2022_Q4), new APIVersion(V2023_Q1)))
            .withPastVersions(List.of(new APIVersion(V2022_Q4)))
            .withFutureVersions(List.of(new APIVersion(V2023_Q2))))
         .withApiInstanceMetaData(new ApiInstanceMetaData()
            .withName(ALTERNATIVE_API_NAME)
            .withPath("/person-api/")
            .withLabel("Person-Only API")
            .withDescription("QQQ Test API, that only has the Person table.")
            .withContactEmail("contact@kingsrook.com")
            .withCurrentVersion(new APIVersion(CURRENT_API_VERSION))
            .withSupportedVersions(List.of(new APIVersion(V2022_Q4), new APIVersion(V2023_Q1)))
            .withPastVersions(List.of(new APIVersion(V2022_Q4)))
            .withFutureVersions(List.of(new APIVersion(V2023_Q2))))
      );

      return (qInstance);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QPossibleValueSource definePersonPossibleValueSource()
   {
      return new QPossibleValueSource()
         .withName(TABLE_NAME_PERSON)
         .withType(QPossibleValueSourceType.TABLE)
         .withTableName(TABLE_NAME_PERSON)
         .withValueFormatAndFields(PVSValueFormatAndFields.LABEL_ONLY);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QProcessMetaData defineProcessGetPersonInfo()
   {
      QProcessMetaData process = new QProcessMetaData()
         .withName(PROCESS_NAME_GET_PERSON_INFO)
         .withLabel("Get Person Info")
         .withTableName(TABLE_NAME_PERSON)
         .addStep(new QFrontendStepMetaData()
            .withName("enterInputs")
            .withLabel("Person Info Input")
            .withComponent(new QFrontendComponentMetaData().withType(QComponentType.EDIT_FORM))

            .withFormField(new QFieldMetaData("age", QFieldType.INTEGER).withIsRequired(true))
            .withFormField(new QFieldMetaData("partnerPersonId", QFieldType.INTEGER).withPossibleValueSourceName(TABLE_NAME_PERSON))
            .withFormField(new QFieldMetaData("heightInches", QFieldType.DECIMAL).withIsRequired(true))
            .withFormField(new QFieldMetaData("weightPounds", QFieldType.INTEGER).withIsRequired(true))
            .withFormField(new QFieldMetaData("homeTown", QFieldType.STRING).withIsRequired(true))

            .withComponent(new NoCodeWidgetFrontendComponentMetaData()

               .withOutput(new WidgetHtmlLine()
                  .withWrapper(HtmlWrapper.divWithStyles(HtmlWrapper.STYLE_FLOAT_RIGHT, HtmlWrapper.STYLE_MEDIUM_CENTERED, HtmlWrapper.styleWidth("50%")))
                  .withVelocityTemplate("""
                     <b>Density:</b><br />$density<br/>
                     """))

               .withOutput(new WidgetHtmlLine()
                  .withVelocityTemplate("""
                     <b>Days old:</b> $daysOld<br/>
                     <b>Nickname:</b> $nickname<br/>
                     """))
            ))

         .addStep(new QBackendStepMetaData()
            .withName("execute")
            .withCode(new QCodeReference(GetPersonInfoStep.class)))

         .addStep(new QFrontendStepMetaData()
            .withName("dummyStep")
         );

      process.withSupplementalMetaData(new ApiProcessMetaDataContainer()
         .withApiProcessMetaData(API_NAME, new ApiProcessMetaData()
            .withInitialVersion(CURRENT_API_VERSION)
            .withMethod(HttpMethod.GET)
            .withInput(new ApiProcessInput()
               .withQueryStringParams(new ApiProcessInputFieldsContainer().withInferredInputFields(process)))
            .withOutput(new ApiProcessObjectOutput()
               .withOutputField(new QFieldMetaData("density", QFieldType.DECIMAL))
               .withOutputField(new QFieldMetaData("daysOld", QFieldType.INTEGER))
               .withOutputField(new QFieldMetaData("nickname", QFieldType.STRING)))
         ));

      return (process);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static QProcessMetaData defineProcessTransformPeople()
   {
      QProcessMetaData process = StreamedETLWithFrontendProcess.processMetaDataBuilder()
         .withName(PROCESS_NAME_TRANSFORM_PEOPLE)
         .withTableName(TABLE_NAME_PERSON)
         .withSourceTable(TABLE_NAME_PERSON)
         .withDestinationTable(TABLE_NAME_PERSON)
         .withMinInputRecords(1)
         .withExtractStepClass(ExtractViaQueryStep.class)
         .withTransformStepClass(TransformPersonStep.class)
         .withLoadStepClass(LoadViaUpdateStep.class)
         .getProcessMetaData();

      process.withSupplementalMetaData(new ApiProcessMetaDataContainer()
         .withApiProcessMetaData(API_NAME, new ApiProcessMetaData()
            .withInitialVersion(CURRENT_API_VERSION)
            .withMethod(HttpMethod.POST)
            .withInput(new ApiProcessInput()
               .withQueryStringParams(new ApiProcessInputFieldsContainer().withRecordIdsField(new QFieldMetaData("id", QFieldType.STRING))))
            .withOutput(new ApiProcessSummaryListOutput())));

      return (process);
   }



   /*******************************************************************************
    ** Define the in-memory backend used in standard tests
    *******************************************************************************/
   public static QBackendMetaData defineMemoryBackend()
   {
      return new QBackendMetaData()
         .withName(MEMORY_BACKEND_NAME)
         .withBackendType(MemoryBackendModule.class);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class PersonPreInsertCustomizer extends AbstractPreInsertCustomizer
   {

      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public List<QRecord> apply(List<QRecord> records)
      {
         for(QRecord record : CollectionUtils.nonNullList(records))
         {
            if(record.getValueString("firstName") != null && !record.getValueString("firstName").matches(".*[a-z].*"))
            {
               record.addWarning(new QWarningMessage("First name does not contain any letters..."));
            }
         }

         return (records);
      }

   }



   /*******************************************************************************
    ** Define the 'person' table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTablePerson()
   {
      QTableMetaData table = new QTableMetaData()
         .withName(TABLE_NAME_PERSON)
         .withLabel("Person")
         .withBackendName(MEMORY_BACKEND_NAME)
         .withPrimaryKeyField("id")
         .withUniqueKey(new UniqueKey("email"))
         .withCustomizer(TableCustomizers.PRE_DELETE_RECORD, new QCodeReference(PersonPreDeleteCustomizer.class))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE))
         .withField(new QFieldMetaData("email", QFieldType.STRING))
         // .withField(new QFieldMetaData("homeStateId", QFieldType.INTEGER).withPossibleValueSourceName(POSSIBLE_VALUE_SOURCE_STATE))
         // .withField(new QFieldMetaData("favoriteShapeId", QFieldType.INTEGER).withPossibleValueSourceName(POSSIBLE_VALUE_SOURCE_SHAPE))
         // .withField(new QFieldMetaData("customValue", QFieldType.INTEGER).withPossibleValueSourceName(POSSIBLE_VALUE_SOURCE_CUSTOM))
         .withField(new QFieldMetaData("noOfShoes", QFieldType.INTEGER).withDisplayFormat(DisplayFormat.COMMAS))
         .withField(new QFieldMetaData("cost", QFieldType.DECIMAL).withDisplayFormat(DisplayFormat.CURRENCY))
         .withField(new QFieldMetaData("price", QFieldType.DECIMAL).withDisplayFormat(DisplayFormat.CURRENCY))
         .withField(new QFieldMetaData("photo", QFieldType.BLOB));

      table.withCustomizer(TableCustomizers.PRE_INSERT_RECORD.getRole(), new QCodeReference(PersonPreInsertCustomizer.class));

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // make some changes to this table in the "main" api (but leave it like the backend in the ALTERNATIVE_API_NAME) //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      table.withSupplementalMetaData(new ApiTableMetaDataContainer()
         .withApiTableMetaData(API_NAME, new ApiTableMetaData()
            .withInitialVersion(V2022_Q4)

            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // in 2022.Q4, this table had a "shoeCount" field. but for the 2023.Q1 version, we renamed it to noOfShoes! //
            //////////////////////////////////////////////////////////////////////////////////////////////////////////////
            .withRemovedApiField(new QFieldMetaData("shoeCount", QFieldType.INTEGER).withDisplayFormat(DisplayFormat.COMMAS)
               .withSupplementalMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME,
                  new ApiFieldMetaData().withFinalVersion(V2022_Q4).withReplacedByFieldName("noOfShoes"))))
         )
         .withApiTableMetaData(ALTERNATIVE_API_NAME, new ApiTableMetaData().withInitialVersion(V2022_Q4)));

      /////////////////////////////////////////////////////
      // change the name for this field for the main api //
      /////////////////////////////////////////////////////
      table.getField("birthDate").withSupplementalMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME, new ApiFieldMetaData().withApiFieldName("birthDay")));

      ////////////////////////////////////////////////////////////////////////////////
      // See above - we renamed this field (in the backend) for the 2023_Q1 version //
      ////////////////////////////////////////////////////////////////////////////////
      table.getField("noOfShoes").withSupplementalMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME, new ApiFieldMetaData().withInitialVersion(V2023_Q1)));

      /////////////////////////////////////////////////////////////////////////////////////////////////
      // 2 new fields - one will appear in a future version of the API, the other is always excluded //
      /////////////////////////////////////////////////////////////////////////////////////////////////
      table.getField("cost").withSupplementalMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME, new ApiFieldMetaData().withInitialVersion(V2023_Q2)));
      table.getField("price").withSupplementalMetaData(new ApiFieldMetaDataContainer().withApiFieldMetaData(API_NAME, new ApiFieldMetaData().withIsExcluded(true)));

      return (table);
   }



   /*******************************************************************************
    ** Define the order table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTableOrder()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_ORDER)
         .withCustomizer(TableCustomizers.PRE_INSERT_RECORD.getRole(), new QCodeReference(OrderPreInsertCustomizer.class))
         .withCustomizer(TableCustomizers.PRE_UPDATE_RECORD.getRole(), new QCodeReference(OrderPreUpdateCustomizer.class))
         .withBackendName(MEMORY_BACKEND_NAME)
         .withSupplementalMetaData(new ApiTableMetaDataContainer().withApiTableMetaData(TestUtils.API_NAME, new ApiTableMetaData().withInitialVersion(V2022_Q4)))
         .withPrimaryKeyField("id")
         .withAssociation(new Association().withName("orderLines").withAssociatedTableName(TABLE_NAME_LINE_ITEM).withJoinName("orderLineItem"))
         .withAssociation(new Association().withName("extrinsics").withAssociatedTableName(TABLE_NAME_ORDER_EXTRINSIC).withJoinName("orderOrderExtrinsic"))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("orderNo", QFieldType.STRING))
         .withField(new QFieldMetaData("orderDate", QFieldType.DATE))
         .withField(new QFieldMetaData("storeId", QFieldType.INTEGER))
         .withField(new QFieldMetaData("total", QFieldType.DECIMAL).withDisplayFormat(DisplayFormat.CURRENCY));
   }



   /*******************************************************************************
    ** Define the lineItem table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTableLineItem()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_LINE_ITEM)
         .withBackendName(MEMORY_BACKEND_NAME)
         .withSupplementalMetaData(new ApiTableMetaDataContainer().withApiTableMetaData(TestUtils.API_NAME, new ApiTableMetaData().withInitialVersion(V2022_Q4)))
         .withPrimaryKeyField("id")
         .withAssociation(new Association().withName("extrinsics").withAssociatedTableName(TABLE_NAME_LINE_ITEM_EXTRINSIC).withJoinName("lineItemLineItemExtrinsic"))
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("orderId", QFieldType.INTEGER))
         .withField(new QFieldMetaData("lineNumber", QFieldType.STRING))
         .withField(new QFieldMetaData("sku", QFieldType.STRING))
         .withField(new QFieldMetaData("quantity", QFieldType.INTEGER));
   }



   /*******************************************************************************
    ** Define the lineItemExtrinsic table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTableLineItemExtrinsic()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_LINE_ITEM_EXTRINSIC)
         .withBackendName(MEMORY_BACKEND_NAME)
         .withSupplementalMetaData(new ApiTableMetaDataContainer().withApiTableMetaData(TestUtils.API_NAME, new ApiTableMetaData().withInitialVersion(V2022_Q4)))
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("lineItemId", QFieldType.INTEGER))
         .withField(new QFieldMetaData("key", QFieldType.STRING))
         .withField(new QFieldMetaData("value", QFieldType.STRING));
   }



   /*******************************************************************************
    ** Define the orderExtrinsic table used in standard tests.
    *******************************************************************************/
   public static QTableMetaData defineTableOrderExtrinsic()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_ORDER_EXTRINSIC)
         .withBackendName(MEMORY_BACKEND_NAME)
         .withSupplementalMetaData(new ApiTableMetaDataContainer().withApiTableMetaData(TestUtils.API_NAME, new ApiTableMetaData().withInitialVersion(V2022_Q4)))
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER).withIsEditable(false))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withIsEditable(false))
         .withField(new QFieldMetaData("orderId", QFieldType.INTEGER))
         .withField(new QFieldMetaData("key", QFieldType.STRING))
         .withField(new QFieldMetaData("value", QFieldType.STRING));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QJoinMetaData defineJoinOrderLineItem()
   {
      return new QJoinMetaData()
         .withName("orderLineItem")
         .withType(JoinType.ONE_TO_MANY)
         .withLeftTable(TABLE_NAME_ORDER)
         .withRightTable(TABLE_NAME_LINE_ITEM)
         .withJoinOn(new JoinOn("id", "orderId"))
         .withOrderBy(new QFilterOrderBy("lineNumber"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QJoinMetaData defineJoinLineItemLineItemExtrinsic()
   {
      return new QJoinMetaData()
         .withName("lineItemLineItemExtrinsic")
         .withType(JoinType.ONE_TO_MANY)
         .withLeftTable(TABLE_NAME_LINE_ITEM)
         .withRightTable(TABLE_NAME_LINE_ITEM_EXTRINSIC)
         .withJoinOn(new JoinOn("id", "lineItemId"))
         .withOrderBy(new QFilterOrderBy("key"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QJoinMetaData defineJoinOrderOrderExtrinsic()
   {
      return new QJoinMetaData()
         .withName("orderOrderExtrinsic")
         .withType(JoinType.ONE_TO_MANY)
         .withLeftTable(TABLE_NAME_ORDER)
         .withRightTable(TABLE_NAME_ORDER_EXTRINSIC)
         .withJoinOn(new JoinOn("id", "orderId"))
         .withOrderBy(new QFilterOrderBy("key"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void insertPersonRecord(Integer id, String firstName, String lastName) throws QException
   {
      insertPersonRecord(id, firstName, lastName, null);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void insertPersonRecord(Integer id, String firstName, String lastName, Consumer<QRecord> recordCustomizer) throws QException
   {
      InsertInput insertInput = new InsertInput();
      insertInput.setTableName(TestUtils.TABLE_NAME_PERSON);
      QRecord record = new QRecord().withValue("id", id).withValue("firstName", firstName).withValue("lastName", lastName);
      if(recordCustomizer != null)
      {
         recordCustomizer.accept(record);
      }
      insertInput.setRecords(List.of(record));
      new InsertAction().execute(insertInput);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static void insertSimpsons() throws QException
   {
      insertPersonRecord(1, "Homer", "Simpson");
      insertPersonRecord(2, "Marge", "Simpson");
      insertPersonRecord(3, "Bart", "Simpson");
      insertPersonRecord(4, "Lisa", "Simpson");
      insertPersonRecord(5, "Maggie", "Simpson");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class OrderPreInsertCustomizer extends AbstractPreInsertCustomizer
   {
      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public List<QRecord> apply(List<QRecord> records) throws QException
      {
         for(QRecord record : records)
         {
            for(QRecord orderLine : CollectionUtils.nonNullList(record.getAssociatedRecords().get("orderLines")))
            {
               if(orderLine.getValueInteger("quantity") != null && orderLine.getValueInteger("quantity") <= 0)
               {
                  record.addError(new BadInputStatusMessage("Quantity may not be less than 0.  See SKU " + orderLine.getValueString("sku")));
               }
            }

            if("throw".equals(record.getValueString("orderNo")))
            {
               record.addError(new SystemErrorStatusMessage("Throwing error, as requested..."));
            }
         }

         return (records);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class OrderPreUpdateCustomizer extends AbstractPreUpdateCustomizer
   {
      @Override
      public List<QRecord> apply(List<QRecord> records) throws QException
      {
         /////////////////////////////////////////////
         // use same logic as pre-insert customizer //
         /////////////////////////////////////////////
         return new OrderPreInsertCustomizer().apply(records);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class PersonPreDeleteCustomizer extends AbstractPreDeleteCustomizer
   {
      public static final Integer DELETE_ERROR_ID = 9999;
      public static final Integer DELETE_WARN_ID  = 9998;



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public List<QRecord> apply(List<QRecord> records)
      {
         for(QRecord record : records)
         {
            if(DELETE_ERROR_ID.equals(record.getValue("id")))
            {
               record.addError(new BadInputStatusMessage("You may not delete this person"));
            }
            else if(DELETE_WARN_ID.equals(record.getValue("id")))
            {
               record.addWarning(new QWarningMessage("It was bad that you deleted this person"));
            }
         }

         return (records);
      }
   }

}
