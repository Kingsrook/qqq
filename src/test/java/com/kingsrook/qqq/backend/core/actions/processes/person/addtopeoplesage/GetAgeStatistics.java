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

package com.kingsrook.qqq.backend.core.actions.processes.person.addtopeoplesage;


import java.time.LocalDate;
import java.time.Period;
import com.kingsrook.qqq.backend.core.interfaces.BackendStep;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepRequest;
import com.kingsrook.qqq.backend.core.model.actions.processes.RunBackendStepResult;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public class GetAgeStatistics implements BackendStep
{
   @Override
   public void run(RunBackendStepRequest runBackendStepRequest, RunBackendStepResult runBackendStepResult)
   {
      Integer min = null;
      Integer max = null;
      LocalDate now = LocalDate.now();
      for(QRecord record : runBackendStepRequest.getRecords())
      {
         LocalDate birthDate = record.getValueDate("birthDate");
         Period until = birthDate.until(now);
         int age = until.getYears();
         record.setValue("age", age);
         System.out.println(birthDate + " -> " + age);
         min = (min == null || age < min) ? age : min;
         max = (max == null || age > max) ? age : max;
      }

      runBackendStepResult.addValue("minAge", min);
      runBackendStepResult.addValue("maxAge", max);
   }
}
