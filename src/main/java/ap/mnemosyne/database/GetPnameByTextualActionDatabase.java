package ap.mnemosyne.database;

import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.parser.resources.TextualAction;
import ap.mnemosyne.util.Tuple;
import org.apache.xpath.operations.Bool;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class GetPnameByTextualActionDatabase
{
	private final String stmt = "SELECT * FROM mnemosyne.requires WHERE verb=? AND item=?";
	private final Connection conn;
	private final TextualAction ta;

	public GetPnameByTextualActionDatabase(Connection conn, TextualAction ta)
	{
		this.conn = conn;
		this.ta = ta;
	}

	public Tuple<ParamsName, Boolean> getPnameByTextualAction() throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Tuple<ParamsName, Boolean> ret = null;

		try
		{
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, ta.getVerb());
			pstmt.setString(2, ta.getSubject());

			rs = pstmt.executeQuery();

			while (rs.next()) {
				ret = new Tuple<>(ParamsName.valueOf(rs.getString("parameter")), rs.getBoolean("critical"));
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

