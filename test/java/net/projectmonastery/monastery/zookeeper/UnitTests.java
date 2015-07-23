package net.projectmonastery.monastery.zookeeper;

import org.junit.extensions.cpsuite.ClasspathSuite;
import org.junit.runner.RunWith;

/**
 * Created by Arnon Moscona on 7/23/2015.
 * A support class for running unit tests. The suite is UnitTestSuite
 */
@RunWith(ClasspathSuite.class)
@ClasspathSuite.ClassnameFilters({ "net.projectmonastery.monastery.zookeeper.*Test" })
public class UnitTests {
}
