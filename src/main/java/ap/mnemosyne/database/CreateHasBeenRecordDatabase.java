package ap.mnemosyne.database;

import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.Parameter;
import ap.mnemosyne.resources.ParserCacheRecord;
import ap.mnemosyne.resources.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;

public class CreateHasBeenRecordDatabase
{
	private final String stmt = "INSERT INTO mnemosyne.hasBeen VALUES (?, ?, ?) RETURNING *";
	private Connection conn;
	private User u;
	private ParamsName p;
	private boolean value;

	public CreateHasBeenRecordDatabase(Connection conn, User u, ParamsName p, boolean value)
	{
		this.conn = conn;
		this.u = u;
		this.p = p;
		this.value = value;
	}

	public boolean createHasBeenRecord() throws SQLException, IOException
	{
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);

			pstmt.setString(1, u.getEmail());
			pstmt.setString(2, p.toString());
			pstmt.setBoolean(3, value);
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
