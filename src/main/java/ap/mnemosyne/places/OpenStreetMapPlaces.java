package ap.mnemosyne.places;

import ap.mnemosyne.resources.Place;
import ap.mnemosyne.resources.Point;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class OpenStreetMapPlaces implements PlacesProvider
{

	private static long lastRequest = 0;
	private final static long msTimeBetweenRequests = 1000;
	private final String requestUrl = "https://nominatim.openstreetmap.org/search";

	@Override
	public List<Point> getPointsFromQuery(String query) throws RuntimeException
	{
		long timeWait = System.currentTimeMillis()-lastRequest-msTimeBetweenRequests;

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

		try
		{
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, (chain, authType) -> true);
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder.build(), NoopHostnameVerifier.INSTANCE);
			CloseableHttpClient httpclient =  HttpClients.custom().setSSLSocketFactory(sslsf).build();
			
			URIBuilder uri = new URIBuilder(requestUrl)
					.addParameter("q", query)
					.addParameter("format", "json")
					.addParameter("limit", "400")
					.addParameter("addressdetails", "1");

			HttpGet httpGet = new HttpGet(uri.toString());
			CloseableHttpResponse resp = httpclient.execute(httpGet);

			System.out.println(resp.getStatusLine());
		}
		catch (KeyManagementException | NoSuchAlgorithmException | KeyStoreException | IOException | URISyntaxException e)
		{
			throw new RuntimeException(e);
		}

		lastRequest = System.currentTimeMillis();
		return null;
	}

	@Override
	public Place getPlaceFromLatLon(double lat, double lon)
	{
		long timeWait = System.currentTimeMillis()-lastRequest-msTimeBetweenRequests;

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

		lastRequest = System.currentTimeMillis();
		return null;
	}
}
