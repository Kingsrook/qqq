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

package com.kingsrook.qqq.backend.module.filesystem.s3.utils;


import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


/*******************************************************************************
 ** Verification for some of the behavior in the S3Utils - working with PathMatcher
 ** globs.
 *******************************************************************************/
public class PathMatcherGlobTest
{

   @Test
   public void testPathMatcher() throws Exception
   {
      /////////////////////////////////////////////////////////////
      // note:  must start with for both the pattern and the uri //
      /////////////////////////////////////////////////////////////
      PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher("glob:/root/*/acme/*/*.csv");

      Assertions.assertTrue(pathMatcher.matches(Path.of(URI.create("file:///root/stl/acme/20220627/1234.csv"))), "Glob should match");
      Assertions.assertTrue(pathMatcher.matches(Path.of(URI.create("file:///root/nj/acme/20220627/1234.csv"))), "Glob should match");
      Assertions.assertTrue(pathMatcher.matches(Path.of(URI.create("file:///root/stl/acme/20220628/1234.csv"))), "Glob should match");
      Assertions.assertTrue(pathMatcher.matches(Path.of(URI.create("file:///root/stl/acme/20220627/12345.csv"))), "Glob should match");

      Assertions.assertFalse(pathMatcher.matches(Path.of(URI.create("file:///root/stl/beta/20220627/1234.csv"))), "Glob should not match (beta vs acme)");
      Assertions.assertFalse(pathMatcher.matches(Path.of(URI.create("file:///something/stl/acme/20220627/1234.csv"))), "Glob should not match (wrong start path)");
      Assertions.assertFalse(pathMatcher.matches(Path.of(URI.create("file:///root/stl/acme/20220627/csv"))), "Glob should not match (no file basename)");
      Assertions.assertFalse(pathMatcher.matches(Path.of(URI.create("file:///root/stl/acme/20220627/1234.CSV"))), "Glob should not match (wrong case extension)");
      Assertions.assertFalse(pathMatcher.matches(Path.of(URI.create("file:///root/stl/acme/20220627/extra/1234.csv"))), "Glob should not match (extra dir)");
      Assertions.assertFalse(pathMatcher.matches(Path.of(URI.create("file:///root/stl/extra/acme/20220627/1234.csv"))), "Glob should not match (extra dir)");
      Assertions.assertFalse(pathMatcher.matches(Path.of(URI.create("file:///root/extra/stl/acme/20220627/1234.csv"))), "Glob should not match (extra dir)");

      pathMatcher = FileSystems.getDefault().getPathMatcher("glob:/root/**/acme/*/*.csv");
      Assertions.assertTrue(pathMatcher.matches(Path.of(URI.create("file:///root/extra/stl/acme/20220627/1234.csv"))), "Glob should match with extra dir");
      Assertions.assertTrue(pathMatcher.matches(Path.of(URI.create("file:///root/extra/extra2/stl/acme/20220627/1234.csv"))), "Glob should match with 2 extra dirs");
      Assertions.assertFalse(pathMatcher.matches(Path.of(URI.create("file:///root/acme/20220627/1234.csv"))), "Glob does not match with no dir for **");

      pathMatcher = FileSystems.getDefault().getPathMatcher("glob:/root/**");
      Assertions.assertTrue(pathMatcher.matches(Path.of(URI.create("file:///root/1234.csv"))), "Glob should match with extra dir");

      pathMatcher = FileSystems.getDefault().getPathMatcher("glob:/*");
      Assertions.assertTrue(pathMatcher.matches(Path.of(URI.create("file:///1234.csv"))), "Glob should match");
   }

}