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

package com.kingsrook.qqq.backend.core.model.session;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.security.QSecurityKeyType;
import com.kingsrook.qqq.backend.core.utils.StringUtils;


/*******************************************************************************
 ** Special session, indicating that an action being executed is being done not
 ** on behalf of a (human or otherwise) user - but instead, is the application/
 ** system itself.
 **
 ** Generally this means, escalated privileges - e.g., permission to all tables,
 ** processes etc, and all security keys (e.g., all-access keys).
 *******************************************************************************/
public class QSystemUserSession extends QSession
{
   private static final QLogger LOG = QLogger.getLogger(QSystemUserSession.class);

   private static List<String> allAccessKeyNames = null;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public QSystemUserSession()
   {
      super();

      ////////////////////////////////////////////////////////
      // always give system user all of the all-access keys //
      ////////////////////////////////////////////////////////
      for(String allAccessKeyName : getAllAccessKeyNames())
      {
         withSecurityKeyValue(allAccessKeyName, true);
      }
   }



   /*******************************************************************************
    ** System User Sessions should always have permission to all the things.
    *******************************************************************************/
   @Override
   public boolean hasPermission(String permissionName)
   {
      return (true);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private List<String> getAllAccessKeyNames()
   {
      if(allAccessKeyNames == null)
      {
         QInstance qInstance = QContext.getQInstance();
         if(qInstance == null)
         {
            LOG.warn("QInstance was not set in context when creating a QSystemUserSession and trying to prime allAccessKeyNames... This SystemUserSession will NOT have any allAccessKeys.");
            return (Collections.emptyList());
         }

         ///////////////////////////////////////////////////////////////////////////////////////
         // ideally only 1 thread would do this, but, it's cheap, so don't bother locking.    //
         // and, if multiple get in, only the last one will assign to the field, so, s/b fine //
         ///////////////////////////////////////////////////////////////////////////////////////
         List<String> list = new ArrayList<>();
         for(QSecurityKeyType securityKeyType : qInstance.getSecurityKeyTypes().values())
         {
            if(StringUtils.hasContent(securityKeyType.getAllAccessKeyName()))
            {
               list.add(securityKeyType.getAllAccessKeyName());
            }
         }

         LOG.info("Initialized allAccessKeyNames for SystemUserSessions as: " + list);
         allAccessKeyNames = list;
      }

      return (allAccessKeyNames);
   }



   /*******************************************************************************
    ** Meant for use in tests - to explicitly null-out the allAccessKeyNames field.
    *******************************************************************************/
   static void unsetAllAccessKeyNames()
   {
      allAccessKeyNames = null;
   }

}
