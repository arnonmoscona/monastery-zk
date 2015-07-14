package net.projectmonastery.monastery.zookeeper;

import net.projectmonastery.monastery.api.core.Capability;
import net.projectmonastery.monastery.cando.NodeAnnouncement;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;
import org.junit.Test;
import org.junit.Before; 
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.AfterClass;

import static org.fest.assertions.api.Assertions.*;
import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/** 
* ZookeeperNodeBuilder Tester. 
* 
* @author Arnon Moscona 
* @since <pre>Jun 20, 2015</pre> 
* @version 1.0 
*/ 
public class ZookeeperNodeBuilderTest {
    public static final int MS_BETWEEN_RETRY = 100;
    static Logger logger = LoggerFactory.getLogger(ZookeeperNodeBuilderTest.class);
    private static TestingServer server;
    private static String connectionString;

    /**
     * Only need one test server instance (expensive to start, and no state between tests in this class)
     * @throws Exception
     */
    @BeforeClass
    public static void beforeAll() throws Exception {
        server = new TestingServer(true);
        connectionString = server.getConnectString();
        logger.debug("Connection string: " + connectionString);

    }

    @AfterClass
    public static void afterAll() throws Exception {
        logger.debug("shutting down...");
    }

    @Before
    public void before() throws Exception {
        server.start();
    }

    @After
    public void after() throws Exception {
        server.stop();
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * The builder can be used with an already set-up Curator framework
     * @throws Exception
     */
    @Test
    public void shouldAcceptReadyMadeCuratorFramework() throws Exception {
        CuratorFramework cf = CuratorFrameworkFactory.newClient(connectionString, new RetryOneTime(MS_BETWEEN_RETRY));
        ZookeeperNodeBuilder builder = new ZookeeperNodeBuilder().
                withCuratorFramework(cf);
        assertThat(builder.getConnectionString()).isEqualTo(connectionString);
    }

    /**
     * The builder can be used with a curator framework factory
     * @throws Exception
     */
    @Test
    public void shouldAcceptCuratorFrameworkFactoryBuilder() throws Exception {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        builder.connectString(connectionString);
        ZookeeperNodeBuilder nodeBuilder = new ZookeeperNodeBuilder().
            withCuratorFrameworkBuilder(builder);
//        assertThat(nodeBuilder.getConnectionString()).isEqualTo(connectionString); // no way to get the connection string from the factory
    }

    /**
     * The builder does not allow to mix a user given curator framework as well as a curator framework factory
     * @throws Exception
     */
    @Test(expected = Exception.class)
    public void shouldNotAcceptFrameworkBuilderIfHasFramework() throws Exception {
        CuratorFramework cf = CuratorFrameworkFactory.newClient(connectionString, new RetryOneTime(MS_BETWEEN_RETRY));
        CuratorFrameworkFactory.Builder framewrokBuilder = CuratorFrameworkFactory.builder();
        ZookeeperNodeBuilder builder = new ZookeeperNodeBuilder().
                withCuratorFramework(cf).
                withCuratorFrameworkBuilder(framewrokBuilder);
    }

    /**
     * The builder does not allow to mix a user given curator framework as well as a curator framework factory
     * @throws Exception
     */
    @Test(expected = Exception.class)
    public void shouldNotAcceptFrameworkIfHasFrameworkBuilder() throws Exception {
        CuratorFramework cf = CuratorFrameworkFactory.newClient(connectionString, new RetryOneTime(MS_BETWEEN_RETRY));
        CuratorFrameworkFactory.Builder framewrokBuilder = CuratorFrameworkFactory.builder();
        ZookeeperNodeBuilder builder = new ZookeeperNodeBuilder().
                withCuratorFrameworkBuilder(framewrokBuilder).
                withCuratorFramework(cf);
    }

    /**
     * If neither a Curator framework nor a framework factory is given, the builder can still manage by creating a
     * default framework factory, provided that sufficient data is provided.
     * @throws Exception
     */
    @Test
    public void shouldCreateDefaultFrameworkFactory() throws Exception {
        throw new Exception("missing test for default framework factory before build");

    }

    /** 
     * 
     * Method: add(Capability... capabilities) 
     * 
     */ 
    @Test
    public void testAdd() throws Exception {
        Capability cap1 = new MockCapability();
        Capability cap2 = new MockCapability();
        ZookeeperNodeBuilder builder = (ZookeeperNodeBuilder) new ZookeeperNodeBuilder()
                .add(cap1, cap2);
        assertThat(builder.getCapabilities()).contains(cap1);
        assertThat(builder.getCapabilities()).contains(cap2);
    }

    /**
     *
     * Method: add(Capability... capabilities) : nulls not allowed
     *
     */
    @Test(expected = Exception.class)
    public void testAddNull() throws Exception {
        Capability cap1 = new MockCapability();
        ZookeeperNodeBuilder builder = (ZookeeperNodeBuilder) new ZookeeperNodeBuilder()
                .withCapabilities(cap1, null);
    }

    /** 
     * 
     * Method: build() 
     * 
     */ 
    @Test
    public void testBuild() throws Exception {
        CuratorFramework cf = CuratorFrameworkFactory.newClient(connectionString, new RetryOneTime(MS_BETWEEN_RETRY));
        ZookeeperNodeBuilder builder = new ZookeeperNodeBuilder().
                withCuratorFramework(cf);
        ZookeeperNode node = (ZookeeperNode) builder.build();
        assertThat(node).isNotNull();
        assertThat(node.getId().isPresent()).isFalse(); // node ot announced yet
        CompletableFuture<NodeAnnouncement> future = node.getCapability(NodeAnnouncement.class);
        assertThat(future).isNotNull();
        NodeAnnouncement result = future.get(1000, TimeUnit.MILLISECONDS); // synchronous is OK in test, besides we expect no network operations in this implementation
        assertThat(result).isNotNull();
    }

    public static class MockCapability implements Capability {

    }
} 
