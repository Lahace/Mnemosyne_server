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
		p.parseString("devo prendere il latte domani prima delle 17", false);
		p.parseString("devo prendere il latte prima di domani alle due di pomeriggio", false);
		p.parseString("devo prendere il latte prima di domani alle tre di mattina", false);
		p.parseString("devo prendere il latte prima di domani alle quattro di notte", false);
		p.parseString("devo prendere il latte prima di domani alle sette di sera", false);
		p.parseString("devo prendere il latte prima di rientrare a casa", false);
		p.parseString("devo prendere le medicine quando ceno", false);
		p.parseString("devo assumere l'insulina quando arrivo al lavoro", false);
		p.parseString("devo comprare la medicina quando arrivo al lavoro", false);
		p.parseString("devo prendere le vitamine quando arrivo al lavoro", false);
		p.parseString("devo prendere le vitamine quando esco dal lavoro", false);
		p.parseString("devo pagare il bollo quando arrivo al lavoro", false);
		p.parseString("devo prendere le vitamine prima di arrivare al lavoro", false);
		p.parseString("devo prendere le vitamine quando arrivo a casa", false);
		p.parseString("devo assumere le medicine", false);
		p.parseString("devo assumere la medicina", false);
		p.parseString("devo prendere le medicine prima di domani a mezzogiorno", false);
		p.parseString("devo pagare il bollo entro domani", false);
		p.parseString("devo prendere il pane entro le 16:30", false);
		p.parseString("devo prenotare dal dottore entro le 7:30", false);
		p.parseString("devo prenotare dal dottore prima di domani a mezzogiorno", false);
		p.parseString("devo dar da mangiare al gatto quando torno a casa", false);
		p.parseString("devo dar da mangiare al gatto quando arrivo a casa", false);
		p.parseString("devo dar da mangiare al gatto domani quando torno a casa", false);
		p.parseString("devo dare da mangiare al gatto quando arrivo a casa", false);
		p.parseString("devo fare la lavatrice quando torno a casa", false);
		p.parseString("devo mettere la crema prima di andare a letto", false);
		p.parseString("devo prendere il pane", false);
	}
}