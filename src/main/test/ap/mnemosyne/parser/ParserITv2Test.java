package ap.mnemosyne.parser;

import org.junit.Test;

import java.io.IOException;

public class ParserITv2Test
{
	@Test
	public void testParserv2() throws IOException
	{
		ParserITv2 p = new ParserITv2();
		p.parseString("devo prendere il pane prima di rientrare a casa");
		p.parseString("devo prendere le medicine dopo pranzo");
		p.parseString("devo pagare il bollo entro domani");
		p.parseString("devo prendere il pane entro le 16:30");
		p.parseString("devo prenotare dal dottore entro le 7:30");
		p.parseString("devo dar da mangiare al gatto quando torno a casa");
		p.parseString("devo fare la lavatrice quando torno a casa");
		p.parseString("devo mettere la crema prima di andare a letto");
		p.parseString("devo prendere il pane");
		System.out.println("=======================");
		p.parseString("ricordami di prendere il pane prima di rientrare a casa");
		p.parseString("ricordami di prendere le medicine dopo pranzo");
		p.parseString("ricordami di pagare il bollo entro domani");
		p.parseString("ricordami di prendere il pane entro le 16");
		p.parseString("ricordami di prenotare dal dottore entro le 17");
		p.parseString("ricordami di dar da mangiare al gatto quando torno a casa");
		p.parseString("ricordami di fare la lavatrice quando torno a casa");
		p.parseString("ricordami di mettere la crema prima di andare a letto");
		p.parseString("ricordami di prendere il pane");
	}
}