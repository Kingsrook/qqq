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


import java.util.function.Function;
import com.kingsrook.qqq.backend.core.exceptions.QException;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountInput;
import com.kingsrook.qqq.backend.core.model.actions.tables.count.CountOutput;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeReference;
import com.kingsrook.qqq.backend.core.model.metadata.code.QCodeUsage;
import com.kingsrook.qqq.backend.core.model.metadata.tables.QTableMetaData;
import com.kingsrook.qqq.backend.module.filesystem.TestUtils;
import com.kingsrook.qqq.backend.module.filesystem.base.actions.FilesystemCustomizers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 **
 *******************************************************************************/
public class FilesystemCountActionTest extends FilesystemActionTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testCount1() throws QException
   {
      CountInput countInput = new CountInput();
      countInput.setInstance(TestUtils.defineInstance());
      countInput.setTableName(TestUtils.defineLocalFilesystemJSONPersonTable().getName());
      CountOutput countOutput = new FilesystemCountAction().execute(countInput);
      Assertions.assertEquals(3, countOutput.getCount(), "Unfiltered count should find all rows");
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   public void testCountWithFileCustomizer() throws QException
   {
      CountInput countInput = new CountInput();
      QInstance  instance   = TestUtils.defineInstance();

      QTableMetaData table = instance.getTable(TestUtils.TABLE_NAME_PERSON_LOCAL_FS_JSON);
      table.withCustomizer(FilesystemCustomizers.POST_READ_FILE, new QCodeReference(ValueUpshifter.class, QCodeUsage.CUSTOMIZER));

      countInput.setInstance(instance);
      countInput.setTableName(TestUtils.defineLocalFilesystemJSONPersonTable().getName());
      CountOutput countOutput = new FilesystemCountAction().execute(countInput);
      Assertions.assertEquals(3, countOutput.getCount(), "Unfiltered count should find all rows");
   }



   public static class ValueUpshifter implements Function<String, String>
   {
      @Override
      public String apply(String s)
      {
         return (s.replaceAll("kingsrook.com", "KINGSROOK.COM"));
      }
   }

}