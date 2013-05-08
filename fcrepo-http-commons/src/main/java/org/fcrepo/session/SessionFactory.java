/**
 * The contents of this file are subject to the license and copyright terms
 * detailed in the license directory at the root of the source tree (also
 * available online at http://fedora-commons.org/license/).
 */
package org.fcrepo.session;

import static org.slf4j.LoggerFactory.getLogger;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.SecurityContext;

import org.modeshape.jcr.api.ServletCredentials;
import org.slf4j.Logger;

/**
 * @todo Add Documentation.
 *
 * @author Benjamin Armintor
 * @date Apr 2, 2013
 */
public class SessionFactory {

    private static final Logger logger = getLogger(SessionFactory.class);

    @Inject
    private Repository repo;

    /**
     * @todo Add Documentation.
     */
    public SessionFactory() {
    }

    /**
     * @todo Add Documentation.
     */
    @PostConstruct
    public void init() {
        if (repo == null) {
            logger.error("SessionFactory requires a Repository instance!");
            throw new IllegalStateException();
        }
    }

    /**
     * @todo Add Documentation.
     */
    public void setRepository(final Repository repo) {
        this.repo = repo;
    }

    /**
     * @todo Add Documentation.
     */
    public Session getSession() throws RepositoryException {
        return repo.login();
    }

    private static ServletCredentials getCredentials(final SecurityContext securityContext,
                                                     final HttpServletRequest servletRequest) {
        if (securityContext.getUserPrincipal() != null) {
            logger.debug("Authenticated user: " +
                         securityContext.getUserPrincipal().getName());
            return new ServletCredentials(servletRequest);
        } else {
            logger.debug("No authenticated user found!");
            return null;
        }
    }

    /**
     * @todo Add Documentation.
     */
    public Session getSession(final SecurityContext securityContext,
                              final HttpServletRequest servletRequest) {

        try {
            ServletCredentials creds = getCredentials(securityContext, servletRequest);
            return (creds != null) ? repo.login(creds) : repo.login();
        } catch (final RepositoryException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * @todo Add Documentation.
     */
    public AuthenticatedSessionProvider getSessionProvider(final SecurityContext securityContext,
                                                           final HttpServletRequest servletRequest) {
        ServletCredentials creds = getCredentials(securityContext, servletRequest);
        return new AuthenticatedSessionProviderImpl(repo, creds);
    }
}
