package net.projectmonastery.monastery.zookeeper.data;

import org.apache.curator.framework.CuratorFramework;

/**
 * Created by Arnon Moscona on 7/23/2015.
 * This class handles the real interface to zookeeper for complying with any of the node registry conventions
 */
public class NodeRegistry {
    private final CuratorFramework curatorFramework;
    private final String rootPath;

    public NodeRegistry(CuratorFramework curatorFramework, String rootPath) {
        this.curatorFramework = curatorFramework;
        this.rootPath = rootPath;
    }

    /**
     * Registers a node
     * @return the new node ID
     * @throws Exception
     */
    public String makeNewNode() throws Exception {
        throw new Exception("makeNewNode() not implemented");
    }
}
