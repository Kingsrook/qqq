/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2025.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.core.instances.loaders;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.kingsrook.qqq.backend.core.instances.loaders.implementations.QTableMetaDataLoader;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.utils.ClassPathUtils;


/*******************************************************************************
 **
 *******************************************************************************/
public class MetaDataLoaderRegistry
{
   private static final QLogger LOG = QLogger.getLogger(AbstractMetaDataLoader.class);

   private static final Map<Class<?>, Class<? extends AbstractMetaDataLoader<?>>> registeredLoaders                   = new HashMap<>();
   private static final Map<String, Class<? extends AbstractMetaDataLoader<?>>>   registeredLoadersByTargetSimpleName = new HashMap<>();

   static
   {
      try
      {
         List<Class<?>> classesInPackage = ClassPathUtils.getClassesInPackage(QTableMetaDataLoader.class.getPackageName());
         for(Class<?> possibleLoaderClass : classesInPackage)
         {
            try
            {
               Type superClass = possibleLoaderClass.getGenericSuperclass();
               if(superClass.getTypeName().startsWith(AbstractMetaDataLoader.class.getName() + "<"))
               {
                  Type actualTypeArgument = ((ParameterizedType) superClass).getActualTypeArguments()[0];
                  if(actualTypeArgument instanceof Class)
                  {
                     //noinspection unchecked
                     Class<? extends AbstractMetaDataLoader<?>> loaderClass = (Class<? extends AbstractMetaDataLoader<?>>) possibleLoaderClass;

                     Class<?> metaDataObjectType = Class.forName(actualTypeArgument.getTypeName());
                     registeredLoaders.put(metaDataObjectType, loaderClass);
                     registeredLoadersByTargetSimpleName.put(metaDataObjectType.getSimpleName(), loaderClass);
                  }
               }
            }
            catch(Exception e)
            {
               LOG.info("Error on class: " + possibleLoaderClass, e);
            }
         }

         System.out.println("Registered loaders: " + registeredLoadersByTargetSimpleName);
      }
      catch(Exception e)
      {
         LOG.error("Error in static init block for MetaDataLoaderRegistry", e);
      }
   }

   /***************************************************************************
    **
    ***************************************************************************/
   public static boolean hasLoaderForClass(Class<?> metaDataClass)
   {
      return registeredLoaders.containsKey(metaDataClass);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static Class<? extends AbstractMetaDataLoader<?>> getLoaderForClass(Class<?> metaDataClass)
   {
      return registeredLoaders.get(metaDataClass);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static boolean hasLoaderForSimpleName(String targetSimpleName)
   {
      return registeredLoadersByTargetSimpleName.containsKey(targetSimpleName);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   public static Class<? extends AbstractMetaDataLoader<?>> getLoaderForSimpleName(String targetSimpleName)
   {
      return registeredLoadersByTargetSimpleName.get(targetSimpleName);
   }
}
