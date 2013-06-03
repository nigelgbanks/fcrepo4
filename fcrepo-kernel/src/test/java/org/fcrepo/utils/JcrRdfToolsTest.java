/**
 * The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also
 * available online at http://fedora-commons.org/license/).
 */

package org.fcrepo.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.slf4j.LoggerFactory.getLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.Workspace;
import javax.jcr.nodetype.NodeType;

import com.google.common.collect.Iterators;
import com.hp.hpl.jena.update.GraphStore;
import com.hp.hpl.jena.update.GraphStoreFactory;
import org.fcrepo.rdf.GraphSubjects;
import org.fcrepo.rdf.impl.DefaultGraphSubjects;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.modeshape.jcr.api.JcrConstants;
import org.modeshape.jcr.api.NamespaceRegistry;
import org.powermock.api.mockito.PowerMockito;

import com.google.common.base.Function;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import org.slf4j.Logger;

/**
 * @todo Add Documentation.
 * @author Chris Beer
 * @date May 10, 2013
 */
public class JcrRdfToolsTest {

    private static final Logger LOGGER = getLogger(JcrRdfToolsTest.class);

    private Node mockNode;

    private NamespaceRegistry mockNsRegistry;

    private GraphSubjects testSubjects;

    private Session mockSession;
    private Repository mockRepository;

