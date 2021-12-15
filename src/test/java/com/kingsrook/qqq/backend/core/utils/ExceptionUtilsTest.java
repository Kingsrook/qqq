/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.utils;


import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QUserFacingException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


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
   void findClassInRootChain()
   {
      assertNull(ExceptionUtils.findClassInRootChain(null, QUserFacingException.class));
      QUserFacingException target = new QUserFacingException("target");

      assertSame(target, ExceptionUtils.findClassInRootChain(target, QUserFacingException.class));
      assertSame(target, ExceptionUtils.findClassInRootChain(new QException("decoy", target), QUserFacingException.class));
      assertNull(ExceptionUtils.findClassInRootChain(new QException("decoy", target), IllegalArgumentException.class));
   }
}
