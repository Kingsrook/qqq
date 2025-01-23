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


import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QMetaDataObject;
import com.kingsrook.qqq.backend.core.model.metadata.TopLevelMetaDataInterface;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.Pair;


/*******************************************************************************
 ** class that loads a directory full of meta data files into meta data objects,
 ** and then sets all of them in a QInstance.
 *******************************************************************************/
public class MetaDataLoaderHelper
{
   private static final QLogger LOG = QLogger.getLogger(MetaDataLoaderHelper.class);



   /***************************************************************************
    *
    ***************************************************************************/
   public static void processAllMetaDataFilesInDirectory(QInstance qInstance, String path) throws QException
   {
      List<Pair<File, AbstractMetaDataLoader<?>>> loaders = new ArrayList<>();

      File directory = new File(path);
      processAllMetaDataFilesInDirectory(loaders, directory);

      // todo - some version of sorting the loaders by type or possibly a sort field within the files (or file names)

      for(Pair<File, AbstractMetaDataLoader<?>> pair : loaders)
      {
         File                      file   = pair.getA();
         AbstractMetaDataLoader<?> loader = pair.getB();
         try(FileInputStream fileInputStream = new FileInputStream(file))
         {
            QMetaDataObject qMetaDataObject = loader.fileToMetaDataObject(qInstance, fileInputStream, file.getName());

            if(CollectionUtils.nullSafeHasContents(loader.getProblems()))
            {
               loader.getProblems().forEach(System.out::println);
            }

            if(qMetaDataObject instanceof TopLevelMetaDataInterface topLevelMetaData)
            {
               topLevelMetaData.addSelfToInstance(qInstance);
            }
            else
            {
               LOG.warn("Received a non-topLevelMetaDataObject from file: " + file.getAbsolutePath());
            }
         }
         catch(Exception e)
         {
            LOG.error("Error processing file: " + file.getAbsolutePath(), e);
         }
      }
   }



   /***************************************************************************
    *
    ***************************************************************************/
   private static void processAllMetaDataFilesInDirectory(List<Pair<File, AbstractMetaDataLoader<?>>> loaders, File directory) throws QException
   {
      for(File file : Objects.requireNonNullElse(directory.listFiles(), new File[0]))
      {
         if(file.isDirectory())
         {
            processAllMetaDataFilesInDirectory(loaders, file);
         }
         else
         {
            try(FileInputStream fileInputStream = new FileInputStream(file))
            {
               AbstractMetaDataLoader<?> loader = new ClassDetectingMetaDataLoader().getLoaderForFile(fileInputStream, file.getName());
               loaders.add(Pair.of(file, loader));
            }
            catch(Exception e)
            {
               LOG.error("Error processing file: " + file.getAbsolutePath(), e);
            }
         }
      }
   }

}
