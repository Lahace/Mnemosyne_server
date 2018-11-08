package ap.mnemosyne.database;

import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.*;
import org.postgresql.geometric.PGpoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class GetUserDefinedParameterByNameDatabase
{
	private final String stmt = "SELECT * FROM mnemosyne.defines WHERE email=? AND parameter=?";
	private final Connection conn;
	private final User user;
	private final ParamsName param;

	public GetUserDefinedParameterByNameDatabase(Connection conn, User user, ParamsName param)
	{
		this.conn = conn;
		this.user = user;
		this.param = param;
	}

	public Parameter getUserDefinedParameterByName() throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Parameter ret = null;

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
					ret = new LocationParameter(param, user.getEmail(), new Point(point.x, point.y), rs.getInt("location_cellID"), rs.getString("location_SSID"));
				}
				else if(rs.getString("type").equals("time"))
				{
					LocalTime to = null;
					try
					{
						to = rs.getTime("to_time").toLocalTime();
					}
					catch (DateTimeParseException dtpe)
					{
						//ignore
					}
					ret = new TimeParameter(param, user.getEmail(), rs.getTime("from_time").toLocalTime(), to);
				}
				else
				{
					//should never happen
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

