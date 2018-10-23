package ap.mnemosyne.database;

import ap.mnemosyne.resources.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SearchUserByEmailDatabase
{
	public final String stmt = "SELECT * FROM mnemosyne.user WHERE email=?";
	public final Connection conn;
	public final String email;

	public SearchUserByEmailDatabase(Connection conn, String email)
	{
		this.conn = conn;
		this.email = email;
	}

	public User searchUserByEmail() throws SQLException
	{
		PreparedStatement pstmt = null;
		User u = null;
		ResultSet rs = null;
		try
		{
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, email);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				u = new User(rs.getString("sessionid"), rs.getString("email"), rs.getString("password"));
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

		return u;
	}

}
