/**
 * The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.utils;

import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.NamespaceRegistry;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Workspace;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @todo Add Documentation.
 * @author Chris Beer
 * @date May 23, 2013
 */
public class NamespaceChangedStatementListenerTest {

    private NamespaceChangedStatementListener testObj;
    private NamespaceRegistry mockNamespaceRegistry;

    /**
     * @todo Add Documentation.
     */
    @Before
    public void setUp() throws RepositoryException {
        Session mockSession = mock(Session.class);
        Workspace mockWorkspace = mock(Workspace.class);
        mockNamespaceRegistry = mock(NamespaceRegistry.class);
        when(mockWorkspace.getNamespaceRegistry())
            .thenReturn(mockNamespaceRegistry);
        when(mockSession.getWorkspace()).thenReturn(mockWorkspace);
        testObj = new NamespaceChangedStatementListener(mockSession);

    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldAddNamespaceStatement() throws RepositoryException {
        Statement mockStatement = mock(Statement.class);
        when(mockStatement.getSubject())
            .thenReturn(ResourceFactory.createResource("uri"));
        when(mockStatement.getPredicate())
            .thenReturn(ResourceFactory
                        .createProperty(JcrRdfTools.HAS_NAMESPACE_PREDICATE));
        when(mockStatement.getObject())
            .thenReturn(ResourceFactory.createPlainLiteral("123"));

        testObj.addedStatement(mockStatement);
        verify(mockNamespaceRegistry).registerNamespace("123", "uri");
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldIgnoreNonNamespaceStatements()
        throws RepositoryException {
        Statement mockStatement = mock(Statement.class);
        when(mockStatement.getSubject())
            .thenReturn(ResourceFactory.createResource("uri"));

        when(mockStatement.getPredicate())
            .thenReturn(ResourceFactory
                        .createProperty("some-random-predicate"));

        when(mockStatement.getObject())
            .thenReturn(ResourceFactory.createPlainLiteral("abc"));

        testObj.addedStatement(mockStatement);

        verify(mockNamespaceRegistry, never()).registerNamespace("abc", "uri");
    }


    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldRemoveNamespaceStatement() throws RepositoryException {
        Statement mockStatement = mock(Statement.class);
        when(mockStatement.getSubject())
            .thenReturn(ResourceFactory.createResource("uri"));
        when(mockStatement.getPredicate())
            .thenReturn(ResourceFactory
                        .createProperty(JcrRdfTools.HAS_NAMESPACE_PREDICATE));
        when(mockStatement.getObject())
            .thenReturn(ResourceFactory.createPlainLiteral("123"));

        when(mockNamespaceRegistry.getPrefix("uri")).thenReturn("123");
        testObj.removedStatement(mockStatement);
        verify(mockNamespaceRegistry).unregisterNamespace("123");
    }


    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldIgnoreNonMatchingNamespacesOnRemoveNamespaceStatement()
        throws RepositoryException {
        Statement mockStatement = mock(Statement.class);

        when(mockStatement.getSubject())
            .thenReturn(ResourceFactory.createResource("uri"));

        when(mockStatement.getPredicate())
            .thenReturn(ResourceFactory
                        .createProperty(JcrRdfTools.HAS_NAMESPACE_PREDICATE));

        when(mockStatement.getObject())
            .thenReturn(ResourceFactory.createPlainLiteral("456"));

        when(mockNamespaceRegistry.getPrefix("uri")).thenReturn("123");
        testObj.removedStatement(mockStatement);
        verify(mockNamespaceRegistry, never()).unregisterNamespace("456");
    }

    /**
     * @todo Add Documentation.
     */
    @Test
    public void shouldIgnoreNonNamespaceStatementsOnRemove()
        throws RepositoryException {
        Statement mockStatement = mock(Statement.class);
        when(mockStatement.getSubject())
            .thenReturn(ResourceFactory.createResource("uri"));

        when(mockStatement.getPredicate())
            .thenReturn(ResourceFactory
                        .createProperty("some-random-predicate"));

        when(mockStatement.getObject())
            .thenReturn(ResourceFactory.createPlainLiteral("abc"));

        testObj.removedStatement(mockStatement);

        verify(mockNamespaceRegistry, never()).unregisterNamespace("abc");

    }
}
