
package org.fcrepo.api;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.PathSegment;
import javax.ws.rs.core.StreamingOutput;

import org.fcrepo.AbstractResource;
import org.fcrepo.serialization.FedoraObjectSerializer;
import org.fcrepo.session.InjectedSession;
import org.slf4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Path("/rest/{path: .*}/fcr:export")
public class FedoraExport extends AbstractResource {

    @InjectedSession
    private Session session;

    @Resource
    private Map<String, FedoraObjectSerializer> serializers;

    private final Logger logger = getLogger(this.getClass());

    @GET
    public StreamingOutput exportObject(@PathParam("path")
    final List<PathSegment> pathList, @QueryParam("format")
    @DefaultValue("jcr/xml")
    final String format) {
        final String path = toPath(pathList);

        logger.debug("Requested object serialization for: " + path +
                " using serialization format " + format);

        return new StreamingOutput() {

            @Override
            public void write(final OutputStream out) throws IOException {
                try {
                    logger.debug("Selecting from serializer map: " +
                            serializers);
                    final FedoraObjectSerializer serializer =
                            serializers.get(format);
                    logger.debug("Retrieved serializer for format: " + format);
                    serializer.serialize(
                            objectService.getObject(session, path), out);
                    logger.debug("Successfully serialized object: " + path);
                } catch (final RepositoryException e) {
                    throw new WebApplicationException(e);
                } finally {
                    session.logout();
                }
            }
        };
    }

    public void setSerializers(
            final Map<String, FedoraObjectSerializer> serializers) {
        this.serializers = serializers;
    }

    public void setSession(final Session session) {
        this.session = session;
    }
}
