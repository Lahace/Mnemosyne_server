package ap.mnemosyne.parser.pResources;

public class TextualTask
{
	TextualAction textualAction;
	TextualConstraint textualConstraint;

	public TextualTask(TextualAction textualAction, TextualConstraint textualConstraint)
	{
		this.textualAction = textualAction;
		this.textualConstraint = textualConstraint;
	}

	public TextualAction getTextualAction()
	{
		return textualAction;
	}

	public void setTextualAction(TextualAction textualAction)
	{
		this.textualAction = textualAction;
	}

	public TextualConstraint getTextualConstraint()
	{
		return textualConstraint;
	}

	public void setTextualConstraint(TextualConstraint textualConstraint)
	{
		this.textualConstraint = textualConstraint;
	}

	@Override
	public String toString()
	{
		return "TextualTask{" +
				"textualAction=" + textualAction +
				", textualConstraint=" + textualConstraint +
				'}';
	}

}
