package net.projectmonastery.monastery.zookeeper;

import net.projectmonastery.monastery.api.core.Capability;
import net.projectmonastery.monastery.api.core.Node;
import net.projectmonastery.monastery.zookeeper.capabilities.ZookeeperNodeAnnouncement;
import org.apache.curator.framework.CuratorFramework;
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
    private String id;
    private String rootPath;

    public ZookeeperNode(CuratorFramework curatorFramework, List<Capability> capabilities, String rootPath) {
        assert curatorFramework != null : "must provide a CuratorFramework";
        assert capabilities != null : "must provide a list of capabilities, even if empty";
        PathUtils.validatePath(rootPath);

        this.curatorFramework = curatorFramework;
        this.capabilities = new ArrayList<>(capabilities);
        this.rootPath = rootPath;
    }

    @Override
    public Optional<String> getId() {
        return Optional.ofNullable(id);
    }

    @Override
    public <T extends Capability> CompletableFuture<T> getCapability(Class<T> capabilityClass) {
        // in this case we do no network operations and the lookup is fast, so no need to throw this on a background thread...

        CompletableFuture<T> future = new CompletableFuture<>();

        for (Capability capability: capabilities) {
            if (capabilityClass.isAssignableFrom(capability.getClass())) {
                future.complete(capabilityClass.cast(capability));
                return future;
            }
        }

        future.completeExceptionally(new Exception("cannot find a capability matching "+capabilityClass.getName()));
        return future;
    }

    @Override
    public List<Capability> getCapabilities() {
        return null;
    }

    @Override
    public String getConnectionString() {
        return null;
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
}
