package ap.mnemosyne.places;

import ap.mnemosyne.exceptions.NoDataReceivedException;
import ap.mnemosyne.resources.Place;
import ap.mnemosyne.resources.Point;
import org.apache.http.client.ClientProtocolException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

public interface PlacesProvider
{
	List<Place> getPlacesFromQuery(String query) throws RuntimeException, NoDataReceivedException;
	Place getPlaceFromPoint(Point point) throws NoDataReceivedException;
}
