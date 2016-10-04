package joptsimple.annot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Mark that this argument is to store nonOptionArguments list. The argument must be with type {@link List}<String>
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JONonOptionArgumentsList {

}
