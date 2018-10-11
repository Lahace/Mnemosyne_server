package ap.mnemosyne.database;

import ap.mnemosyne.resources.Animale;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class SearchAnimalsDatabase
{

	public final String stmt = "SELECT * FROM pontinialb.animali WHERE categoria=?";
	
	public final Connection conn;
	
	public SearchAnimalsDatabase(final Connection conn)
	{
		this.conn = conn;
	}
	
	public List<Animale> searchAnimals(String categoria) throws SQLException
	{
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		// the create employee
		List<Animale> l = new ArrayList<Animale>();

		try {
			pstmt = conn.prepareStatement(stmt);
			pstmt.setString(1, categoria);

			rs = pstmt.executeQuery();

			while (rs.next()) {
				l.add(new Animale(rs.getInt("id"), rs.getString("razza"), rs.getString("categoria"), rs.getInt("anni")));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}

			if (pstmt != null) {
				pstmt.close();
			}

			conn.close();
		}

		return l;
	}

}
