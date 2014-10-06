package joptsimple.annot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Add help content to an joptsimple argument.
 * @author rizsi
 *
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface JOHelp {
	/**
	 * The help content shown in the help section of the field.
	 * @return
	 */
	String value();
}
