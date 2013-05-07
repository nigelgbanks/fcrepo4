package org.fcrepo.api;

import javax.inject.Inject;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.RepositoryFactory;
import javax.jcr.Session;
import javax.jcr.Workspace;
import javax.jcr.version.VersionManager;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.RandomStringUtils;
import org.fcrepo.AbstractResource;
import org.modeshape.jcr.JcrRepositoryFactory;
import org.modeshape.jcr.cache.WorkspaceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/rest/fcr:workspaces")
public class FedoraWorkspaces extends AbstractResource {

	@Autowired
	private RepositoryFactory jcrFactory;

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public Response createWorkspace() throws RepositoryException {
		final Session sess = getAuthenticatedSession();
		String name = RandomStringUtils.randomAlphanumeric(16);
		sess.getWorkspace().createWorkspace(name);
		return Response.status(201).entity(name).build();
	}

	@GET
	@Path("{name}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_XML })
	public Response getWorkspace(@PathParam("name") final String name) throws RepositoryException {
		final Session sess = getAuthenticatedSession();
		if (!isWorkspaceAccessible(sess, name)) {
			throw new WorkspaceNotFoundException("Unable to access Workspace " + name);
		}
		final Session wsSess = sess.getRepository().login(name);
		String current = wsSess.getWorkspace().getName();
		return Response.ok(current).build();
	}

	@DELETE
	@Path("{name}")
	public Response deleteWorkspace(@PathParam("name") final String name) throws RepositoryException {
		final Session sess = getAuthenticatedSession();
		if (name.equals("default")) {
			throw new RepositoryException("Unable to delete the default workspace");
		}
		sess.getWorkspace().deleteWorkspace(name);
		return Response.ok().build();
	}

	@POST
	@Path("/fcr:merge/{name}")
	public Response mergeWorkspace(@PathParam("name") final String name) throws RepositoryException {
		final Session sess = getAuthenticatedSession();
		Workspace ws1 = sess.getWorkspace();
		VersionManager vm = ws1.getVersionManager();
		vm.merge("/", name, false);
		return Response.ok().build();
	}

	private boolean isWorkspaceAccessible(Session sess, String name) throws RepositoryException {
		for (String wsName : sess.getWorkspace().getAccessibleWorkspaceNames()) {
			if (wsName.equals(name)) {
				return true;
			}
		}
		return false;

	}
}
