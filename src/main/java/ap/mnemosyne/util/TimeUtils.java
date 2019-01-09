package ap.mnemosyne.util;

import ap.mnemosyne.resources.Place;
import org.joda.time.LocalTime;

import java.util.Set;

public class TimeUtils
{
	public static boolean isTimeBetween(LocalTime time, LocalTime from, LocalTime to)
	{
		if(time.equals(from) || time.equals(to))
			return true;

		if(from.isAfter(to))
		{
			if (!(time.isAfter(from) && time.isBefore(to)))
				return true;
		}
		else
		{
			if (time.isAfter(from) && time.isBefore(to))
				return true;
		}

		return false;
	}

	public static Place findLatestOpenedPlace(Set<Place> pset)
	{
		Place toRet = null;
		for(Place p : pset)
		{
			if(toRet == null)
			{
				toRet = p;
			}
			else if(p.getClosing().isAfter(toRet.getClosing()))
			{
				toRet = p;
			}
		}
		return toRet;
	}

	public static Place findEarliestOpeningPlace(Set<Place> pset)
	{
		Place toRet = null;
		for(Place p : pset)
		{
			if(toRet == null)
			{
				toRet = p;
			}
			else if(p.getOpening().isBefore(toRet.getOpening()))
			{
				toRet = p;
			}
		}
		return toRet;
	}
}
