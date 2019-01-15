package ap.mnemosyne.database;

import ap.mnemosyne.resources.PlaceCacheRecord;

import java.sql.*;

public class CreatePlaceCacheRecordDatabase
{
	private final String stmt = "INSERT INTO mnemosyne.place_cache VALUES (?, ?, ?, ?::JSON) RETURNING *";
	private Connection conn;
	private PlaceCacheRecord pcr;

	public CreatePlaceCacheRecordDatabase(Connection conn, PlaceCacheRecord pcr)
	{
		this.conn = conn;
		this.pcr = pcr;
	}

	public boolean createPlaceCacheRecord() throws SQLException
	{
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, pcr.getQuery());
			pstmt.setString(2, pcr.getProvider());
			pstmt.setTimestamp(3, new Timestamp(pcr.getResponseDate().getMillis()));
			pstmt.setObject(4, pcr.getResponse());
			rs = pstmt.executeQuery();

			if(rs.next())
				return true;
			return false;
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
	}
}
