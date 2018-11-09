package ap.mnemosyne.util;

import java.time.LocalTime;

public class TimeUtils
{
	public static boolean isTimeBetween(LocalTime time, LocalTime from, LocalTime to)
	{
		if(time.isAfter(from) && time.isBefore(to))
			return true;

		return false;
	}
}
