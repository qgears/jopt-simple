package joptsimple.annot;

import joptsimple.ValueConversionException;
import joptsimple.ValueConverter;

/**
 * Custom converter class for parsing boolean values. The converter only
 * accepts <code>true</code> or <code>false</code> as argument (ignoring
 * letter case).
 * <p>
 * If other values are received, then an exception will be thrown (instead
 * of returning <code>false</code>, which is the default behavior for
 * {@link Boolean#valueOf(String)}).
 * <p>
 * This class is a singleton.
 * 
 * @author agostoni
 * 
 * @see <a href="https://github.com/qgears/jopt-simple/issues/1">https://github.com/qgears/jopt-simple/issues/1</a>
 * @see #getInstance()
 */
public class BooleanConverter implements ValueConverter<Boolean> {
	private static final BooleanConverter theInstance = new BooleanConverter();
	
	private BooleanConverter() {}
	
	@Override
	public Boolean convert(String value) {
		if ("true".equalsIgnoreCase(value)){
			return Boolean.TRUE;
		} else if ("false".equalsIgnoreCase(value)){
			return Boolean.FALSE;
		} else {
			throw new ValueConversionException("Value '"+value+"' cannot be interpreted as Boolean. 'true' or 'false' expected");
		}
	}

	@Override
	public Class<? extends Boolean> valueType() {
		return Boolean.class;
	}

	@Override
	public String valuePattern() {
		return null;
	}
	
	/**
	 * Returns the only instance of this converter.
	 * 
	 * @return
	 */
	public static BooleanConverter getInstance() {
		return theInstance;
	}
}
