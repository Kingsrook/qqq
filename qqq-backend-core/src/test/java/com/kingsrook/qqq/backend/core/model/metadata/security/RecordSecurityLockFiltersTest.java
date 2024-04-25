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

package com.kingsrook.qqq.backend.core.model.metadata.security;


import java.util.List;
import com.kingsrook.qqq.backend.core.BaseTest;
import org.junit.jupiter.api.Test;
import static com.kingsrook.qqq.backend.core.model.metadata.security.MultiRecordSecurityLock.BooleanOperator.AND;
import static com.kingsrook.qqq.backend.core.model.metadata.security.MultiRecordSecurityLock.BooleanOperator.OR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;


/*******************************************************************************
 ** Unit test for RecordSecurityLockFilters 
 *******************************************************************************/
class RecordSecurityLockFiltersTest extends BaseTest
{

   /*******************************************************************************
    **
    *******************************************************************************/
   @Test
   void test()
   {
      MultiRecordSecurityLock nullBecauseNull = RecordSecurityLockFilters.filterForReadLockTree(null);
      assertNull(nullBecauseNull);

      MultiRecordSecurityLock emptyBecauseEmptyList = RecordSecurityLockFilters.filterForReadLockTree(List.of());
      assertEquals(0, emptyBecauseEmptyList.getLocks().size());

      MultiRecordSecurityLock emptyBecauseAllWrite = RecordSecurityLockFilters.filterForReadLockTree(List.of(
         new RecordSecurityLock().withFieldName("A").withLockScope(RecordSecurityLock.LockScope.WRITE),
         new RecordSecurityLock().withFieldName("B").withLockScope(RecordSecurityLock.LockScope.WRITE)
      ));
      assertEquals(0, emptyBecauseAllWrite.getLocks().size());

      MultiRecordSecurityLock onlyA = RecordSecurityLockFilters.filterForReadLockTree(List.of(
         new RecordSecurityLock().withFieldName("A").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE),
         new RecordSecurityLock().withFieldName("B").withLockScope(RecordSecurityLock.LockScope.WRITE)
      ));
      assertMultiRecordSecurityLock(onlyA, AND, "A");

