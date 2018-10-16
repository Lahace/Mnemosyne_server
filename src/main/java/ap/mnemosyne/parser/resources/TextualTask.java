package ap.mnemosyne.parser.resources;

import java.util.ArrayList;
import java.util.List;

public class TextualTask
{
	TextualAction textualAction;
	List<TextualConstraint> textualConstraints;

	public TextualTask(TextualAction textualAction, List<TextualConstraint> textualConstraints)
	{
		this.textualAction = textualAction;
		this.textualConstraints = textualConstraints;
	}

	public TextualAction getTextualAction()
	{
		return textualAction;
	}

	public void setTextualAction(TextualAction textualAction)
	{
		this.textualAction = textualAction;
	}

	public List<TextualConstraint> getTextualConstraints()
	{
		return textualConstraints;
	}

	public void setTextualConstraints(List<TextualConstraint> textualConstraint)
	{
		this.textualConstraints = textualConstraint;
	}

	@Override
	public String toString()
	{
		return "TextualTask{" +
				"textualAction=" + textualAction +
				", textualConstraint=" + textualConstraints +
				'}';
	}

}
