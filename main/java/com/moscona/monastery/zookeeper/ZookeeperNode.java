package com.moscona.monastery.zookeeper;

import com.moscona.monastery.api.core.Capability;
import com.moscona.monastery.api.core.Node;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Created by Arnon Moscona on 6/17/2015.
 * A Zookeeper implementation of a node.
 */
public class ZookeeperNode implements Node<String> {
    @Override
    public Optional<String> getId() {
        return null;
    }

    @Override
    public <T extends Capability> CompletableFuture<T> getCapability(Class<T> aClass) {
        return null;
    }

    @Override
    public List<Capability> getCapabilities() {
        return null;
    }

    @Override
    public String getConnectionString() {
        return null;
    }
}
