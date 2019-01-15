package ap.mnemosyne.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DeleteParserCacheRecordDatabase
{
	private final String stmt = "DELETE FROM mnemosyne.parser_cache WHERE task=?";
	private final String task;
	private final Connection conn;

	public DeleteParserCacheRecordDatabase(Connection conn,  String task)
	{
		this.task = task;
		this.conn = conn;
	}

	public boolean deleteParserCacheRecordDatabase() throws SQLException
	{
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, task);
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
