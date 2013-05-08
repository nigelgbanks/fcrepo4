
package org.fcrepo.session;

import static org.slf4j.LoggerFactory.getLogger;

import javax.jcr.Session;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.PerRequestTypeInjectableProvider;

/**
 * A JAX-RS Provider that supplies {@link InjectableSession}s for use with
 * the {@link InjectedSession} annotation.
 * 
 * @author ajs6f
 *
 */
@Component
@Provider
public class SessionProvider extends
        PerRequestTypeInjectableProvider<InjectedSession, Session> {

    @Autowired
    private SessionFactory sessionFactory;

    @Context
    private SecurityContext secContext;

    @Context
    private HttpServletRequest request;

    private static final Logger logger = getLogger(SessionProvider.class);

    public SessionProvider() {
        super(Session.class);
    }

    @Override
    public Injectable<Session> getInjectable(final ComponentContext ic,
            final InjectedSession a) {
        logger.trace("Returning new InjectableSession...");
        return new InjectableSession(sessionFactory, secContext, request);
    }
}