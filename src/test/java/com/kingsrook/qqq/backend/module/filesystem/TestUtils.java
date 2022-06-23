/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2022.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.module.filesystem;


import java.io.File;
import java.io.IOException;
import com.kingsrook.qqq.backend.core.exceptions.QInstanceValidationException;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QAuthenticationMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QFieldType;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.modules.mock.MockAuthenticationModule;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.local.model.metadata.FilesystemTableBackendDetails;
import com.kingsrook.qqq.backend.module.filesystem.s3.BaseS3Test;
import com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata.S3BackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.s3.model.metadata.S3TableBackendDetails;
import org.apache.commons.io.FileUtils;


/*******************************************************************************
 ** Utility methods for Filesystem module tests
 *******************************************************************************/
public class TestUtils
{
   public static final String BACKEND_NAME_LOCAL_FS      = "local-filesystem";
   public static final String BACKEND_NAME_S3            = "s3";
   public static final String TABLE_NAME_PERSON_LOCAL_FS = "person";
   public static final String TABLE_NAME_PERSON_S3       = "person-s3";

   ///////////////////////////////////////////////////////////////////
   // shouldn't be accessed directly, as we append a counter to it. //
   ///////////////////////////////////////////////////////////////////
   public static final String BASE_PATH = "/tmp/filesystem-tests";

   private static int testInstanceCounter = 0;



   /*******************************************************************************
    ** Meant to be called in a @BeforeEach - increment an internal counter that will
    ** give us a unique directory name for each test method.
    *******************************************************************************/
   public static void increaseTestInstanceCounter()
   {
      testInstanceCounter++;
   }



   /*******************************************************************************
    ** Reset the counter to 0 (e.g., to let some tests have a known value).
    *******************************************************************************/
   public static void resetTestInstanceCounter()
   {
      testInstanceCounter = 0;
   }



   /*******************************************************************************
    ** Get the current value of the testInstanceCounter.  See {@link #increaseTestInstanceCounter()}
    *******************************************************************************/
   public static int getTestInstanceCounter()
   {
      return (testInstanceCounter);
   }



   /*******************************************************************************
    ** Meant to run both after and before test methods - makes sure the file system
    ** is empty for the path under the instance.
    *******************************************************************************/
   public static void cleanInstanceFiles() throws IOException
   {
      FileUtils.deleteDirectory(new File(BASE_PATH + File.separator + testInstanceCounter));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QInstance defineInstance() throws QInstanceValidationException
   {
      QInstance qInstance = new QInstance();
      qInstance.setAuthentication(defineAuthentication());
      qInstance.addBackend(defineLocalFilesystemBackend());
      qInstance.addTable(defineLocalFilesystemCSVPersonTable());
      qInstance.addBackend(defineS3Backend());
      qInstance.addTable(defineS3CSVPersonTable());

      new QInstanceValidator().validate(qInstance);

      return (qInstance);
   }



   /*******************************************************************************
    ** Define the authentication used in standard tests - using 'mock' type.
    **
    *******************************************************************************/
   public static QAuthenticationMetaData defineAuthentication()
   {
      return new QAuthenticationMetaData()
         .withName("mock")
         .withType("mock");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static FilesystemBackendMetaData defineLocalFilesystemBackend()
   {
      return (new FilesystemBackendMetaData()
         .withBasePath(BASE_PATH + File.separator + testInstanceCounter)
         .withName(BACKEND_NAME_LOCAL_FS));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QTableMetaData defineLocalFilesystemCSVPersonTable()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_PERSON_LOCAL_FS)
         .withLabel("Person")
         .withBackendName(defineLocalFilesystemBackend().getName())
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withBackendName("create_date"))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withBackendName("modify_date"))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING).withBackendName("first_name"))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING).withBackendName("last_name"))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE).withBackendName("birth_date"))
         .withField(new QFieldMetaData("email", QFieldType.STRING))
         .withBackendDetails(new FilesystemTableBackendDetails()
            .withPath("persons")
            .withRecordFormat("json")
            .withCardinality("many")
         );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static S3BackendMetaData defineS3Backend()
   {
      return (new S3BackendMetaData()
         .withBucketName(BaseS3Test.BUCKET_NAME)
         .withBasePath(BaseS3Test.TEST_FOLDER)
         .withName(BACKEND_NAME_S3));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QTableMetaData defineS3CSVPersonTable()
   {
      return new QTableMetaData()
         .withName(TABLE_NAME_PERSON_S3)
         .withLabel("Person S3 Table")
         .withBackendName(defineS3Backend().getName())
         .withPrimaryKeyField("id")
         .withField(new QFieldMetaData("id", QFieldType.INTEGER))
         .withField(new QFieldMetaData("createDate", QFieldType.DATE_TIME).withBackendName("create_date"))
         .withField(new QFieldMetaData("modifyDate", QFieldType.DATE_TIME).withBackendName("modify_date"))
         .withField(new QFieldMetaData("firstName", QFieldType.STRING).withBackendName("first_name"))
         .withField(new QFieldMetaData("lastName", QFieldType.STRING).withBackendName("last_name"))
         .withField(new QFieldMetaData("birthDate", QFieldType.DATE).withBackendName("birth_date"))
         .withField(new QFieldMetaData("email", QFieldType.STRING))
         .withBackendDetails(new S3TableBackendDetails()
            .withRecordFormat("csv")
            .withCardinality("many")
         );
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static QSession getMockSession()
   {
      MockAuthenticationModule mockAuthenticationModule = new MockAuthenticationModule();
      return (mockAuthenticationModule.createSession(null));
   }
}
