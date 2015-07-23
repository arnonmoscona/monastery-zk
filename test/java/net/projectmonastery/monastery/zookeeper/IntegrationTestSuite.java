package net.projectmonastery.monastery.zookeeper;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Created by Arnon Moscona on 7/23/2015.
 * A test suite for integration tests.
 * Integration tests generally are slow and require a instance of zookeeper and a real curator framework
 */
@RunWith(Categories.class)
@Categories.IncludeCategory(IntegrationTestCategory.class)
@Suite.SuiteClasses( { IntegrationTests.class })
public class IntegrationTestSuite {
}
