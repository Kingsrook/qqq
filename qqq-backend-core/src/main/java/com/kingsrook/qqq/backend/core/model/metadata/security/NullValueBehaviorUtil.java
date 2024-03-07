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

package com.kingsrook.qqq.backend.core.model.metadata.security;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.security.RecordSecurityLock.NullValueBehavior;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Utility for working with security key, nullValueBehaviors.
 *******************************************************************************/
public class NullValueBehaviorUtil
{
   private static final QLogger LOG = QLogger.getLogger(NullValueBehaviorUtil.class);



   /*******************************************************************************
    ** Look at a RecordSecurityLock, but also the active session - and if the session
    ** has a null-value-behavior key for the lock's key-type, then allow that behavior
    ** to override the lock's default.
    *******************************************************************************/
   public static NullValueBehavior getEffectiveNullValueBehavior(RecordSecurityLock recordSecurityLock)
   {
      QSecurityKeyType securityKeyType = QContext.getQInstance().getSecurityKeyType(recordSecurityLock.getSecurityKeyType());
      if(StringUtils.hasContent(securityKeyType.getNullValueBehaviorKeyName()))
      {
         List<Serializable> nullValueSessionValueList = QContext.getQSession().getSecurityKeyValues(securityKeyType.getNullValueBehaviorKeyName());
         if(CollectionUtils.nullSafeHasContents(nullValueSessionValueList))
         {
            NullValueBehavior nullValueBehavior = NullValueBehavior.tryToGetFromString(ValueUtils.getValueAsString(nullValueSessionValueList.get(0)));
            if(nullValueBehavior != null)
            {
               return nullValueBehavior;
            }
            else
            {
               LOG.info("Unexpected value in nullValueBehavior security key.  Will use recordSecurityLock's nullValueBehavior",
                  logPair("nullValueBehaviorKeyName", securityKeyType.getNullValueBehaviorKeyName()),
                  logPair("value", nullValueSessionValueList.get(0)));
            }
         }
      }

      return (recordSecurityLock.getNullValueBehavior());
   }

}
