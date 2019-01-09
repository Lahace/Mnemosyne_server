package ap.mnemosyne.database;

import ap.mnemosyne.resources.User;

import java.sql.*;

public class DeleteTaskDatabase
{
	private final String stmt = "DELETE FROM mnemosyne.task WHERE id=? AND useremail=?";
	private final int id;
	private final User u;
	private final Connection conn;

	public DeleteTaskDatabase(Connection conn,  int id, User u)
	{
		this.id = id;
		this.u = u;
		this.conn = conn;
	}

	public boolean deleteTask() throws SQLException
	{
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);
			pstmt.setInt(1, id);
			pstmt.setString(2,u.getEmail());
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
