package joptsimple.annot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Attach documentation to a field that represents a command line argument. This
 * help text will be printed when
 * {@link AnnotatedClass#printHelpOn(java.io.PrintStream)} is called.
 * 
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
