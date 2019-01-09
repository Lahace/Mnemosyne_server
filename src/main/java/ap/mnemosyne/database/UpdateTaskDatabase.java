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
	private final String stmt = "UPDATE mnemosyne.task SET name=?, constr=?, possibleAtWork=?, critical=?, repeatable=?, doneToday=?, " +
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
			pstmt.setBoolean(4, t.isCritical());
			pstmt.setBoolean(5, t.isRepeatable());
			pstmt.setBoolean(6, t.isDoneToday());
			pstmt.setBoolean(7, t.isFailed());
			pstmt.setBoolean(8, t.isIgnoredToday());
			pstmt.setBytes(9, places);
			pstmt.setInt(10, t.getId());
			pstmt.setString(11, u.getEmail());


			rs = pstmt.executeQuery();

			if(rs.next())
				ret = new Task(rs.getInt("id"), rs.getString("useremail"), rs.getString("name"),
						t.getConstr(), rs.getBoolean("possibleAtWork"), rs.getBoolean("critical"), rs.getBoolean("repeatable"), rs.getBoolean("doneToday"),
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
