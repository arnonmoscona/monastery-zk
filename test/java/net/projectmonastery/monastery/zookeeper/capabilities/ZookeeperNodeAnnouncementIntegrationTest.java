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

import net.projectmonastery.monastery.zookeeper.IntegrationTestCategory;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import org.junit.experimental.categories.Category;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.*;
import static org.fest.assertions.api.Assertions.*;

import net.projectmonastery.monastery.zookeeper.IntegrationTestClassMarker;

/**
 * Created by Arnon Moscona on 7/23/2015.
 * Integration tests for the node announcement capability
 */
@Category(IntegrationTestCategory.class)
public class ZookeeperNodeAnnouncementIntegrationTest implements IntegrationTestClassMarker {
    @Test
    public void reminder() throws Exception {
        throw new Exception("still need an integration test (with a real zookeeper and a real node registry)");
    }
}
