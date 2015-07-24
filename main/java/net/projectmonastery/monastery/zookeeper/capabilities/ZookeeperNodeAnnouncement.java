package net.projectmonastery.monastery.zookeeper.capabilities;

import net.projectmonastery.monastery.api.core.Node;
import net.projectmonastery.monastery.cando.NodeAnnouncement;
import net.projectmonastery.monastery.cando.NodeState;
import net.projectmonastery.monastery.zookeeper.ZookeeperNode;
import net.projectmonastery.monastery.zookeeper.data.NodeRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * Created by Arnon Moscona on 7/13/2015.
 * A node announcement capability for the Zookeeper implementation
 */
public class ZookeeperNodeAnnouncement implements NodeAnnouncement<String> {
    private Logger logger = LoggerFactory.getLogger(ZookeeperNodeAnnouncement.class);
    private CuratorFramework curatorFramework;
    private ZookeeperNode parentNode;
    private NodeRegistry registry;

    public ZookeeperNodeAnnouncement() {

    }

    public ZookeeperNodeAnnouncement(ZookeeperNode node) {
        bind(node);
    }

    /**
     * Extra method to ease mocking
     * @return a new node registry
     */
    NodeRegistry makeRegistry() {
        assert parentNode!= null : "makeRegistry may not be called before the node is bound";
        return new NodeRegistry(curatorFramework, parentNode.getRootPath());
    }

    @Override
    public NodeState getState() {
        return NodeState.DISCONNECTED;
    }

    @Override
    public CompletableFuture<NodeAnnouncement<String>> announce() {
        CompletableFuture<NodeAnnouncement<String>> future = new CompletableFuture<>(); // this is what we will return
        CompletableFuture.runAsync(()->{ // this task will complete the future
            try {
                String newId = registry.makeNewNode();
                ZookeeperNodeAnnouncement.this.setId(newId);

                logger.debug("Completing the announcement process");
                future.complete(ZookeeperNodeAnnouncement.this);
                logger.debug("Completed announcement");
            } catch (Exception e) {
                logger.error("Trouble completing the future: "+e.getMessage(), e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @Override
    public Optional<String> getId() {
        assert parentNode != null : "cannot call getId() before binding a node";
        return parentNode.getId();
    }

    void setId(String newId) {
        assert ! parentNode.getId().isPresent() : "The parent node already has an ID. Will not change it";
        parentNode.setId(newId);
    }

    @Override
    public Optional<Node> getBoundNode() {
        return Optional.ofNullable(parentNode);
    }

    @Override
    public NodeAnnouncement<String> addJoinListener(Consumer<Node<String>> consumer) {
        return null;
    }

    @Override
    public void bind(Node<?> node) {
        assert node != null: "may not create this capability without a node";
        assert ZookeeperNode.class.isAssignableFrom(node.getClass()) : "bound node must be a ZookeeperNode";
        parentNode = (ZookeeperNode) node;
        curatorFramework = parentNode.getCuratorFramework();
        assert curatorFramework != null: "node has a null curator framework";
        registry = makeRegistry();
    }
}
