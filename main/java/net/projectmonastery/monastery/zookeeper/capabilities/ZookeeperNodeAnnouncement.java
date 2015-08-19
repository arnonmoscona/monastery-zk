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

package net.projectmonastery.monastery.zookeeper.capabilities;

import net.projectmonastery.monastery.api.core.Node;
import net.projectmonastery.monastery.cando.NodeAnnouncement;
import net.projectmonastery.monastery.cando.NodeState;
import net.projectmonastery.monastery.zookeeper.ZookeeperNode;
import net.projectmonastery.monastery.zookeeper.data.NodeRegistry;
import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
    private ArrayList<Consumer<Node<String>>> joinListeners;

    public ZookeeperNodeAnnouncement() {
        joinListeners = new ArrayList<>();
    }

    public ZookeeperNodeAnnouncement(ZookeeperNode node) {
        this();
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

    public void setState(NodeState state) {
        parentNode.setState(state);
    }

    @Override
    public NodeState getState() {
        return parentNode.getState();
    }

    @Override
    public CompletableFuture<NodeAnnouncement<String>> announce() {
        assert parentNode.getState().equals(NodeState.DISCONNECTED) : "may not announce a node unless it is in a DISCONNECTED state";

        CompletableFuture<NodeAnnouncement<String>> future = new CompletableFuture<>(); // this is what we will return
        CompletableFuture.runAsync(() -> { // this task will complete the future
            registry.makeNewNode().handle((id, throwable) -> {
                if (throwable != null) {
                    logger.error("Trouble completing the future: " + throwable.getMessage(), throwable);
                    future.completeExceptionally(throwable);
                    return null;
                }
                else {
                    ZookeeperNodeAnnouncement.this.setId(id);
                    ZookeeperNodeAnnouncement.this.setState(NodeState.ANNOUNCED); // this is done only for the benefit of listeners
                    ZookeeperNodeAnnouncement.this.setState(NodeState.JOINED);
                    ZookeeperNodeAnnouncement.this.invokeJoinListeners();

                    logger.debug("Completing the announcement process");
                    future.complete(ZookeeperNodeAnnouncement.this);
                    logger.debug("Completed announcement");
                    return id;
                }
            });
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
        joinListeners.add(consumer);
        return this;
    }

    private void invokeJoinListeners() {
        if (joinListeners == null || joinListeners.size() == 0) {
            return;
        }
        joinListeners.forEach(listener -> listener.accept(parentNode));
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
