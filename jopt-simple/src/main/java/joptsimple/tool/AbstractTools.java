package joptsimple.tool;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractTools {

	private List<ITool> tools=new ArrayList<>();

	public int mainEntryPoint(String[] args) {
		registerTools();
		List<String> largs=new ArrayList<>();
		for(String a: args)
		{
			largs.add(a);
		}
		int ret=exec(largs);
		return ret;
	}

	public int exec(List<String> args){
		try {
			if(args.size()>0)
			{
				if(args.get(0).equals("help"))
				{
					if(args.size()>1)
					{
						for(ITool t: tools)
						{
							if(args.get(1).equals(t.getId()))
							{
								return t.help(args.subList(2, args.size()));
							}
						}
					}else
					{
						System.err.println("Help command must be specified");
						return 1;
					}
				}
				for(ITool t: tools)
				{
					if(args.get(0).equals(t.getId()))
					{
						return t.exec(args.subList(1, args.size()));
					}
				}
				System.err.println("Tool not exist: "+args.get(0));
				return 1;
			}else
			{
				printIntro();
				for(ITool t: tools)
				{
					System.out.println(""+t.getId()+": "+head(t.getDescription()));
				}
				return 1;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 1;
		}
	}
	/**
	 * Print introduction to the console
	 */
	protected void printIntro() {
		System.out.println("Q-Gears command line tools");
		System.out.println("Tool not specified.\n");
		System.out.println("Help: $ java -jar tools.jar help {toolId}\n");
		System.out.println("Available tools:");
		System.out.println("");
	}

	/**
	 * First line and first 30 characters.
	 * @param description
	 * @return
	 */
	private String head(String description) {
		try {
			String s= description.split("\r\n")[0];
			if(s.length()>60)
			{
				return s.substring(0, 60);
			}
			return s;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	public void register(ITool tool) {
		tools.add(tool);
	}
	
	protected abstract void registerTools();
}
