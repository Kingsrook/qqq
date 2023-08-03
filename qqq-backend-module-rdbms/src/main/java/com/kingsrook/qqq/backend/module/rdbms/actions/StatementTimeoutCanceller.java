/*
 * QQQ - Low-code Application Framework for Engineers.
 * Copyright (C) 2021-2023.  Kingsrook, LLC
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

package com.kingsrook.qqq.backend.module.rdbms.actions;


import java.sql.Statement;
import com.kingsrook.qqq.backend.core.exceptions.QRuntimeException;
import com.kingsrook.qqq.backend.core.logging.QLogger;
import static com.kingsrook.qqq.backend.core.logging.LogUtils.logPair;


/*******************************************************************************
 ** Helper to cancel statements that timeout.
 *******************************************************************************/
public class StatementTimeoutCanceller implements Runnable
{
   private static final QLogger LOG = QLogger.getLogger(StatementTimeoutCanceller.class);

   private final Statement statement;
   private final String    sql;



   /*******************************************************************************
    ** Constructor
    **
    *******************************************************************************/
   public StatementTimeoutCanceller(Statement statement, CharSequence sql)
   {
      this.statement = statement;
      this.sql = sql.toString();
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   @Override
   public void run()
   {
      try
      {
         statement.cancel();
         LOG.info("Cancelled timed out statement", logPair("sql", sql));
      }
      catch(Exception e)
      {
         LOG.warn("Error trying to cancel statement after timeout", e, logPair("sql", sql));
      }

      throw (new QRuntimeException("Statement timed out and was cancelled."));
   }
}
