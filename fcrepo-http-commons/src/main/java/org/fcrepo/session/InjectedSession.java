
package org.fcrepo.session;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Annotation for use with JAX-RS context injection that supplies 
 * an authenticated or non-authenticated JCR {@link Session}.
 * 
 * @author ajs6f
 *
 */
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
@Documented
public @interface InjectedSession {

}