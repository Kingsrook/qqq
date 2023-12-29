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
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QCriteriaOperator;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QFilterCriteria;
import com.kingsrook.qqq.backend.core.model.actions.tables.query.QQueryFilter;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.utils.CollectionUtils;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.core.utils.ValueUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.actions.AbstractBaseFilesystemAction;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.AbstractFilesystemTableBackendDetails;
import com.kingsrook.qqq.backend.module.filesystem.base.model.metadata.Cardinality;
import com.kingsrook.qqq.backend.module.filesystem.exceptions.FilesystemException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;


/*******************************************************************************
 ** Base class for all (local) Filesystem actions
 *******************************************************************************/
public class AbstractFilesystemAction extends AbstractBaseFilesystemAction<File>
{
   private static final QLogger LOG = QLogger.getLogger(AbstractFilesystemAction.class);



   /*******************************************************************************
    ** List the files for this table.
    *******************************************************************************/
   @Override
   public List<File> listFiles(QTableMetaData table, QBackendMetaData backendBase, QQueryFilter filter) throws QException
   {
      String fullPath  = getFullBasePath(table, backendBase);
      File   directory = new File(fullPath);
      File[] files     = null;

      AbstractFilesystemTableBackendDetails tableBackendDetails = getTableBackendDetails(AbstractFilesystemTableBackendDetails.class, table);

      FileFilter fileFilter = TrueFileFilter.INSTANCE;

      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      // if each file is its own record (ONE), then we may need to do filtering of the directory listing based on the input filter //
      ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
      if(Cardinality.ONE.equals(tableBackendDetails.getCardinality()))
      {
         if(filter != null && filter.hasAnyCriteria())
         {
            if(CollectionUtils.nullSafeHasContents(filter.getSubFilters()))
            {
               ///////////////////////////////////////////////////////////////////////
               // todo - well, we could - just build up a tree of and's and or's... //
               ///////////////////////////////////////////////////////////////////////
               throw (new QException("Filters with sub-filters are not supported for querying filesystems at this time."));
            }

            List<FileFilter> fileFilterList = new ArrayList<>();
            for(QFilterCriteria criteria : filter.getCriteria())
            {
               if(tableBackendDetails.getFileNameFieldName().equals(criteria.getFieldName()))
               {
                  if(QCriteriaOperator.EQUALS.equals(criteria.getOperator()) && CollectionUtils.nonNullList(criteria.getValues()).size() == 1)
                  {
                     fileFilterList.add(new NameFileFilter(ValueUtils.getValueAsString(criteria.getValues().get(0))));
                  }
                  else if(QCriteriaOperator.IN.equals(criteria.getOperator()) && !CollectionUtils.nonNullList(criteria.getValues()).isEmpty())
                  {
                     List<NameFileFilter> nameInFilters = new ArrayList<>();
                     for(int i = 0; i < criteria.getValues().size(); i++)
                     {
                        nameInFilters.add(new NameFileFilter(ValueUtils.getValueAsString(criteria.getValues().get(i))));
                     }
                     fileFilterList.add(new OrFileFilter(nameInFilters));
                  }
                  else
                  {
                     throw (new QException("Unable to query filename field using operator: " + criteria.getOperator()));
                  }
               }
               else
               {
                  throw (new QException("Unable to query filesystem table by field: " + criteria.getFieldName()));
               }
            }

            fileFilter = QQueryFilter.BooleanOperator.AND.equals(filter.getBooleanOperator()) ? new AndFileFilter(fileFilterList) : new OrFileFilter(fileFilterList);
         }
      }

      ///////////////////////////////////////////////////////////////////////////////////////////
      // if the table has a glob specified, add it as an AND to the filter built to this point //
      ///////////////////////////////////////////////////////////////////////////////////////////
      if(StringUtils.hasContent(tableBackendDetails.getGlob()))
      {
         WildcardFileFilter globFilenameFilter = new WildcardFileFilter(tableBackendDetails.getGlob(), IOCase.INSENSITIVE);
         fileFilter = new AndFileFilter(List.of(globFilenameFilter, fileFilter));
      }

      files = directory.listFiles(fileFilter);

      if(files == null)
      {
         return Collections.emptyList();
      }

      return (Arrays.stream(files).filter(File::isFile).toList());
   }



