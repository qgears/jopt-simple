package joptsimple.annot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Means that the field is a boolean that can only be set to true without
 * arguments.
 * <p>
 * If the parameter is present amongst command line arguments, that the field
 * value will be <code>true</code>, otherwise if will be <code>false</code> (so
 * --argName can be used instead of --argName=true).
 * 
 * @author rizsi
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JOSimpleBoolean {

}
