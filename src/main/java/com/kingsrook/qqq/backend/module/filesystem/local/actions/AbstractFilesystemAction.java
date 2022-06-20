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

package com.kingsrook.qqq.backend.module.filesystem.local.actions;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.module.filesystem.base.actions.AbstractBaseFilesystemAction;


/*******************************************************************************
 ** Base class for all (local) Filesystem actions
 *******************************************************************************/
public class AbstractFilesystemAction extends AbstractBaseFilesystemAction<File>
{

   /*******************************************************************************
    ** List the files for this table.
    *******************************************************************************/
   @Override
   public List<File> listFiles(QTableMetaData table, QBackendMetaData backendBase)
   {
      String fullPath  = getFullPath(table, backendBase);
      File   directory = new File(fullPath);
      return Arrays.asList(directory.listFiles());
   }



   /*******************************************************************************
    ** Read the contents of a file.
    *******************************************************************************/
   @Override
   public InputStream readFile(File file) throws IOException
   {
      return (new FileInputStream(file));
   }

}
