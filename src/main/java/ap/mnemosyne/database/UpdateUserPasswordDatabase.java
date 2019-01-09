package ap.mnemosyne.database;

import ap.mnemosyne.resources.User;

import java.sql.*;

public class UpdateUserPasswordDatabase
{
	private final String stmt = "UPDATE mnemosyne.user SET password=crypt(?, gen_salt('bf')) WHERE email=?";
	private final User u;
	private final Connection conn;

	public UpdateUserPasswordDatabase(Connection conn, User u)
	{
		this.u = u;
		this.conn = conn;
	}

	public User updateUser() throws SQLException
	{
		User ret = null;
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, u.getPassword());
			pstmt.setString(2, u.getEmail());
			int rows = pstmt.executeUpdate();

			if(rows > 0)
				ret = new User(u.getSessionID(), u.getEmail(),null);

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
