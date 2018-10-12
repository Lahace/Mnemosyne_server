import org.junit.Test;
import ap.mnemosyne.parser.ParserIT;
import static org.junit.Assert.*;

public class ParserITTest
{

	@Test
	public void testParser()
	{
		ParserIT p = new ParserIT();
		p.parseString("devo dar da mangiare al gatto quando torno a casa");
	}
}