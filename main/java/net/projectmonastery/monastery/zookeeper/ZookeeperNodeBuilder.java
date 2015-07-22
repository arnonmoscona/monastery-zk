package net.projectmonastery.monastery.zookeeper;

import net.projectmonastery.monastery.api.core.Capability;
import net.projectmonastery.monastery.api.core.Node;
import net.projectmonastery.monastery.api.core.NodeBuilder;
import net.projectmonastery.monastery.zookeeper.capabilities.ZookeeperNodeAnnouncement;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.framework.state.ConnectionStateListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.common.PathUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Arnon Moscona on 6/17/2015.
 * A builder for Zookeeper nodes
 */
public class ZookeeperNodeBuilder implements NodeBuilder<String> {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperNodeBuilder.class);
    /**
     * The default path to the root znode for the cluster
     */
    public static final String DEFAULT_ROOT_PATH = "/net.projectmonastery.monastery.root";
    public static final int DEFAULT_CONNECTION_TIMEOUT_MILLIS = 60000;
    private ArrayList<Capability> capabilities;

    /**
     * if given then we're using a ready-made Curator framework, and this will override any defaults, even if somehow set
     */
    private CuratorFramework appCuratorFramework;
    /**
     * If given then this builder will be used to create the Curator framework.
     * Cannot coexist with a given framework
     */
    private CuratorFrameworkFactory.Builder frameBuilder;
    private String connectionString;
    private String rootPath = DEFAULT_ROOT_PATH;
    private int connectionTimeout = DEFAULT_CONNECTION_TIMEOUT_MILLIS;

    public ZookeeperNodeBuilder() {
        capabilities = new ArrayList<>();
    }

    @Override
    public NodeBuilder<String> add(Capability... capabilities) throws Exception {
        List<Capability> list = Arrays.asList(capabilities);
        if (list.contains(null)) {
            throw new Exception("Null capabilities not allowed");
        }
        this.capabilities.addAll(list);
        return this;
    }

    /**
     * @return a list of capabilities currently added (copy of internal state)
     */
    public List<Capability> getCapabilities() {
        return new ArrayList<>(capabilities);
    }

    @Override
    public Node<String> build() throws Exception {
        validateConflicts();
        logger.debug("passed conflict validation");
        CuratorFramework cf = appCuratorFramework;
        if (cf == null) {
            throw new Exception("No CuratorFramework provided, and not enough information to create a default.");
        }
        logger.debug("checking framework state");
        CuratorFrameworkState state = cf.getState();
        logger.debug("Got state: " + state);
        if (!state.equals(CuratorFrameworkState.STARTED)) {
            startFramework(cf, connectionTimeout);
        }
        ZookeeperNode node = new ZookeeperNode(cf, connectionString, capabilities, rootPath);
        node.prependCapability(new ZookeeperNodeAnnouncement(node));
        return node;
    }

    private void startFramework(CuratorFramework cf, int timeoutMillis) throws Exception {
        if (cf.getState().equals(CuratorFrameworkState.STARTED)) {
            return;
        }

        logger.info("Connecting with timeout "+timeoutMillis+" millis");
        CountDownLatch latch = new CountDownLatch(1); // this allows blocking until connected
        AtomicReference<ConnectionState> lastConnectionState = new AtomicReference<>();

        cf.getConnectionStateListenable().addListener(new ConnectionStateListener() {
            @Override
            public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {
                lastConnectionState.set(connectionState);
                if (connectionState.equals(ConnectionState.CONNECTED)) {
                    latch.countDown();
                }
            }
        });
        cf.start(); // this should connect us

        if (!cf.getZookeeperClient().isConnected()) { // maybe already connected by now
            if (connectionTimeout <= 0) {
                logger.debug("blocking indefinitely until connected");
                cf.blockUntilConnected();
            } else {
                logger.debug("blocking with timeout of " + timeoutMillis + " millis");
                cf.blockUntilConnected(timeoutMillis, TimeUnit.MILLISECONDS);
            }
        }

        if (!cf.getZookeeperClient().isConnected()) {
            throw new Exception("Failed to connect to Zookeeper. Framework state: " + cf.getState() +
            " connection state: "+lastConnectionState.get());
        }

        if (!cf.getState().equals(CuratorFrameworkState.STARTED)) {
            throw new Exception("framework state is not STARTED after connecting. Got state: "
            + cf.getState());
        }
    }

    /**
     * Allows callers to provide a ready-made CuratorFramework
     * (e.g. with any fancy authentication and ACL providers etc.)
     * Very useful if you have other uses of Zookeeper and want to use a single framework for the entire application.
     * @param cf the CuratorFramework to use.
     */
    public ZookeeperNodeBuilder withCuratorFramework(CuratorFramework cf) throws Exception {
        appCuratorFramework = cf;
        validateConflicts();
        initFieldsFromFramework(appCuratorFramework);
        return this;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public ZookeeperNodeBuilder withCuratorFrameworkBuilder(CuratorFrameworkFactory.Builder builder) throws Exception {
        frameBuilder = builder;
        validateConflicts();
        return this;
    }

    private void validateConflicts() throws Exception {
        if (appCuratorFramework != null && frameBuilder != null) {
            throw new Exception("Cannot accept both a framework and a framework builder. Decide on one");
        }
    }

    private void initFieldsFromFramework(CuratorFramework cf) {
        connectionString = cf.getZookeeperClient().getCurrentConnectionString();
    }

    public ZookeeperNodeBuilder withRootPath(String rootPath) {
        PathUtils.validatePath(rootPath);
        this.rootPath = rootPath;
        return this;
    }

    public ZookeeperNodeBuilder withConnectionTimeoutMillis(int timeout) {
        connectionTimeout = timeout;
        return this;
    }
}
