package ap.mnemosyne.database;

import ap.mnemosyne.parser.resources.TextualConstraint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class GetConstraintResolveByTextualConstraintDatabase
{
	private final String stmt = "SELECT constraint_marker, timing, verb, normalized_action, constraint_word, parameter " +
			"FROM mnemosyne.wants " +
			"LEFT JOIN verb ON wants.verb=verb.word " +
			"LEFT JOIN constraint_marker ON wants.constraint_marker=constraint_marker.marker " +
			"WHERE constraint_marker=? AND verb=? AND constraint_word=?";
	private final Connection conn;
	private final TextualConstraint tc;

	public GetConstraintResolveByTextualConstraintDatabase(Connection conn, TextualConstraint tc)
	{
		this.conn = conn;
		this.tc = tc;
	}

	public Map<String, String> getConstraintResolvesByTextualAction() throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<String, String> ret = new HashMap<>();

		try
		{
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, tc.getConstraintMarker());
			pstmt.setString(2, tc.getVerb());
			pstmt.setString(3, tc.getConstraintWord());

			rs = pstmt.executeQuery();

			while (rs.next()) {
				ret.put("constraint_marker",rs.getString("constraint_marker"));
				ret.put("timing",rs.getString("timing"));
				ret.put("verb",rs.getString("verb"));
				ret.put("normalized_action",rs.getString("normalized_action"));
				ret.put("constraint_word",rs.getString("constraint_word"));
				ret.put("parameter",rs.getString("parameter"));
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

