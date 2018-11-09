package ap.mnemosyne.places;

import ap.mnemosyne.exceptions.NoDataReceivedException;
import ap.mnemosyne.resources.Point;
import org.junit.Test;

public class PlacesManagerTest
{
	@Test
	public void testPlaces() throws NoDataReceivedException
	{
		//System.out.println(new OpenStreetMapPlaces().getPlaceFromPoint(new Point(45.7151009, 11.2585446)));
		System.out.println(new PlacesManager().getMinutesToDestination(new Point(45.703280,11.356476),new Point(45.723583, 11.430306)));
	}
}