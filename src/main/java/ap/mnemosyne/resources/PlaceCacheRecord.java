package ap.mnemosyne.resources;

import org.joda.time.DateTime;

import java.util.Objects;

public class PlaceCacheRecord
{
	private String query;
	private String provider;
	private DateTime responseDate;
	private String response;

	public PlaceCacheRecord(String query, String provider, DateTime responseDate, String response)
	{
		this.query = query;
		this.provider = provider;
		this.responseDate = responseDate;
		this.response = response;
	}

	public String getQuery()
	{
		return query;
	}

	public String getProvider()
	{
		return provider;
	}

	public DateTime getResponseDate()
	{
		return responseDate;
	}

	public String getResponse()
	{
		return response;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PlaceCacheRecord that = (PlaceCacheRecord) o;
		return Objects.equals(query, that.query) &&
				Objects.equals(provider, that.provider);
	}

	@Override
	public int hashCode()
	{
		return Objects.hash(query, provider);
	}

}
