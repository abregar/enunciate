package org.codehaus.enunciate.jaxrs;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generic holder for a changelog description.
 *
 * @author Ales Bregar
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({})
public @interface Changelog {

    /**
     * The version.
     *
     * @return The version.
     */
    int version();

    /**
     * Detailed description line for some introduced change
     */
    String description();

}
