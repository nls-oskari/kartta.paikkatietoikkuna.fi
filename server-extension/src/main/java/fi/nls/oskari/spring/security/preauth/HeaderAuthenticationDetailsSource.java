package fi.nls.oskari.spring.security.preauth;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;

public class HeaderAuthenticationDetailsSource extends WebAuthenticationDetailsSource {

	public static String getHeaderPrexif() {
		return "auth-";
	}
	/**
	 * @param context the {@code HttpServletRequest} object.
	 * @return the {@code WebAuthenticationDetails} containing information about the
	 * current request
	 */
	@Override
	public WebAuthenticationDetails buildDetails(HttpServletRequest context) {
		return new HeaderAuthenticationDetails(context, getHeaderPrexif());
	}
}