package joptsimple.annot;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;

/**
 * Parses command line parameters into a Java DTO.
 * <ul>
 * <li>All public fields of DTO class is identified as a command line argument
 * (arg name equals field name)
 * <li>Use {@link JOHelp} annotation on fields, data types and enum literals, to
 * attach documentation to an argument.
 * </ul>
 * 
 * <h3>Usage</h3>
 * <ol>
 * <li>Define DTO class, that describes arguments
 * <li>Create a new instance of DTO
 * <li>Create a new {@link AnnotatedClass}, and call
 * {@link #parseAnnotations(Object)}, pass DTO as parameter.
 * <li>Call {@link #parseArgs(String[])}.
 * <li>DTO will contain the data, parsed from input parameters
 * <li>After parsing annotations, the {@link #printHelpOn(PrintStream)} can be
 * used to generate a user readable documentation of arguments.
 * </ol>
 * <p>
 * 
 * 
 * The tool automatically parses argument values, and tries to convert them to
 * expected datatype (which is the type of the field in DTO class). Primitive
 * types, enumerations, Strings, and Lists from these types are supported out of the
 * box. For parsing more complex datatypes automatically see
 * {@link ArgumentAcceptingOptionSpec#ofType(Class)}
 * 
 * @author rizsi, agostoni
 * 
 * @see JOHelp
 * @see JOSimpleBoolean
 * @see OptionParser
 */
public class AnnotatedClass {
	private OptionParser parser;
	private OptionSet options;
	private List<Option> args;
	private Option nonOptionArguments;
	/**
	 * Prints all arguments and the value of them to stdout.
	 * 
	 * To print it elsewhere use optionsToString() instead.
	 * 
	 * Must be called after {@link #parseAnnotations(Object)} was called.
	 * <p>
	 * This method should only be for debugging purposes to quickly
	 * print the parsed parameters of the application.
	 * @throws Exception
	 */
	public void print() throws Exception
	{
		System.out.println(optionsToString());
	}
	/**
	 * Format all arguments and the value of them in user readable format to a String.
	 * Must be called after
	 * {@link #parseAnnotations(Object)} was called.
	 * <p>
	 * This method should only be for debugging purposes to quickly
	 * print the parsed parameters of the application.
	 * @throws Exception in case of reflective access of the object fields fails.
	 * @return The actual argument values parsed in a user readable format.
	 */
	public String optionsToString() throws Exception
	{
		StringBuilder ret=new StringBuilder();
		for(Option o: args)
		{
			ret.append(""+o.f.getName()+": "+o.f.get(o.o)+"\n");
		}
		ret.append("Remaining args: "+nonOptionArguments());
		return ret.toString();
	}
	
	/**
	 * Parse the command line arguments array using the embedded parser instance.
	 * Sets the values on the stored arguments object based on the command line arguments.
	 *  Method {@link #parseAnnotations(Object)} must be called before!
	 * 
	 * @param args The arguments to parse as String array
	 * @throws Exception
	 */
	public void parseArgs(String [] args) throws Exception
	{
		options=parser.parse(args);
		for(Option opt: this.args)
		{
			Object value=null;
			if(isSimpleBoolean(opt.f))
			{
				value=options.has(opt.spec);
			}else if(opt.f.getType()==List.class)
			{
				value=options.valuesOf(opt.spec);
			}else
			{
				value=opt.spec.value(options);
			}
			if(value!=null)
			{
				if(opt.f.getType().isEnum())
				{
					value=parseEnumValue(opt.f, value);
				}
				opt.f.set(opt.o, value);
			}
		}
		if(nonOptionArguments!=null)
		{
			nonOptionArguments.f.set(nonOptionArguments.o, nonOptionArguments());
		}
	}
	@SuppressWarnings("unchecked")
	private Object parseEnumValue(Field f, Object value) {
		@SuppressWarnings("rawtypes")
		Class<Enum> e=(Class<Enum>)f.getType();
		return Enum.valueOf(e, ""+value);
	}
	private boolean isSimpleBoolean(Field f) {
		return f.getAnnotation(JOSimpleBoolean.class)!=null;
	}

