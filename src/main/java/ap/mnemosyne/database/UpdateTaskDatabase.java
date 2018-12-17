package ap.mnemosyne.database;

import ap.mnemosyne.resources.Task;
import ap.mnemosyne.resources.User;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.*;

public class UpdateTaskDatabase
{
	//NOT to be used to update sessionid
	private final String stmt = "UPDATE mnemosyne.task SET name=?, constr=?, possibleAtWork=?, repeatable=?, doneToday=?, " +
			"failed=?, ignoredToday=?, placesToSatisfy=? WHERE id=? AND useremail=? RETURNING *";
	private final Task t;
	private final User u;
	private final Connection conn;

	public UpdateTaskDatabase(Connection conn, Task t, User u)
	{
		this.t = t;
		this.u = u;
		this.conn = conn;
	}

	public Task updateTask() throws SQLException, IOException
	{
		Task ret = null;
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);

			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(t.getConstr());
			byte[] constr = baos.toByteArray();
			oos.close();
			baos.close();

			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(t.getPlacesToSatisfy());
			byte[] places = baos.toByteArray();
			oos.close();
			baos.close();

			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, t.getName());
			pstmt.setBytes(2, constr);
			pstmt.setBoolean(3, t.isPossibleAtWork());
			pstmt.setBoolean(4, t.isRepeatable());
			pstmt.setBoolean(5, t.isDoneToday());
			pstmt.setBoolean(6, t.isFailed());
			pstmt.setBoolean(7, t.isIgnoredToday());
			pstmt.setBytes(8, places);
			pstmt.setInt(9, t.getId());
			pstmt.setString(10, u.getEmail());


			rs = pstmt.executeQuery();

			if(rs.next())
				ret = new Task(rs.getInt("id"), rs.getString("useremail"), rs.getString("name"),
						t.getConstr(), rs.getBoolean("possibleAtWork"), rs.getBoolean("repeatable"), rs.getBoolean("doneToday"),
						rs.getBoolean("failed"), rs.getBoolean("ignoredToday") ,t.getPlacesToSatisfy());

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
