/**
 * The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.test.util;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.MessageDigest;
import java.security.Principal;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import javax.jcr.LoginException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;
import javax.jcr.nodetype.NodeTypeIterator;
import javax.jcr.nodetype.NodeTypeManager;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.util.EntityUtils;
import org.fcrepo.AbstractResource;
import org.fcrepo.Datastream;
import org.fcrepo.FedoraObject;
import org.fcrepo.identifiers.UUIDPidMinter;
import org.fcrepo.session.AuthenticatedSessionProvider;
import org.fcrepo.session.SessionFactory;
import org.fcrepo.utils.ContentDigest;
import org.modeshape.jcr.api.NamespaceRegistry;
import org.modeshape.jcr.api.Repository;
import org.modeshape.jcr.api.query.QueryManager;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import com.sun.jersey.api.uri.UriBuilderImpl;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.MultiPart;
import com.sun.jersey.multipart.file.StreamDataBodyPart;

/**
 * @todo Add Documentation.
 *
 * @author fasseg
 * @date May 2, 2013
 */
public abstract class TestHelpers {

    public static String MOCK_PREFIX = "mockPrefix";
    public static String MOCK_URI_STRING = "mock.namespace.org";

    /**
     * @todo Add Documentation.
     */
    public static UriInfo getUriInfoImpl() {

        final UriInfo ui = mock(UriInfo.class);
        final UriBuilder ub = new UriBuilderImpl();
        ub.scheme("http");
        ub.host("localhost");
        ub.path("/fcrepo");

        when(ui.getRequestUri()).thenReturn(
                                            URI.create("http://localhost/fcrepo"));
        when(ui.getBaseUri()).thenReturn(URI.create("http://localhost/"));
        when(ui.getBaseUriBuilder()).thenReturn(ub);
        when(ui.getAbsolutePathBuilder()).thenReturn(ub);

        return ui;
    }

    /**
     * @todo Add Documentation.
     */
    public static Session getQuerySessionMock() {

        final Session mock = mock(Session.class);
        final Workspace mockWS = mock(Workspace.class);
        final QueryManager mockQM = mock(QueryManager.class);
        try {
            final Query mockQ = getQueryMock();
            when(mockQM.createQuery(anyString(), anyString()))
                .thenReturn(mockQ);
            when(mockWS.getQueryManager()).thenReturn(mockQM);
        } catch (final RepositoryException e) {
            e.printStackTrace();
        }
        when(mock.getWorkspace()).thenReturn(mockWS);
        final ValueFactory mockVF = mock(ValueFactory.class);
        try {
            when(mock.getValueFactory()).thenReturn(mockVF);
        } catch (final RepositoryException e) {
            e.printStackTrace();
        }
        return mock;
    }

    /**
     * @todo Add Documentation.
     */
    @SuppressWarnings("unchecked")
    public static Query getQueryMock() {

        final Query mockQ = mock(Query.class);
        final QueryResult mockResults = mock(QueryResult.class);
        final NodeIterator mockNodes = mock(NodeIterator.class);
        when(mockNodes.getSize()).thenReturn(2L);
        when(mockNodes.hasNext()).thenReturn(true, true, false);
        final Node node1 = mock(Node.class);
        final Node node2 = mock(Node.class);
        try {
            when(node1.getName()).thenReturn("node1");
            when(node2.getName()).thenReturn("node2");
        } catch (final RepositoryException e) {
            e.printStackTrace();
        }
        when(mockNodes.nextNode()).thenReturn(node1, node2)
            .thenThrow(IndexOutOfBoundsException.class);
        try {
            when(mockResults.getNodes()).thenReturn(mockNodes);
            when(mockQ.execute()).thenReturn(mockResults);
        } catch (final RepositoryException e) {
            e.printStackTrace();
        }
        return mockQ;
    }

    /**
     * @todo Add Documentation.
     */
    @SuppressWarnings("unchecked")
    public static Session getSessionMock() throws RepositoryException {

        final String[] mockPrefixes = { MOCK_PREFIX };
        final Session mockSession = mock(Session.class);
        final Workspace mockWorkspace = mock(Workspace.class);
        final NamespaceRegistry mockNameReg = mock(NamespaceRegistry.class);
        final NodeTypeManager mockNTM = mock(NodeTypeManager.class);
        final NodeTypeIterator mockNTI = mock(NodeTypeIterator.class);
        final NodeType mockNodeType = mock(NodeType.class);
        when(mockSession.getWorkspace()).thenReturn(mockWorkspace);
        when(mockWorkspace.getNamespaceRegistry()).thenReturn(mockNameReg);
        when(mockNameReg.getPrefixes()).thenReturn(mockPrefixes);
        when(mockNameReg.getURI(MOCK_PREFIX)).thenReturn(MOCK_URI_STRING);
        when(mockWorkspace.getNodeTypeManager()).thenReturn(mockNTM);
        when(mockNodeType.getName()).thenReturn("mockName");
        when(mockNodeType.toString()).thenReturn("mockString");
        when(mockNTM.getAllNodeTypes()).thenReturn(mockNTI);
        when(mockNTI.hasNext()).thenReturn(true, false);
        when(mockNTI.nextNodeType()).thenReturn(mockNodeType)
            .thenThrow(ArrayIndexOutOfBoundsException.class);
        return mockSession;
    }

