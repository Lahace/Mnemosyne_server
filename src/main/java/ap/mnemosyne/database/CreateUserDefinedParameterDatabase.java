package ap.mnemosyne.database;

import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.*;
import org.joda.time.LocalTime;
import org.postgresql.geometric.PGpoint;

import java.sql.*;

public class CreateUserDefinedParameterDatabase
{
	private final String stmt = "INSERT INTO mnemosyne.defines(email, parameter, location, location_SSID, location_cellID, from_time, to_time) VALUES (?, ?, ?::point, ?, ?, ?, ?) RETURNING *";
	private final User u;
	private final Connection conn;
	private final Parameter p;

	public CreateUserDefinedParameterDatabase(Connection conn, User u, Parameter p)
	{
		this.u = u;
		this.p = p;
		this.conn = conn;
	}

	public Parameter createUserDefinedParameter() throws SQLException
	{
		Parameter ret = null;
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try {
			pstmt = conn.prepareStatement(stmt);
			if(p instanceof TimeParameter)
			{
				pstmt.setString(1, u.getEmail());
				pstmt.setString(2, p.getName().toString());
				pstmt.setObject(3, null);
				pstmt.setString(4, null);
				pstmt.setInt(5, -1);
				Time tfrom = new Time(((TimeParameter) p).getFromTime().toDateTimeToday().getMillis());
				pstmt.setTime(6, tfrom);
				Time tto = new Time(((TimeParameter) p).getToTime().toDateTimeToday().getMillis());
				pstmt.setTime(7, tto);
			}
			else if(p instanceof LocationParameter)
			{
				pstmt.setString(1, u.getEmail());
				pstmt.setString(2, p.getName().toString());
				PGpoint point = new PGpoint(((LocationParameter)p).getLocation().getLat(), ((LocationParameter)p).getLocation().getLon());
				pstmt.setObject(3, point);
				pstmt.setString(4, ((LocationParameter)p).getSSID());
				pstmt.setInt(5, ((LocationParameter)p).getCellID());
				pstmt.setTime(6, null);
				pstmt.setTime(7, null);
			}
			else
			{
				throw new SQLException("Unknown parameter type");
			}

			rs = pstmt.executeQuery();

			if(rs.next())
			{
				if(p instanceof TimeParameter)
				{
					ret = new TimeParameter(ParamsName.valueOf(rs.getString("parameter")), rs.getString("email"),
							new LocalTime(rs.getTime("from_time")), new LocalTime(rs.getTime("to_time")));
				}
				else
				{
					PGpoint retPoint = (PGpoint) rs.getObject("location");
					ret = new LocationParameter(ParamsName.valueOf(rs.getString("parameter")), rs.getString("email"),
							new Point(retPoint.x, retPoint.y), rs.getInt("location_cellID"), rs.getString("location_SSID"));
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
