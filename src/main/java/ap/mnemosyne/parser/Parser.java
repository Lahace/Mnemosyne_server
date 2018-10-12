package ap.mnemosyne.parser;

import java.util.List;

public abstract class Parser
{
	public final List<String> CONSTRAINT_MARKERS;

	Parser(List<String> constr)
	{
		CONSTRAINT_MARKERS = constr;
	}
}