   /*******************************************************************************
    ** Read the contents of a file.
    *******************************************************************************/
   @Override
   public InputStream readFile(File file) throws IOException
   {
      return (new FileInputStream(file));
   }



   /*******************************************************************************
    ** Write a file - to be implemented in module-specific subclasses.
    *******************************************************************************/
   @Override
   public void writeFile(QBackendMetaData backend, String path, byte[] contents) throws IOException
   {
      FileUtils.writeByteArrayToFile(new File(path), contents);
   }



   /*******************************************************************************
    ** Get a string that represents the full path to a file.
    *******************************************************************************/
   @Override
   public String getFullPathForFile(File file)
   {
      return (file.getAbsolutePath());
   }



   /*******************************************************************************
    ** In contrast with the DeleteAction, which deletes RECORDS - this is a
    ** filesystem-(or s3, sftp, etc)-specific extension to delete an entire FILE
    ** e.g., for post-ETL.
    **
    ** @throws FilesystemException if the delete is known to have failed, and the file is thought to still exit
    *******************************************************************************/
   @Override
   public void deleteFile(QInstance instance, QTableMetaData table, String fileReference) throws FilesystemException
   {
      File file = new File(fileReference);
      if(!file.exists())
      {
         //////////////////////////////////////////////////////////////////////////////////////////////
         // if the file doesn't exist, just exit with noop.  don't throw an error - that should only //
         // happen if the "contract" of the method is broken, and the file still exists              //
         //////////////////////////////////////////////////////////////////////////////////////////////
         LOG.debug("Not deleting file [{}], because it does not exist.", file);
         return;
      }

      if(!file.delete())
      {
         throw (new FilesystemException("Failed to delete file: " + fileReference));
      }
   }



   /*******************************************************************************
    ** Move a file from a source path, to a destination path.
    **
    ** @param destination assumed to be a file path - not a directory
    ** @throws FilesystemException if the delete is known to have failed
    *******************************************************************************/
   @Override
   public void moveFile(QInstance instance, QTableMetaData table, String source, String destination) throws FilesystemException
   {
      File sourceFile        = new File(source);
      File destinationFile   = new File(destination);
      File destinationParent = destinationFile.getParentFile();

      if(!sourceFile.exists())
      {
         throw (new FilesystemException("Cannot move file " + source + ", as it does not exist."));
      }

      //////////////////////////////////////////////////////////////////////////////////////
      // if the destination folder doesn't exist, try to make it - and fail if that fails //
      //////////////////////////////////////////////////////////////////////////////////////
      if(!destinationParent.exists())
      {
         LOG.debug("Making destination directory {} for move", destinationParent.getAbsolutePath());
         if(!destinationParent.mkdirs())
         {
            throw (new FilesystemException("Failed to make destination directory " + destinationParent.getAbsolutePath() + " to move " + source + " into."));
         }
      }

      if(!sourceFile.renameTo(destinationFile))
      {
         throw (new FilesystemException("Failed to move (rename) file " + source + " to " + destination));
      }
   }



   /*******************************************************************************
    ** e.g., with a base path of /foo/
    ** and a table path of /bar/
    ** and a file at /foo/bar/baz.txt
    ** give us just the baz.txt part.
    *******************************************************************************/
   @Override
   public String stripBackendAndTableBasePathsFromFileName(String filePath, QBackendMetaData backend, QTableMetaData table)
   {
      String tablePath    = getFullBasePath(table, backend);
      String strippedPath = filePath.replaceFirst(".*" + tablePath, "");
      return (strippedPath);
   }

}
