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

package com.kingsrook.qqq.backend.core.instances.loaders;


import java.io.InputStream;
import java.util.Map;
import com.kingsrook.qqq.backend.core.instances.loaders.implementations.QTableMetaDataLoader;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QMetaDataObject;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;


/*******************************************************************************
 ** Generic implementation of AbstractMetaDataLoader, who "detects" the class
 ** of meta data object to be created, then defers to an appropriate subclass
 ** to do the work.
 *******************************************************************************/
public class ClassDetectingMetaDataLoader extends AbstractMetaDataLoader<QMetaDataObject>
{

   /***************************************************************************
    *
    ***************************************************************************/
   public AbstractMetaDataLoader<?> getLoaderForFile(InputStream inputStream, String fileName) throws QMetaDataLoaderException
   {
      Map<String, Object> map = fileToMap(inputStream, fileName);
      return (getLoaderForMap(map));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   public AbstractMetaDataLoader<?> getLoaderForMap(Map<String, Object> map) throws QMetaDataLoaderException
   {
      if(map.containsKey("class"))
      {
         String classProperty = ValueUtils.getValueAsString(map.get("class"));
         AbstractMetaDataLoader<?> loader = switch(classProperty)
         {
            case "QTableMetaData" -> new QTableMetaDataLoader();
            // todo!! case "QTableMetaData" -> new QTableMetaDataLoader();
            default -> throw new QMetaDataLoaderException("Unexpected class [" + classProperty + "] specified in " + getFileName());
         };

         return (loader);
      }

      throw new QMetaDataLoaderException("Cannot detect meta-data type, because [class] attribute was not specified in file: " + getFileName());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public QMetaDataObject mapToMetaDataObject(QInstance qInstance, Map<String, Object> map) throws QMetaDataLoaderException
   {
      AbstractMetaDataLoader<?> loaderForMap = getLoaderForMap(map);
      return loaderForMap.mapToMetaDataObject(qInstance, map);
   }
}
