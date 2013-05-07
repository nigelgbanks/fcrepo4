package org.fcrepo.integration.api;

import static org.junit.Assert.*;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.junit.Test;

public class FedoraWorkspacesIT extends AbstractResourceIT {

	@Test
	public void testWorkspaceCreate() throws Exception {
		HttpPost createWs = new HttpPost(serverAddress + "fcr:workspaces");
		HttpResponse resp = execute(createWs);
		assertTrue(resp.getStatusLine().getStatusCode() == 201);
		String name = IOUtils.toString(resp.getEntity().getContent());
		assertTrue("invalid name returned: " + name, name.length() == 16);

		HttpGet getWs = new HttpGet(serverAddress + "fcr:workspaces/" + name);
		resp = execute(getWs);
		assertTrue(resp.getStatusLine().toString(),resp.getStatusLine().getStatusCode() == 200);
		assertEquals(name, IOUtils.toString(resp.getEntity().getContent()));
	}

	@Test
	public void testInvalidWorkspaceGet() throws Exception {
		String name = "invalidws";
		HttpGet getWs = new HttpGet(serverAddress + "fcr:workspaces/" + name);
		HttpResponse resp = execute(getWs);
		assertTrue(resp.getStatusLine().toString(),resp.getStatusLine().getStatusCode() == 500);
	}

	@Test
	public void testWorkspaceDelete() throws Exception {
		HttpPost createWs = new HttpPost(serverAddress + "fcr:workspaces");
		HttpResponse resp = execute(createWs);
		assertTrue(resp.getStatusLine().getStatusCode() == 201);
		String name = IOUtils.toString(resp.getEntity().getContent());
		assertTrue("invalid name returned: " + name, name.length() == 16);

		HttpDelete rmWs = new HttpDelete(serverAddress + "fcr:workspaces/" + name);
		resp = execute(rmWs);
		assertTrue(resp.getStatusLine().getStatusCode() == 200);

		/* TODO: this should fail but the repo still returns the Workspace after the deletion */
		HttpGet getWs = new HttpGet(serverAddress + "fcr:workspaces/" + name);
		resp = execute(getWs);
		assertTrue("Repository returned: " + resp.getStatusLine(), resp.getStatusLine().getStatusCode() == 500);
	}

	@Test
	public void testWorkspaceMerge() throws Exception {
		HttpPost createWs = new HttpPost(serverAddress + "fcr:workspaces");
		HttpResponse resp = execute(createWs);
		assertTrue(resp.getStatusLine().getStatusCode() == 201);
		String name = IOUtils.toString(resp.getEntity().getContent());
		assertTrue("invalid name returned: " + name, name.length() == 16);

		HttpPost mergeWs = new HttpPost(serverAddress + "fcr:workspaces/fcr:merge/" + name);
		resp = execute(mergeWs);
		assertTrue("Repository returned: " + resp.getStatusLine(), resp.getStatusLine().getStatusCode() == 200);
	}
}
