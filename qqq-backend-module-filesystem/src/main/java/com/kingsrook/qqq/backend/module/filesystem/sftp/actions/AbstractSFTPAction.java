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

package com.kingsrook.qqq.backend.module.filesystem.sftp.actions;


import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.data.QRecord;
import com.kingsrook.qqq.backend.core.model.metadata.QBackendMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.core.model.metadata.variants.BackendVariantSetting;
import com.kingsrook.qqq.backend.core.model.metadata.variants.BackendVariantsUtil;
import com.kingsrook.qqq.backend.core.utils.StringUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.actions.AbstractBaseFilesystemAction;
import com.kingsrook.qqq.backend.module.filesystem.exceptions.FilesystemException;
import com.kingsrook.qqq.backend.module.filesystem.sftp.model.SFTPDirEntryWithPath;
import com.kingsrook.qqq.backend.module.filesystem.sftp.model.metadata.SFTPBackendMetaData;
import com.kingsrook.qqq.backend.module.filesystem.sftp.model.metadata.SFTPBackendVariantSetting;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Base class for all SFTP filesystem actions
 *******************************************************************************/
public class AbstractSFTPAction extends AbstractBaseFilesystemAction<SFTPDirEntryWithPath>
{
   private static final QLogger LOG = QLogger.getLogger(AbstractSFTPAction.class);

   /***************************************************************************
    ** singleton implementing Initialization-on-Demand Holder idiom
    ** to help ensure only a single SshClient object exists in a server.
    ***************************************************************************/
   private static class SshClientManager
   {

      /***************************************************************************
       **
       ***************************************************************************/
      private static class Holder
      {
         private static final SshClient INSTANCE = SshClient.setUpDefaultClient();

         static
         {
            INSTANCE.start();
         }
      }



      /***************************************************************************
       **
       ***************************************************************************/
      public static SshClient getInstance()
      {
         return Holder.INSTANCE;
      }
   }



   ////////////////////////////////////////////////////////////////
   // open clientSessionFirst, then sftpClient                   //
   // and close them in reverse (sftpClient, then clientSession) //
   ////////////////////////////////////////////////////////////////
   private ClientSession clientSession;
   private SftpClient    sftpClient;



