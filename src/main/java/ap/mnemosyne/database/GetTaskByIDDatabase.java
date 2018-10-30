package ap.mnemosyne.database;

import ap.mnemosyne.resources.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class GetTaskByIDDatabase
{
	private final String stmt = "SELECT * FROM mnemosyne.task WHERE id=?";
	private final Connection conn;
	private final int id;

	public GetTaskByIDDatabase(Connection conn, int id)
	{
		this.conn = conn;
		this.id = id;
	}

	public Task getTaskByID() throws SQLException, IOException, java.lang.ClassNotFoundException
	{
		PreparedStatement pstmt = null;
		Task t = null;
		ResultSet rs = null;
		try
		{
			pstmt = conn.prepareStatement(stmt);
			pstmt.setInt(1, id);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(rs.getBytes("placesToSatisfy")));
				List<Place> list = (List<Place>) in.readObject();
				in.close();

				in = new ObjectInputStream(new ByteArrayInputStream(rs.getBytes("constr")));
				Object read = in.readObject();
				in.close();

				if(read instanceof TaskPlaceConstraint)
				{
					t = new Task(rs.getInt("id"), rs.getString("useremail"), rs.getString("name"), (TaskPlaceConstraint) read,
							rs.getBoolean("possibleAtWork"), rs.getBoolean("repeatable"), rs.getBoolean("doneToday"),
							rs.getBoolean("failed"), list);
				}
				else if(read instanceof TaskTimeConstraint)
				{
					t = new Task(rs.getInt("id"), rs.getString("useremail"), rs.getString("name"), (TaskTimeConstraint) read,
							rs.getBoolean("possibleAtWork"), rs.getBoolean("repeatable"), rs.getBoolean("doneToday"),
							rs.getBoolean("failed"), list);
				}
				else
				{
					t = new Task(rs.getInt("id"), rs.getString("useremail"), rs.getString("name"), null,
							rs.getBoolean("possibleAtWork"), rs.getBoolean("repeatable"), rs.getBoolean("doneToday"),
							rs.getBoolean("failed"), list);
				}
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

		return t;
	}

}
