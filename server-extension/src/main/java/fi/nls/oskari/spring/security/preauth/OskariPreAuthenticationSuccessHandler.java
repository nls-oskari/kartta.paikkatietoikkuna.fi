package fi.nls.oskari.spring.security.preauth;

import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.domain.Role;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.service.ServiceException;
import fi.nls.oskari.service.UserService;
import fi.nls.oskari.user.DatabaseUserService;
import fi.nls.oskari.util.JSONHelper;
import org.oskari.log.AuditLog;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;

public class OskariPreAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final DatabaseUserService userService;

    public OskariPreAuthenticationSuccessHandler() {
        this.userService = getUserService();
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        final Object principal = authentication.getPrincipal();

        if (!(principal instanceof OskariUserDetails)) {
            throw new IllegalArgumentException(
                    "Expected fi.nls.oskari.spring.security.preauth.OskariUserDetails, "
                            + "got: " + principal.getClass().getName());
        }
        OskariUserDetails oud = (OskariUserDetails) principal;

        User user = null;
        try {
            user = getUser(oud);
            if (user.getId() == -1) {
                userService.createUser(user);
                // the user returned by createUser() doesn't have roles so find the user with email
                user = userService.getUserByEmail(user.getEmail());
            } else {
                userService.modifyUser(user);
            }
        } catch (ServiceException | RuntimeException e) {
            throw new AuthenticationServiceException(
                    "Unable to load/store Oskari user data from/to PostgreSQL",
                    e);
        }

        setupSession(user, request);

        super.onAuthenticationSuccess(request, response, authentication);
    }

    private User getUser(OskariUserDetails oud) throws ServiceException {
        User user = userService.getUserByEmail(oud.getUser().getEmail());
        if(user == null) {
            user = oud.getUser();
            user.addRole(Role.getDefaultUserRole());
        } else {
            // copy data that we got from headers
            user.setFirstname(oud.getUser().getFirstname());
            user.setLastname(oud.getUser().getLastname());
            user.setScreenname(oud.getUser().getScreenname());
            // merge attributes
            JSONHelper.merge(user.getAttributesJSON(), oud.getUser().getAttributesJSON());
        }
        return user;

    }

    protected void setupSession(User authenticatedUser, HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        synchronized (session) {
            session.setAttribute(User.class.getName(), authenticatedUser);
        }
        AuditLog.user(ActionParameters.getClientIp(request), authenticatedUser.getEmail())
                .withMsg("Login")
                .updated(AuditLog.ResourceType.USER);

        // update last login
        try {
            User userToUpdate = userService.getUser(authenticatedUser.getId());
            userToUpdate.setLastLogin(OffsetDateTime.now());
            userService.modifyUser(userToUpdate);
        } catch (Exception e) {

        }


    }

    protected DatabaseUserService getUserService() {
        try {
            // throws class cast exception if configured other than intended
            return (DatabaseUserService) UserService.getInstance();
        } catch (ServiceException e) {
            throw new RuntimeException("Error getting UserService. Is it configured?", e);
        }
    }
}