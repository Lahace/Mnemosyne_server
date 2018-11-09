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
import java.util.Scanner;

public class OpenRoutePlaces implements PlacesProvider
{

	private final String timeUrl = "https://api.openrouteservice.org/matrix";

	@Override
	public List<Place> getPlacesFromQuery(String query) throws RuntimeException, NoDataReceivedException
	{
		return new ArrayList<>();
	}

	@Override
	public Place getPlaceFromPoint(Point point) throws NoDataReceivedException
	{
		return null;
	}

	@Override
	public int getMinutesToDestination(Point from, Point to) throws NoDataReceivedException
	{
		URL keyUrl = System.class.getResource("/APIKeys/openroute.key");
		String apiKey = null;
		try
		{
			File key = new File(keyUrl.toURI());
			Scanner s = new Scanner(key);
			while(s.hasNext())
				apiKey = s.next();
		}
		catch (URISyntaxException | FileNotFoundException e)
		{
			e.printStackTrace();
		}

		try
		{
			CloseableHttpClient httpclient = getHttpClient();
			URIBuilder uri = new URIBuilder(timeUrl)
					.addParameter("api_key", apiKey)
					.addParameter("profile", "driving-car") //Always assume that you're driving
					.addParameter("locations", from.getLon() + "," + from.getLat() + "|" + to.getLon() + "," + to.getLat());

			HttpGet httpGet = new HttpGet(uri.toString());
			CloseableHttpResponse resp = httpclient.execute(httpGet);
			ResponseHandler<String> handler = new BasicResponseHandler();

			String body = handler.handleResponse(resp);
			ObjectMapper map = new ObjectMapper();
			JsonNode obj = map.readTree(body);
			if(obj.size() == 0) throw new NoDataReceivedException("No data received with this query");

			JsonNode duration = obj.get("durations");
			double timeDouble = Double.parseDouble(duration.get(0).get(1).toString());
			return (int) (timeDouble/60);
		}
		catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException | URISyntaxException e)
		{
			throw new RuntimeException(e);
		}

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
