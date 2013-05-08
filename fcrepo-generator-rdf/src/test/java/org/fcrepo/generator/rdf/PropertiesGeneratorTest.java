
package org.fcrepo.generator.rdf;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.ws.rs.core.UriInfo;

import org.fcrepo.generator.rdf.TripleSource.Triple;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Utils.class})
public class PropertiesGeneratorTest {

    private PropertiesGenerator testObj;

    @Before
    public void setUp() {
        testObj = new PropertiesGenerator();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testGetTriples() throws RepositoryException {
        final String path = "/testing/fake/object";
        final UriInfo mockUris = mock(UriInfo.class);
        final Node mockNode = mock(Node.class);
        when(mockNode.getPath()).thenReturn(path);
        final Property mockProp = mock(Property.class);
        when(mockProp.getParent()).thenReturn(mockNode);
        when(mockProp.getString()).thenReturn("mockValue");
        final PropertyIterator mockProps = mock(PropertyIterator.class);
        when(mockProps.hasNext()).thenReturn(true, false);
        when(mockProps.next()).thenReturn(mockProp).thenThrow(
                IndexOutOfBoundsException.class);
        when(mockNode.getProperties()).thenReturn(mockProps);

        // mock the static method on Utils
        PowerMockito.mockStatic(Utils.class);
        when(Utils.expandJCRNamespace(mockProp)).thenReturn(
                "{http://fcrepo.co.uk/test/}property");

        final List<Triple> triples = testObj.getTriples(mockNode, mockUris);
        assertEquals(1, triples.size());
        final Triple t = triples.get(0);
        assertEquals("{http://fcrepo.co.uk/test/}property", t.predicate);
        assertEquals("mockValue", t.object);
    }
}
