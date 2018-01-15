package joptsimple.tool;

import java.util.List;

public interface ITool {

	String getId();

	String getDescription();

	int exec(List<String> subList) throws Exception;

	int help(List<String> subList) throws Exception;

}
