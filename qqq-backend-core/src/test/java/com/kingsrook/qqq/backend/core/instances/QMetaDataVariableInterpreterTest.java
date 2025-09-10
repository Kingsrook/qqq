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

package com.kingsrook.qqq.backend.core.instances;


import java.math.BigDecimal;
import java.util.Map;
import com.kingsrook.qqq.backend.core.BaseTest;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


/*******************************************************************************
 ** Unit test for QSecretReader
 *******************************************************************************/
class QMetaDataVariableInterpreterTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @BeforeEach
   public void beforeEach()
   {
      System.setProperty("username", "joe");
      System.setProperty("password", "b1d3n");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @AfterEach
   public void afterEach()
   {
      System.clearProperty("username");
      System.clearProperty("password");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInterpretObject() throws QException
   {
      GoodTestClass goodTestClass = new GoodTestClass();
      goodTestClass.setUsername("${prop.username}");
      goodTestClass.setPassword("${prop.password}");

      new QMetaDataVariableInterpreter().interpretObject(goodTestClass);

      assertEquals("joe", goodTestClass.getUsername());
      assertEquals("b1d3n", goodTestClass.getPassword());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testBadAnnotatedObjects()
   {
      assertThrows(QException.class, () -> new QMetaDataVariableInterpreter().interpretObject(new BadTestClassAnnotatedInteger()));
      assertThrows(QException.class, () -> new QMetaDataVariableInterpreter().interpretObject(new BadTestClassNoGetter()));
      assertThrows(QException.class, () -> new QMetaDataVariableInterpreter().interpretObject(new BadTestClassNoSetter()));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInterpretFromEnvironment()
   {
      QMetaDataVariableInterpreter secretReader = new QMetaDataVariableInterpreter();
      String                       key          = "CUSTOM_PROPERTY";
      String                       value        = "ABCD-9876";
      secretReader.setEnvironmentOverrides(Map.of(key, value));

      assertNull(secretReader.interpret(null));
      assertEquals("foo", secretReader.interpret("foo"));
      assertNull(secretReader.interpret("${env.NOT-" + key + "}"));
      assertEquals(value, secretReader.interpret("${env." + key + "}"));
      assertEquals("${env.NOT-" + key, secretReader.interpret("${env.NOT-" + key));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testDotEnvFile()
   {
      QMetaDataVariableInterpreter secretReader = new QMetaDataVariableInterpreter();
      String                       key          = "CUSTOM_PROPERTY";
      String                       value        = "ABCD-9876";
      assertNull(secretReader.interpret("${env.NOT-" + key + "}"));
      assertEquals(value, secretReader.interpret("${env." + key + "}"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInterpretFromProperties()
   {
      QMetaDataVariableInterpreter secretReader = new QMetaDataVariableInterpreter();
      String                       key          = "MY_PROPERTY";
      String                       value        = "WXYZ-6789";
      System.setProperty(key, value);

      assertNull(secretReader.interpret(null));
      assertEquals("foo", secretReader.interpret("foo"));
      assertNull(secretReader.interpret("${prop.NOT-" + key + "}"));
      assertEquals(value, secretReader.interpret("${prop." + key + "}"));
      assertEquals("${prop.NOT-" + key, secretReader.interpret("${prop.NOT-" + key));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testInterpretLiterals()
   {
      QMetaDataVariableInterpreter secretReader = new QMetaDataVariableInterpreter();
      assertEquals("${env.X}", secretReader.interpret("${literal.${env.X}}"));
      assertEquals("${prop.X}", secretReader.interpret("${literal.${prop.X}}"));
      assertEquals("${literal.X}", secretReader.interpret("${literal.${literal.X}}"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testValueMaps()
   {
      QMetaDataVariableInterpreter variableInterpreter = new QMetaDataVariableInterpreter();
      variableInterpreter.addValueMap("input", Map.of("foo", "bar", "amount", new BigDecimal("3.50")));

      assertEquals("bar", variableInterpreter.interpretForObject("${input.foo}"));
      assertEquals(new BigDecimal("3.50"), variableInterpreter.interpretForObject("${input.amount}"));
      assertNull(variableInterpreter.interpretForObject("${input.x}"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testMultipleValueMapsNullish()
   {
      QMetaDataVariableInterpreter variableInterpreter = new QMetaDataVariableInterpreter();
      variableInterpreter.addValueMap("input", Map.of("amount", new BigDecimal("3.50"), "x", "y"));
      variableInterpreter.addValueMap("others", Map.of("foo", "fu", "amount", new BigDecimal("1.75")));

      assertNull(variableInterpreter.interpretForObject("${input.foo}"));
      assertEquals("fu", variableInterpreter.interpretForObject("${input.foo}??${others.foo}"));
      assertEquals("fu", variableInterpreter.interpretForObject("${others.foo}??${input.foo}"));
      assertEquals("fu", variableInterpreter.interpretForObject("${output.foo}??${others.foo}"));
      assertNull(variableInterpreter.interpretForObject("${input.bar}??${others.bar}"));
      assertNull(variableInterpreter.interpretForObject("${output.bar}??${smothers.bar}"));
      assertNull(variableInterpreter.interpretForObject("${output.bar}??${smothers.bar}"));
   }



   /*******************************************************************************
    ** Global coalescing: ${env.X}??${prop.x}??${input.x} (env wins)
    *******************************************************************************/
   @Test
   void testGlobalCoalescing_envPropInput_envWins()
   {
      QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();
      interpreter.setEnvironmentOverrides(Map.of("MY_VAR", "ENVV"));
      System.setProperty("my.var", "PROP");
      interpreter.addValueMap("input", Map.of("myVar", "INPUT"));

      assertEquals("ENVV", interpreter.interpretForObject("${env.MY_VAR}??${prop.my.var}??${input.myVar}"));
      assertEquals("PROP", interpreter.interpretForObject("${prop.my.var}??${input.myVar}"));
      assertEquals("INPUT", interpreter.interpretForObject("${input.myVar}"));
   }



   /*******************************************************************************
    ** Global coalescing: ${env.X}??${prop.x}??${input.x} (prop wins when env missing)
    *******************************************************************************/
   @Test
   void testGlobalCoalescing_envPropInput_propWinsWhenEnvMissing()
   {
      QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();
      // no env override for MY_OTHER
      System.setProperty("my.other", "PROP_ONLY");
      interpreter.addValueMap("input", Map.of("myOther", "INPUT_ONLY"));

      assertEquals("PROP_ONLY", interpreter.interpretForObject("${env.MY_OTHER}??${prop.my.other}??${input.myOther}"));
      assertEquals("INPUT_ONLY", interpreter.interpretForObject("${env.MY_OTHER}??${input.myOther}"));
      assertNull(interpreter.interpretForObject("${env.MY_OTHER}??${prop.NOT_SET}??${input.notSet}"));
   }



   /*******************************************************************************
    ** Global coalescing respects "looks-like-variable" rule: unknown prefix returns literal, which is skipped
    *******************************************************************************/
   @Test
   void testGlobalCoalescing_skipsUnknownPrefixLiteral()
   {
      QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();
      interpreter.addValueMap("others", Map.of("foo", "fu"));

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // ${output.foo} is not a known value map; interpretForObject returns the literal string "${output.foo}". //
      // Global coalescer skips it (since it's exactly the same token), and falls back to the next resolvable.  //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////
      assertEquals("fu", interpreter.interpretForObject("${output.foo}??${others.foo}"));
   }



   /*******************************************************************************
    ** Global coalescing skips empty/blank strings and uses next value
    *******************************************************************************/
   @Test
   void testGlobalCoalescing_skipsEmptyAndBlankStrings()
   {
      QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();
      interpreter.setEnvironmentOverrides(Map.of("EMPTY_ENV", ""));
      System.setProperty("blank.prop", "   ");
      interpreter.addValueMap("input", Map.of("nonEmpty", "ok"));

      ///////////////////////////////////////////////////////////////////////////////
      // empty env -> skipped; blank prop -> skipped; falls to non-empty value map //
      ///////////////////////////////////////////////////////////////////////////////
      assertEquals("ok", interpreter.interpretForObject("${env.EMPTY_ENV}??${prop.blank.prop}??${input.nonEmpty}"));
   }



   /*******************************************************************************
    ** Global coalescing selects first non-null non-string value (e.g., BigDecimal)
    *******************************************************************************/
   @Test
   void testGlobalCoalescing_nonStringTypes()
   {
      QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();
      interpreter.addValueMap("input", Map.of("amount", new BigDecimal("3.50")));

      ///////////////////////////////////////////////////////////////
      // prop/env not set -> resolves to BigDecimal from value map //
      ///////////////////////////////////////////////////////////////
      assertEquals(new BigDecimal("3.50"), interpreter.interpretForObject("${prop.amount}??${env.AMOUNT}??${input.amount}"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testMultipleValueMaps()
   {
      QMetaDataVariableInterpreter variableInterpreter = new QMetaDataVariableInterpreter();
      variableInterpreter.addValueMap("input", Map.of("amount", new BigDecimal("3.50"), "x", "y"));
      variableInterpreter.addValueMap("others", Map.of("foo", "fu", "amount", new BigDecimal("1.75")));

      assertNull(variableInterpreter.interpretForObject("${input.foo}"));
      assertEquals("fu", variableInterpreter.interpretForObject("${others.foo}"));
      assertEquals(new BigDecimal("3.50"), variableInterpreter.interpretForObject("${input.amount}"));
      assertEquals(new BigDecimal("1.75"), variableInterpreter.interpretForObject("${others.amount}"));
      assertEquals("y", variableInterpreter.interpretForObject("${input.x}"));
      assertNull(variableInterpreter.interpretForObject("${others.x}"));
      assertNull(variableInterpreter.interpretForObject("${input.nil}"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testLooksLikeVariableButNotFound()
   {
      QMetaDataVariableInterpreter variableInterpreter = new QMetaDataVariableInterpreter();
      variableInterpreter.addValueMap("input", Map.of("x", 1, "y", 2));
      variableInterpreter.addValueMap("others", Map.of("foo", "bar"));

      assertNull(variableInterpreter.interpretForObject("${input.notFound}", null));
      assertEquals(0, variableInterpreter.interpretForObject("${input.notFound}", 0));
      assertEquals("--", variableInterpreter.interpretForObject("${input.notFound}", "--"));
      assertEquals("--", variableInterpreter.interpretForObject("${others.notFound}", "--"));

      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // this one doesn't count as "looking like a variable" - because the "prefix" (notValid) isn't a value map... //
      ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      assertEquals("${notValid.notFound}", variableInterpreter.interpretForObject("${notValid.notFound}", "--"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetStringFromPropertyOrEnvironment()
   {
      QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();

      //////////////////////////////////////////////////////////
      // if neither prop nor env is set, get back the default //
      //////////////////////////////////////////////////////////
      assertEquals("default", interpreter.getStringFromPropertyOrEnvironment("notSet", "NOT_SET", "default"));

      /////////////////////////////////
      // if only prop is set, get it //
      /////////////////////////////////
      assertEquals("default", interpreter.getStringFromPropertyOrEnvironment("foo.value", "FOO_VALUE", "default"));
      System.setProperty("foo.value", "fooPropertyValue");
      assertEquals("fooPropertyValue", interpreter.getStringFromPropertyOrEnvironment("foo.value", "FOO_VALUE", "default"));

      ////////////////////////////////
      // if only env is set, get it //
      ////////////////////////////////
      assertEquals("default", interpreter.getStringFromPropertyOrEnvironment("bar.value", "BAR_VALUE", "default"));
      interpreter.setEnvironmentOverrides(Map.of("BAR_VALUE", "barEnvValue"));
      assertEquals("barEnvValue", interpreter.getStringFromPropertyOrEnvironment("bar.value", "BAR_VALUE", "default"));

      ///////////////////////////////////
      // if both are set, get the prop //
      ///////////////////////////////////
      System.setProperty("baz.value", "bazPropertyValue");
      interpreter.setEnvironmentOverrides(Map.of("BAZ_VALUE", "bazEnvValue"));
      assertEquals("bazPropertyValue", interpreter.getStringFromPropertyOrEnvironment("baz.value", "BAZ_VALUE", "default"));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetBooleanFromPropertyOrEnvironment()
   {
      QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();

      //////////////////////////////////////////////////////////
      // if neither prop nor env is set, get back the default //
      //////////////////////////////////////////////////////////
      assertFalse(interpreter.getBooleanFromPropertyOrEnvironment("notSet", "NOT_SET", false));
      assertTrue(interpreter.getBooleanFromPropertyOrEnvironment("notSet", "NOT_SET", true));

      /////////////////////////////////////////////
      // unrecognized values are same as not set //
      /////////////////////////////////////////////
      System.setProperty("unrecognized", "asdf");
      interpreter.setEnvironmentOverrides(Map.of("UNRECOGNIZED", "1234"));
      assertFalse(interpreter.getBooleanFromPropertyOrEnvironment("unrecognized", "UNRECOGNIZED", false));
      assertTrue(interpreter.getBooleanFromPropertyOrEnvironment("unrecognized", "UNRECOGNIZED", true));

      /////////////////////////////////
      // if only prop is set, get it //
      /////////////////////////////////
      assertFalse(interpreter.getBooleanFromPropertyOrEnvironment("foo.enabled", "FOO_ENABLED", false));
      System.setProperty("foo.enabled", "true");
      assertTrue(interpreter.getBooleanFromPropertyOrEnvironment("foo.enabled", "FOO_ENABLED", false));

      ////////////////////////////////
      // if only env is set, get it //
      ////////////////////////////////
      assertFalse(interpreter.getBooleanFromPropertyOrEnvironment("bar.enabled", "BAR_ENABLED", false));
      interpreter.setEnvironmentOverrides(Map.of("BAR_ENABLED", "true"));
      assertTrue(interpreter.getBooleanFromPropertyOrEnvironment("bar.enabled", "BAR_ENABLED", false));

      ///////////////////////////////////
      // if both are set, get the prop //
      ///////////////////////////////////
      System.setProperty("baz.enabled", "true");
      interpreter.setEnvironmentOverrides(Map.of("BAZ_ENABLED", "false"));
      assertTrue(interpreter.getBooleanFromPropertyOrEnvironment("baz.enabled", "BAZ_ENABLED", true));
      assertTrue(interpreter.getBooleanFromPropertyOrEnvironment("baz.enabled", "BAZ_ENABLED", false));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetIntegerFromPropertyOrEnvironment()
   {
      QMetaDataVariableInterpreter interpreter = new QMetaDataVariableInterpreter();

      //////////////////////////////////////////////////////////
      // if neither prop nor env is set, get back the default //
      //////////////////////////////////////////////////////////
      assertEquals(1, interpreter.getIntegerFromPropertyOrEnvironment("notSet", "NOT_SET", 1));
      assertEquals(2, interpreter.getIntegerFromPropertyOrEnvironment("notSet", "NOT_SET", 2));

      /////////////////////////////////////////////
      // unrecognized values are same as not set //
      /////////////////////////////////////////////
      System.setProperty("unrecognized", "asdf");
      interpreter.setEnvironmentOverrides(Map.of("UNRECOGNIZED", "qwerty"));
      assertEquals(3, interpreter.getIntegerFromPropertyOrEnvironment("unrecognized", "UNRECOGNIZED", 3));
      assertEquals(4, interpreter.getIntegerFromPropertyOrEnvironment("unrecognized", "UNRECOGNIZED", 4));

      /////////////////////////////////
      // if only prop is set, get it //
      /////////////////////////////////
      assertEquals(5, interpreter.getIntegerFromPropertyOrEnvironment("foo.size", "FOO_SIZE", 5));
      System.setProperty("foo.size", "6");
      assertEquals(6, interpreter.getIntegerFromPropertyOrEnvironment("foo.size", "FOO_SIZE", 7));

      ////////////////////////////////
      // if only env is set, get it //
      ////////////////////////////////
      assertEquals(8, interpreter.getIntegerFromPropertyOrEnvironment("bar.size", "BAR_SIZE", 8));
      interpreter.setEnvironmentOverrides(Map.of("BAR_SIZE", "9"));
      assertEquals(9, interpreter.getIntegerFromPropertyOrEnvironment("bar.size", "BAR_SIZE", 10));

      ///////////////////////////////////
      // if both are set, get the prop //
      ///////////////////////////////////
      System.setProperty("baz.size", "11");
      interpreter.setEnvironmentOverrides(Map.of("BAZ_SIZE", "12"));
      assertEquals(11, interpreter.getIntegerFromPropertyOrEnvironment("baz.size", "BAZ_SIZE", 13));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @InterpretableFields(fieldNames = { "username", "password" })
   public static class GoodTestClass
   {
      private String username;
      private String password;



      /*******************************************************************************
       ** Getter for username
       **
       *******************************************************************************/
      public String getUsername()
      {
         return username;
      }



      /*******************************************************************************
       ** Setter for username
       **
       *******************************************************************************/
      public void setUsername(String username)
      {
         this.username = username;
      }



      /*******************************************************************************
       ** Getter for password
       **
       *******************************************************************************/
      public String getPassword()
      {
         return password;
      }



      /*******************************************************************************
       ** Setter for password
       **
       *******************************************************************************/
      public void setPassword(String password)
      {
         this.password = password;
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @InterpretableFields(fieldNames = { "port" })
   public static class BadTestClassAnnotatedInteger
   {
      private Integer port;



      /*******************************************************************************
       ** Getter for port
       **
       *******************************************************************************/
      public Integer getPort()
      {
         return port;
      }



      /*******************************************************************************
       ** Setter for port
       **
       *******************************************************************************/
      public void setPort(Integer port)
      {
         this.port = port;
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @InterpretableFields(fieldNames = { "foo" })
   public static class BadTestClassNoGetter
   {
      private String foo;



      /*******************************************************************************
       ** Setter for foo
       **
       *******************************************************************************/
      public void setFoo(String foo)
      {
         this.foo = foo;
      }
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @InterpretableFields(fieldNames = { "foo" })
   public static class BadTestClassNoSetter
   {
      private String foo;



      /*******************************************************************************
       ** Getter for foo
       **
       *******************************************************************************/
      public String getFoo()
      {
         return foo;
      }
   }

}
