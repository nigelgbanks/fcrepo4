
package org.fcrepo.generator;

import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.Response.ok;
import static org.fcrepo.services.ObjectService.getObjectNode;

import java.io.InputStream;
import java.util.List;

import javax.annotation.Resource;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.fcrepo.AbstractResource;
import org.fcrepo.generator.dublincore.DCGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Named
@Path("/rest/objects/{pid}/oai_dc")
public class DublinCoreGenerator extends AbstractResource {
	
	
	private static final Logger logger = LoggerFactory.getLogger(DublinCoreGenerator.class);
	
    @Resource
    List<DCGenerator> dcgenerators;

    @GET
    @Produces(TEXT_XML)
    public Response getObjectAsDublinCore(@PathParam("pid")
    final String pid) throws RepositoryException {
    	logger.debug("fetching dc record for pid: " + pid);
        final Node obj = getObjectNode(pid);

        for (DCGenerator indexer : dcgenerators) {
            InputStream inputStream = indexer.getStream(obj);

            if (inputStream != null) {
                return ok(inputStream).build();
            }
        }
        // no indexers = no path for DC
        throw new PathNotFoundException();

    }

}
