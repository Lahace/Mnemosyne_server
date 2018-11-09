package ap.mnemosyne.places;

import ap.mnemosyne.exceptions.NoDataReceivedException;
import ap.mnemosyne.resources.Place;
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

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class OpenStreetMapPlaces implements PlacesProvider
{

	private static Long lastRequest = new Long(0);
	private final static long msTimeBetweenRequests = 1000;
	private final String requestUrl = "https://nominatim.openstreetmap.org/search";
	private final String requestUrlReverse = "https://nominatim.openstreetmap.org/reverse";

	@Override
	public List<Place> getPlacesFromQuery(String query) throws RuntimeException, NoDataReceivedException
	{
		long timeWait = 0;
		synchronized (lastRequest)
		{
			timeWait = System.currentTimeMillis() - lastRequest - msTimeBetweenRequests;
		}

		if(timeWait<0)
		{
			try
			{
				Thread.sleep(-1*timeWait);
			}
			catch (InterruptedException e)
			{
				//ignored
			}
		}
		List<Place> toRet = new ArrayList<>();
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
			ObjectMapper map = new ObjectMapper();
			JsonNode obj = map.readTree(body);
			if(obj.size() == 0) throw new NoDataReceivedException("OpenStreetMaps: No data received with this query (" + query + ")");

			for(JsonNode node : obj)
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
					if(address.get("village") != null)
					{
						town = address.get("village").asText();
					}
				}
				else
				{
					town = address.get("town").asText();
				}
				int houseNumber = -1;
				try{ houseNumber = address.get("house_number").asInt();}catch (NullPointerException npe){} //Ignore
				Place p = new Place(address.get("country")!=null ? address.get("country").asText() : null,
						address.get("state")!=null ? address.get("state").asText() : null,
						town,
						address.get("suburb")!=null ? address.get("suburb").asText() : null,
						houseNumber,
						name,
						node.get("type")!=null ? node.get("type").asText() : null,
						new Point(node.get("lat").asDouble(), node.get("lon").asDouble()),
						null,
						null);
				toRet.add(p);
			}
		}
		catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException | URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
		synchronized (lastRequest)
		{
			lastRequest = System.currentTimeMillis();
		}
		return toRet;
	}

	@Override
	public Place getPlaceFromPoint(Point point) throws NoDataReceivedException
	{
		long timeWait;
		synchronized (lastRequest)
		{
			timeWait = System.currentTimeMillis() - lastRequest - msTimeBetweenRequests;
		}

		if(timeWait<0)
		{
			try
			{
				Thread.sleep(-1*timeWait);
			}
			catch (InterruptedException e)
			{
				//ignored
			}
		}

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
			ObjectMapper map = new ObjectMapper();
			JsonNode node = map.readTree(body);
			if(node.size() == 0) throw new NoDataReceivedException("OpenStreetMaps: No data received with this lat/lon " + node);

			JsonNode address = node.get("address");
			String name = null;
			if(node.get("type") != null)
			{
				name = address.get(node.get("type").asText()) != null ? address.get(node.get("type").asText()).asText() : null;
			}
			int houseNumber = -1;
			String town = null;

			if(address.get("town") == null)
			{
				if(address.get("village") != null)
				{
					town = address.get("village").asText();
				}
			}
			else
			{
				town = address.get("town").asText();
			}

			try{ houseNumber = address.get("house_number").asInt();}catch (NullPointerException npe){}
			p = new Place(address.get("country")!=null ? address.get("country").asText() : null,
					address.get("state")!=null ? address.get("state").asText() : null,
					town,
					address.get("suburb")!=null ? address.get("suburb").asText() : null,
					houseNumber,
					name,
					node.get("type")!=null ? node.get("type").asText() : null,
					new Point(node.get("lat").asDouble(), node.get("lon").asDouble()),
					null,
					null);

		}
		catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException | URISyntaxException e)
		{
			throw new RuntimeException(e);
		}
		synchronized (lastRequest)
		{
			lastRequest = System.currentTimeMillis();
		}

		if(p.getTown() == null)
		{
			//If coordinates are off, no town is returned
			throw new NoDataReceivedException("OpenStreetMaps: Invalid data received");
		}
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
}
