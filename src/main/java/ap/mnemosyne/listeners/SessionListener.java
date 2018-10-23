package ap.mnemosyne.listeners;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.HashMap;

public class SessionListener implements HttpSessionListener
{
	public static HashMap<String, HttpSession> map = new HashMap<>();

	@Override
	public void sessionCreated(HttpSessionEvent arg0)
	{
		map.put(arg0.getSession().getId(), arg0.getSession());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0)
	{
		map.remove(arg0.getSession().getId());
	}
}
