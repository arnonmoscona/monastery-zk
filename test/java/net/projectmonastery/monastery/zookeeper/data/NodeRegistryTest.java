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

package net.projectmonastery.monastery.zookeeper.data;

import net.projectmonastery.monastery.zookeeper.IntegrationTestCategory;
import net.projectmonastery.monastery.zookeeper.IntegrationTestClassMarker;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryNTimes;
import org.apache.curator.test.TestingServer;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.junit.rules.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.fest.assertions.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 
 * NodeRegistry Tester. 
 * 
 * @author Arnon Moscona 
 * @since <pre>Aug 9, 2015</pre> 
 * @version 1.0 
 */
@Category(IntegrationTestCategory.class)
public class NodeRegistryTest implements IntegrationTestClassMarker {
    private static Logger logger = LoggerFactory.getLogger(NodeRegistryTest.class);
    private static TestingServer server;
    private static String connectionString;
    private CuratorFramework cf;
    private String rootPath;
    private NodeRegistry registry;

    @Rule
    public Timeout globalTimeout = new Timeout(30_000); // 30 sec

    @BeforeClass
    public static void beforeAll() throws Exception {
        server = new TestingServer(true);
        server.start();
        connectionString = server.getConnectString();
        logger.debug("Connection string: " + connectionString);

    }

    @AfterClass
    public static void afterAll() throws Exception {
        logger.debug("shutting down...");
        server.stop();
    }
    @Before
    public void before() throws Exception {
        rootPath = "/root";
        logger.debug("Restarting server...");
        server.restart();
        logger.debug("Creating CuratorFramework");
        cf = CuratorFrameworkFactory.builder()
                .connectString(server.getConnectString())
                .connectionTimeoutMs(2000)
                .retryPolicy(new RetryNTimes(3, 500))
                .build();
        logger.debug("Waiting to connect...");
        cf.start();
        cf.blockUntilConnected();
        logger.debug("Framework ready");
        registry = new NodeRegistry(cf, rootPath);
    }

    @After
    public void after() throws Exception { 
    } 

    /** 
     * 
     * Method: makeNewNode() 
     * 
     */ 
    @Test
    public void testMakeNewNode() throws Exception {
        CompletableFuture<String> future = registry.makeNewNode();
        String id = future.get(10, TimeUnit.SECONDS);
        assertThat(id).isEqualTo("node-0000000000");

        // now a second node
        future = registry.makeNewNode();
        String id2 = future.get(10, TimeUnit.SECONDS);
        assertThat(id2).isEqualTo("node-0000000001");
    }

    /** 
     * 
     * Method: nodeExists(String nodeId) 
     * 
     */ 
    @Test
    public void testNodeExists() throws Exception {
        CompletableFuture<String> future = registry.makeNewNode();
        String id = future.get(10, TimeUnit.SECONDS);

        assertTrue(registry.nodeExists(id).get());
        assertFalse(registry.nodeExists(id+"0").get());
    }


} 
