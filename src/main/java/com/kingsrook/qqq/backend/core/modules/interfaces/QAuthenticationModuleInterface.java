/*
 * Copyright © 2021-2021. Kingsrook LLC <contact@kingsrook.com>.  All Rights Reserved.
 */

package com.kingsrook.qqq.backend.core.modules.interfaces;


import java.util.Map;
import com.kingsrook.qqq.backend.core.model.session.QSession;


/*******************************************************************************
 ** Interface that a QAuthenticationModule must implement.
 **
 *******************************************************************************/
public interface QAuthenticationModuleInterface
{
   /*******************************************************************************
    **
    *******************************************************************************/
   QSession createSession(Map<String, String> context);


   /*******************************************************************************
    **
    *******************************************************************************/
   boolean isSessionValid(QSession session);
}
