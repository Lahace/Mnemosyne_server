package ap.mnemosyne.database;

import ap.mnemosyne.resources.User;
import ap.mnemosyne.util.ServletUtils;

import java.security.NoSuchAlgorithmException;
import java.sql.*;

public class CreateUserDatabase
{
	public final String stmt = "INSERT INTO mnemosyne.user(email, password, sessionid) VALUES (?, ?, ?)";
	public final User u;
	public final Connection conn;

	public CreateUserDatabase(Connection conn, User u)
	{
		this.u = u;
		this.conn = conn;
	}

	public User CreateUser() throws SQLException, NoSuchAlgorithmException
	{
		User ret = null;
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, u.getEmail());
			pstmt.setString(2, ServletUtils.SHA256Hash(u.getPassword()));
			pstmt.setString(3, null);
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
