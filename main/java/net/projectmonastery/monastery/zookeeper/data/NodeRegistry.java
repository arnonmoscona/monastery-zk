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

package net.projectmonastery.monastery.zookeeper.data;

import com.sun.xml.internal.ws.util.CompletedFuture;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Arnon Moscona on 7/23/2015.
 * This class handles the real interface to zookeeper for complying with any of the node registry conventions.
 * All operations that interact with Zookeeper and return a value must use a CompletedFuture for this purpose
 */
public class NodeRegistry {
    private final static Logger logger = LoggerFactory.getLogger(NodeRegistry.class);
    private final CuratorFramework curatorFramework;
    private final String rootPath;

    public NodeRegistry(CuratorFramework curatorFramework, String rootPath) {
        this.curatorFramework = curatorFramework;
        this.rootPath = rootPath;
        assert rootPath.startsWith("/") : "invalid root path :"+rootPath;
    }

    /**
     * Registers a node.
     * Assumes that the framework is already started, connected, and ready.
     * @return the new node ID (as a future)
     */
    public CompletableFuture<String> makeNewNode() {
        assert curatorFramework.getState() == CuratorFrameworkState.STARTED : "Curator framework not started";

        final CompletableFuture<String> future = new CompletableFuture<>();

        CompletableFuture.runAsync(()-> {
            try {
                String nodes = makeNodesRootPath();

                Stat exists =curatorFramework.checkExists()
                        .forPath(nodes);
                if (exists == null) {
                    curatorFramework.create()
                            .creatingParentsIfNeeded()
                            .withMode(CreateMode.PERSISTENT)
                            .forPath(nodes);
                }

                String node = curatorFramework.create()
                        .withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
                        .forPath(nodes+"/node-");
                logger.debug("Created node \"" + node +"\"");
                String[] path = node.split("/");
                future.complete(path[path.length-1]);
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    private String makeNodesRootPath() {
        return rootPath + "/nodes";
    }

    /**
     * checks whether the node ID exists in the registry
     * @param nodeId the node ID
     * @return a future boolean indicating whether the node exists
     */
    public CompletableFuture<Boolean> nodeExists(String nodeId) {
        CompletableFuture<Boolean> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                String fullPath = makeNodesRootPath() + "/" + nodeId;
                Stat result = curatorFramework.checkExists()
                        .forPath(fullPath);
                future.complete(result != null);
            } catch (Exception e) {
                logger.debug("Exception while testing for existence of node "+nodeId, e);
                future.completeExceptionally(e);
            }
        });

        return future;
    }
}
