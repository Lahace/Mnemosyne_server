import org.junit.Test;
import ap.mnemosyne.parser.ParserIT;
import static org.junit.Assert.*;

public class ParserITTest
{

	@Test
	public void testParser()
	{
		ParserIT p = new ParserIT();
		System.out.print(p.parseString("devo mangiare per domani"));
	}
}