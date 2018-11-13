package ap.mnemosyne.places;

import ap.mnemosyne.exceptions.NoDataReceivedException;
import ap.mnemosyne.resources.Place;
import ap.mnemosyne.resources.Point;

import java.util.*;
import java.util.logging.Logger;

public class PlacesManager
{
	final Set<PlacesProvider> classList;
	private final Logger LOGGER = Logger.getLogger(PlacesManager.class.getName());

	public PlacesManager()
	{
		//TODO: load array with config file
		classList = new HashSet<>();
		classList.add(new OpenStreetMapPlaces());
		classList.add(new OpenRoutePlaces());
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
				LOGGER.info(p.getClass().getSimpleName() + " gave no results with query: " + query);
			}
		}
		return toRet;
	}

	public Place getPlacesFromPoint(Point point) throws NoDataReceivedException
	{
		//TODO: choose the best provider for places
		Place ret = null;
		for(PlacesProvider p : classList)
		{
			Place temp = p.getPlaceFromPoint(point);
			if(temp==null) LOGGER.info(p.getClass().getSimpleName()+ " gave no results with point: " + point);
			else ret = temp;
		}

		return ret;
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
