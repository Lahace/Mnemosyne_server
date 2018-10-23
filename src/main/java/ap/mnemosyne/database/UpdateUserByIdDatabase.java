package ap.mnemosyne.database;

import ap.mnemosyne.resources.User;

import java.sql.*;

public class UpdateUserByIdDatabase
{
	public final String stmt = "UPDATE mnemosyne.user SET password=?, sessionid=? WHERE email=?";
	public final User u;
	public final Connection conn;

	public UpdateUserByIdDatabase(Connection conn, User u)
	{
		this.u = u;
		this.conn = conn;
	}

	public User updateUserById() throws SQLException
	{
		User ret = null;
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, u.getPassword());
			pstmt.setString(2, u.getSessionID());
			pstmt.setString(3, u.getEmail());
			int rows = pstmt.executeUpdate();

			if(rows > 0)
				ret = new User(u.getSessionID(), u.getEmail(),u.getPassword());

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
