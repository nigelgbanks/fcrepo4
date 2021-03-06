/**
 * Copyright 2013 DuraSpace, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.fcrepo.kernel.rdf.impl.mappings;

import static com.google.common.collect.Iterators.singletonIterator;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createProperty;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createResource;
import static com.hp.hpl.jena.rdf.model.ResourceFactory.createTypedLiteral;
import static javax.jcr.PropertyType.BOOLEAN;
import static javax.jcr.PropertyType.DATE;
import static javax.jcr.PropertyType.DECIMAL;
import static javax.jcr.PropertyType.DOUBLE;
import static javax.jcr.PropertyType.LONG;
import static javax.jcr.PropertyType.PATH;
import static javax.jcr.PropertyType.REFERENCE;
import static javax.jcr.PropertyType.STRING;
import static javax.jcr.PropertyType.URI;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.slf4j.LoggerFactory.getLogger;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Iterator;

import javax.jcr.AccessDeniedException;
import javax.jcr.ItemNotFoundException;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFormatException;

import org.fcrepo.kernel.rdf.GraphSubjects;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.slf4j.Logger;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class PropertyToTripleTest {

    // for mocks and setup gear see after tests

    @Test
    public void testMultiValuedLiteralTriple() throws RepositoryException {

        when(mockProperty.isMultiple()).thenReturn(true);
        when(mockProperty.getType()).thenReturn(STRING);
        when(mockProperty.getValues()).thenReturn(
                new Value[] {mockValue, mockValue2});
        when(mockValue.getString()).thenReturn(TEST_VALUE);
        when(mockValue.getType()).thenReturn(STRING);
        when(mockValue2.getString()).thenReturn(TEST_VALUE);
        when(mockValue2.getType()).thenReturn(STRING);
        final Function<Iterator<Value>, Iterator<Triple>> mapping =
            testPropertyToTriple.apply(mockProperty);
        final Iterator<Triple> ts =
            mapping.apply(twoValueIterator(mockValue, mockValue2));
        final Triple t1 = ts.next();
        LOGGER.debug("Constructed triple: {}", t1);
        final Triple t2 = ts.next();
        LOGGER.debug("Constructed triple: {}", t2);

        assertEquals("Got wrong RDF object!", TEST_VALUE, t1.getObject()
                .getLiteralValue());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t1.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t1
                .getSubject());

        assertEquals("Got wrong RDF object!", TEST_VALUE, t2.getObject()
                .getLiteralValue());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t2.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t2
                .getSubject());
    }

    @Test
    public void testSingleValuedResourceTriple() throws RepositoryException {

        when(mockProperty.isMultiple()).thenReturn(false);
        when(mockProperty.getType()).thenReturn(PATH);
        when(mockProperty.getString()).thenReturn(TEST_NODE_PATH);
        when(mockProperty.getValue()).thenReturn(mockValue);
        when(mockValue.getString()).thenReturn(TEST_NODE_PATH);
        when(mockValue.getType()).thenReturn(PATH);
        final Function<Iterator<Value>, Iterator<Triple>> mapping =
            testPropertyToTriple.apply(mockProperty);
        final Triple t = mapping.apply(singletonIterator(mockValue)).next();
        LOGGER.debug("Constructed triple: {}", t);
        assertEquals("Got wrong RDF object!", TEST_NODE_SUBJECT.asNode(), t
                .getObject());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t
                .getSubject());
    }

    @Test
    public void testSingleValuedStringLiteralTriple()
                                                     throws RepositoryException {

        when(mockProperty.getType()).thenReturn(STRING);
        when(mockValue.getType()).thenReturn(STRING);
        when(mockValue.getString()).thenReturn(TEST_VALUE);
        final Triple t = createSingleValuedLiteralTriple();
        assertEquals("Got wrong RDF object!", TEST_VALUE, t.getObject()
                .getLiteralValue());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t
                .getSubject());
    }

    @Test
    public void
            testSingleValuedBooleanLiteralTriple() throws RepositoryException {

        when(mockProperty.getType()).thenReturn(BOOLEAN);
        when(mockValue.getType()).thenReturn(BOOLEAN);
        when(mockValue.getString()).thenReturn("true");
        final Triple t = createSingleValuedLiteralTriple();
        assertEquals("Got wrong RDF object!", "true", t.getObject()
                .getLiteralValue());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t
                .getSubject());
    }

    @Test
    public void testSingleValuedDateLiteralTriple() throws RepositoryException {

        final Calendar date = Calendar.getInstance();

        when(mockProperty.getType()).thenReturn(DATE);
        when(mockValue.getType()).thenReturn(DATE);
        when(mockValue.getDate()).thenReturn(date);
        final Triple t = createSingleValuedLiteralTriple();
        assertEquals("Got wrong RDF object!", createTypedLiteral(date).asNode()
                .getLiteral(), t.getObject().getLiteral());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t
                .getSubject());
    }

    @Test
    public void
            testSingleValuedDecimalLiteralTriple() throws RepositoryException {

        final BigDecimal num = BigDecimal.valueOf(3.141);

        when(mockProperty.getType()).thenReturn(DECIMAL);
        when(mockValue.getType()).thenReturn(DECIMAL);
        when(mockValue.getDecimal()).thenReturn(num);
        final Triple t = createSingleValuedLiteralTriple();
        assertEquals("Got wrong RDF object!", createTypedLiteral(num).asNode()
                .getLiteral(), t.getObject().getLiteral());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t
                .getSubject());
    }

    @Test
    public void testSingleValuedDoubleLiteralTriple()
                                                     throws RepositoryException {

        final Double num = 3.141;

        when(mockProperty.getType()).thenReturn(DOUBLE);
        when(mockValue.getType()).thenReturn(DOUBLE);
        when(mockValue.getDouble()).thenReturn(num);
        final Triple t = createSingleValuedLiteralTriple();
        assertEquals("Got wrong RDF object!", createTypedLiteral(num).asNode()
                .getLiteral(), t.getObject().getLiteral());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t
                .getSubject());
    }

    @Test
    public void testSingleValuedLongLiteralTriple() throws RepositoryException {

        final Long num = 3L;

        when(mockProperty.getType()).thenReturn(LONG);
        when(mockValue.getType()).thenReturn(LONG);
        when(mockValue.getLong()).thenReturn(num);
        final Triple t = createSingleValuedLiteralTriple();
        assertEquals("Got wrong RDF object!", createTypedLiteral(num).asNode()
                .getLiteral(), t.getObject().getLiteral());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t
                .getSubject());
    }

    @Test
    public void testSingleValuedUriLiteralTriple() throws RepositoryException {

        final String uri = "http://example.com/example-uri";

        when(mockProperty.getType()).thenReturn(URI);
        when(mockValue.getType()).thenReturn(URI);
        when(mockValue.getString()).thenReturn(uri);
        final Triple t = createSingleValuedLiteralTriple();
        assertEquals("Got wrong RDF object!", createResource(uri).asNode(), t
                .getObject());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t
                .getSubject());
    }

    @Test(expected = RuntimeException.class)
    public void testBadSingleValuedTriple() throws RepositoryException {
        when(mockProperty.getType()).thenReturn(URI);
        when(mockValue.getType()).thenReturn(URI);
        when(mockValue.getString()).thenThrow(
                new RepositoryException("Bad value!"));
        createSingleValuedLiteralTriple();
    }

    @Test
    public void testMultiValuedResourceTriple() throws RepositoryException {

        when(mockProperty.isMultiple()).thenReturn(true);
        when(mockProperty.getType()).thenReturn(PATH);
        when(mockProperty.getValues()).thenReturn(
                new Value[] {mockValue, mockValue2});
        when(mockValue.getString()).thenReturn(TEST_NODE_PATH);
        when(mockValue.getType()).thenReturn(PATH);
        when(mockValue2.getString()).thenReturn(TEST_NODE_PATH);
        when(mockValue2.getType()).thenReturn(PATH);
        final Function<Iterator<Value>, Iterator<Triple>> mapping =
            testPropertyToTriple.apply(mockProperty);
        final Iterator<Triple> ts =
            mapping.apply(twoValueIterator(mockValue, mockValue2));
        final Triple t1 = ts.next();
        LOGGER.debug(
                "Constructed triple for testMultiValuedResourceTriple(): {}",
                t1);
        final Triple t2 = ts.next();
        LOGGER.debug(
                "Constructed triple for testMultiValuedResourceTriple(): {}",
                t2);

        assertEquals("Got wrong RDF object!", TEST_NODE_SUBJECT.asNode(), t1
                .getObject());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t1.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t1
                .getSubject());

        assertEquals("Got wrong RDF object!", TEST_NODE_SUBJECT.asNode(), t2
                .getObject());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t2.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t2
                .getSubject());
    }

    @Test
    public
            void
            testMultiValuedResourceTripleWithReference()
                                                        throws RepositoryException {

        when(mockProperty.isMultiple()).thenReturn(true);
        when(mockProperty.getType()).thenReturn(REFERENCE);
        when(mockProperty.getValues()).thenReturn(
                new Value[] {mockValue, mockValue2});
        when(mockValue.getString()).thenReturn(TEST_NODE_PATH);
        when(mockValue.getType()).thenReturn(REFERENCE);
        when(mockValue2.getString()).thenReturn(TEST_NODE_PATH);
        when(mockValue2.getType()).thenReturn(REFERENCE);
        when(mockSession.getNodeByIdentifier(TEST_NODE_PATH)).thenReturn(
                mockNode);

        final Function<Iterator<Value>, Iterator<Triple>> mapping =
            testPropertyToTriple.apply(mockProperty);
        final Iterator<Triple> ts =
            mapping.apply(twoValueIterator(mockValue, mockValue2));
        final Triple t1 = ts.next();
        LOGGER.debug(
                "Constructed triple for testMultiValuedResourceTriple(): {}",
                t1);
        final Triple t2 = ts.next();
        LOGGER.debug(
                "Constructed triple for testMultiValuedResourceTriple(): {}",
                t2);

        assertEquals("Got wrong RDF object!", TEST_NODE_SUBJECT.asNode(), t1
                .getObject());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t1.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t1
                .getSubject());

        assertEquals("Got wrong RDF object!", TEST_NODE_SUBJECT.asNode(), t2
                .getObject());
        assertEquals("Got wrong RDF predicate!", createProperty(
                TEST_PROPERTY_NAME).asNode(), t2.getPredicate());
        assertEquals("Got wrong RDF subject!", TEST_NODE_SUBJECT.asNode(), t2
                .getSubject());
    }

    @Test(expected = RepositoryException.class)
    public void badProperty() throws AccessDeniedException,
                             ItemNotFoundException, RepositoryException {
        when(mockProperty.getParent()).thenThrow(
                new RepositoryException("Bad property!"));
        // we exhaust the mock of mockProperty.getParent() to replace it with an
        // exception
        mockProperty.getParent();
        createSingleValuedLiteralTriple();
    }

    private Triple createSingleValuedLiteralTriple() throws RepositoryException {

        when(mockProperty.isMultiple()).thenReturn(false);
        when(mockProperty.getValue()).thenReturn(mockValue);
        final Function<Iterator<Value>, Iterator<Triple>> mapping =
            testPropertyToTriple.apply(mockProperty);
        final Triple t = mapping.apply(singletonIterator(mockValue)).next();
        LOGGER.debug("Constructed triple: {}", t);
        return t;
    }

    @Before
    public void setUp() throws ValueFormatException, RepositoryException {
        initMocks(this);
        testPropertyToTriple = new PropertyToTriple(mockGraphSubjects);
        when(mockProperty.getValue()).thenReturn(mockValue);
        when(mockProperty.getParent()).thenReturn(mockNode);
        when(mockProperty.getName()).thenReturn(TEST_PROPERTY_NAME);
        when(mockProperty.getSession()).thenReturn(mockSession);
        when(mockNode.getPath()).thenReturn(TEST_NODE_PATH);
        when(mockGraphSubjects.getGraphSubject(mockNode)).thenReturn(
                TEST_NODE_SUBJECT);
        when(mockNode.getNode(TEST_NODE_PATH)).thenReturn(mockNode);
    }

    private <T> Iterator<T> twoValueIterator(final T t, final T t2) {
        return ImmutableList.of(t, t2).iterator();
    }

    private PropertyToTriple testPropertyToTriple;

    @Mock
    private Session mockSession;

    @Mock
    private GraphSubjects mockGraphSubjects;

    @Mock
    private Property mockProperty;

    @Mock
    private Value mockValue, mockValue2;

    @Mock
    private javax.jcr.Node mockNode;

    private final static Logger LOGGER = getLogger(PropertyToTripleTest.class);

    private static final String TEST_NODE_PATH = "/test";

    private static final Resource TEST_NODE_SUBJECT = ResourceFactory
            .createResource("http:/" + TEST_NODE_PATH);

    private static final String TEST_VALUE = "test value";

    private static final String TEST_PROPERTY_NAME = "info:predicate";

}
