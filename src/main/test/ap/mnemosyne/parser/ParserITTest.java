package ap.mnemosyne.parser;

import org.junit.Test;
import ap.mnemosyne.parser.ParserIT;

public class ParserITTest
{

	@Test
	public void testParser()
	{
		ParserIT p = new ParserIT();
		p.parseString("devo prendere il pane entro domani alle 12");
		p.parseString("devo prendere le medicine dopo pranzo");
		p.parseString("devo prendere il pane entro le 16");
		p.parseString("devo dar da mangiare al gatto quando torno a casa");
		p.parseString("devo mettere la crema prima di andare a letto");
		p.parseString("devo prendere il pane");
	}
}