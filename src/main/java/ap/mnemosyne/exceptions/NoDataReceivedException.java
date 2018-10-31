package ap.mnemosyne.exceptions;

/**
 * Created by alber on 31/10/2018.
 */
public class NoDataReceivedException extends RuntimeException
{
    public NoDataReceivedException()
    {
        super();
    }

    public NoDataReceivedException(String message)
    {
        super(message);
    }

}
