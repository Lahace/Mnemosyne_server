package ap.mnemosyne.places;

import org.junit.Test;

public class PlacesManagerTest
{
	@Test
	public void testPlaces()
	{
		System.out.println(new OpenStreetMapPlaces().getPointsFromQuery("panetterie a schio"));
	}
}