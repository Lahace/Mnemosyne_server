package ap.mnemosyne.places;

import ap.mnemosyne.exceptions.NoDataReceivedException;
import ap.mnemosyne.resources.Point;
import org.junit.Test;

public class PlacesManagerTest
{
	@Test
	public void testPlaces() throws NoDataReceivedException
	{
		System.out.println(new OpenStreetMapPlaces().getPlaceFromPoint(new Point(45.703139, 11.356694)));
		//System.out.println(new OpenStreetMapPlaces().getPlacesFromQuery("bakery in schio"));
	}
}