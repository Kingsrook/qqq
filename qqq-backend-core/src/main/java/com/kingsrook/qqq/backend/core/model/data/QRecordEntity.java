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
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.utils.ListingHash;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/*******************************************************************************
 ** Base class for entity beans that are interoperable with QRecords.
 *******************************************************************************/
public abstract class QRecordEntity
{
   private static final Logger LOG = LogManager.getLogger(QRecordEntity.class);

   private static final ListingHash<Class<? extends QRecordEntity>, QRecordEntityField> fieldMapping = new ListingHash<>();



   /*******************************************************************************
    ** Build an entity of this QRecord type from a QRecord
    **
    *******************************************************************************/
   public static <T extends QRecordEntity> T fromQRecord(Class<T> c, QRecord qRecord) throws QException
   {
      try
      {
         T entity = c.getConstructor().newInstance();

         List<QRecordEntityField> fieldList = getFieldList(c);
         for(QRecordEntityField qRecordEntityField : fieldList)
         {
            Serializable value      = qRecord.getValue(qRecordEntityField.getFieldName());
            Object       typedValue = qRecordEntityField.convertValueType(value);
            qRecordEntityField.getSetter().invoke(entity, typedValue);
         }

         return (entity);
      }
      catch(Exception e)
      {
         throw (new QException("Error building entity from qRecord.", e));
      }
   }



   /*******************************************************************************
    ** Convert this entity to a QRecord.
    **
    *******************************************************************************/
   public QRecord toQRecord() throws QException
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
   private static List<QRecordEntityField> getFieldList(Class<? extends QRecordEntity> c)
   {
      if(!fieldMapping.containsKey(c))
      {
         List<QRecordEntityField> fieldList = new ArrayList<>();
         for(Method possibleGetter : c.getMethods())
         {
            if(isGetter(possibleGetter))
            {
               Optional<Method> setter = getSetterForGetter(c, possibleGetter);
               if(setter.isPresent())
               {
                  String name = getFieldNameFromGetter(possibleGetter);
                  fieldList.add(new QRecordEntityField(name, possibleGetter, setter.get(), possibleGetter.getReturnType()));
               }
               else
               {
                  LOG.info("Getter method [" + possibleGetter.getName() + "] does not have a corresponding setter.");
               }
            }
         }
         fieldMapping.put(c, fieldList);
      }
      return (fieldMapping.get(c));
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
            if(!method.getName().equals("getClass"))
            {
               LOG.info("Method [" + method.getName() + "] looks like a getter, but its return type, [" + method.getReturnType() + "], isn't supported.");
            }
         }
      }
      return (false);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private static Optional<Method> getSetterForGetter(Class<? extends QRecordEntity> c, Method getter)
   {
      String setterName = getter.getName().replaceFirst("^get", "set");
      for(Method method : c.getMethods())
      {
         if(method.getName().equals(setterName))
         {
            if(method.getParameterTypes().length == 1 && method.getParameterTypes()[0].equals(getter.getReturnType()))
            {
               return (Optional.of(method));
            }
            else
            {
               LOG.info("Method [" + method.getName() + "] looks like a setter for [" + getter.getName() + "], but its parameters, [" + Arrays.toString(method.getParameterTypes()) + "], don't match the getter's return type [" + getter.getReturnType() + "]");
            }
         }
      }
      return (Optional.empty());
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
         || returnType.equals(BigDecimal.class));
   }

}
