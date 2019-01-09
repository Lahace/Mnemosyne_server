package ap.mnemosyne.database;

import ap.mnemosyne.resources.User;

import java.sql.*;

public class CreateUserDatabase
{
	private final String stmt = "INSERT INTO mnemosyne.user(email, password, sessionid) VALUES (?, crypt(?, gen_salt('bf')), ?) RETURNING *";
	private final User u;
	private final Connection conn;

	public CreateUserDatabase(Connection conn, User u)
	{
		this.u = u;
		this.conn = conn;
	}

	public User CreateUser() throws SQLException
	{
		User ret = null;
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, u.getEmail());
			pstmt.setString(2, u.getPassword());
			pstmt.setString(3, null);
			rs = pstmt.executeQuery();

			if(rs.next())
				ret = new User(rs.getString("sessionid"), rs.getString("email"), null);

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
