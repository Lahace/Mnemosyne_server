package ap.mnemosyne.places;

import ap.mnemosyne.exceptions.NoDataReceivedException;
import ap.mnemosyne.resources.Point;
import org.apache.jena.base.Sys;
import org.junit.Test;

public class PlacesManagerTest
{
	@Test
	public void testPlaces() throws NoDataReceivedException
	{
		//System.out.println(new OpenStreetMapPlaces().getPlacesFromQuery("Jamba a schio"));
		System.out.println(new OpenStreetMapPlaces().getPlaceFromPoint(new Point(45.7151009, 11.2585446)));
	}
}