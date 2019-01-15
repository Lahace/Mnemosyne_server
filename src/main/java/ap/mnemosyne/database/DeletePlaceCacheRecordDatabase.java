package ap.mnemosyne.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeletePlaceCacheRecordDatabase
{
	private final String stmt = "DELETE FROM mnemosyne.place_cache WHERE query=? AND provider=?";
	private final String query;
	private final String provider;
	private final Connection conn;

	public DeletePlaceCacheRecordDatabase(Connection conn,  String query, String provider)
	{
		this.query = query;
		this.provider = provider;
		this.conn = conn;
	}

	public boolean deletePlaceCacheRecordDatabase() throws SQLException
	{
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, query);
			pstmt.setString(2, provider);
			int rows = pstmt.executeUpdate();

			if(rows > 0)
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
