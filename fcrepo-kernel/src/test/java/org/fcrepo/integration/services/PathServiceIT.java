
package org.fcrepo.integration.services;

import org.fcrepo.services.PathService;
import org.junit.Test;

import static org.fcrepo.services.PathService.getDatastreamJcrNodePath;
import static org.fcrepo.services.PathService.getObjectJcrNodePath;
import static org.fcrepo.services.PathService.OBJECT_PATH;
import static org.junit.Assert.assertEquals;

public class PathServiceIT {

    @Test
    public void testGetObjectJcrNodePath() throws Exception {
        new PathService();
        assertEquals(OBJECT_PATH + "/test:123", getObjectJcrNodePath("test:123"));
    }

    @Test
    public void testGetDatastreamJcrNodePath() throws Exception {
        new PathService();
        assertEquals(OBJECT_PATH + "/test:123/asdf", getDatastreamJcrNodePath(
                "test:123", "asdf"));
    }
}
