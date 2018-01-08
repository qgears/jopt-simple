package joptsimple.annot;

import java.lang.reflect.Field;

import joptsimple.OptionSpec;

/**
 * A Joptsimple argument that maps an object field to an {@link OptionSpec} object. 
 */
public class Option {
	public final Object o;
	public final Field f;
	public final OptionSpec<?> spec;
	public Option(Object o, Field f, OptionSpec<?> spec) {
		super();
		this.o = o;
		this.f = f;
		this.spec = spec;
	}
}
