package joptsimple.annot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Means that the field is a boolean that can only be set to true
 * without arguments.
 * @author rizsi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JOSimpleBoolean {

}
