package org.fcrepo.binary;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.Session;

import static junit.framework.Assert.assertNull;
import static org.fcrepo.utils.FedoraJcrTypes.FEDORA_CONTENTTYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PolicyDecisionPointTest {
	static PolicyDecisionPoint pt;
	static private String dummyHint;
	static private String tiffHint;

	@BeforeClass
	public static void setupPdp() {
		pt = new PolicyDecisionPoint();

		dummyHint = "dummy-store-id";
		Policy policy = new MimeTypePolicy("image/x-dummy-type", dummyHint);

		pt.addPolicy(policy);

		tiffHint = "tiff-store-id";
		Policy tiffPolicy = new MimeTypePolicy("image/tiff", tiffHint);

		pt.addPolicy(tiffPolicy);
	}

	@Test
	public void testDummyNode() throws Exception {

		Session mockSession = mock(Session.class);
		Node mockRootNode = mock(Node.class);
		Node mockDsNode = mock(Node.class);

		when(mockDsNode.getSession()).thenReturn(mockSession);
		Property mockProperty = mock(Property.class);
		when(mockProperty.getString()).thenReturn("image/x-dummy-type");
		when(mockDsNode.getProperty(FEDORA_CONTENTTYPE)).thenReturn(mockProperty);

		String receivedHint = pt.evaluatePolicies(mockDsNode);
		assertThat(receivedHint, is(dummyHint));
	}

	@Test
	public void testTiffNode() throws Exception {

		Session mockSession = mock(Session.class);
		Node mockRootNode = mock(Node.class);
		Node mockDsNode = mock(Node.class);

		when(mockDsNode.getSession()).thenReturn(mockSession);
		Property mockProperty = mock(Property.class);
		when(mockProperty.getString()).thenReturn("image/tiff");
		when(mockDsNode.getProperty(FEDORA_CONTENTTYPE)).thenReturn(mockProperty);

		String receivedHint = pt.evaluatePolicies(mockDsNode);
		assertThat(receivedHint, is(tiffHint));
	}


	@Test
	public void testOtherNode() throws Exception {

		Session mockSession = mock(Session.class);
		Node mockRootNode = mock(Node.class);
		Node mockDsNode = mock(Node.class);

		when(mockDsNode.getSession()).thenReturn(mockSession);
		Property mockProperty = mock(Property.class);
		when(mockProperty.getString()).thenReturn("image/x-other");
		when(mockDsNode.getProperty(FEDORA_CONTENTTYPE)).thenReturn(mockProperty);

		String receivedHint = pt.evaluatePolicies(mockDsNode);
		assertNull(receivedHint);
	}


}
