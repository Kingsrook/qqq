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

package com.kingsrook.qqq.lambda;


import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;


/*******************************************************************************
 **
 *******************************************************************************/
public class MockContext implements Context
{
   @Override
   public String getAwsRequestId()
   {
      return null;
   }



   @Override
   public String getLogGroupName()
   {
      return null;
   }



   @Override
   public String getLogStreamName()
   {
      return null;
   }



   @Override
   public String getFunctionName()
   {
      return null;
   }



   @Override
   public String getFunctionVersion()
   {
      return null;
   }



   @Override
   public String getInvokedFunctionArn()
   {
      return null;
   }



   @Override
   public CognitoIdentity getIdentity()
   {
      return null;
   }



   @Override
   public ClientContext getClientContext()
   {
      return null;
   }



   @Override
   public int getRemainingTimeInMillis()
   {
      return 0;
   }



   @Override
   public int getMemoryLimitInMB()
   {
      return 0;
   }



   @Override
   public LambdaLogger getLogger()
   {
      return new LambdaLogger()
      {
         @Override
         public void log(String s)
         {
            System.out.println(s);
         }



         @Override
         public void log(byte[] bytes)
         {
            System.out.println(bytes);
         }
      };
   }
}
