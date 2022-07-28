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


import java.util.Map;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;


/*******************************************************************************
 ** Unit test for QSecretReader
 *******************************************************************************/
class QMetaDataVariableInterpreterTest
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
      secretReader.setCustomEnvironment(Map.of(key, value));

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