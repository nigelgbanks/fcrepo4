package org.fcrepo.binary;

import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static junit.framework.Assert.assertNull;
import static org.fcrepo.utils.FedoraJcrTypes.FEDORA_CONTENTTYPE;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MimeTypePolicyTest {
	@Test
	public void shouldEvaluatePolicyAndReturnHint() throws Exception {
		String hint = "store-id";
		Policy policy = new MimeTypePolicy("image/x-dummy", hint);

		Session mockSession = mock(Session.class);
		Node mockRootNode = mock(Node.class);
		Node mockDsNode = mock(Node.class);

		when(mockDsNode.getSession()).thenReturn(mockSession);
		Property mockProperty = mock(Property.class);
		when(mockProperty.getString()).thenReturn("image/x-dummy");
		when(mockDsNode.getProperty(FEDORA_CONTENTTYPE)).thenReturn(mockProperty);

		String receivedHint = policy.evaluatePolicy(mockDsNode);

		assertThat(receivedHint, is(hint));
	}

	@Test
	public void shouldEvaluatePolicyAndReturnNoHint() throws Exception {
		String hint = "store-id";
		Policy policy = new MimeTypePolicy("image/x-dummy", hint);

		Session mockSession = mock(Session.class);
		Node mockDsNode = mock(Node.class);

		when(mockDsNode.getSession()).thenReturn(mockSession);
		Property mockProperty = mock(Property.class);
		when(mockProperty.getString()).thenReturn("application/x-other");
		when(mockDsNode.getProperty(FEDORA_CONTENTTYPE)).thenReturn(mockProperty);

		String receivedHint = policy.evaluatePolicy(mockDsNode);

		assertNull(receivedHint);
	}

	@Test
	public void shouldEvaluatePolicyAndReturnNoHintOnException() throws Exception {
		String hint = "store-id";
		Policy policy = new MimeTypePolicy("image/x-dummy", hint);

		Session mockSession = mock(Session.class);
		Node mockDsNode = mock(Node.class);

		when(mockDsNode.getProperty(FEDORA_CONTENTTYPE)).thenThrow(new RepositoryException());

		String receivedHint = policy.evaluatePolicy(mockDsNode);

		assertNull(receivedHint);
	}
}
