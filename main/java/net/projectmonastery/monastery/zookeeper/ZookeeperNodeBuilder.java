package net.projectmonastery.monastery.zookeeper;

import net.projectmonastery.monastery.api.core.Capability;
import net.projectmonastery.monastery.api.core.Node;
import net.projectmonastery.monastery.api.core.NodeBuilder;
import net.projectmonastery.monastery.zookeeper.capabilities.ZookeeperNodeAnnouncement;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.zookeeper.common.PathUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private CuratorFrameworkFactory curatorFrameworkFactory;
    private ArrayList<Capability> capabilities;

    /**
     * if given then we're using a ready-made Curator framework, and this will override any defaults, even if somehow set
     */
    private CuratorFramework appCuratorFramework;
    /**
     * If given then this builder will be used to create the Curator framework.
     * Cannot coexist with a given framework
     */
    CuratorFrameworkFactory.Builder frameBuilder;
    private String connectionString;
    private String rootPath = DEFAULT_ROOT_PATH;

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
        ZookeeperNode node = new ZookeeperNode(cf, capabilities, rootPath);
        node.prependCapability(new ZookeeperNodeAnnouncement(node));
        return node;
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
}
