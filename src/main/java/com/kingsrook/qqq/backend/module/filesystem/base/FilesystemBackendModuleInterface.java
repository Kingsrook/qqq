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

package com.kingsrook.qqq.backend.module.filesystem.base;


import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.QTableMetaData;
import com.kingsrook.qqq.backend.module.filesystem.exceptions.FilesystemException;


/*******************************************************************************
 ** Interface to add additional functionality commonly among the various filesystem
 ** module implementations.
 *******************************************************************************/
public interface FilesystemBackendModuleInterface
{
   /*******************************************************************************
    ** In contrast with the DeleteAction, which deletes RECORDS - this is a
    ** filesystem-(or s3, sftp, etc)-specific extension to delete an entire FILE
    ** e.g., for post-ETL.
    **
    ** @throws FilesystemException if the delete is known to have failed, and the file is thought to still exit
    *******************************************************************************/
   void deleteFile(QInstance instance, QTableMetaData table, String fileReference) throws FilesystemException;

   /*******************************************************************************
    ** Move a file from a source path, to a destination path.
    **
    ** @throws FilesystemException if the move is known to have failed
    *******************************************************************************/
   void moveFile(QInstance instance, QTableMetaData table, String source, String destination) throws FilesystemException;
}
