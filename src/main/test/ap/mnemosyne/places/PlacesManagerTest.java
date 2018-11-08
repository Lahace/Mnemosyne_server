package ap.mnemosyne.places;

import ap.mnemosyne.exceptions.NoDataReceivedException;
import ap.mnemosyne.resources.Point;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;

public class PlacesManagerTest
{
	@Test
	public void testPlaces() throws NoDataReceivedException, TransformException, FactoryException
	{
		org.locationtech.jts.geom.Point uno = new Point(45.723475, 11.431567).toJTSPoint();
		org.locationtech.jts.geom.Point due = new Point(45.723594, 11.430312).toJTSPoint();
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
		System.out.println(JTS.orthodromicDistance(uno.getCoordinate(), due.getCoordinate(), sourceCRS));
		System.out.println(new OpenStreetMapPlaces().getPlaceFromPoint(new Point(45.7151009, 11.2585446)));
	}
}