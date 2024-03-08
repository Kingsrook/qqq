/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2024.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.instances.validation.plugins;


import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.instances.QInstanceValidator;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.processes.implementations.basepull.BasepullConfiguration;
import com.kingsrook.qqq.backend.core.processes.implementations.basepull.ExtractViaBasepullQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.ExtractViaQueryStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.LoadViaInsertStep;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcess;
import com.kingsrook.qqq.backend.core.processes.implementations.etl.streamedwithfrontend.StreamedETLWithFrontendProcessTest;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.instances.QInstanceValidatorTest.assertValidationFailureReasons;
import static org.assertj.core.api.Assertions.assertThat;


/*******************************************************************************
 ** Unit test for BasepullExtractStepValidator 
 *******************************************************************************/
class BasepullExtractStepValidatorTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testNoExtractStepAtAllFails()
   {
      QInstance          qInstance = QContext.getQInstance();
      QInstanceValidator validator = new QInstanceValidator();

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
      // turns out, our "basepullTestProcess" doesn't have an extract step, so that makes this condition fire. //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////
      new BasepullExtractStepValidator().validate(qInstance.getProcess(TestUtils.PROCESS_NAME_BASEPULL), qInstance, validator);
      assertValidationFailureReasons(false, validator.getErrors(), "does not have a field with a default value that is a BasepullExtractStepInterface CodeReference");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testExtractViaQueryNotBasePull()
   {
      QInstance          qInstance = QContext.getQInstance();
      QInstanceValidator validator = new QInstanceValidator();

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // set up a streamed-etl process, but with an ExtractViaQueryStep instead of a basepull - it should fail! //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      new BasepullExtractStepValidator().validate(StreamedETLWithFrontendProcess.defineProcessMetaData(
         TestUtils.TABLE_NAME_SHAPE,
         TestUtils.TABLE_NAME_PERSON_MEMORY,
         ExtractViaQueryStep.class,
         StreamedETLWithFrontendProcessTest.TestTransformShapeToPersonStep.class,
         LoadViaInsertStep.class).withBasepullConfiguration(new BasepullConfiguration()), qInstance, validator);
      assertValidationFailureReasons(false, validator.getErrors(), "does not have a field with a default value that is a BasepullExtractStepInterface CodeReference");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testExtractViaBasepullQueryPasses()
   {
      QInstance          qInstance = QContext.getQInstance();
      QInstanceValidator validator = new QInstanceValidator();

      //////////////////////////////////////////////////////////////////////////////////////////////////
      // set up a streamed-etl process, with an ExtractViaBasepullQueryStep as expected - should pass //
      //////////////////////////////////////////////////////////////////////////////////////////////////
      new BasepullExtractStepValidator().validate(StreamedETLWithFrontendProcess.defineProcessMetaData(
         TestUtils.TABLE_NAME_SHAPE,
         TestUtils.TABLE_NAME_PERSON_MEMORY,
         ExtractViaBasepullQueryStep.class,
         StreamedETLWithFrontendProcessTest.TestTransformShapeToPersonStep.class,
         LoadViaInsertStep.class).withBasepullConfiguration(new BasepullConfiguration()), qInstance, validator);
      assertThat(validator.getErrors()).isNullOrEmpty();
   }

}