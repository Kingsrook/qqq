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

package com.kingsrook.qqq.backend.core.actions.scripts;


import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.TestScriptInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.TestScriptOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.fields.QFieldType;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import com.kingsrook.qqq.backend.core.utils.TestUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for TestScriptActionInterface
 *******************************************************************************/
class TestScriptActionInterfaceTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test() throws QException
   {
      QInstance qInstance = TestUtils.defineInstance();

      TestScriptInput testScriptInput = new TestScriptInput(qInstance);
      testScriptInput.setSession(new QSession());
      testScriptInput.setInputValues(Map.of("name", "Darin", "age", 42));
      testScriptInput.setCodeReference(new QCodeReference(SampleScript.class, QCodeUsage.CUSTOMIZER));
      TestScriptOutput testScriptOutput = new TestScriptOutput();
      new SampleTestAction().execute(testScriptInput, testScriptOutput);

      assertNull(testScriptOutput.getException());
      assertTrue(testScriptOutput.getOutputObject() instanceof SampleTestOutput);
      SampleTestOutput sampleTestOutput = (SampleTestOutput) testScriptOutput.getOutputObject();
      assertEquals("Darin is 42 years old!", sampleTestOutput.getMessage());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class SampleScript implements Function<Map<String, Object>, Serializable>
   {
      @Override
      public Serializable apply(Map<String, Object> context)
      {
         SampleTestInput  input  = (SampleTestInput) context.get("input");
         SampleTestOutput output = (SampleTestOutput) context.get("output");
         output.setMessage(input.getName() + " is " + input.getAge() + " years old!");
         return (output);
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class SampleTestAction implements TestScriptActionInterface
   {
      @Override
      public void setupTestScriptInput(TestScriptInput testScriptInput, ExecuteCodeInput executeCodeInput)
      {
         SampleTestInput sampleTestInput = new SampleTestInput();
         sampleTestInput.setName(ValueUtils.getValueAsString(testScriptInput.getInputValues().get("name")));
         sampleTestInput.setAge(ValueUtils.getValueAsInteger(testScriptInput.getInputValues().get("age")));
         executeCodeInput.setInput(Map.of("input", sampleTestInput));

         executeCodeInput.getContext().put("output", new SampleTestOutput());
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public List<QFieldMetaData> getTestInputFields()
      {
         return List.of(
            new QFieldMetaData("name", QFieldType.STRING),
            new QFieldMetaData("age", QFieldType.INTEGER)
         );
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public List<QFieldMetaData> getTestOutputFields()
      {
         return List.of(new QFieldMetaData("message", QFieldType.STRING));
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class SampleTestInput implements Serializable
   {
      private String  name;
      private Integer age;



      /*******************************************************************************
       ** Getter for name
       **
       *******************************************************************************/
      public String getName()
      {
         return name;
      }



      /*******************************************************************************
       ** Setter for name
       **
       *******************************************************************************/
      public void setName(String name)
      {
         this.name = name;
      }



      /*******************************************************************************
       ** Getter for age
       **
       *******************************************************************************/
      public Integer getAge()
      {
         return age;
      }



      /*******************************************************************************
       ** Setter for age
       **
       *******************************************************************************/
      public void setAge(Integer age)
      {
         this.age = age;
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class SampleTestOutput implements Serializable
   {
      private String message;



      /*******************************************************************************
       ** Getter for message
       **
       *******************************************************************************/
      public String getMessage()
      {
         return message;
      }



      /*******************************************************************************
       ** Setter for message
       **
       *******************************************************************************/
      public void setMessage(String message)
      {
         this.message = message;
      }
   }

}