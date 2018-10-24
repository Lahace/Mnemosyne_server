package ap.mnemosyne.filters;

import ap.mnemosyne.resources.Message;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class SessionFilter implements Filter
{
	private Message m;

	public void init(FilterConfig conf) {}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException
	{
		HttpServletRequest hreq = (HttpServletRequest) req;
		HttpServletResponse hres = (HttpServletResponse) res;

		if(hreq.getSession(false) == null || hreq.getSession(false).getAttribute("current")==null)
		{
			m = new Message("No session Found", "401", "No session is found in the server, either you have not set the JSESSIONID cookie or the session does not exists");
			hres.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			hres.setHeader("Content-Type", "application/json");
			m.toJSON(hres.getOutputStream());
		}
		else
		{
			chain.doFilter(hreq, hres);
		}
		return;
	}

	public void destroy() {}
}
