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

package com.kingsrook.qqq.backend.core.scheduler.simple;


import java.util.Map;
import com.kingsrook.qqq.backend.core.actions.customizers.QCodeLoader;
import com.kingsrook.qqq.backend.core.context.CapturedContext;
import com.kingsrook.qqq.backend.core.context.QContext;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import com.kingsrook.qqq.backend.core.model.metadata.QInstance;
import com.kingsrook.qqq.backend.core.scheduler.schedulable.runner.SchedulableRunner;
import com.kingsrook.qqq.backend.core.scheduler.schedulable.SchedulableType;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 **
 *******************************************************************************/
public class SimpleJobRunner implements Runnable
{
   private static final QLogger LOG = QLogger.getLogger(SimpleJobRunner.class);

   private QInstance           qInstance;
   private SchedulableType     schedulableType;
   private Map<String, Object> params;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public SimpleJobRunner(QInstance qInstance, SchedulableType type, Map<String, Object> params)
   {
      this.qInstance = qInstance;
      this.schedulableType = type;
      this.params = params;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run()
   {
      CapturedContext capturedContext = QContext.capture();
      try
      {
         SimpleScheduler simpleScheduler = SimpleScheduler.getInstance(qInstance);
         QContext.init(qInstance, simpleScheduler.getSessionSupplier().get());

         SchedulableRunner schedulableRunner = QCodeLoader.getAdHoc(SchedulableRunner.class, schedulableType.getRunner());
         schedulableRunner.run(params);
      }
      catch(Exception e)
      {
         LOG.warn("Error running SimpleScheduler job", e, logPair("params", params));
      }
      finally
      {
         QContext.init(capturedContext);
      }
   }

}
