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

package com.kingsrook.qqq.backend.core.model.data;


import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Base class for enums that are interoperable with QRecords.
 *******************************************************************************/
public interface QRecordEnum
{
   Logger LOG = LogManager.getLogger(QRecordEnum.class);

   ListingHash<Class<? extends QRecordEnum>, QRecordEntityField> fieldMapping = new ListingHash<>();


   /*******************************************************************************
    ** Convert this entity to a QRecord.
    **
    *******************************************************************************/
   default QRecord toQRecord() throws QException
   {
      try
      {
         QRecord qRecord = new QRecord();

         List<QRecordEntityField> fieldList = getFieldList(this.getClass());
         for(QRecordEntityField qRecordEntityField : fieldList)
         {
            qRecord.setValue(qRecordEntityField.getFieldName(), (Serializable) qRecordEntityField.getGetter().invoke(this));
         }

         return (qRecord);
      }
      catch(Exception e)
      {
         throw (new QException("Error building qRecord from entity.", e));
      }
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   public static List<QRecordEntityField> getFieldList(Class<? extends QRecordEnum> c)
   {
      if(!fieldMapping.containsKey(c))
      {
         List<QRecordEntityField> fieldList = new ArrayList<>();
         for(Method possibleGetter : c.getMethods())
         {
            if(isGetter(possibleGetter))
            {
               String           fieldName       = getFieldNameFromGetter(possibleGetter);
               Optional<QField> fieldAnnotation = getQFieldAnnotation(c, fieldName);
               fieldList.add(new QRecordEntityField(fieldName, possibleGetter, null, possibleGetter.getReturnType(), fieldAnnotation.orElse(null)));
            }
         }
         fieldMapping.put(c, fieldList);
      }
      return (fieldMapping.get(c));
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   public static Optional<QField> getQFieldAnnotation(Class<? extends QRecordEnum> c, String fieldName)
   {
      try
      {
         Field field = c.getDeclaredField(fieldName);
         return (Optional.ofNullable(field.getAnnotation(QField.class)));
      }
      catch(NoSuchFieldException e)
      {
         //////////////////////////////////////////
         // ok, we just won't have an annotation //
         //////////////////////////////////////////
      }
      return (Optional.empty());
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   public static String getFieldNameFromGetter(Method getter)
   {
      String nameWithoutGet = getter.getName().replaceFirst("^get", "");
      if(nameWithoutGet.length() == 1)
      {
         return (nameWithoutGet.toLowerCase(Locale.ROOT));
      }
      return (nameWithoutGet.substring(0, 1).toLowerCase(Locale.ROOT) + nameWithoutGet.substring(1));
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean isGetter(Method method)
   {
      if(method.getParameterTypes().length == 0 && method.getName().matches("^get[A-Z].*"))
      {
         if(isSupportedFieldType(method.getReturnType()))
         {
            return (true);
         }
         else
         {
            if(!method.getName().equals("getClass") && !method.getName().equals("getDeclaringClass") && !method.getName().equals("getPossibleValueId"))
            {
               LOG.debug("Method [" + method.getName() + "] looks like a getter, but its return type, [" + method.getReturnType() + "], isn't supported.");
            }
         }
      }
      return (false);
   }


   /*******************************************************************************
    **
    *******************************************************************************/
   private static boolean isSupportedFieldType(Class<?> returnType)
   {
      // todo - more types!!
      return (returnType.equals(String.class)
         || returnType.equals(Integer.class)
         || returnType.equals(int.class)
         || returnType.equals(Boolean.class)
         || returnType.equals(boolean.class)
         || returnType.equals(BigDecimal.class)
         || returnType.equals(Instant.class)
         || returnType.equals(LocalDate.class)
         || returnType.equals(LocalTime.class));
      /////////////////////////////////////////////
      // note - this list has implications upon: //
      // - QFieldType.fromClass                  //
      // - QRecordEntityField.convertValueType   //
      /////////////////////////////////////////////
   }

}
