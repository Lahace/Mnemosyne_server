package ap.mnemosyne.parser.resources;

public class TextualConstraint
{
	private String constraintMarker, constraintWord, verb;

	public TextualConstraint(String constraintMarker, String constraintWord, String verb)
	{
		this.constraintMarker = constraintMarker;
		this.constraintWord = constraintWord;
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
				'}';
	}

}
