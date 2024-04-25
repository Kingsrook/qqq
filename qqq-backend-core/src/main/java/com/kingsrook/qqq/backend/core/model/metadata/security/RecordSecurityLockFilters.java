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

package com.kingsrook.qqq.backend.core.model.metadata.security;


import java.util.List;


/*******************************************************************************
 ** standard filtering operations for lists of record security locks.
 *******************************************************************************/
public class RecordSecurityLockFilters
{

   /*******************************************************************************
    ** filter a list of locks so that we only see the ones that apply to reads.
    *******************************************************************************/
   public static List<RecordSecurityLock> filterForReadLocks(List<RecordSecurityLock> recordSecurityLocks)
   {
      if(recordSecurityLocks == null)
      {
         return (null);
      }

      return (recordSecurityLocks.stream().filter(rsl -> RecordSecurityLock.LockScope.READ_AND_WRITE.equals(rsl.getLockScope())).toList());
   }



   /*******************************************************************************
    ** filter a list of locks so that we only see the ones that apply to reads.
    *******************************************************************************/
   public static MultiRecordSecurityLock filterForReadLockTree(List<RecordSecurityLock> recordSecurityLocks)
   {
      if(recordSecurityLocks == null)
      {
         return (null);
      }

      MultiRecordSecurityLock result = new MultiRecordSecurityLock();
      result.setOperator(MultiRecordSecurityLock.BooleanOperator.AND);

      for(RecordSecurityLock recordSecurityLock : recordSecurityLocks)
      {
         if(recordSecurityLock instanceof MultiRecordSecurityLock multiRecordSecurityLock)
         {
            MultiRecordSecurityLock filteredSubLock = filterForReadLockTree(multiRecordSecurityLock.getLocks());
            filteredSubLock.setOperator(multiRecordSecurityLock.getOperator());
            result.withLock(filteredSubLock);
         }
         else
         {
            if(RecordSecurityLock.LockScope.READ_AND_WRITE.equals(recordSecurityLock.getLockScope()))
            {
               result.withLock(recordSecurityLock);
            }
         }
      }

      return (result);
   }



   /*******************************************************************************
    ** filter a list of locks so that we only see the ones that apply to writes.
    *******************************************************************************/
   public static List<RecordSecurityLock> filterForWriteLocks(List<RecordSecurityLock> recordSecurityLocks)
   {
      if(recordSecurityLocks == null)
      {
         return (null);
      }

      return (recordSecurityLocks.stream().filter(rsl ->
         RecordSecurityLock.LockScope.READ_AND_WRITE.equals(rsl.getLockScope())
            || RecordSecurityLock.LockScope.WRITE.equals(rsl.getLockScope()
         )).toList());
   }



   /*******************************************************************************
    ** filter a list of locks so that we only see the ones that are WRITE type only.
    *******************************************************************************/
   public static List<RecordSecurityLock> filterForOnlyWriteLocks(List<RecordSecurityLock> recordSecurityLocks)
   {
      if(recordSecurityLocks == null)
      {
         return (null);
      }

      return (recordSecurityLocks.stream().filter(rsl -> RecordSecurityLock.LockScope.WRITE.equals(rsl.getLockScope())).toList());
   }

}
