package net.projectmonastery.monastery.zookeeper.capabilities;

import net.projectmonastery.monastery.api.core.Node;
import net.projectmonastery.monastery.cando.NodeAnnouncement;
import net.projectmonastery.monastery.cando.NodeState;
import net.projectmonastery.monastery.zookeeper.ZookeeperNode;
import net.projectmonastery.monastery.zookeeper.data.NodeRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.mockito.InOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static org.fest.assertions.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * ZookeeperNodeAnnouncement Tester.
 *
 * @author Arnon Moscona
 * @version 1.0
 *          <p>
 *          This is a </i>unit test only</>. Not an integration test. The integration test is separate
 *          ZookeeperNodeAnnouncementIntegrationTest, no actual nodes is used.
 *          </p>
 * @since <pre>Jul 13, 2015</pre>
 */
public class ZookeeperNodeAnnouncementTest {
    private static final String ID1 = "0000000001";
    private static final String ID2 = "0000000002";
    private static Logger logger = LoggerFactory.getLogger(ZookeeperNodeAnnouncementTest.class);
    private ZookeeperNode node;
    private NodeRegistry registry;
    private CuratorFramework cf;


    @Before
    public void before() throws Exception {
        cf = mock(CuratorFramework.class);

        node = mock(ZookeeperNode.class); // a new mock node
        when(node.getCuratorFramework()).thenReturn(cf);
        when(node.getId()).thenReturn(Optional.ofNullable(null)).thenReturn(Optional.of("0000000001"));
        when(node.getRootPath()).thenReturn("/net.projectmonastery");

        registry = mock(NodeRegistry.class);  // a new mock registry
        when(registry.makeNewNode())
                .thenReturn(ID1)
                .thenReturn(ID2);
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: getState()
     */
    @Test
    public void testGetStateBeforeAnnouncement() throws Exception {
        when(node.getState()).thenReturn(NodeState.DISCONNECTED);
        ZookeeperNodeAnnouncement announcement = new ZookeeperNodeAnnouncement(node);
        assertThat(announcement.getState()).isEqualTo(NodeState.DISCONNECTED);
    }

    /**
     * Method: announce(), getId()
     */
    @Test
    public void testAnnounce() throws Exception {
        ZookeeperNodeAnnouncement announcement = spy(new ZookeeperNodeAnnouncement());
        // inject the mock registry. Note that this mocking works only if you bind later
        doReturn(registry)
                .when(announcement)
                .makeRegistry();
        when(node.getState()).thenReturn(NodeState.DISCONNECTED);
        InOrder nodeInOrder = inOrder(node);

        announcement.bind(node);


        // now we can start testing
        CompletableFuture<NodeAnnouncement<String>> future = announcement.announce();
        assertThat(future).isNotNull();
        ZookeeperNodeAnnouncement retval = (ZookeeperNodeAnnouncement) future.get(1, TimeUnit.SECONDS);
        assertThat(retval).isEqualTo(announcement);
        Optional<String> id = retval.getId();
        assertThat(id).isNotNull();
        assertThat(id.isPresent());
        assertThat(id.get()).isEqualTo(ID1);
        verify(registry, times(1)).makeNewNode();
        nodeInOrder.verify(node, times(1)).setState(NodeState.ANNOUNCED);
        nodeInOrder.verify(node, times(1)).setState(NodeState.JOINED);
    }

    /**
     * Method: announce(), getId()
     */
    @Test(expected = AssertionError.class)
    public void testAnnounceNodeTwice() throws Exception {
        ZookeeperNodeAnnouncement announcement = spy(new ZookeeperNodeAnnouncement());
        // inject the mock registry. Note that this mocking works only if you bind later
        doReturn(registry)
                .when(announcement)
                .makeRegistry();
        when(node.getState()).thenReturn(NodeState.DISCONNECTED); // the node is a mock...

        announcement.bind(node);


        // now we can start testing
        CompletableFuture<NodeAnnouncement<String>> future = announcement.announce();
        assertThat(future).isNotNull();
        ZookeeperNodeAnnouncement retval = (ZookeeperNodeAnnouncement) future.get(1, TimeUnit.SECONDS);
        assertThat(retval).isEqualTo(announcement);
        verify(node, times(1)).setState(NodeState.JOINED);

        // first announcement done. Now let's try to re-announce the node and see that it is not allowed
        when(node.getState()).thenReturn(NodeState.JOINED); // the node is a mock...
        announcement.announce(); // should throw exception
    }

    /**
     * Method: getBoundNode()
     */
    @Test
    public void testGetBoundNode() throws Exception {
        ZookeeperNodeAnnouncement announcement = new ZookeeperNodeAnnouncement(node);
        Optional<Node> boundNode = announcement.getBoundNode();
        assertThat(boundNode).isNotNull();
        assertThat(boundNode.isPresent());
        assertThat(announcement.getBoundNode().get()).isEqualTo(node);
    }

    /**
     * Method: addJoinListener(Consumer<Node<String>> consumer)
     */
    @Test
    public void testAddJoinListener() throws Exception {
        ZookeeperNodeAnnouncement announcement = spy(new ZookeeperNodeAnnouncement());
        @SuppressWarnings("unchecked")
        Consumer<Node<String>> listener = mock(Consumer.class);
        announcement.addJoinListener(listener);
        // inject the mock registry. Note that this mocking works only if you bind later
        doReturn(registry)
                .when(announcement)
                .makeRegistry();
        when(node.getState()).thenReturn(NodeState.DISCONNECTED); // the node is a mock...

        announcement.bind(node);


        // now we can start testing
        CompletableFuture<NodeAnnouncement<String>> future = announcement.announce();
        assertThat(future).isNotNull();
        ZookeeperNodeAnnouncement retval = (ZookeeperNodeAnnouncement) future.get(1, TimeUnit.SECONDS);
        assertThat(retval).isEqualTo(announcement);
        verify(node, times(1)).setState(NodeState.JOINED);

        verify(listener, times(1)).accept(any(ZookeeperNode.class));
    }

    /**
     * Method: addJoinListener(Consumer<Node<String>> consumer)
     */
    @Test
    public void testBind() throws Exception {
        ZookeeperNodeAnnouncement announcement = new ZookeeperNodeAnnouncement();
        assertThat(announcement.getBoundNode()).isNotNull();
        assertThat(announcement.getBoundNode().isPresent()).isFalse();

        announcement.bind(node);

        assertThat(announcement.getBoundNode()).isNotNull();
        assertThat(announcement.getBoundNode().isPresent()).isTrue();
        assertThat(announcement.getBoundNode().get()).isSameAs(node);
    }


} 
