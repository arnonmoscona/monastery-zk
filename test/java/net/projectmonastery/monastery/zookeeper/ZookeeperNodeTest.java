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

import net.projectmonastery.monastery.api.core.Capability;
import net.projectmonastery.monastery.api.core.Node;
import net.projectmonastery.monastery.cando.NodeAnnouncement;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.apache.curator.test.TestingServer;
import org.junit.*;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;
import static org.fest.assertions.api.Assertions.*;
import static org.mockito.Mockito.*;

/** 
* ZookeeperNode Tester. 
* 
* @author Arnon Moscona 
* @since <pre>Jul 13, 2015</pre> 
* @version 1.0 
*/
@Category(IntegrationTestCategory.class)
public class ZookeeperNodeTest implements IntegrationTestClassMarker {
    public static final int MS_BETWEEN_RETRY = 100;
    private static Logger logger = LoggerFactory.getLogger(ZookeeperNodeBuilderTest.class);
    private static TestingServer server;
    private static String connectionString;
    private static ZookeeperNodeBuilder builder;
    private static ZookeeperNode node;
    private static CuratorFramework cf;

    /**
     * Only need one test server instance (expensive to start, and no state between tests in this class)
     * @throws Exception
     */
    @BeforeClass
    public static void beforeAll() throws Exception {
        server = new TestingServer(true);
        connectionString = server.getConnectString();
        logger.debug("Connection string: " + connectionString);

        // OK, starting and stopping and connecting on every test is overkill for this test class,
        // as the node is (nearly) immutable and there is no point is waiting so long before each test
        prepareForTest();
    }

    @AfterClass
    public static void afterAll() throws Exception {
        cf.close();
        server.stop();
        logger.debug("shutting down...");
    }

    @Before
    public void before() throws Exception {
    }

    private static void prepareForTest() throws Exception {
        server.start();
        cf = CuratorFrameworkFactory.newClient(connectionString, new RetryOneTime(MS_BETWEEN_RETRY));
        cf.start();
        cf.blockUntilConnected(5000, TimeUnit.MILLISECONDS);
        builder = new ZookeeperNodeBuilder()
            .withCuratorFramework(cf)
        .withConnectionTimeoutMillis(-1); // block until connected
        node = (ZookeeperNode) builder.build();
        assertThat(node).isNotNull();
    }

    @After
    public void after() throws Exception {
    }

    /** 
     * 
     * Method: getId() should not be present until announced
     * 
     */ 
    @Test
    public void testGetId() throws Exception { 
        assertThat(node.getId().isPresent()).isFalse();
    } 

    /** 
     * 
     * Method: getCapability(Class<T> aClass) 
     * 
     */ 
    @Test
    public void testGetCapability() throws Exception {
        CompletableFuture<NodeAnnouncement> future = node.getCapability(NodeAnnouncement.class);
        assertThat(future).isNotNull();
        NodeAnnouncement result = future.get(1000, TimeUnit.MILLISECONDS); // synchronous is OK in test, besides we expect no network operations in this implementation
        assertThat(result).isNotNull();
    } 

    /** 
     * 
     * Method: getCapabilities() 
     * 
     */ 
    @Test
    public void testGetCapabilities() throws Exception {
        List<Capability> capabilities = node.getCapabilities();
        assertThat(capabilities).isNotNull();
        assertThat(capabilities.size()).isGreaterThanOrEqualTo(1);
        assertThat(capabilities.get(0)).isNotNull();
    } 

    /** 
     * 
     * Method: getConnectionString() 
     * 
     */ 
    @Test
    public void testGetConnectionString() throws Exception { 
        assertThat(node.getConnectionString()).isEqualTo(connectionString);
    } 

    /** 
     * 
     * Method: getCuratorFramework() 
     * 
     */ 
    @Test
    public void testGetCuratorFramework() throws Exception { 
        assertThat(node.getCuratorFramework()).isEqualTo(cf);
    } 

    /** 
     * 
     * Method: prependCapability(Capability capability) 
     * 
     */ 
    @Test
    public void testPrependCapability() throws Exception { 
        Capability nullCapability = new Capability() {
            @Override
            public void bind(Node<?> context) {
                // do nothing
            }
        };
        node.prependCapability(nullCapability);
        Capability firstCapability = node.getCapabilities().get(0);
        assertThat(firstCapability).isEqualTo(nullCapability);
    }

    @Test
    public void testGetRootPath() throws Exception {
        assertThat(node.getRootPath()).isEqualTo(ZookeeperNodeBuilder.DEFAULT_ROOT_PATH);
    }

    @Test
    public void testCapabilityBinding() throws Exception {
        Capability cap1 = mock(Capability.class);
        when(cap1.isReady()).thenReturn(true);

        builder = (ZookeeperNodeBuilder) new ZookeeperNodeBuilder()
                .withCuratorFramework(cf)
                .withConnectionTimeoutMillis(-1) // block until connected
                .withCapability(cap1);
        node = (ZookeeperNode) builder.build();
        assertThat(node).isNotNull();

        verify(cap1, times(1)).bind(node);
        verify(cap1, times(1)).onAllCapabilitiesBound();
        verify(cap1, times(1)).isReady();
    }

    @Test(expected = AssertionError.class)
    public void testBadCapabilityBinding() throws Exception {
        Capability cap1 = mock(Capability.class);
        when(cap1.isReady()).thenReturn(false); // the capability reports that it is not ready

        builder = (ZookeeperNodeBuilder) new ZookeeperNodeBuilder()
                .withCuratorFramework(cf)
                .withConnectionTimeoutMillis(-1) // block until connected
                .withCapability(cap1);
        node = (ZookeeperNode) builder.build();
        assertThat(node).isNotNull();

        verify(cap1, times(1)).bind(node);
        verify(cap1, times(1)).onAllCapabilitiesBound();
        verify(cap1, times(1)).isReady();
    }
}
