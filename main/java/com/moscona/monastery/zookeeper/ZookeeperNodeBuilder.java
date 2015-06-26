package com.moscona.monastery.zookeeper;

import com.moscona.monastery.api.core.Capability;
import com.moscona.monastery.api.core.Node;
import com.moscona.monastery.api.core.NodeBuilder;
import org.apache.curator.framework.CuratorFrameworkFactory;
import sun.plugin.dom.exception.InvalidStateException;

/**
 * Created by Arnon Moscona on 6/17/2015.
 * A builder for Zookeeper nodes
 */
public class ZookeeperNodeBuilder implements NodeBuilder<String> {
    private CuratorFrameworkFactory curatorFrameworkFactory;

    @Override
    public NodeBuilder<String> add(Capability... capabilities) {
        return null;
    }

    @Override
    public Node<String> build() throws InvalidStateException {
        return null;
    }

}
