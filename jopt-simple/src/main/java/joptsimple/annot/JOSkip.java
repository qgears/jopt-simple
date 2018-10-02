package joptsimple.annot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Skip field marker. Means that the field is not handled by joptsimple.
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JOSkip {
}
