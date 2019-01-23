package ap.mnemosyne.database;

import ap.mnemosyne.enums.ParamsName;
import ap.mnemosyne.resources.*;
import org.joda.time.LocalTime;
import org.postgresql.geometric.PGpoint;

import java.sql.*;

public class UpdateUserDefinedParameterDatabase
{
	private final String stmt = "UPDATE mnemosyne.defines SET location=?::point, location_SSID=?, location_cellID=?, from_time=?, to_time=? WHERE email=? AND " +
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
				pstmt.setString(1, null);
				pstmt.setObject(2, null);
				pstmt.setInt(3, -1);
				Time tfrom = new Time(((TimeParameter) p).getFromTime().toDateTimeToday().getMillis());
				pstmt.setTime(4, tfrom);
				Time tto = new Time(((TimeParameter) p).getToTime().toDateTimeToday().getMillis());
				pstmt.setTime(5, tto);
				pstmt.setString(6, u.getEmail());
				pstmt.setString(7, p.getName().toString());
			}
			else if(p instanceof LocationParameter)
			{

				PGpoint point = new PGpoint(((LocationParameter)p).getLocation().getLat(), ((LocationParameter)p).getLocation().getLon());
				pstmt.setObject(1, point);
				pstmt.setString(2, ((LocationParameter)p).getSSID());
				pstmt.setInt(3, ((LocationParameter)p).getCellID());
				pstmt.setTime(4, null);
				pstmt.setTime(5, null);
				pstmt.setString(6, u.getEmail());
				pstmt.setString(7, p.getName().toString());
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
