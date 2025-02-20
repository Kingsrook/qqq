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

package com.kingsrook.qqq.backend.module.filesystem.sftp;


import com.kingsrook.qqq.backend.module.filesystem.BaseTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.MountableFile;


/*******************************************************************************
 ** Base class for tests that want to be able to work with sftp testcontainer
 *******************************************************************************/
public class BaseSFTPTest extends BaseTest
{
   public static final int    PORT      = 22;
   public static final String USERNAME  = "testuser";
   public static final String PASSWORD  = "testpass";
   public static final String HOST_NAME = "localhost";

   public static final String BACKEND_FOLDER = "upload";
   public static final String TABLE_FOLDER   = "files";
   public static final String REMOTE_DIR     = "/home/" + USERNAME + "/" + BACKEND_FOLDER + "/" + TABLE_FOLDER;

   private static GenericContainer<?> sftpContainer;
   private static Integer             currentPort;



   /***************************************************************************
    **
    ***************************************************************************/
   @BeforeAll
   static void setUp() throws Exception
   {
      sftpContainer = new GenericContainer<>("atmoz/sftp:latest")
         .withExposedPorts(PORT)
         .withCommand(USERNAME + ":" + PASSWORD + ":1001");

      sftpContainer.start();

      for(int i = 0; i < 5; i++)
      {
         copyFileToContainer("files/testfile.txt", REMOTE_DIR + "/testfile-" + i + ".txt");
      }

      grantUploadFilesDirWritePermission();

      currentPort = sftpContainer.getMappedPort(22);
   }


   /***************************************************************************
    **
    ***************************************************************************/
   protected static void copyFileToContainer(String sourceFileClasspathResourceName, String fullRemotePath)
   {
      sftpContainer.copyFileToContainer(MountableFile.forClasspathResource(sourceFileClasspathResourceName), fullRemotePath);
   }


   /***************************************************************************
    **
    ***************************************************************************/
   protected static void rmrfInContainer(String fullRemotePath) throws Exception
   {
      sftpContainer.execInContainer("rm", "-rf", fullRemotePath);
   }


   /***************************************************************************
    **
    ***************************************************************************/
   @AfterAll
   static void tearDown()
   {
      if(sftpContainer != null)
      {
         sftpContainer.stop();
      }
   }



   /*******************************************************************************
    ** Getter for currentPort
    **
    *******************************************************************************/
   public static Integer getCurrentPort()
   {
      return currentPort;
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected static void revokeUploadFilesDirWritePermission() throws Exception
   {
      setUploadFilesDirPermission("444");
   }



   /***************************************************************************
    **
    ***************************************************************************/
   protected static void grantUploadFilesDirWritePermission() throws Exception
   {
      setUploadFilesDirPermission("777");
   }



   /***************************************************************************
    **
    ***************************************************************************/
   private static void setUploadFilesDirPermission(String mode) throws Exception
   {
      sftpContainer.execInContainer("chmod", mode, "/home/testuser/upload/files");
   }



   /***************************************************************************
    *
    ***************************************************************************/
   protected void mkdirInSftpContainerUnderHomeTestuser(String path) throws Exception
   {
      sftpContainer.execInContainer("mkdir", "-p", "/home/testuser/" + path);
   }
}
