package ap.mnemosyne.exceptions;

public class ParameterNotDefinedException extends RuntimeException
{
	public ParameterNotDefinedException()
	{
		super();
	}

	public ParameterNotDefinedException(String message)
	{
		super(message);
	}
}
