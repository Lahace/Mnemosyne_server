package ap.mnemosyne.places;

import ap.mnemosyne.exceptions.NoDataReceivedException;
import ap.mnemosyne.resources.Place;
import ap.mnemosyne.resources.Point;

import java.util.*;

public class PlacesManager
{
	final Set<PlacesProvider> classList;

	public PlacesManager()
	{
		//TODO: load array with config file
		classList = new HashSet<>();
		classList.add(new OpenStreetMapPlaces());
		classList.add(new OpenRoutePlaces());
	}

	public Set<Place> getPlacesFromQuery(String query) throws NoDataReceivedException
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

	public Place getPlacesFromPoint(Point point) throws NoDataReceivedException
	{
		//TODO: choose the best provider for places
		return classList.iterator().next().getPlaceFromPoint(point);
	}

	public int getMinutesToDestination(Point from, Point to)
	{
		List<Integer> times = new ArrayList<>();
		for(PlacesProvider p : classList)
		{
			try
			{
				int time = p.getMinutesToDestination(from,to);
				if(time>=0)
				{
					times.add(time);
				}
			}
			catch (NoDataReceivedException ndre)
			{
				//ignore
			}
		}

		//average the result
		return (times.stream().mapToInt(Integer::intValue).sum())/times.size();
	}
}