    /**
     * @todo Add Documentation.
     */
    public static MultiPart getStringsAsMultipart(final Map<String, String> contents) {

        final MultiPart multipart = new MultiPart();
        for (final Entry<String, String> e : contents.entrySet()) {
            final String id = e.getKey();
            final String content = e.getValue();
            final InputStream src = IOUtils.toInputStream(content);
            final StreamDataBodyPart part = new StreamDataBodyPart(id, src);
            try {
                final FormDataContentDisposition cd =
                    new FormDataContentDisposition("form-data;name=" + id +
                                                   ";filename=" + id + ".txt");
                part.contentDisposition(cd);
            } catch (final ParseException ex) {
                ex.printStackTrace();
            }
            multipart.bodyPart(part);
        }
        return multipart;
    }

    /**
     * @todo Add Documentation.
     */
    public static Collection<String> parseChildren(HttpEntity entity)
        throws IOException {

        String body = EntityUtils.toString(entity);
        System.err.println(body);
        String[] names = body.replace("[", "").replace("]", "").trim().split(",\\s?");
        return Arrays.asList(names);
    }

    /**
     * @todo Add Documentation.
     */
    public static Session mockSession(AbstractResource testObj)
        throws RepositoryException {

        SecurityContext mockSecurityContext = mock(SecurityContext.class);
        HttpServletRequest mockServletRequest = mock(HttpServletRequest.class);
        Principal mockPrincipal = mock(Principal.class);
        String mockUser = "testuser";
        Session mockSession = mock(Session.class);
        when(mockSession.getUserID()).thenReturn(mockUser);
        when(mockSecurityContext.getUserPrincipal()).thenReturn(mockPrincipal);
        when(mockPrincipal.getName()).thenReturn(mockUser);
        final SessionFactory mockSessions = mock(SessionFactory.class);
        final AuthenticatedSessionProvider mockProvider =
            mock(AuthenticatedSessionProvider.class);
        when(mockSessions.getSession()).thenReturn(mockSession);
        when(mockSessions.getSession(any(SecurityContext.class),
                                     any(HttpServletRequest.class)))
            .thenReturn(mockSession);
        when(mockProvider.getAuthenticatedSession()).thenReturn(mockSession);
        when(mockSessions.getSessionProvider(any(SecurityContext.class),
                                             any(HttpServletRequest.class)))
            .thenReturn(mockProvider);
        testObj.setSessionFactory(mockSessions);
        testObj.setUriInfo(getUriInfoImpl());
        testObj.setPidMinter(new UUIDPidMinter());
        testObj.setHttpServletRequest(mockServletRequest);
        testObj.setSecurityContext(mockSecurityContext);
        return mockSession;
    }

    /**
     * @todo Add Documentation.
     */
    public static Repository mockRepository()
        throws LoginException, RepositoryException {

        final Repository mockRepo = mock(Repository.class);
        final Session mockSession = mock(Session.class);
        when(mockRepo.login()).thenReturn(mockSession);
        return mockRepo;
    }

    /**
     * @todo Add Documentation.
     */
    public static Datastream mockDatastream(final String pid,
                                            final String dsId,
                                            final String content) {

        final Datastream mockDs = mock(Datastream.class);
        final FedoraObject mockObj = mock(FedoraObject.class);
        try {
            when(mockObj.getName()).thenReturn(pid);
            when(mockDs.getPath()).thenReturn("/" + pid + "/" + dsId);
            when(mockDs.getObject()).thenReturn(mockObj);
            when(mockDs.getDsId()).thenReturn(dsId);
            when(mockDs.getMimeType()).thenReturn("application/octet-stream");
            when(mockDs.getCreatedDate()).thenReturn(new Date());
            when(mockDs.getLastModifiedDate()).thenReturn(new Date());
            if (content != null) {
                final MessageDigest md = MessageDigest.getInstance("SHA-1");
                final byte[] digest = md.digest(content.getBytes());
                final URI cd = ContentDigest.asURI("SHA-1", digest);
                when(mockDs.getContent()).thenReturn(
                                                     IOUtils.toInputStream(content));
                when(mockDs.getContentDigest()).thenReturn(cd);
            }
        } catch (final Throwable t) {
        }
        return mockDs;
    }

    /**
     * @todo Add Documentation.
     */
    public static GraphStore parseTriples(final InputStream content) {
        final Model model = ModelFactory.createDefaultModel();
        model.read(content, "", "N3");
        return GraphStoreFactory.create(model);
    }
}