    /**
     * @todo Add Documentation.
     */
    @Before
    public void setUp() throws RepositoryException {

        mockSession = mock(Session.class);
        testSubjects = new DefaultGraphSubjects();
        mockNode = mock(Node.class);
        when(mockNode.getSession()).thenReturn(mockSession);

        mockNsRegistry = mock(NamespaceRegistry.class);

        when(mockNsRegistry.isRegisteredUri("registered-uri#"))
            .thenReturn(true);
        when(mockNsRegistry.isRegisteredUri("not-registered-uri#"))
            .thenReturn(false);
        when(mockNsRegistry.isRegisteredUri("http://www.jcp.org/jcr/1.0"))
            .thenReturn(true);
        when(mockNsRegistry.getPrefix("http://www.jcp.org/jcr/1.0"))
            .thenReturn("jcr");
        when(mockNsRegistry.getPrefix("registered-uri#"))
            .thenReturn("some-prefix");
        when(mockNsRegistry.getURI("jcr"))
            .thenReturn("http://www.jcp.org/jcr/1.0");
        when(mockNsRegistry.getURI("some-prefix"))
            .thenReturn("registered-uri#");
        when(mockNsRegistry.getPrefixes())
            .thenReturn(new String[] { "jcr", "some-prefix"});

        final Workspace mockWorkspace = mock(Workspace.class);
        when(mockWorkspace.getNamespaceRegistry()).thenReturn(mockNsRegistry);

        mockRepository = mock(Repository.class);
        when(mockSession.getRepository()).thenReturn(mockRepository);
        when(mockSession.getWorkspace()).thenReturn(mockWorkspace);

        when(NamespaceTools.getNamespaceRegistry(mockSession))
            .thenReturn(mockNsRegistry);

    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldMapInternalJcrNamespaceToFcrepoNamespace() {
        final String jcrNamespace = "http://www.jcp.org/jcr/1.0";
        assertEquals("info:fedora/fedora-system:def/internal#",
                     JcrRdfTools.getRDFNamespaceForJcrNamespace(jcrNamespace));
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldMapFcrepoNamespaceToJcrNamespace() {
        final String rdfNamespace = "info:fedora/fedora-system:def/internal#";
        assertEquals("http://www.jcp.org/jcr/1.0",
                     JcrRdfTools.getJcrNamespaceForRDFNamespace(rdfNamespace));
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldPassThroughOtherNamespaceValues() {
        assertEquals("some-namespace-uri", JcrRdfTools
                     .getJcrNamespaceForRDFNamespace("some-namespace-uri"));
        assertEquals("some-namespace-uri", JcrRdfTools
                     .getRDFNamespaceForJcrNamespace("some-namespace-uri"));
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldMapRdfPredicatesToJcrProperties()
        throws RepositoryException {

        final Property p = ResourceFactory
            .createProperty("info:fedora/fedora-system:def/internal#", "uuid");
        assertEquals("jcr:uuid",
                     JcrRdfTools.getPropertyNameFromPredicate(mockNode, p));

    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldReuseRegisteredNamespaces() throws RepositoryException {
        final Property p =
            ResourceFactory.createProperty("registered-uri#", "uuid");
        assertEquals("some-prefix:uuid", JcrRdfTools
                     .getPropertyNameFromPredicate(mockNode, p));
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldRegisterUnknownUris() throws RepositoryException {
        when(mockNsRegistry.registerNamespace("not-registered-uri#"))
            .thenReturn("ns001");
        final Property p =
            ResourceFactory.createProperty("not-registered-uri#", "uuid");
        assertEquals("ns001:uuid",
                     JcrRdfTools.getPropertyNameFromPredicate(mockNode, p));
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldMapJcrNodeNamestoRDFResources()
        throws RepositoryException {
        when(mockNode.getPath()).thenReturn("/abc");

        assertEquals("info:fedora/abc",
                     JcrRdfTools.getGraphSubject(testSubjects, mockNode)
                     .toString());
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldMapJcrContentNodeNamestoRDFResourcesIntheFcrNamespace()
        throws RepositoryException {
        when(mockNode.getPath()).thenReturn("/abc/jcr:content");

        assertEquals("info:fedora/abc/fcr:content", JcrRdfTools
                     .getGraphSubject(testSubjects, mockNode).toString());
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldMapRDFResourcesToJcrNodes() throws RepositoryException {
        when(mockSession.nodeExists("/abc")).thenReturn(true);
        when(mockSession.getNode("/abc")).thenReturn(mockNode);
        final Resource resource =
            ResourceFactory.createResource("info:fedora/abc");
        assertEquals(mockNode, JcrRdfTools.getNodeFromGraphSubject(testSubjects,
                                                                   mockSession,
                                                                   resource));
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldMapRDFContentResourcesToJcrContentNodes()
        throws RepositoryException {
        when(mockSession.nodeExists("/abc/jcr:content")).thenReturn(true);
        when(mockSession.getNode("/abc/jcr:content")).thenReturn(mockNode);
        final Resource resource =
            ResourceFactory.createResource("info:fedora/abc/fcr:content");
        assertEquals(mockNode, JcrRdfTools.getNodeFromGraphSubject(testSubjects,
                                                                   mockSession,
                                                                   resource));
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldReturnNullIfItFailstoMapRDFResourcesToJcrNodes()
        throws RepositoryException {
        when(mockSession.nodeExists("/does-not-exist")).thenReturn(false);
        Resource resource =
            ResourceFactory.createResource("this-is-not-a-fedora-node/abc");
        assertNull("should receive null for a non-JCR resource",
                   JcrRdfTools.getNodeFromGraphSubject(testSubjects,
                                                       mockSession,
                                                       resource));
        resource =
            ResourceFactory.createResource("info:fedora/does-not-exist");
        assertNull("should receive null a JCR node that isn't found",
                   JcrRdfTools.getNodeFromGraphSubject(testSubjects,
                                                       mockSession,
                                                       resource));
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void testGetPropertiesModel() throws RepositoryException {
        final String fakeInternalUri = "info:fedora/test/jcr";
        final Node mockParent = mock(Node.class);
        when(mockParent.getPath()).thenReturn("/test");
        when(mockNode.getPath()).thenReturn("/test/jcr");
        when(mockNode.getParent()).thenReturn(mockParent);
        final NodeType nodeType = mock(NodeType.class);
        when(mockNode.getPrimaryNodeType()).thenReturn(nodeType);
        when(mockNode.getPrimaryNodeType().getName()).thenReturn("");
        when(mockNode.getPath()).thenReturn("/test/jcr");
        final javax.jcr.NodeIterator mockNodes =
            mock(javax.jcr.NodeIterator.class);
        when(mockNode.getNodes()).thenReturn(mockNodes);
        PowerMockito.mockStatic(NamespaceTools.class);
        final NamespaceRegistry mockNames = mock(NamespaceRegistry.class);
        final String[] testPrefixes = new String[] {"jcr"};
        when(mockNames.getPrefixes()).thenReturn(testPrefixes);
        when(mockNames.getURI("jcr")).thenReturn(fakeInternalUri);
        when(NamespaceTools.getNamespaceRegistry(mockNode))
            .thenReturn(mockNames);
        final PropertyIterator mockProperties =
            mock(PropertyIterator.class);
        when(mockNode.getProperties()).thenReturn(mockProperties);
        when(mockParent.getProperties()).thenReturn(mockProperties);
        when(mockProperties.hasNext()).thenReturn(false);

        final Model actual =
            JcrRdfTools.getJcrPropertiesModel(testSubjects, mockNode);
        assertEquals(fakeInternalUri, actual.getNsPrefixURI("fedora-internal"));

        //TODO exercise the jcr:content child node logic
        //TODO exercise non-empty PropertyIterator
    }


    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldExcludeBinaryProperties() throws RepositoryException {
        String fakeInternalUri = "info:fedora/test/jcr";
        Node mockParent = mock(Node.class);
        when(mockParent.getPath()).thenReturn("/test");
        when(mockNode.getPath()).thenReturn("/test/jcr");
        when(mockNode.getParent()).thenReturn(mockParent);
        when(mockNode.getDepth()).thenReturn(2);
        final NodeType nodeType = mock(NodeType.class);
        when(mockNode.getPrimaryNodeType()).thenReturn(nodeType);
        when(mockNode.getPrimaryNodeType().getName())
            .thenReturn("fedora:object");
        when(mockNode.getPath()).thenReturn("/test/jcr");
        javax.jcr.NodeIterator mockNodes = mock(javax.jcr.NodeIterator.class);
        when(mockNode.getNodes()).thenReturn(mockNodes);
        PowerMockito.mockStatic(NamespaceTools.class);
        NamespaceRegistry mockNames = mock(NamespaceRegistry.class);
        String[] testPrefixes = new String[]{"jcr"};
        when(mockNames.getPrefixes()).thenReturn(testPrefixes);
        when(mockNames.getURI("jcr")).thenReturn(fakeInternalUri);
        when(NamespaceTools.getNamespaceRegistry(mockNode))
            .thenReturn(mockNames);
        PropertyIterator mockProperties = mock(PropertyIterator.class);
        when(mockNode.getProperties()).thenReturn(mockProperties);
        when(mockParent.getProperties()).thenReturn(mockProperties);
        when(mockProperties.hasNext()).thenReturn(true, false);
        javax.jcr.Property mockProperty = mock(javax.jcr.Property.class);
        Value mockValue = mock(Value.class);
        when(mockProperty.getValue()).thenReturn(mockValue);
        when(mockValue.getType()).thenReturn(PropertyType.BINARY);
        when(mockProperties.nextProperty()).thenReturn(mockProperty);

        Model actual = JcrRdfTools
            .getJcrPropertiesModel(testSubjects, mockNode);

        // we expect 2 statements, both auto-generated
        assertEquals(2, actual.size());
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldMapRdfValuesToJcrPropertyValues()
        throws RepositoryException {
        final ValueFactory mockValueFactory = mock(ValueFactory.class);

        @SuppressWarnings("unchecked")
        final Function<Node, ValueFactory> mockValueFactoryFunc =
            mock(Function.class);
        when(mockValueFactoryFunc.apply(mockNode)).thenReturn(mockValueFactory);

        final Function<Node, ValueFactory> holdValueFactory =
            FedoraTypesUtils.getValueFactory;
        FedoraTypesUtils.getValueFactory = mockValueFactoryFunc;

        try {
            RDFNode n = ResourceFactory.createResource("info:fedora/abc");

            // node references
            when(mockSession.getNode("/abc")).thenReturn(mockNode);
            JcrRdfTools.createValue(mockNode, n, PropertyType.REFERENCE);
            JcrRdfTools.createValue(mockNode, n, PropertyType.WEAKREFERENCE);
            verify(mockValueFactory, times(2)).createValue(mockNode);

            // uris
            JcrRdfTools.createValue(mockNode, n, PropertyType.UNDEFINED);
            verify(mockValueFactory).createValue("info:fedora/abc",
                                                 PropertyType.URI);

            // other random resources
            n = ResourceFactory.createResource();
            JcrRdfTools.createValue(mockNode, n, 0);
            verify(mockValueFactory).createValue(n.toString(),
                                                 PropertyType.UNDEFINED);

            // undeclared types, but infer them from rdf types

            n = ResourceFactory.createTypedLiteral(true);
            JcrRdfTools.createValue(mockNode, n, 0);
            verify(mockValueFactory).createValue(true);

            n = ResourceFactory.createTypedLiteral((byte) 1);
            JcrRdfTools.createValue(mockNode, n, 0);
            verify(mockValueFactory).createValue((byte) 1);

            n = ResourceFactory.createTypedLiteral((double) 2);
            JcrRdfTools.createValue(mockNode, n, 0);
            verify(mockValueFactory).createValue((double) 2);

            n = ResourceFactory.createTypedLiteral((float) 3);
            JcrRdfTools.createValue(mockNode, n, 0);
            verify(mockValueFactory).createValue((float) 3);

            n = ResourceFactory.createTypedLiteral(4);
            JcrRdfTools.createValue(mockNode, n, 0);
            verify(mockValueFactory).createValue(4);

            n = ResourceFactory.createTypedLiteral((long) 5);
            JcrRdfTools.createValue(mockNode, n, 0);
            verify(mockValueFactory).createValue(5);

            n = ResourceFactory.createTypedLiteral((short) 6);
            JcrRdfTools.createValue(mockNode, n, 0);
            verify(mockValueFactory).createValue((short) 6);

            final Calendar calendar = Calendar.getInstance();
            n = ResourceFactory.createTypedLiteral(calendar);
            JcrRdfTools.createValue(mockNode, n, 0);
            verify(mockValueFactory).createValue(any(Calendar.class));

            n = ResourceFactory.createTypedLiteral("string");
            JcrRdfTools.createValue(mockNode, n, 0);
            verify(mockValueFactory).createValue("string", PropertyType.STRING);

            n = ResourceFactory.createTypedLiteral("string");
            JcrRdfTools.createValue(mockNode, n, PropertyType.NAME);
            verify(mockValueFactory).createValue("string", PropertyType.NAME);

        } finally {
            FedoraTypesUtils.getValueFactory = holdValueFactory;
        }

    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldAddPropertiesToModel() throws RepositoryException {
        final javax.jcr.Property mockProperty = mock(javax.jcr.Property.class);
        final Property mockPredicate = mock(Property.class);

        @SuppressWarnings("unchecked")
        final Function<javax.jcr.Property, com.hp.hpl.jena.rdf.model.Property>
            mockPredicateFactoryFunc = mock(Function.class);

        when(mockPredicateFactoryFunc.apply(mockProperty))
            .thenReturn(mockPredicate);

        final Function<javax.jcr.Property, com.hp.hpl.jena.rdf.model.Property>
            holdPredicate = FedoraTypesUtils.getPredicateForProperty;
        FedoraTypesUtils.getPredicateForProperty = mockPredicateFactoryFunc;

        try {
            final Resource mockSubject = mock(Resource.class);
            final Model mockModel = mock(Model.class);

            final Value mockValue = mock(Value.class);
            when(mockValue.getString()).thenReturn("");

            when(mockProperty.isMultiple()).thenReturn(false);
            when(mockProperty.getValue()).thenReturn(mockValue);

            JcrRdfTools
                .addPropertyToModel(mockSubject, mockModel, mockProperty);
            verify(mockModel).add(mockSubject, mockPredicate, "");

        } finally {
            FedoraTypesUtils.getPredicateForProperty = holdPredicate;
        }

    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldAddMultivaluedPropertiesToModel()
        throws RepositoryException {
        final javax.jcr.Property mockProperty = mock(javax.jcr.Property.class);
        final Property mockPredicate = mock(Property.class);

        @SuppressWarnings("unchecked")
        final Function<javax.jcr.Property, com.hp.hpl.jena.rdf.model.Property>
            mockPredicateFactoryFunc = mock(Function.class);

        when(mockPredicateFactoryFunc.apply(mockProperty))
            .thenReturn(mockPredicate);

        final Function<javax.jcr.Property, com.hp.hpl.jena.rdf.model.Property>
            holdPredicate = FedoraTypesUtils.getPredicateForProperty;
        FedoraTypesUtils.getPredicateForProperty = mockPredicateFactoryFunc;

        try {
            final Resource mockSubject = mock(Resource.class);
            final Model mockModel = mock(Model.class);

            final Value mockValue = mock(Value.class);
            when(mockValue.getString()).thenReturn("1");

            final Value mockValue2 = mock(Value.class);
            when(mockValue2.getString()).thenReturn("2");

            when(mockProperty.isMultiple()).thenReturn(true);
            when(mockProperty.getValues())
                .thenReturn(Arrays.asList(mockValue, mockValue2)
                            .toArray(new Value[2]));

            JcrRdfTools
                .addPropertyToModel(mockSubject, mockModel, mockProperty);
            verify(mockModel).add(mockSubject, mockPredicate, "1");
            verify(mockModel).add(mockSubject, mockPredicate, "2");

        } finally {
            FedoraTypesUtils.getPredicateForProperty = holdPredicate;
        }

    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldMapJcrTypesToRdfDataTypes() throws RepositoryException {
        final javax.jcr.Property mockProperty = mock(javax.jcr.Property.class);
        final Property mockPredicate = mock(Property.class);

        @SuppressWarnings("unchecked")
        final Function<javax.jcr.Property, com.hp.hpl.jena.rdf.model.Property>
            mockPredicateFactoryFunc = mock(Function.class);

        when(mockPredicateFactoryFunc.apply(mockProperty))
            .thenReturn(mockPredicate);

        final Function<javax.jcr.Property, com.hp.hpl.jena.rdf.model.Property>
            holdPredicate = FedoraTypesUtils.getPredicateForProperty;
        FedoraTypesUtils.getPredicateForProperty = mockPredicateFactoryFunc;

        try {
            final Resource mockSubject = mock(Resource.class);
            final Model mockModel = mock(Model.class);

            Value mockValue = mock(Value.class);
            when(mockValue.getType()).thenReturn(PropertyType.URI);
            when(mockValue.getString()).thenReturn("info:fedora");
            when(mockModel.createResource("info:fedora"))
                .thenReturn(ResourceFactory.createResource("info:fedora"));

            JcrRdfTools.addPropertyToModel(mockSubject, mockModel,
                                           mockProperty, mockValue);
            verify(mockModel)
                .add(mockSubject, mockPredicate,
                     ResourceFactory.createResource("info:fedora"));

            mockValue = mock(Value.class);
            when(mockProperty.getSession()).thenReturn(mockSession);
            when(mockSession.getNodeByIdentifier("uuid")).thenReturn(mockNode);
            when(mockNode.getPath()).thenReturn("/abc");
            when(mockModel.createResource("info:fedora/abc"))
                .thenReturn(ResourceFactory.createResource("info:fedora/abc"));

            when(mockValue.getType()).thenReturn(PropertyType.REFERENCE);
            when(mockValue.getString()).thenReturn("uuid");
            JcrRdfTools.addPropertyToModel(mockSubject, mockModel,
                                           mockProperty, mockValue);

            // verify(mockModel).add(mockSubject, mockPredicate,
            // ResourceFactory.createResource("info:fedora/abc"));

            mockValue = mock(Value.class);
            when(mockValue.getType()).thenReturn(PropertyType.WEAKREFERENCE);
            when(mockValue.getString()).thenReturn("uuid");
            JcrRdfTools.addPropertyToModel(mockSubject, mockModel,
                                           mockProperty, mockValue);

            // verify(mockModel).add(mockSubject, mockPredicate,
            // ResourceFactory.createResource("info:fedora/abc"));

            mockValue = mock(Value.class);
            when(mockValue.getType()).thenReturn(PropertyType.PATH);
            when(mockValue.getString()).thenReturn("/abc");
            JcrRdfTools.addPropertyToModel(mockSubject, mockModel,
                                           mockProperty, mockValue);

            verify(mockModel, times(3))
                .add(mockSubject, mockPredicate,
                     ResourceFactory.createResource("info:fedora/abc"));

        } finally {
            FedoraTypesUtils.getPredicateForProperty = holdPredicate;
        }

    }

    /**
     * @todo Add Documentation.
     */
    @Test
    @Ignore
    public void testJcrNodeContent() throws RepositoryException {

        final NodeType nodeType = mock(NodeType.class);
        when(mockNode.getPrimaryNodeType()).thenReturn(nodeType);
        when(mockNode.getPrimaryNodeType().getName()).thenReturn("");

        PropertyIterator mockProperties = mock(PropertyIterator.class);
        when(mockProperties.hasNext()).thenReturn(false);
        when(mockNode.getProperties()).thenReturn(mockProperties);

        when(mockNode.getPath()).thenReturn("/path/to/node");
        NodeIterator mockIterator = mock(NodeIterator.class);
        when(mockIterator.hasNext()).thenReturn(false);
        when(mockNode.getNodes()).thenReturn(mockIterator);

        Node mockContent = mock(Node.class);
        when(mockContent.getPath()).thenReturn("/path/to/node/content");
        when(mockContent.getProperties()).thenReturn(mockProperties);
        when(mockContent.getSession()).thenReturn(mockSession);

        when(mockNode.hasNode(JcrConstants.JCR_CONTENT)).thenReturn(true);
        when(mockNode.getNode(JcrConstants.JCR_CONTENT))
            .thenReturn(mockContent);
        Model model = JcrRdfTools.getJcrPropertiesModel(testSubjects, mockNode);

        assertTrue(model != null);
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void testJcrNodeIteratorModel() throws RepositoryException {

        Resource mockResource = mock(Resource.class);
        NodeIterator mockIterator = mock(NodeIterator.class);
        when(mockIterator.hasNext()).thenReturn(false);
        final Model model =
            JcrRdfTools.getJcrNodeIteratorModel(testSubjects,
                                                mockIterator,
                                                mockResource);
        assertTrue(model != null);
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void testJcrNodeIteratorAddsPredicatesForEachNode()
        throws RepositoryException {
        Resource mockResource =
            ResourceFactory.createResource("info:fedora/search/resource");
        Node mockNode1 = mock(Node.class);
        Node mockNode2 = mock(Node.class);
        Node mockNode3 = mock(Node.class);
        PropertyIterator mockProperties = mock(PropertyIterator.class);
        when(mockProperties.hasNext()).thenReturn(false);
        when(mockNode1.getProperties()).thenReturn(mockProperties);
        when(mockNode1.getSession()).thenReturn(mockSession);

        when(mockNode1.getPath()).thenReturn("/path/to/first/node");
        when(mockNode2.getPath()).thenReturn("/second/path/to/node");
        when(mockNode3.getPath()).thenReturn("/third/path/to/node");
        when(mockNode1.getProperties()).thenReturn(mockProperties);
        when(mockNode2.getProperties()).thenReturn(mockProperties);
        when(mockNode3.getProperties()).thenReturn(mockProperties);

        Iterator<Node> mockIterator =
            Arrays.asList(mockNode1, mockNode2, mockNode3).iterator();
        final Model model =
            JcrRdfTools.getJcrNodeIteratorModel(testSubjects,
                                                mockIterator,
                                                mockResource);
        Property property =
            ResourceFactory.createProperty("info:fedora/iterator#hasNode");
        assertEquals(3, model.listObjectsOfProperty(property).toSet().size());
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void testGetFixityResultsModel()
        throws RepositoryException, URISyntaxException {
        LowLevelCacheEntry mockEntry = mock(LowLevelCacheEntry.class);
        when(mockEntry.getExternalIdentifier()).thenReturn("xyz");
        final FixityResult mockResult =
            new FixityResult(mockEntry, 123, new URI("abc"));
        mockResult.status.add(FixityResult.FixityState.BAD_CHECKSUM);
        mockResult.status.add(FixityResult.FixityState.BAD_SIZE);

        final List<FixityResult> mockBlobs = Arrays.asList(mockResult);
        when(mockNode.getPath()).thenReturn("/path/to/node");

        PropertyIterator mockProperties = mock(PropertyIterator.class);
        when(mockProperties.hasNext()).thenReturn(false);
        when(mockNode.getProperties()).thenReturn(mockProperties);

        final Model fixityResultsModel =
            JcrRdfTools.getFixityResultsModel(testSubjects,
                                              mockNode,
                                              mockBlobs);

        LOGGER.info("Got graph {}", fixityResultsModel);

        GraphStore gs =  GraphStoreFactory.create(fixityResultsModel);
        assertTrue(gs.contains(com.hp.hpl.jena.graph.Node.ANY,
                               com.hp.hpl.jena.graph.Node.ANY,
                               ResourceFactory
                               .createProperty("info:fedora/fedora-system:def" +
                                               "/internal#isFixityResultOf")
                               .asNode(),
                               ResourceFactory.createResource("info:fedora/" +
                                                              "path/to/node")
                               .asNode()));
        assertTrue(gs.contains(com.hp.hpl.jena.graph.Node.ANY,
                               com.hp.hpl.jena.graph.Node.ANY,
                               ResourceFactory
                               .createProperty("info:fedora/fedora-system:def" +
                                               "/internal#computedChecksum")
                               .asNode(),
                               ResourceFactory.createResource("abc").asNode()));
        assertTrue(gs.contains(com.hp.hpl.jena.graph.Node.ANY,
                               com.hp.hpl.jena.graph.Node.ANY,
                               ResourceFactory
                               .createProperty("info:fedora/fedora-system:def" +
                                               "/internal#computedSize")
                               .asNode(),
                               ResourceFactory.createTypedLiteral(123)
                               .asNode()));

    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void testGetJcrNamespaceModel() throws Exception {
        final Model jcrNamespaceModel =
            JcrRdfTools.getJcrNamespaceModel(mockSession);

        Resource resource =
            ResourceFactory.createResource("info:fedora/fedora-system:def/" +
                                           "internal#");
        Property property =
            ResourceFactory.createProperty(JcrRdfTools.HAS_NAMESPACE_PREDICATE);
        assertTrue(jcrNamespaceModel.contains(resource, property,
                                              "fedora-internal"));

        resource = ResourceFactory.createResource("registered-uri#");
        property =
            ResourceFactory.createProperty(JcrRdfTools.HAS_NAMESPACE_PREDICATE);
        assertTrue(jcrNamespaceModel.contains(resource, property,
                                              "some-prefix"));
    }
}
