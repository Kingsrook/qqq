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

package com.kingsrook.qqq.languages.javascript;


import java.io.Serializable;
import com.kingsrook.qqq.backend.core.actions.scripts.ExecuteCodeAction;
import com.kingsrook.qqq.backend.core.exceptions.QCodeException;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeInput;
import com.kingsrook.qqq.backend.core.model.actions.scripts.ExecuteCodeOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeType;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.session.QSession;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for ExecuteCodeAction
 *******************************************************************************/
class ExecuteCodeActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testHelloWorld() throws QException
   {
      ExecuteCodeInput input = new ExecuteCodeInput(TestUtils.defineInstance())
         .withCodeReference(new QCodeReference("helloWorld.js", QCodeType.JAVA_SCRIPT, QCodeUsage.CUSTOMIZER)
            .withInlineCode("""
               return "Hello, " + input"""))
         .withContext("input", "World");
      ExecuteCodeOutput output = new ExecuteCodeOutput();
      new ExecuteCodeAction().run(input, output);
      assertEquals("Hello, World", output.getOutput());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSetInContextObject() throws QException
   {
      OneTestOutput oneTestOutput = testOne(3, """
         var a = 1;
         var b = 2;
         output.setD(a + b + input.getC());
         """);
      assertEquals(6, oneTestOutput.testOutput().getD());
      assertNull(oneTestOutput.executeCodeOutput().getOutput());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testReturnsContextObject() throws QException
   {
      OneTestOutput oneTestOutput = testOne(4, """
         var a = 1;
         var b = 2;
         output.setD(a + b + input.getC());
         return (output);
         """);
      assertEquals(7, oneTestOutput.testOutput().getD());
      assertTrue(oneTestOutput.executeCodeOutput().getOutput() instanceof TestOutput);
      assertEquals(7, ((TestOutput) oneTestOutput.executeCodeOutput().getOutput()).getD());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCompiledFromTypeScript() throws QException
   {
      OneTestOutput oneTestOutput = testOne(4, """
         var script = (function (exports) {
            function main() {
               output.setD(7);
               return (output);
            }
            exports.main = main;
            return exports;
         })({});
         """);
      assertEquals(7, oneTestOutput.testOutput().getD());
      assertTrue(oneTestOutput.executeCodeOutput().getOutput() instanceof TestOutput);
      assertEquals(7, ((TestOutput) oneTestOutput.executeCodeOutput().getOutput()).getD());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testCompiledFromTypeScriptThatThrows() throws QException
   {
      assertThatThrownBy(() ->
      {
         testOne(4, """
            var script = (function (exports) {
               function main() {
                  throw "inline script failure";
                  return (output);
               }
               exports.main = main;
               return exports;
            })({});
            """);
      }).hasMessageContaining("inline script failure");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testReturnsPrimitive() throws QException
   {
      OneTestOutput oneTestOutput = testOne(5, """
         var a = 1;
         var b = 2;
         output.setD(a + b + input.getC());
         return output.getD()
         """);
      assertEquals(8, oneTestOutput.testOutput().getD());
      assertEquals(8, oneTestOutput.executeCodeOutput().getOutput());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testThrows() throws QException
   {
      String code = """
         var a = 1;
         var b = 2;
         if (input.getC() === 6)
         {
            throw ("oh no, six!");
         }
         output.setD(a + b + input.getC());
         return output.getD()
         """;

      Assertions.assertThatThrownBy(() -> testOne(6, code))
         .isInstanceOf(QCodeException.class)
         .hasMessageContaining("threw")
         .hasMessageContaining("oh no, six!")
         .hasMessageContaining("line 5:");

      OneTestOutput oneTestOutput = testOne(7, code);
      assertEquals(10, oneTestOutput.testOutput().getD());
      assertEquals(10, oneTestOutput.executeCodeOutput().getOutput());

      Assertions.assertThatThrownBy(() -> testOne(6, """
            var a = null;
            return a.toString();
            """))
         .isInstanceOf(QCodeException.class)
         .hasMessageContaining("threw")
         .hasMessageContaining("TypeError: null has no such function \"toString\"");

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testSyntaxError() throws QException
   {
      Assertions.assertThatThrownBy(() -> testOne(6, """
            var a = 1;
            if (input.getC() === 6
            {
            """))
         .isInstanceOf(QCodeException.class)
         .hasMessageContaining("parser")
         .hasMessageContaining("line 3 column 0");

      Assertions.assertThatThrownBy(() -> testOne(6, """
            var a = 1;
            vr b = 2;
            """))
         .isInstanceOf(QCodeException.class)
         .hasMessageContaining("parser")
         .hasMessageContaining("line 2 column 3");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLogs() throws QException
   {
      OneTestOutput oneTestOutput = testOne(5, """
         logger.log("This is a log.");
         """);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private OneTestOutput testOne(Integer inputValueC, String code) throws QException
   {
      System.out.println();
      QInstance instance = TestUtils.defineInstance();

      TestInput testInput = new TestInput();
      testInput.setC(inputValueC);

      TestOutput testOutput = new TestOutput();

      ExecuteCodeInput input = new ExecuteCodeInput(instance);
      input.setSession(new QSession());
      input.setCodeReference(new QCodeReference("test.js", QCodeType.JAVA_SCRIPT, QCodeUsage.CUSTOMIZER).withInlineCode(code));
      input.withContext("input", testInput);
      input.withContext("output", testOutput);

      ExecuteCodeOutput output = new ExecuteCodeOutput();

      ExecuteCodeAction executeCodeAction = new ExecuteCodeAction();
      executeCodeAction.run(input, output);

      return (new OneTestOutput(output, testOutput));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private record OneTestOutput(ExecuteCodeOutput executeCodeOutput, TestOutput testOutput)
   {

   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class TestInput implements Serializable
   {
      private Integer c;



      /*******************************************************************************
       ** Getter for c
       **
       *******************************************************************************/
      public Integer getC()
      {
         return c;
      }



      /*******************************************************************************
       ** Setter for c
       **
       *******************************************************************************/
      public void setC(Integer c)
      {
         this.c = c;
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public String toString()
      {
         return "TestInput{c=" + c + '}';
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public static class TestOutput implements Serializable
   {
      private Integer d;



      /*******************************************************************************
       ** Getter for d
       **
       *******************************************************************************/
      public Integer getD()
      {
         return d;
      }



      /*******************************************************************************
       ** Setter for d
       **
       *******************************************************************************/
      public void setD(Integer d)
      {
         this.d = d;
      }



      /*******************************************************************************
       **
       *******************************************************************************/
      @Override
      public String toString()
      {
         return "TestOutput{d=" + d + '}';
      }
   }

}