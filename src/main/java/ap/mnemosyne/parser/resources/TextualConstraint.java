package ap.mnemosyne.parser.resources;

import java.io.Serializable;

public class TextualConstraint implements Serializable
{
	private String constraintMarker, constraintWord, verb;
	private boolean isFuture;

	public TextualConstraint(String constraintMarker, String constraintWord, String verb, boolean isFuture)
	{
		this.constraintMarker = constraintMarker;
		this.constraintWord = constraintWord;
		this.isFuture = isFuture;
		this.verb = verb;
	}

	public String getConstraintMarker()
	{
		return constraintMarker;
	}

	public String getConstraintWord()
	{
		return constraintWord;
	}

	public boolean isFuture()
	{
		return isFuture;
	}

	public String getVerb()
	{
		return verb;
	}

	@Override
	public String toString()
	{
		return "TextualConstraint{" +
				"constraintMarker='" + constraintMarker + '\'' +
				", constraintWord='" + constraintWord + '\'' +
				", verb='" + verb + '\'' +
				", isFuture='" + isFuture + '\'' +
				'}';
	}

}
