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
