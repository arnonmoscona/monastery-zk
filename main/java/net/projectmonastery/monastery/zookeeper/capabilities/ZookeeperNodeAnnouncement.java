package net.projectmonastery.monastery.zookeeper.capabilities;

import net.projectmonastery.monastery.api.core.Node;
import net.projectmonastery.monastery.cando.NodeAnnouncement;
import net.projectmonastery.monastery.cando.NodeState;
import net.projectmonastery.monastery.zookeeper.ZookeeperNode;
import org.apache.curator.framework.CuratorFramework;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Created by Arnon Moscona on 7/13/2015.
 * A node announcement capability for the Zookeeper implementation
 */
public class ZookeeperNodeAnnouncement implements NodeAnnouncement<String> {
    private CuratorFramework curatorFramework;
    private ZookeeperNode parentNode;

    public ZookeeperNodeAnnouncement(ZookeeperNode node) {
        assert node != null: "may not create this capability without a node";
        parentNode = node;
        curatorFramework = node.getCuratorFramework();
        assert curatorFramework != null: "node has a null curator framework";
    }

    @Override
    public NodeState getState() {
        return NodeState.DISCONNECTED;
    }

    @Override
    public CompletableFuture<NodeAnnouncement<String>> announce() {
        return null;
    }

    @Override
    public Optional<String> getId() {
        return null;
    }

    @Override
    public Optional<Node> getBoundNode() {
        return null;
    }

    @Override
    public NodeAnnouncement<String> addJoinListener(Consumer<Node<String>> consumer) {
        return null;
    }
}
