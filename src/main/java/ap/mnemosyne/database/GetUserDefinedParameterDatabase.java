package ap.mnemosyne.database;

import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.Point;
import ap.mnemosyne.resources.User;
import javafx.util.Pair;
import org.postgresql.geometric.PGpoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;

public class GetUserDefinedParameterDatabase
{
	private final String stmt = "SELECT * FROM mnemosyne.defines WHERE email=? AND pname=?";
	private final Connection conn;
	private final User user;
	private final ParamsName param;

	public GetUserDefinedParameterDatabase(Connection conn, User user, ParamsName param)
	{
		this.conn = conn;
		this.user = user;
		this.param = param;
	}

	public Pair<Class, Object> getUserDefinedParameter() throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Pair<Class, Object> ret = null;

		try
		{
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, user.getEmail());
			pstmt.setString(2, param.toString());

			rs = pstmt.executeQuery();

			while (rs.next())
			{
				if(rs.getString("type").equals("location"))
				{
					PGpoint point = (PGpoint) rs.getObject("location");
					ret = new Pair<>(Point.class, new Point(point.x, point.y));
				}
				else if(rs.getString("type").equals("time"))
				{
					ret = new Pair<>(LocalTime.class, rs.getTime("time").toLocalTime());
				}
				else
				{
					throw new SQLException("Parameter type " + rs.getString("type") + "not known");
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

		return ret;
	}

}

