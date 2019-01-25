package ap.mnemosyne.database;

import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.parser.resources.TextualTask;
import ap.mnemosyne.resources.ParserCacheRecord;
import ap.mnemosyne.resources.User;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class GetHasBeenRecordsByUserDatabase
{
	private final String stmt = "SELECT * FROM mnemosyne.hasBeen WHERE email=?";
	private final Connection conn;
	private final User u;

	public GetHasBeenRecordsByUserDatabase(Connection conn, User u)
	{
		this.conn = conn;
		this.u = u;
	}

	public Map<ParamsName, Boolean> getHasBeenRecordsByUser() throws SQLException, IOException, ClassNotFoundException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ParserCacheRecord ret = null;
		Map<ParamsName, Boolean> toRet = new HashMap<>();

		try
		{
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, u.getEmail());

			rs = pstmt.executeQuery();

			while (rs.next())
			{
				toRet.put(ParamsName.valueOf(rs.getString("place")), rs.getBoolean("beenthere"));
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
		return toRet;
	}
}