      MultiRecordSecurityLock twoOutOfThreeTopLevel = RecordSecurityLockFilters.filterForReadLockTree(List.of(
         new RecordSecurityLock().withFieldName("A").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE),
         new RecordSecurityLock().withFieldName("B").withLockScope(RecordSecurityLock.LockScope.WRITE),
         new RecordSecurityLock().withFieldName("C").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE)
      ));
      assertMultiRecordSecurityLock(twoOutOfThreeTopLevel, AND, "A", "C");

      MultiRecordSecurityLock treeOfAllReads = RecordSecurityLockFilters.filterForReadLockTree(List.of(
         new MultiRecordSecurityLock().withOperator(OR).withLocks(List.of(
            new RecordSecurityLock().withFieldName("A").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE),
            new RecordSecurityLock().withFieldName("B").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE)
         )),
         new MultiRecordSecurityLock().withOperator(OR).withLocks(List.of(
            new RecordSecurityLock().withFieldName("C").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE),
            new RecordSecurityLock().withFieldName("D").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE)
         ))
      ));
      assertEquals(2, treeOfAllReads.getLocks().size());
      assertEquals(AND, treeOfAllReads.getOperator());
      assertMultiRecordSecurityLock((MultiRecordSecurityLock) treeOfAllReads.getLocks().get(0), OR, "A", "B");
      assertMultiRecordSecurityLock((MultiRecordSecurityLock) treeOfAllReads.getLocks().get(1), OR, "C", "D");

      MultiRecordSecurityLock treeWithOneBranchReadsOneBranchWrites = RecordSecurityLockFilters.filterForReadLockTree(List.of(
         new MultiRecordSecurityLock().withOperator(OR).withLocks(List.of(
            new RecordSecurityLock().withFieldName("A").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE),
            new RecordSecurityLock().withFieldName("B").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE)
         )),
         new MultiRecordSecurityLock().withOperator(OR).withLocks(List.of(
            new RecordSecurityLock().withFieldName("C").withLockScope(RecordSecurityLock.LockScope.WRITE),
            new RecordSecurityLock().withFieldName("D").withLockScope(RecordSecurityLock.LockScope.WRITE)
         ))
      ));
      assertEquals(2, treeWithOneBranchReadsOneBranchWrites.getLocks().size());
      assertEquals(AND, treeWithOneBranchReadsOneBranchWrites.getOperator());
      assertMultiRecordSecurityLock((MultiRecordSecurityLock) treeWithOneBranchReadsOneBranchWrites.getLocks().get(0), OR, "A", "B");
      assertMultiRecordSecurityLock((MultiRecordSecurityLock) treeWithOneBranchReadsOneBranchWrites.getLocks().get(1), OR);

      MultiRecordSecurityLock deepSparseTree = RecordSecurityLockFilters.filterForReadLockTree(List.of(
         new MultiRecordSecurityLock().withOperator(OR).withLocks(List.of(
            new MultiRecordSecurityLock().withOperator(AND).withLocks(List.of(
               new RecordSecurityLock().withFieldName("A").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE),
               new RecordSecurityLock().withFieldName("B").withLockScope(RecordSecurityLock.LockScope.WRITE)
            )),
            new RecordSecurityLock().withFieldName("C").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE),
            new RecordSecurityLock().withFieldName("D").withLockScope(RecordSecurityLock.LockScope.WRITE)
         )),
         new MultiRecordSecurityLock().withOperator(OR).withLocks(List.of(
            new MultiRecordSecurityLock().withOperator(AND).withLocks(List.of(
               new RecordSecurityLock().withFieldName("E").withLockScope(RecordSecurityLock.LockScope.WRITE),
               new RecordSecurityLock().withFieldName("F").withLockScope(RecordSecurityLock.LockScope.WRITE)
            )),
            new MultiRecordSecurityLock().withOperator(AND).withLocks(List.of(
               new RecordSecurityLock().withFieldName("G").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE),
               new RecordSecurityLock().withFieldName("H").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE)
            ))
         )),
         new RecordSecurityLock().withFieldName("I").withLockScope(RecordSecurityLock.LockScope.READ_AND_WRITE),
         new RecordSecurityLock().withFieldName("J").withLockScope(RecordSecurityLock.LockScope.WRITE)
      ));

      assertEquals(3, deepSparseTree.getLocks().size());
      assertEquals(AND, deepSparseTree.getOperator());
      MultiRecordSecurityLock deepChild0 = (MultiRecordSecurityLock) deepSparseTree.getLocks().get(0);
      assertEquals(2, deepChild0.getLocks().size());
      assertEquals(OR, deepChild0.getOperator());
      MultiRecordSecurityLock deepGrandChild0 = (MultiRecordSecurityLock) deepChild0.getLocks().get(0);
      assertMultiRecordSecurityLock(deepGrandChild0, AND, "A");
      assertEquals("C", deepChild0.getLocks().get(1).getFieldName());

      MultiRecordSecurityLock deepChild1 = (MultiRecordSecurityLock) deepSparseTree.getLocks().get(1);
      assertEquals(2, deepChild1.getLocks().size());
      assertEquals(OR, deepChild1.getOperator());
      MultiRecordSecurityLock deepGrandChild1 = (MultiRecordSecurityLock) deepChild1.getLocks().get(0);
      assertMultiRecordSecurityLock(deepGrandChild1, AND);
      MultiRecordSecurityLock deepGrandChild2 = (MultiRecordSecurityLock) deepChild1.getLocks().get(1);
      assertMultiRecordSecurityLock(deepGrandChild2, AND, "G", "H");

      assertEquals("I", deepSparseTree.getLocks().get(2).getFieldName());
   }



   /*******************************************************************************
    **
    *******************************************************************************/
   private void assertMultiRecordSecurityLock(MultiRecordSecurityLock lock, MultiRecordSecurityLock.BooleanOperator operator, String... lockFieldNames)
   {
      assertEquals(lockFieldNames.length, lock.getLocks().size());
      assertEquals(operator, lock.getOperator());

      for(int i = 0; i < lockFieldNames.length; i++)
      {
         assertEquals(lockFieldNames[i], lock.getLocks().get(i).getFieldName());
      }
   }

}