package ap.mnemosyne.database;

import ap.mnemosyne.resources.User;

import java.sql.*;

public class DeleteUserDatabase
{
	private final String stmt = "DELETE FROM mnemosyne.user WHERE email=?";
	private final User u;
	private final Connection conn;

	public DeleteUserDatabase(Connection conn, User u)
	{
		this.u = u;
		this.conn = conn;
	}

	public boolean deleteUser() throws SQLException
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
