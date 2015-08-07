package org.codehaus.enunciate.jaxrs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The changelog lines for the named method.
 *
 * @author Ales Bregar
 */
@Retention ( RetentionPolicy.RUNTIME )
@Target ({ ElementType.TYPE, ElementType.METHOD })
public @interface Changelogs {

  Changelog[] value() default {};
}
