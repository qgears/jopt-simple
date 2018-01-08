package joptsimple.annot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Delegate options class. Means that all fields of this field must be added to the option list.
 * 
 * @author rizsi
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JODelegate {
	/**
	 * Prefix to add to all delegated options.
	 * @return
	 */
	public String prefix();
}
