package ap.mnemosyne.ontology;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.ModelFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.net.URISyntaxException;
import java.net.URL;

@Deprecated
public class OntologyEngine
{
	public final URL ONTOLOGY_LOCATION = getClass().getResource("/ontology/Ontology.owl");

	private final OntModel MODEL;
	private final String REPLACE_CHAR = "@=@";
	public final String ONTOLOGY_URI;

	private String query;

	public OntologyEngine()
	{
		MODEL = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM_RULE_INF);
		try
		{
			File ont = new File(ONTOLOGY_LOCATION.toURI());
			Reader r = new FileReader(ont);
			MODEL.read(r,null);
		}
		catch (URISyntaxException e)
		{
			e.printStackTrace();
			System.exit(-1);
		} catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		ONTOLOGY_URI = MODEL.getNsPrefixURI("");

	}
	public void prepareQuery(String query)
	{
		this.query = query;
	}

	public void setParam(String param)
	{
		query = query.replaceFirst(REPLACE_CHAR, param);
	}

	public String getQuery()
	{
		return query;
	}

	public ResultSet execute()
	{
		if(query==null) throw new QueryException("Null Query");
		Query q = QueryFactory.create(query);
		query = null;
		QueryExecution qe = QueryExecutionFactory.create(q, MODEL);
		return qe.execSelect();
	}
}
