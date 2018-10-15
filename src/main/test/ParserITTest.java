import org.junit.Test;
import ap.mnemosyne.parser.ParserIT;
import static org.junit.Assert.*;

public class ParserITTest
{

	@Test
	public void testParser()
	{
		ParserIT p = new ParserIT();
		p.parseString("devo prendere il pane quando esco dal lavoro");
	}
}