package ap.mnemosyne.database;

import ap.mnemosyne.resources.PlaceCacheRecord;
import org.joda.time.DateTime;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetPlaceCacheRecordByQueryAndProviderDatabase
{
	private final String stmt = "SELECT * FROM mnemosyne.place_cache WHERE query=? AND provider=?";
	private final Connection conn;
	private final String query;
	private final String provider;

	public GetPlaceCacheRecordByQueryAndProviderDatabase(Connection conn, String query, String provider)
	{
		this.conn = conn;
		this.query = query;
		this.provider = provider;
	}

	public PlaceCacheRecord getPlaceCacheRecordByQueryAndProvider() throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		PlaceCacheRecord ret = null;

		try
		{
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, query);
			pstmt.setString(2, provider);

			rs = pstmt.executeQuery();

			while (rs.next())
			{
				DateTime temp = new DateTime(rs.getTimestamp("response_date").getTime());
				ret = new PlaceCacheRecord(rs.getString("query"), rs.getString("provider"), temp, rs.getString("response"));
			}

		}
		finally
		{
			if (rs != null)
			{
				rs.close();
			}

			if (pstmt != null)
			{
				pstmt.close();
			}

			conn.close();
		}

		return ret;
	}
}
