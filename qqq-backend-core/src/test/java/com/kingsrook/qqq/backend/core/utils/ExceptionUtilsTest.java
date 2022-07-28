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

package com.kingsrook.qqq.backend.core.utils;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;


/*******************************************************************************
 ** Unit test for ExceptionUtils
 **
 *******************************************************************************/
class ExceptionUtilsTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testFindClassInRootChain()
   {
      assertNull(ExceptionUtils.findClassInRootChain(null, QUserFacingException.class));

      QUserFacingException target = new QUserFacingException("target");
      assertSame(target, ExceptionUtils.findClassInRootChain(target, QUserFacingException.class));
      assertSame(target, ExceptionUtils.findClassInRootChain(new QException("decoy", target), QUserFacingException.class));
      assertNull(ExceptionUtils.findClassInRootChain(new QException("decoy", target), IllegalArgumentException.class));
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void testGetRootException()
   {
      assertNull(ExceptionUtils.getRootException(null));

      Exception root = new Exception("root");
      assertSame(root, ExceptionUtils.getRootException(root));

      Exception container = new Exception("container", root);
      assertSame(root, ExceptionUtils.getRootException(container));

      Exception middle = new Exception("middle", root);
      Exception top = new Exception("top", middle);
      assertSame(root, ExceptionUtils.getRootException(top));

      ////////////////////////////////////////////////////////////////////////////////////////
      // without the code that checks for loops, these next two checks cause infinite loops //
      ////////////////////////////////////////////////////////////////////////////////////////
      MyException selfCaused = new MyException("selfCaused");
      selfCaused.setCause(selfCaused);
      assertSame(selfCaused, ExceptionUtils.getRootException(selfCaused));

      MyException cycle1 = new MyException("cycle1");
      MyException cycle2 = new MyException("cycle2");
      cycle1.setCause(cycle2);
      cycle2.setCause(cycle1);
      assertSame(cycle1, ExceptionUtils.getRootException(cycle1));
      assertSame(cycle2, ExceptionUtils.getRootException(cycle2));
   }



   /*******************************************************************************
    ** Test exception class - lets you set the cause, easier to create a loop.
    *******************************************************************************/
   public class MyException extends Exception
   {
      private Throwable myCause = null;



      public MyException(String message)
      {
         super(message);
      }



      public MyException(Throwable cause)
      {
         super(cause);
      }



      public void setCause(Throwable cause)
      {
         myCause = cause;
      }



      @Override
      public synchronized Throwable getCause()
      {
         return (myCause);
      }
   }
}
