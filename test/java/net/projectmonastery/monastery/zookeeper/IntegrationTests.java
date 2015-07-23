package net.projectmonastery.monastery.zookeeper;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;

/**
 * Created by Arnon Moscona on 7/23/2015.
 * Dynamic test suite support for integration tests. The suite itself is IntegrationTestSuite
 */
@RunWith(ClasspathSuite.class)
@ClasspathSuite.ClassnameFilters({ "net.projectmonastery.monastery.zookeeper.*Test" })
public class IntegrationTests {
}
