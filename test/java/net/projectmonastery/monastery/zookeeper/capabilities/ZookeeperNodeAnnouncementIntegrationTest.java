package net.projectmonastery.monastery.zookeeper.capabilities;

import net.projectmonastery.monastery.zookeeper.IntegrationTestCategory;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.fest.assertions.api.Assertions.*;

import net.projectmonastery.monastery.zookeeper.IntegrationTestClassMarker;

/**
 * Created by Arnon Moscona on 7/23/2015.
 * Integration tests for the node announcement capability
 */
@Category(IntegrationTestCategory.class)
public class ZookeeperNodeAnnouncementIntegrationTest implements IntegrationTestClassMarker {
    @Test
    public void reminder() throws Exception {
        throw new Exception("still need an integration test");
    }
}
