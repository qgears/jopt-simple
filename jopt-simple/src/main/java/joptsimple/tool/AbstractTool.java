package joptsimple.tool;

import java.io.PrintStream;
import java.util.List;

import joptsimple.annot.AnnotatedClass;

abstract public class AbstractTool implements ITool
{
	public interface IArgs
	{
		void validate();
	}
	protected AnnotatedClass ac;
	@Override
	final public int exec(List<String> subList) throws Exception {
		IArgs a=createArgsObject();
		AnnotatedClass ac=new AnnotatedClass();
		ac.parseAnnotations(a);
		try
		{
			ac.parseArgs(subList.toArray(new String[]{}));
			this.ac=ac;
			a.validate();
		}catch(Exception e)
		{
			if(e instanceof IllegalArgumentException)
			{
				System.out.println("Illegal arguments: "+e.getMessage());
			}else
			{
				e.printStackTrace();
			}
			System.out.println(getId()+": "+getDescription());
			AnnotatedClass ac2=new AnnotatedClass();
			ac2.parseAnnotations(createArgsObject());
			ac2.printHelpOn(System.out);
			return 1;
		}
		this.ac=ac;
		return doExec(a);
	}
	public void printValues(PrintStream out) throws Exception
	{
		if(ac!=null)
		{
			out.println(ac.optionsToString());
		}
	}
	@Override
	public int help(List<String> subList) throws Exception {
		System.out.println(getId()+": "+getDescription());
		System.out.println("\n\nARGUMENTS:");
		AnnotatedClass ac2=new AnnotatedClass();
		ac2.parseAnnotations(createArgsObject());
		ac2.printHelpOn(System.out);
		return 0;
	}
	abstract protected int doExec(IArgs a) throws Exception;
	abstract protected IArgs createArgsObject();
}
