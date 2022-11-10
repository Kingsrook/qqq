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

package com.kingsrook.qqq.backend.core.model.actions.scripts;


import java.io.Serializable;
import java.util.List;
import com.kingsrook.qqq.backend.core.model.actions.AbstractActionOutput;
import com.kingsrook.qqq.backend.core.model.data.QRecord;


/*******************************************************************************
 **
 *******************************************************************************/
public class TestScriptOutput extends AbstractActionOutput
{
   private Serializable  outputObject;
   private Exception     exception;
   private QRecord       scriptLog;
   private List<QRecord> scriptLogLines;



   /*******************************************************************************
    ** Getter for outputObject
    **
    *******************************************************************************/
   public Serializable getOutputObject()
   {
      return outputObject;
   }



   /*******************************************************************************
    ** Setter for outputObject
    **
    *******************************************************************************/
   public void setOutputObject(Serializable outputObject)
   {
      this.outputObject = outputObject;
   }



   /*******************************************************************************
    ** Fluent setter for outputObject
    **
    *******************************************************************************/
   public TestScriptOutput withOutputObject(Serializable outputObject)
   {
      this.outputObject = outputObject;
      return (this);
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setException(Exception exception)
   {
      this.exception = exception;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public Exception getException()
   {
      return exception;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setScriptLog(QRecord scriptLog)
   {
      this.scriptLog = scriptLog;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public QRecord getScriptLog()
   {
      return scriptLog;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public void setScriptLogLines(List<QRecord> scriptLogLines)
   {
      this.scriptLogLines = scriptLogLines;
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   public List<QRecord> getScriptLogLines()
   {
      return scriptLogLines;
   }

}
