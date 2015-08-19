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
import net.projectmonastery.monastery.cando.NodeState;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.common.PathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Arnon Moscona on 6/17/2015.
 * A Zookeeper implementation of a node.
 */
public class ZookeeperNode implements Node<String> {
    private final CuratorFramework curatorFramework;
    private final ArrayList<Capability> capabilities;
    private final String connectionString;
    private String id;
    private String rootPath;
    private NodeState state = NodeState.DISCONNECTED;

    public ZookeeperNode(CuratorFramework curatorFramework, String connectionString, List<Capability> capabilities, String rootPath) {
        assert curatorFramework != null : "must provide a CuratorFramework";
        assert connectionString != null : "must have a connection string";
        assert capabilities != null : "must provide a list of capabilities, even if empty";
        PathUtils.validatePath(rootPath);
        assert curatorFramework.getState().equals(CuratorFrameworkState.STARTED) : "cannot create node with a framework that is not started";

        this.curatorFramework = curatorFramework;
        this.capabilities = new ArrayList<>(capabilities);
        this.rootPath = rootPath;
//        this.connectionString = connectionString;
        this.connectionString = curatorFramework.getZookeeperClient().getCurrentConnectionString();
        state = NodeState.DISCONNECTED;
        bindAllCapabilities();
        resolveCapabilityDependencies();
        validateCapabilityState();
    }

    /**
     * binds all capabilities to this instance
     */
    private void bindAllCapabilities() {
        capabilities.forEach(capability -> capability.bind(this));
    }

    /**
     * After all capabilities are bound, let them know so they can find their dependencies and resolve
     */
    private void resolveCapabilityDependencies() {
        capabilities.forEach(Capability::onAllCapabilitiesBound);
    }

    private void validateCapabilityState() {
        capabilities.forEach(capability -> {
            String name = capability.getClass().getSimpleName();
            assert capability.isReady() : "after binding all capabilities "+name+"is not ready";
        });
    }

    @Override
    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    @Override
    public <T extends Capability> CompletableFuture<T> getCapability(Class<T> capabilityClass) {
        // in this case we do no network operations and the lookup is fast, so no need to throw this on a background thread...

        CompletableFuture<T> future = new CompletableFuture<>(); // we cannot use supplyAsync because we want to complete exceptionally

        for (Capability capability: capabilities) {
            if (capabilityClass.isAssignableFrom(capability.getClass())) {
                future.complete(capabilityClass.cast(capability));
                return future;
            }
        }

        future.completeExceptionally(new Exception("cannot find a capability matching " + capabilityClass.getName()));
        return future;
    }

    @Override
    public List<Capability> getCapabilities() {
        return new ArrayList<>(capabilities);
    }

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    public CuratorFramework getCuratorFramework() {
        return curatorFramework;
    }

    /**
     * Used strictly by the builder to prepend Zookeeper implementation capabilities
     * @param capability the capability to prepend to the list
     */
    void prependCapability(Capability capability) {
        assert capability != null : "cannot prepend a null capability";
        ArrayList<Capability> newCapabilities = new ArrayList<>();
        newCapabilities.add(capability);
        newCapabilities.addAll(capabilities);
        capabilities.clear();
        capabilities.addAll(newCapabilities);
    }

    public String getRootPath() {
        return rootPath;
    }

    /**
     * This is part of the implementation and should not be used outside the implementation code
     * @param newId the new ID to assign to the node (write once)
     */
    public void setId(String newId) {
        assert id == null : "Node already has an ID. May not be changed.";
        id = newId;
    }

    /**
     * To be used only by Monastery implementation classes
     * @param state the new state
     */
    public void setState(NodeState state) {
        this.state = state==null?NodeState.DISCONNECTED:state;
    }

    /**
     * @return the node state with respect to announcement
     */
    public NodeState getState() {
        return state;
    }
}
