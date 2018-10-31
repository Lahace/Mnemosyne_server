package ap.mnemosyne.places;

import ap.mnemosyne.resources.Point;
import org.junit.Test;

public class PlacesManagerTest
{
	@Test
	public void testPlaces()
	{
		System.out.println(new OpenStreetMapPlaces().getPlaceFromLatLon(new Point(45.7151009, 11.3585446)));
	}
}