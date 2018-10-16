package ap.mnemosyne.parser.resources;

public class TextualConstraint
{
	String constraintMarker, constraintWord;

	public TextualConstraint(String constraintMarker, String constraintWord)
	{
		this.constraintMarker = constraintMarker;
		this.constraintWord = constraintWord;
	}

	public String getConstraintMarker()
	{
		return constraintMarker;
	}

	public void setConstraintMarker(String constraintMarker)
	{
		this.constraintMarker = constraintMarker;
	}

	public String getConstraintWord()
	{
		return constraintWord;
	}

	public void setConstraintWord(String constraintWord)
	{
		this.constraintWord = constraintWord;
	}

	@Override
	public String toString()
	{
		return "TextualConstraint{" +
				"constraintMarker='" + constraintMarker + '\'' +
				", constraintWord='" + constraintWord + '\'' +
				'}';
	}

}
