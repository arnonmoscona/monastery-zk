/*
 * Copyright (c) 2015. Arnon Moscona
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package net.projectmonastery.monastery.zookeeper;

import org.junit.experimental.categories.Categories;
import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Arnon Moscona on 7/23/2015.
 * A test suite for unit tests only. Achieved by <strong>excluding</strong> integration tests
 */
@RunWith(Categories.class)
@Categories.ExcludeCategory(IntegrationTestCategory.class)
@ClasspathSuite.ExcludeBaseTypeFilter(IntegrationTestClassMarker.class)
@Suite.SuiteClasses( { UnitTests.class })
public class UnitTestSuite {
}
