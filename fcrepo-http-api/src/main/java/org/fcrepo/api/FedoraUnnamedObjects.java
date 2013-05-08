
package org.fcrepo.api;

import static com.google.common.collect.ImmutableList.builder;
import static org.fcrepo.utils.FedoraJcrTypes.FEDORA_OBJECT;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.util.List;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.Response;

import org.fcrepo.AbstractResource;
import org.fcrepo.exception.InvalidChecksumException;
import org.fcrepo.session.InjectedSession;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

@Component
@Scope("prototype")
@Path("/rest/{path: .*}/fcr:new")
public class FedoraUnnamedObjects extends AbstractResource {

    @InjectedSession
    private Session session;

    private static final Logger logger = getLogger(FedoraUnnamedObjects.class);

    @Autowired
    FedoraDatastreams datastreamsResource;

    @Autowired
    FedoraNodes objectsResource;

    /**
     * Create an anonymous object with a newly minted name
     * @param pathList
     * @return 201
     */
    @POST
    public Response ingestAndMint(@PathParam("path")
    final List<PathSegment> pathList) throws RepositoryException {
        logger.debug("Creating a new unnamed object");
        final String pid = pidMinter.mintPid();
        final PathSegment path = new PathSegment() {

            @Override
            public String getPath() {
                return pid;
            }

            @Override
            public MultivaluedMap<String, String> getMatrixParameters() {
                return null;
            }

        };
        final ImmutableList.Builder<PathSegment> segments = builder();
        segments.addAll(pathList.subList(0, pathList.size() - 1));
        segments.add(path);
        try {
            return objectsResource.createObject(segments.build(), "test label",
                    FEDORA_OBJECT, null, null, null, session, uriInfo, null);
        } catch (final IOException e) {
            throw new RepositoryException(e.getMessage(), e);
        } catch (final InvalidChecksumException e) {
            throw new RepositoryException(e.getMessage(), e);
        }
    }

    public void setSession(final Session session) {
        this.session = session;
    }

}