   /*******************************************************************************
    ** Set up the sftp utils object to be used for this action.
    *******************************************************************************/
   @Override
   public void preAction(QBackendMetaData backendMetaData) throws QException
   {
      super.preAction(backendMetaData);

      if(sftpClient != null)
      {
         LOG.debug("sftpClient object is already set - not re-setting it.");
         return;
      }

      try
      {
         SFTPBackendMetaData sftpBackendMetaData = getBackendMetaData(SFTPBackendMetaData.class, backendMetaData);

         String  username = sftpBackendMetaData.getUsername();
         String  password = sftpBackendMetaData.getPassword();
         String  hostName = sftpBackendMetaData.getHostName();
         Integer port     = sftpBackendMetaData.getPort();

         if(backendMetaData.getUsesVariants())
         {
            QRecord variantRecord = BackendVariantsUtil.getVariantRecord(backendMetaData);
            LOG.debug("Getting SFTP connection credentials from variant record",
               logPair("tableName", backendMetaData.getBackendVariantsConfig().getOptionsTableName()),
               logPair("id", variantRecord.getValue("id")),
               logPair("name", variantRecord.getRecordLabel()));
            Map<BackendVariantSetting, String> fieldNameMap = backendMetaData.getBackendVariantsConfig().getBackendSettingSourceFieldNameMap();

            if(fieldNameMap.containsKey(SFTPBackendVariantSetting.USERNAME))
            {
               username = variantRecord.getValueString(fieldNameMap.get(SFTPBackendVariantSetting.USERNAME));
            }

            if(fieldNameMap.containsKey(SFTPBackendVariantSetting.PASSWORD))
            {
               password = variantRecord.getValueString(fieldNameMap.get(SFTPBackendVariantSetting.PASSWORD));
            }

            if(fieldNameMap.containsKey(SFTPBackendVariantSetting.HOSTNAME))
            {
               hostName = variantRecord.getValueString(fieldNameMap.get(SFTPBackendVariantSetting.HOSTNAME));
            }

            if(fieldNameMap.containsKey(SFTPBackendVariantSetting.PORT))
            {
               port = variantRecord.getValueInteger(fieldNameMap.get(SFTPBackendVariantSetting.PORT));
            }
         }

         makeConnection(username, hostName, port, password);
      }
      catch(IOException e)
      {
         throw (new QException("Error setting up SFTP connection", e));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void postAction()
   {
      Consumer<AutoCloseable> closer = closable ->
      {
         if(closable != null)
         {
            try
            {
               closable.close();
            }
            catch(Exception e)
            {
               LOG.info("Error closing SFTP resource", e, logPair("type", closable.getClass().getSimpleName()));
            }
         }
      };

      closer.accept(sftpClient);
      closer.accept(clientSession);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected SftpClient makeConnection(String username, String hostName, Integer port, String password) throws IOException
   {
      this.clientSession = SshClientManager.getInstance().connect(username, hostName, port).verify().getSession();
      clientSession.addPasswordIdentity(password);
      clientSession.auth().verify();

      this.sftpClient = SftpClientFactory.instance().createSftpClient(clientSession);
      return (this.sftpClient);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public Long getFileSize(SFTPDirEntryWithPath sftpDirEntryWithPath)
   {
      try
      {
         return sftpDirEntryWithPath.dirEntry().getAttributes().getSize();
      }
      catch(Exception e)
      {
         return (null);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public Instant getFileCreateDate(SFTPDirEntryWithPath sftpDirEntryWithPath)
   {
      try
      {
         return sftpDirEntryWithPath.dirEntry().getAttributes().getCreateTime().toInstant();
      }
      catch(Exception e)
      {
         return (null);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public Instant getFileModifyDate(SFTPDirEntryWithPath sftpDirEntryWithPath)
   {
      try
      {
         return sftpDirEntryWithPath.dirEntry().getAttributes().getModifyTime().toInstant();
      }
      catch(Exception e)
      {
         return (null);
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public List<SFTPDirEntryWithPath> listFiles(QTableMetaData table, QBackendMetaData backendBase, String requestedPath) throws QException
   {
      try
      {
         String fullPath = getFullBasePath(table, backendBase);
         if(StringUtils.hasContent(requestedPath))
         {
            fullPath = stripDuplicatedSlashes(fullPath + File.separatorChar + requestedPath + File.separatorChar);
         }

         List<SFTPDirEntryWithPath> rs = new ArrayList<>();

         for(SftpClient.DirEntry dirEntry : sftpClient.readDir(fullPath))
         {
            if(".".equals(dirEntry.getFilename()) || "..".equals(dirEntry.getFilename()))
            {
               continue;
            }

            if(dirEntry.getAttributes().isDirectory())
            {
               // todo - recursive??
               continue;
            }

            rs.add(new SFTPDirEntryWithPath(fullPath, dirEntry));
         }

         return (rs);
      }
      catch(Exception e)
      {
         throw (new QException("Error listing files", e));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public InputStream readFile(SFTPDirEntryWithPath dirEntry) throws IOException
   {
      return (sftpClient.read(getFullPathForFile(dirEntry)));
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void writeFile(QBackendMetaData backend, String path, byte[] contents) throws IOException
   {
      sftpClient.put(new ByteArrayInputStream(contents), path);
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public String getFullPathForFile(SFTPDirEntryWithPath dirEntry)
   {
      return (dirEntry.path() + "/" + dirEntry.dirEntry().getFilename());
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void deleteFile(QTableMetaData table, String fileReference) throws FilesystemException
   {
      try
      {
         sftpClient.remove(fileReference);
      }
      catch(Exception e)
      {
         throw (new FilesystemException("Error deleting file from SFTP", e));
      }
   }



   /***************************************************************************
    **
    ***************************************************************************/
   @Override
   public void moveFile(QInstance instance, QTableMetaData table, String source, String destination) throws FilesystemException
   {
      throw (new QRuntimeException("Not yet implemented"));
   }



   /***************************************************************************
    *
    ***************************************************************************/
   protected SftpClient getSftpClient(QBackendMetaData backend) throws QException
   {
      if(sftpClient == null)
      {
         preAction(backend);
      }

      return (sftpClient);
   }
}
