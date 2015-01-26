package org.jboss.capedwarf.users;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.capedwarf.appidentity.CapedwarfHttpServletRequestWrapper;
import org.jboss.capedwarf.common.servlet.ServletUtils;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public abstract class AuthHandler {

    public abstract void handleLoginRequest(HttpServletRequest request, HttpServletResponse response) throws IOException;

    public void handleLogoutRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            request.getSession().removeAttribute(CapedwarfHttpServletRequestWrapper.USER_PRINCIPAL_SESSION_ATTRIBUTE_KEY);

            ServletUtils.handleResponse(response);

            String destinationUrl = request.getParameter(AuthServlet.DESTINATION_URL_PARAM);
            response.sendRedirect(destinationUrl);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleOpenIDCallBackRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    protected void setupUserPrincipal(HttpServletRequest request, String email, String userId, String authDomain, boolean isAdmin) {
        request.getSession().setAttribute(
            CapedwarfHttpServletRequestWrapper.USER_PRINCIPAL_SESSION_ATTRIBUTE_KEY,
            new CapedwarfUserPrincipal(userId, email, authDomain, isAdmin)
        );
    }
}
