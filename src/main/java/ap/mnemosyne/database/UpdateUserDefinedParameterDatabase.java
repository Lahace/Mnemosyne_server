package ap.mnemosyne.database;

import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.*;
import org.joda.time.LocalTime;
import org.postgresql.geometric.PGpoint;

import java.sql.*;

public class UpdateUserDefinedParameterDatabase
{
	private final String stmt = "UPDATE mnemosyne.defines SET type=?::paramType, location=?::point, location_SSID=?, location_cellID=?, from_time=?, to_time=? WHERE email=? AND " +
			"parameter=? RETURNING *";
	private final User u;
	private final Connection conn;
	private final Parameter p;

	public UpdateUserDefinedParameterDatabase(Connection conn, User u, Parameter p)
	{
		this.u = u;
		this.p = p;
		this.conn = conn;
	}

	public Parameter updateUserDefinedParameter() throws SQLException
	{
		Parameter ret = null;
		ResultSet rs = null;

		PreparedStatement pstmt = null;
		try
		{
			pstmt = conn.prepareStatement(stmt);
			if(p instanceof TimeParameter)
			{


				pstmt.setString(1, "time");
				pstmt.setString(2, null);
				pstmt.setObject(3, null);
				pstmt.setInt(4, -1);
				Time tfrom = new Time(((TimeParameter) p).getFromTime().toDateTimeToday().getMillis());
				pstmt.setTime(5, tfrom);
				Time tto = new Time(((TimeParameter) p).getToTime().toDateTimeToday().getMillis());
				pstmt.setTime(6, tto);
				pstmt.setString(7, u.getEmail());
				pstmt.setString(8, p.getName().toString());
			}
			else if(p instanceof LocationParameter)
			{

				pstmt.setString(1, "location");
				PGpoint point = new PGpoint(((LocationParameter)p).getLocation().getLat(), ((LocationParameter)p).getLocation().getLon());
				pstmt.setObject(2, point);
				pstmt.setString(3, ((LocationParameter)p).getSSID());
				pstmt.setInt(4, ((LocationParameter)p).getCellID());
				pstmt.setTime(5, null);
				pstmt.setTime(6, null);
				pstmt.setString(7, u.getEmail());
				pstmt.setString(8, p.getName().toString());
			}
			else
			{
				throw new SQLException("Unknown parameter type");
			}

			rs = pstmt.executeQuery();

			if(rs.next())
			{
				if(rs.getString("type").equals("time"))
				{
					ret = new TimeParameter(ParamsName.valueOf(rs.getString("parameter")), rs.getString("email"),
							new LocalTime(rs.getTime("from_time")), new LocalTime(rs.getTime("to_time")));
				}
				else if(rs.getString("type").equals("location"))
				{
					PGpoint retPoint = (PGpoint) rs.getObject("location");
					ret = new LocationParameter(ParamsName.valueOf(rs.getString("parameter")), rs.getString("email"),
							new Point(retPoint.x, retPoint.y), rs.getInt("location_cellID"), rs.getString("location_SSID"));
				}
				else
				{
					throw new SQLException("Unknown parameter type, someone screwed up managing DB records");
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
