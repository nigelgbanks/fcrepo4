/**
 * The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also
 * available online at http://fedora-commons.org/license/).
 */

package org.fcrepo.integration.services;

import static org.jgroups.util.Util.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;

import javax.inject.Inject;
import javax.jcr.NamespaceRegistry;
import javax.jcr.Repository;
import javax.jcr.Session;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.UpdateAction;
import org.fcrepo.integration.AbstractIT;
import org.fcrepo.services.DatastreamService;
import org.fcrepo.services.ObjectService;
import org.fcrepo.utils.JcrRdfTools;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

/**
 * @todo Add Documentation.
 * @author Chris Beer
 * @date Mar 22, 2013
 */
@ContextConfiguration({"/spring-test/repo.xml"})
public class ObjectServiceIT extends AbstractIT {

    @Inject
    private Repository repository;

    @Inject
    ObjectService objectService;

    @Inject
    DatastreamService datastreamService;

    /**
     * @todo Add Documentation.
     */
    @Test
    public void testGetAllObjectsDatastreamSize() throws Exception {
        Session session = repository.login();

        final double originalSize = objectService.getRepositorySize();

        datastreamService.createDatastreamNode(session,
                "testObjectServiceNode", "application/octet-stream",
                new ByteArrayInputStream("asdf".getBytes()));
        session.save();
        session.logout();

        session = repository.login();

        final double afterSize = objectService.getRepositorySize();

        assertEquals(4.0, afterSize - originalSize);

        session.logout();
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void testGetNamespaceRegistryGraph() throws Exception {
        Session session = repository.login();

        final Dataset registryGraph =
            objectService.getNamespaceRegistryGraph(session);

        final NamespaceRegistry namespaceRegistry =
            session.getWorkspace().getNamespaceRegistry();

        logger.info(namespaceRegistry.toString());
        logger.info(registryGraph.toString());
        for (String s : namespaceRegistry.getPrefixes()) {
            if (s.isEmpty() || s.equals("xmlns") || s.equals("jcr")) {
                continue;
            }
            final String uri = namespaceRegistry.getURI(s);
            assertTrue("expected to find JCR namespaces " + s + " in graph",
                       registryGraph.asDatasetGraph()
                       .contains(Node.ANY,
                                 ResourceFactory.createResource(uri).asNode(),
                                 ResourceFactory
                                 .createProperty(JcrRdfTools
                                                 .HAS_NAMESPACE_PREDICATE)
                                 .asNode(),
                                 ResourceFactory.createPlainLiteral(s).asNode()));
        }
        session.logout();
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void testUpdateNamespaceRegistryGraph() throws Exception {
        Session session = repository.login();

        final Dataset registryGraph =
            objectService.getNamespaceRegistryGraph(session);
        final NamespaceRegistry namespaceRegistry =
            session.getWorkspace().getNamespaceRegistry();

        UpdateAction
            .parseExecute("INSERT { <info:abc> <" +
                          JcrRdfTools.HAS_NAMESPACE_PREDICATE +
                          "> \"abc\" } WHERE { }", registryGraph);

        assertEquals("abc", namespaceRegistry.getPrefix("info:abc"));
        session.logout();
    }
}
