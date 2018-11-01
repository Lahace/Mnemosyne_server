package ap.mnemosyne.parser;

import org.junit.Test;

import java.io.IOException;

public class ParserITv2Test
{
	@Test
	public void testParserv2() throws IOException
	{
		ParserITv2 p = new ParserITv2();
		p.parseString("devo prendere il pane prima di arrivare a casa");
		p.parseString("ricordami che prima di arrivare a casa devo prendere il pane");
		p.parseString("devo prendere le medicine dopo pranzo");
		p.parseString("ricordami di mettere la crema prima di andare a letto");
	}
}