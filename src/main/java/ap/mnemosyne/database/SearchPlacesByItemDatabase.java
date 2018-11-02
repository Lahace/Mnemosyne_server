package ap.mnemosyne.database;


import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchPlacesByItemDatabase
{
	private final String stmt = "SELECT * FROM mnemosyne.found_in WHERE name=?";
	private final Connection conn;
	private final String item;

	public SearchPlacesByItemDatabase(Connection conn, String item)
	{
		this.conn = conn;
		this.item = item;
	}

	public List<String> searchPlacesByItem() throws SQLException
	{
		PreparedStatement pstmt = null;
		List<String> l = new ArrayList<>();
		ResultSet rs = null;
		try
		{
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, item);

			rs = pstmt.executeQuery();

			while (rs.next())
			{
				l.add(rs.getString("type"));
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

		return l;
	}
}
