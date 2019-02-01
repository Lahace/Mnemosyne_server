package ap.mnemosyne.places;

import ap.mnemosyne.database.CreatePlaceCacheRecordDatabase;
import ap.mnemosyne.database.DeletePlaceCacheRecordDatabase;
import ap.mnemosyne.database.GetPlaceCacheRecordByQueryAndProviderDatabase;
import ap.mnemosyne.exceptions.NoDataReceivedException;
import ap.mnemosyne.resources.Place;
import ap.mnemosyne.resources.PlaceCacheRecord;
import ap.mnemosyne.resources.Point;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.joda.time.*;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.sql.DataSource;
import java.io.*;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class OpenStreetMapPlaces implements PlacesProvider
{

	private static Long lastRequest = 0L;
	private final static long msTimeBetweenRequests = 1000;
	private final String requestUrl = "https://nominatim.openstreetmap.org/search";
	private final String requestUrlReverse = "https://nominatim.openstreetmap.org/reverse";
	private final String PROVIDER_CACHE_ID = "OpenStreetMap";
	private final Logger LOGGER = Logger.getLogger(OpenStreetMapPlaces.class.getName());

	@Override
	public List<Place> getPlacesFromQuery(String query) throws RuntimeException, NoDataReceivedException
	{
		List<Place> toRet = new ArrayList<>();
		long time = System.currentTimeMillis();
		LOGGER.info("Resolving places from query... Checking cache");
		try
		{
			PlaceCacheRecord pcr = (new GetPlaceCacheRecordByQueryAndProviderDatabase(getDataSource().getConnection(), query, PROVIDER_CACHE_ID))
					.getPlaceCacheRecordByQueryAndProvider();
			if(pcr != null)
			{
				if(Months.monthsBetween(pcr.getResponseDate(), DateTime.now()).getMonths() == 0)
				{
					LOGGER.info("Cache hit");
					ObjectMapper map = new ObjectMapper();
					JsonNode obj = map.readTree(pcr.getResponse());
					for (JsonNode node : obj)
					{
						toRet.add(computePlace(node));
					}
					LOGGER.info("Query solved in " + (System.currentTimeMillis()-time) + "ms");
					return toRet;
				}
				else
				{
					new DeletePlaceCacheRecordDatabase(getDataSource().getConnection(), pcr.getQuery(), pcr.getProvider())
							.deletePlaceCacheRecordDatabase();
					LOGGER.info("Cache record expired, proceeding in sending a request...");
				}
			}
			else
			{
				LOGGER.info("Cache miss, proceeding in sending a request...");
			}
		}
		catch (SQLException | ServletException | IOException e)
		{
			e.printStackTrace();
		}

		LOGGER.info("Resolving places from query... waiting");
		long timeWait = 0;

		//TODO: ADD QUEUE FOR REQUESTS!
		//This next block is totally not working, it needs to allow requests to be sent with 1s delay between them but it does not
		while (true)
		{
			synchronized (lastRequest)
			{
				timeWait = System.currentTimeMillis() - lastRequest - msTimeBetweenRequests;
			}

			if (timeWait < 0)
			{
				try
				{
					Thread.sleep(-1 * timeWait);
				}
				catch (InterruptedException e)
				{
					//ignored
				}
			}
			else
			{
				break;
			}
		}
		LOGGER.info("Resolving places from query");
		try
		{

			CloseableHttpClient httpclient = getHttpClient();
			
			URIBuilder uri = new URIBuilder(requestUrl)
					.addParameter("q", query)
					.addParameter("format", "jsonv2")
					.addParameter("limit", "400")
					.addParameter("addressdetails", "1")
					.addParameter("extratags", "1");

			HttpGet httpGet = new HttpGet(uri.toString());
			CloseableHttpResponse resp = httpclient.execute(httpGet);
			ResponseHandler<String> handler = new BasicResponseHandler();

			String body = handler.handleResponse(resp);
			synchronized (lastRequest)
			{
				lastRequest = System.currentTimeMillis();
			}
			try
			{
				LOGGER.info("PlacesFromQuery response: " + body.substring(0, 2000) + "...");
			}
			catch(StringIndexOutOfBoundsException e)
			{
				LOGGER.info("PlacesFromQuery response: " + body);
			}
			ObjectMapper map = new ObjectMapper();
			JsonNode obj = map.readTree(body);
			if((new CreatePlaceCacheRecordDatabase(getDataSource().getConnection(), new PlaceCacheRecord(query, PROVIDER_CACHE_ID, DateTime.now(), body)))
					.createPlaceCacheRecord())
			{
				LOGGER.info("Cache record written");
			}
			else
			{
				LOGGER.warning("Error writing cache record");
			}
			if(obj.size() == 0) throw new NoDataReceivedException("OpenStreetMaps: No data received with this query (" + query + ")");

			for(JsonNode node : obj)
			{
				toRet.add(computePlace(node));
			}
		}
		catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException | URISyntaxException | ServletException e)
		{
			LOGGER.info("RUNTIME EXCEPTION " + e.getMessage());
			throw new RuntimeException(e);
		}
		catch(SQLException sqle)
		{
			LOGGER.warning("SQL EXCEPTION" + sqle.getMessage());
			sqle.printStackTrace();
		}
		LOGGER.info("Query solved in " + (System.currentTimeMillis()-time) + "ms");
		return toRet;
	}

	@Override
	public Place getPlaceFromPoint(Point point) throws NoDataReceivedException
	{
		LOGGER.info("Resolving place from point.. waiting");
		long timeWait;
		while (true)
		{
			synchronized (lastRequest)
			{
				timeWait = System.currentTimeMillis() - lastRequest - msTimeBetweenRequests;
			}

			if (timeWait < 0)
			{
				try
				{
					Thread.sleep(-1 * timeWait);
				}
				catch (InterruptedException e)
				{
					//ignored
				}
			}
			else
			{
				break;
			}
		}

		LOGGER.info("Resolving place from point");
		Place p = null;
		try {

			CloseableHttpClient httpclient = getHttpClient();
			URIBuilder uri = new URIBuilder(requestUrlReverse)
					.addParameter("format", "jsonv2")
					.addParameter("lat", Double.toString(point.getLat()))
					.addParameter("lon", Double.toString(point.getLon()))
					.addParameter("addressdetails", "1")
					.addParameter("extratags", "1");

			HttpGet httpGet = new HttpGet(uri.toString());
			CloseableHttpResponse resp = httpclient.execute(httpGet);
			ResponseHandler<String> handler = new BasicResponseHandler();

			String body = handler.handleResponse(resp);
			synchronized (lastRequest)
			{
				lastRequest = System.currentTimeMillis();
			}
			try
			{
				LOGGER.info("PlacesFromQuery response: " + body.substring(0, 200) + "...");
			}
			catch(StringIndexOutOfBoundsException e)
			{
				LOGGER.info("PlacesFromQuery response: " + body);
			}
			ObjectMapper map = new ObjectMapper();
			JsonNode node = map.readTree(body);
			if(node.size() == 0) throw new NoDataReceivedException("OpenStreetMaps: No data received with this lat/lon " + node);

			p = computePlace(node);

		}
		catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException | URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
		catch (IndexOutOfBoundsException e)
		{
			throw new NoDataReceivedException("OpenStreetMaps: No data received with this lat/lon");
		}

		if(p.getTown() == null)
		{
			//If coordinates are off, no town is returned
			throw new NoDataReceivedException("OpenStreetMaps: Invalid data received");
		}
		return p;
	}

	private Place computePlace(JsonNode node)
	{
		JsonNode address = node.get("address");
		String name = null;
		if(node.get("type") != null)
		{
			name = address.get(node.get("type").asText()) != null ? address.get(node.get("type").asText()).asText() : null;
		}

		String town = null;
		if(address.get("town") == null)
		{
			if(address.get("village") == null)
			{
				if(address.get("city") != null)
				{
					town = address.get("city").asText();
				}
			}
			else
			{
				town = address.get("village").asText();
			}
		}
		else
		{
			town = address.get("town").asText();
		}
		int houseNumber = -1;
		try{ houseNumber = address.get("house_number").asInt();} catch (NullPointerException npe){} //Ignore
		Place p = new Place(address.get("country")!=null ? address.get("country").asText() : null,
				address.get("state")!=null ? address.get("state").asText() : null,
				town,
				address.get("suburb")!=null ? address.get("suburb").asText() : null,
				address.get("road")!=null ? address.get("road").asText() : null,
				houseNumber,
				name,
				node.get("type")!=null ? node.get("type").asText() : null,
				new Point(node.get("lat").asDouble(), node.get("lon").asDouble()),
				null,
				null);
		return p;
	}

	@Override
	public int getMinutesToDestination(Point from, Point to) throws NoDataReceivedException
	{
		return -1;
	}

	private CloseableHttpClient getHttpClient() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException
	{
		//allowing every certificate without verification
		//TODO: improve certificate management
		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(null, (chain, authType) -> true);
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
		return HttpClients.custom().setSSLSocketFactory(sslsf).build();
	}

	private DataSource getDataSource() throws ServletException
	{
		InitialContext cxt;
		DataSource ds;

		try {
			cxt = new InitialContext();
			ds = (DataSource) cxt.lookup("java:/comp/env/jdbc/mnemosyne");
		} catch (NamingException e) {
			ds = null;

			throw new ServletException(
					String.format("Impossible to access the connection pool to the database: %s",
							e.getMessage()));
		}
		return ds;
	}
}
