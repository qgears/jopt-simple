package joptsimple.annot;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;

public class AnnotatedClass {
	public static void main(String[] args) throws Exception {
		AnnotatedClass ac=new AnnotatedClass();
		ac.parseAnnotations(new TestClass());
		ac.parseArgs(new String[]{"--korte","cica", "--alma", "112", "--tst", "selected" , "--", "remaining"});
		ac.print();
	}
	Object o;
	OptionParser parser;
	OptionSet options;
	Map<Field, OptionSpec<?>> args;
	public void print() throws Exception
	{
		Class<?> c=o.getClass();
		Field[] fs=c.getFields();
		for(Field f: fs)
		{
			System.out.println(""+f.getName()+": "+f.get(o));
		}
		System.out.println("Remaining args: "+nonOptionArguments());
	}
	public void parseArgs(String [] args) throws Exception
	{
		options=parser.parse(args);
		for(Field f:this.args.keySet())
		{
			Object value=null;
			if(isSimpleBoolean(f))
			{
				value=options.has(this.args.get(f));
			}else if(f.getType()==List.class)
			{
				value=this.args.get(f).values(options);
			}else
			{
				value=this.args.get(f).value(options);
			}
			if(value!=null)
			{
				if(f.getType().isEnum())
				{
					value=parseEnumValue(f, value);
				}
				f.set(o, value);
			}
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
	public void parseAnnotations(Object o) throws Exception
	{
		this.o=o;
		parser = new OptionParser();
		args=new HashMap<Field, OptionSpec<?>>();
		Class<?> c=o.getClass();
		Field[] fs=c.getFields();
		for(Field f: fs)
		{
//			System.out.println("Myfield: "+f.getName()+" "+f.getType());
			Class<?> t=f.getType();
			if(t.isPrimitive())
			{
				t=getWrappingClass(t);
			}
			OptionSpecBuilder spec=parser.accepts(f.getName(), getHelp(f));
			Object defaultValue=f.get(o);
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
					ArgumentAcceptingOptionSpec<Boolean> aa=spec.withRequiredArg().ofType(Boolean.class);
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
//				spec.withRequiredArg().ofType(argumentType)
				ArgumentAcceptingOptionSpec<String> en=spec.withRequiredArg().withValuesSeparatedBy(',');
				a=en;
				if(defaultValue!=null)
				{
					en.defaultsTo("");
				}
			}
			else
			{
				throw new RuntimeException("Type Not implemented: "+t);
			}
			if(a!=null)
			{
				args.put(f, a);
			}
		}
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
//			Enum.
//			System.out.println(""+e.);
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
			ret.append("\n");
			String h=getHelpText(f.getType());
			if(h!=null)
			{
				ret.append(h);
			}
			ret.append("(possible values: ");
			Object[] cs=f.getType().getEnumConstants();
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
			ret.append(")");
		}
		return ret.toString();
	}
	public void printHelpOn(PrintStream out) throws IOException {
		parser.printHelpOn(out);
	}
	public List<String> nonOptionArguments() {
		return (List<String>)options.nonOptionArguments();
	}
}
