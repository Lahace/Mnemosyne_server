package ap.mnemosyne.parser;

import org.junit.Test;

import java.io.IOException;

public class ParserITv2Test
{
	@Test
	public void testParserv2() throws IOException
	{
		System.out.println(new ParserITv2().parseString("devo prendere il pane prima di arrivare a casa"));
		System.out.println(new ParserITv2().parseString("ricordami che prima di arrivare a casa devo prendere il pane"));
	}
}