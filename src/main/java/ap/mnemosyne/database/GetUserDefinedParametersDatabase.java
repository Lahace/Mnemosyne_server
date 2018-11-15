package ap.mnemosyne.database;

import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.*;
import org.joda.time.LocalTime;
import org.postgresql.geometric.PGpoint;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

public class GetUserDefinedParametersDatabase
{
	private final String stmt = "SELECT * FROM mnemosyne.defines WHERE email=?";
	private final Connection conn;
	private final User user;

	public GetUserDefinedParametersDatabase(Connection conn, User user)
	{
		this.conn = conn;
		this.user = user;
	}

	public Map<ParamsName,Parameter> getUserDefinedParameters() throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Map<ParamsName,Parameter> ret = new HashMap<>();

		try
		{
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, user.getEmail());

			rs = pstmt.executeQuery();

			while (rs.next())
			{
				if(rs.getString("type").equals("location"))
				{
					PGpoint point = (PGpoint) rs.getObject("location");
					ret.put(ParamsName.valueOf(rs.getString("parameter")),new LocationParameter(ParamsName.valueOf(rs.getString("parameter")), user.getEmail(), new Point(point.x, point.y),
							rs.getInt("location_cellID"), rs.getString("location_SSID")));
				}
				else if(rs.getString("type").equals("time"))
				{
					LocalTime to = null;
					try
					{
						to = new LocalTime(rs.getTime("to_time"));
					}
					catch (DateTimeParseException dtpe)
					{
						//ignore
					}
					ret.put(ParamsName.valueOf(rs.getString("parameter")),new TimeParameter(ParamsName.valueOf(rs.getString("parameter")),
							user.getEmail(), new LocalTime(rs.getTime("from_time")), to));
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

