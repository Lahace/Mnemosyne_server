package ap.mnemosyne.database;

import ap.mnemosyne.resources.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CheckUserCredentialsDatabase
{
	private final String stmt = "SELECT * FROM mnemosyne.user WHERE email=? AND password=crypt(?, password)";
	private final Connection conn;
	private final String email;
	private final String password;

	public CheckUserCredentialsDatabase(Connection conn, String email, String password)
	{
		this.conn = conn;
		this.email = email;
		this.password = password;

	}

	public User checkUserCredentials() throws SQLException
	{
		PreparedStatement pstmt = null;
		User u = null;
		ResultSet rs = null;
		try
		{
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, email);
			pstmt.setString(2, password);
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
