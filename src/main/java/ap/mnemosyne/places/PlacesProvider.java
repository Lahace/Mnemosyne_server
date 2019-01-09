package ap.mnemosyne.places;

import ap.mnemosyne.exceptions.NoDataReceivedException;
import ap.mnemosyne.resources.Place;
import ap.mnemosyne.resources.Point;

import java.util.List;

public interface PlacesProvider
{
	List<Place> getPlacesFromQuery(String query) throws RuntimeException, NoDataReceivedException;
	Place getPlaceFromPoint(Point point) throws NoDataReceivedException;
	int getMinutesToDestination(Point from, Point to) throws NoDataReceivedException;
}
