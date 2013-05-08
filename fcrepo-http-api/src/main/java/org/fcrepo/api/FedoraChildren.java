
package org.fcrepo.api;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.MediaType.TEXT_HTML;
import static javax.ws.rs.core.MediaType.TEXT_XML;
import static javax.ws.rs.core.Response.ok;
import static org.fcrepo.utils.FedoraJcrTypes.FEDORA_DATASTREAM;
import static org.fcrepo.utils.FedoraJcrTypes.FEDORA_OBJECT;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import org.fcrepo.AbstractResource;
import org.fcrepo.session.InjectedSession;
import org.fcrepo.services.ObjectService;
import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.codahale.metrics.annotation.Timed;

@Component
@Scope("prototype")
@Path("/rest/{path: .*}/fcr:children")
public class FedoraChildren extends AbstractResource {

    @InjectedSession
    private Session session;

    private static final Logger logger = getLogger(FedoraChildren.class);

    /**
     * Returns a list of the first-generation descendants of an object,
     * filtered by an optional mixin parameter
     *
     * @param pathList
     * @param mixin
     * @return 200
     * @throws javax.jcr.RepositoryException
     * @throws java.io.IOException
     */
    @GET
    @Timed
    @Produces({TEXT_XML, APPLICATION_JSON, TEXT_HTML})
    public Response getObjects(@PathParam("path")
    final List<PathSegment> pathList, @QueryParam("mixin")
    @DefaultValue("")
    String mixin) throws RepositoryException, IOException {
        try {
            final String path = toPath(pathList);
            logger.debug("Entering getObjects() with path: {} and mixin: {}",
                    path, mixin);
            if (mixin.isEmpty()) {
                logger.trace("No mixin specified.");
                mixin = null;
            } else {
                switch (mixin) {
                    case FEDORA_OBJECT:
                        mixin = "nt:folder";
                        break;
                    case FEDORA_DATASTREAM:
                        mixin = "nt:file";
                        break;
                    default:
                        /* accept mixin as given */
                }
                logger.trace("Searching for children with mixin: {}", mixin);
            }

            return ok(
                    objectService.getObjectNames(session, path, mixin)
                            .toString()).build();
        } finally {
            session.logout();
        }
    }

    public ObjectService getObjectService() {
        return objectService;
    }

    public void setObjectService(final ObjectService objectService) {
        this.objectService = objectService;
    }

    public void setSession(final Session session) {
        this.session = session;
    }

}