	/**
	 * Read a class using reflection. Finds all public fields of the class
	 * and generates an option entry into embedded {@link OptionParser} instance
	 * for each of them.
	 * {@link JOHelp} and {@link JOSimpleBoolean} annotations are handled.
	 * 
	 * @param programArgumentsObject the object is stored by reference and the
	 * fields of this object are set when the parseArgs() method is called.
	 *
	 * @throws Exception in case of illegal or unknown fields.
	 * @see JOHelp
	 * @see JOSimpleBoolean
	 */
	public void parseAnnotations(Object programArgumentsObject) throws Exception
	{
		parser = new OptionParser();
		args=new ArrayList<Option>();
		parseFields(programArgumentsObject, "");
	}
	private void parseFields(Object programArgumentsObject, String prefix) throws Exception
	{
		Class<?> c=programArgumentsObject.getClass();
		List<Field> fs=getArgFields(c);
		for(Field f: fs)
		{
			if(f.getAnnotation(JONonOptionArgumentsList.class)!=null)
			{
				nonOptionArguments=new Option(programArgumentsObject, f, null);
				continue;
			}
			Class<?> t=f.getType();
			if(t.isPrimitive())
			{
				t=getWrappingClass(t);
			}
			if(f.getAnnotation(JODelegate.class)!=null)
			{
				JODelegate d=f.getAnnotation(JODelegate.class);
				String subprefix=d.prefix()==null?"":d.prefix();
				Object delegate=f.get(programArgumentsObject);
				parseFields(delegate, prefix+subprefix);
				return;
			}
			OptionSpecBuilder spec=parser.accepts(f.getName(), getHelp(f));
			Object defaultValue=f.get(programArgumentsObject);
			OptionSpec<?>a=null;
			if(t==Integer.class)
			{
				ArgumentAcceptingOptionSpec<Integer> aa=spec.withRequiredArg().ofType(Integer.class);
				a=aa;
				if(defaultValue!=null)
				{
					aa.defaultsTo((Integer)defaultValue);
				}
				
			}
			else if(t==Boolean.class)
			{
				if(isSimpleBoolean(f))
				{
					a=spec;
				}else
				{
					ArgumentAcceptingOptionSpec<Boolean> aa=spec.withRequiredArg()
							.withValuesConvertedBy(BooleanConverter.getInstance());
					a=aa;
					if(defaultValue!=null)
					{
						aa.defaultsTo((Boolean)defaultValue);
					}
				}
			}else if(File.class==t)
			{
				ArgumentAcceptingOptionSpec<File> bb=spec.withRequiredArg().ofType(File.class);
				a=bb;
				if(defaultValue!=null)
				{
					bb.defaultsTo((File) defaultValue);
				}
			}
			else if(String.class==t)
			{
				ArgumentAcceptingOptionSpec<String> bb=spec.withRequiredArg().ofType(String.class);
				a=bb;
				if(defaultValue!=null)
				{
					bb.defaultsTo((String) defaultValue);
				}
			}
			else if(t==Long.class)
			{
				ArgumentAcceptingOptionSpec<Long> bb=spec.withRequiredArg().ofType(Long.class);
				a=bb;
				if(defaultValue!=null)
				{
					bb.defaultsTo((Long) defaultValue);
				}
			}
			else if(t.isEnum())
			{
				ArgumentAcceptingOptionSpec<String> en=spec.withRequiredArg().ofType(String.class);
				a=en;
				if(defaultValue!=null)
				{
					en.defaultsTo(""+defaultValue);
				}
			}else if(t==List.class)
			{
				ArgumentAcceptingOptionSpec<?> en = createListParameters(spec,
						defaultValue, getListElementType(f));
				a=en;
			}
			else
			{
				throw new RuntimeException("Type Not implemented: "+t);
			}
			if(a!=null)
			{
				args.add(new Option(programArgumentsObject, f, a));
			}
		}
	}
	private List<Field> getArgFields(Class<?> c) {
		List<Field> fs = new ArrayList<Field>();
		
		for (Field f : c.getFields()){
			if (!skipField(f)) {
				fs.add(f);
			}
		}
		return fs;
	}

