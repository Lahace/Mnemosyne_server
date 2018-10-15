package ap.mnemosyne.parser;

import ap.mnemosyne.parser.pResources.Marker;

import java.util.List;

public abstract class Parser
{
	public final List<Marker> CONSTRAINT_MARKERS;
	public final String[] CONSTRAINT_TOKENS;

	Parser(List<Marker> constr)
	{
		CONSTRAINT_MARKERS = constr;
		CONSTRAINT_TOKENS = new String[CONSTRAINT_MARKERS.size()];
		String[] ctemp = new String[CONSTRAINT_MARKERS.size()];
		for(int i=0; i<CONSTRAINT_MARKERS.size(); i++) CONSTRAINT_TOKENS[i] = CONSTRAINT_MARKERS.get(i).getMarker();
	}
}
