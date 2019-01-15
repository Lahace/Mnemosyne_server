package ap.mnemosyne.database;

import ap.mnemosyne.resources.ParserCacheRecord;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;

public class CreateParserCacheRecordDatabase
{
	private final String stmt = "INSERT INTO mnemosyne.parser_cache VALUES (?, ?, ?) RETURNING *";
	private Connection conn;
	private ParserCacheRecord pcr;

	public CreateParserCacheRecordDatabase(Connection conn, ParserCacheRecord pcr)
	{
		this.conn = conn;
		this.pcr = pcr;
	}

	public boolean createPlaceCacheRecord() throws SQLException, IOException
	{
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(pcr.getResult());
			byte[] result = baos.toByteArray();
			oos.close();
			baos.close();

			pstmt.setString(1, pcr.getTask());
			pstmt.setString(2, pcr.getVersion());
			pstmt.setBytes(3, result);
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