	private boolean skipField(Field f) {
		int mod = f.getModifiers();
		if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
			return true;
		}
		return false;
	}
	
	private <T>ArgumentAcceptingOptionSpec<T> createListParameters(
			OptionSpecBuilder spec, Object defaultValue, Class<T> elementType) {
		if (elementType != null){
			ArgumentAcceptingOptionSpec<T> en=spec.withRequiredArg().ofType(elementType).withValuesSeparatedBy(',');
			if (defaultValue != null){
				@SuppressWarnings("unchecked")
				List<T> defaults = (List<T>) defaultValue;
				if (!defaults.isEmpty()){
					en.defaultsToCollection(defaults);
				}
			}
			return en;
		} else {
			throw new RuntimeException("Raw list types are not supported "+spec.toString());
		}
	}
	
	/**
	 * Returns the generic type argument of {@link List} class, that is used
	 * in the declaration of the specified {@link Field}.
	 * <p>
	 * Example:
	 * <pre>
	 * 
	 * Class M {
	 * 	List&lt;Integer> myField;
	 * }
	 * 
	 * getListElementType(M.class.getField("myField")) will return Integer.class
	 * </pre>
	 * @param f
	 *            A field with type {@link List}
	 * @return
	 */
	private Class<?> getListElementType(Field f) {
		if (f != null){
			if (f.getType().equals(List.class)){
				Type genericFieldType = f.getGenericType();
				if (genericFieldType instanceof ParameterizedType){
					ParameterizedType aType = (ParameterizedType) genericFieldType;
					Type[] fieldArgTypes = aType.getActualTypeArguments();
					if (fieldArgTypes != null && fieldArgTypes.length == 1){
						return (Class<?>) fieldArgTypes[0];
					}
				}
			}
		}
		return null;
	}
	private Class<?> getWrappingClass(Class<?> t) {
		if(t==int.class)
		{
			return Integer.class;
		}
		if(t==long.class)
		{
			return Long.class;
		}
		if(t==boolean.class)
		{
			return Boolean.class;
		}
		throw new RuntimeException("No wrappng type present implemented for: "+t);
	}
	private String getHelpText(Object o) throws SecurityException, NoSuchFieldException
	{
		JOHelp h=null;
		if(o instanceof Class<?>)
		{
			Class<?> c=(Class<?>)o;
			h=c.getAnnotation(JOHelp.class);
		}
		else if(o instanceof Enum<?>)
		{
			
			Enum<?> e=(Enum<?>)o;
			h=e.getClass().getField(""+e).getAnnotation(JOHelp.class);
		}else if(o instanceof Field)
		{
			Field f=(Field)o;
			h=f.getAnnotation(JOHelp.class);
		}
		if(h!=null)
		{
			return h.value();
		}
		return null;
	}
	private String getHelp(Field f) throws Exception {
		StringBuilder ret=new StringBuilder();
		String t=getHelpText(f);
		if(t!=null)
		{
			ret.append(t);
		}else
		{
			ret.append("Undocumented");
		}
		if(f.getType().isEnum())
		{
			appendEnumDocumentation(f.getType(), ret);
		}
		if (f.getType() == List.class){
			Class<?> elementType = getListElementType(f);
			if (elementType != null && elementType.isEnum()){
				appendEnumDocumentation(elementType, ret);
			}
		}
		return ret.toString();
	}

	/**
	 * Prints valid enum literals of specified enum type.
	 * 
	 * @param type
	 *            The enum type (see {@link Class#isEnum()})
	 * @param ret
	 *            The target {@link StringBuilder}
	 * @throws NoSuchFieldException
	 */
	private void appendEnumDocumentation(Class<?> type, StringBuilder ret)
			throws NoSuchFieldException {
		ret.append("\n");
		String h=getHelpText(type);
		if(h!=null)
		{
			ret.append(h);
		}
		ret.append("(possible values: ");
		Object[] cs=type.getEnumConstants();
		for(int i=0;i<cs.length;++i)
		{
			Object o=cs[i];
			h=getHelpText(o);
			ret.append(" "+o);
			if(h!=null)
			{
				ret.append(" ("+h+")");
			}
			if(i<cs.length-1)
			ret.append(", ");
		}
		ret.append(")\n");
	}

	/**
	 * Prints a user readable documentation of arguments. Method
	 * {@link #parseAnnotations(Object)} must be called before.
	 * 
	 * @param out The target {@link PrintStream}
	 * @throws IOException
	 * @see JOHelp
	 * @see OptionParser#printHelpOn(java.io.OutputStream)
	 */
	public void printHelpOn(PrintStream out) throws IOException {
		parser.printHelpOn(out);
		if(nonOptionArguments!=null)
		{
			String help;
			try {
				help = getHelp(nonOptionArguments.f);
			} catch (Exception e) {
				throw new IOException(e);
			}
			out.println("Remaining arguments: "+help);
		}
	}

	/**
	 * Get the list of non option arguments (all arguments after the -- mark on the command line)
	 * that are not parsed by this options parser instance.
	 * <p>
	 * See {@link OptionSet#nonOptionArguments()}.
	 * @return
	 */
	public List<String> nonOptionArguments() {
		List<String> ret=new ArrayList<String>();
		List<?> l=options.nonOptionArguments();
		for(Object o: l)
		{
			ret.add(""+o);
		}
		return ret;
	}
}
