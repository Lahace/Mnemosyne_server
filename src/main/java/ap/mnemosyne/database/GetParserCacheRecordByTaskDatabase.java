package ap.mnemosyne.database;

import ap.mnemosyne.parser.resources.TextualTask;
import ap.mnemosyne.resources.ParserCacheRecord;
import org.joda.time.DateTime;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetParserCacheRecordByTaskDatabase
{
	private final String stmt = "SELECT * FROM mnemosyne.parser_cache WHERE task=?";
	private final Connection conn;
	private final String task;

	public GetParserCacheRecordByTaskDatabase(Connection conn, String task)
	{
		this.conn = conn;
		this.task = task;
	}

	public ParserCacheRecord getParserCacheRecordByTask() throws SQLException, IOException, ClassNotFoundException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ParserCacheRecord ret = null;

		try
		{
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, task);

			rs = pstmt.executeQuery();

			while (rs.next())
			{
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rs.getBytes("result")));
				TextualTask tt = (TextualTask) in.readObject();
				in.close();
				ret = new ParserCacheRecord(rs.getString("task"), rs.getString("version"), tt);
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
