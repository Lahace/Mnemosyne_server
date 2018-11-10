package ap.mnemosyne.database;

import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.User;

import java.sql.*;

public class DeleteUserDefinedParameterDatabase
{
	//NOT to be used to update sessionid
	private final String stmt = "DELETE mnemosyne.defines WHERE email=? AND parameter=?";
	private final User u;
	private final Connection conn;
	private final ParamsName p;

	public DeleteUserDefinedParameterDatabase(Connection conn, User u, ParamsName p)
	{
		this.u = u;
		this.p = p;
		this.conn = conn;
	}

	public boolean deleteUserDefinedParameter() throws SQLException
	{
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, u.getEmail());
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
