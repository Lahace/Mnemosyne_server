package ap.mnemosyne.database;

import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.User;

import java.sql.*;

public class UpdateHasBeenRecordDatabase
{
	private final String stmt = "UPDATE mnemosyne.hasBeen SET beenthere=? WHERE email=? AND place=?";
	private final User u;
	private final boolean beenthere;
	private final ParamsName place;
	private final Connection conn;

	public UpdateHasBeenRecordDatabase(Connection conn, User u, ParamsName place, boolean beenthere)
	{
		this.u = u;
		this.beenthere = beenthere;
		this.place = place;
		this.conn = conn;
	}

	public boolean updateHasBeenRecordDatabase() throws SQLException
	{
		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);
			pstmt.setBoolean(1, beenthere);
			pstmt.setString(2, u.getEmail());
			pstmt.setString(3, place.toString());
			int rows = pstmt.executeUpdate();

			return rows > 0;

		}
		finally
		{
			if (pstmt != null)
			{
				pstmt.close();
			}
			conn.close();
		}
	}
}
