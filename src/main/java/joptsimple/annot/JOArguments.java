package joptsimple.annot;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface JOArguments {
	boolean allowsRemaining=false;
}
