package ap.mnemosyne.places;

import ap.mnemosyne.exceptions.NoDataReceivedException;
import ap.mnemosyne.resources.Place;
import ap.mnemosyne.resources.Point;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PlacesManager
{
	final Set<PlacesProvider> classList;

	public PlacesManager()
	{
		//TODO: load array with config file
		classList = new HashSet<>();

		classList.add(new OpenStreetMapPlaces());
	}

	public Set<Place> getPlacesFromQuery(String query)
	{
		Set<Place> toRet = new HashSet<>();
		for(PlacesProvider p : classList)
		{
			try
			{
				List<Place> list = p.getPlacesFromQuery(query);
				for (Place pl : list)
					toRet.add(pl);
			}
			catch (NoDataReceivedException ndre)
			{
				//ignore
			}
		}
		return toRet;
	}

	public Place getPlacesFromPoint(Point point)
	{
		//TODO: choose the best provider for places

		try
		{
			return classList.iterator().next().getPlaceFromPoint(point);
		}
		catch (NoDataReceivedException e)
		{
			return null;
		}
	}
}
