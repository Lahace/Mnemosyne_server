package ap.mnemosyne.parser;

import ap.mnemosyne.parser.resources.TextualAction;
import ap.mnemosyne.parser.resources.TextualConstraint;
import ap.mnemosyne.parser.resources.TextualTask;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;

public class ParserITv2Test
{
	@Test
	public void testParserv2()
	{
		ParserITv2 p = new ParserITv2();

		/*assertEquals(p.parseString("devo prendere il latte prima di domani all'una di notte"),
				new TextualTask(new TextualAction("prendere", "latte"), new ArrayList<TextualConstraint>(){{
					add(new TextualConstraint("prima", "1:00", "null", true));
				}}
				));*/
		p.parseString("devo prendere il latte domani prima delle dieci");
		p.parseString("devo prendere il latte prima di domani alle due di pomeriggio");
		p.parseString("devo prendere il latte prima di domani alle tre di mattina");
		p.parseString("devo prendere il latte prima di domani alle quattro di notte");
		p.parseString("devo prendere il latte prima di domani alle sette di sera");
		p.parseString("devo prendere il latte prima di rientrare a casa");
		p.parseString("devo prendere le medicine quando ceno");
		p.parseString("devo prendere le medicine prima di domani a mezzogiorno");
		p.parseString("devo pagare il bollo entro domani");
		p.parseString("devo prendere il pane entro le 16:30");
		p.parseString("devo prenotare dal dottore entro le 7:30");
		p.parseString("devo prenotare dal dottore prima di domani a mezzogiorno");
		p.parseString("devo dar da mangiare al gatto quando torno a casa");
		//p.parseString("devo dar da mangiare al gatto quando esco di casa");
		p.parseString("devo dar da mangiare al gatto domani quando torno a casa");
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
		p.parseString("ricordami di dare da mangiare al gatto quando torno a casa");
		p.parseString("ricordami di fare la lavatrice quando torno a casa");
		p.parseString("ricordami di mettere la crema prima di andare a letto");
		p.parseString("ricordami di prendere il pane");
	}
}