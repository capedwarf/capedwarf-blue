package org.jboss.capedwarf.users;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.capedwarf.common.servlet.ServletUtils;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class DevelopmentAuthHandler extends AuthHandler {

    public static final String EMAIL_SESSION_ATTR = "devel_login_email";

    @Override
    public void handleLoginRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        ServletUtils.handleResponse(response);

        String email = request.getParameter("email");
        if (email == null) {
            showLoginForm(request, response);
        } else {
            request.getSession().setAttribute(EMAIL_SESSION_ATTR, email);

            String userId = email;   // TODO
            String authDomain = "gmail.com";    // TODO?
            boolean isAdmin = Boolean.valueOf(request.getParameter("isAdmin"));

            setupUserPrincipal(request, email, userId, authDomain, isAdmin);

            String destination = request.getParameter(AuthServlet.DESTINATION_URL_PARAM);
            ServletUtils.forward(request, response, destination);
        }
    }

    private void showLoginForm(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = (String) request.getSession().getAttribute(EMAIL_SESSION_ATTR);
        if (email == null) {
            email = "test@example.com";
        }

        PrintWriter out = response.getWriter();
        String destinationUrl = request.getParameter(AuthServlet.DESTINATION_URL_PARAM);
        if (destinationUrl == null) {
            destinationUrl = request.getContextPath();
        }
        out.print("<html>\n" +
            "<body>\n" +
            "<form method=\"post\" style=\"text-align:center; font-family: Arial, sans-serif; font-size: 14px;\">\n" +
            "    <div style=\"width: 20em; margin: 3em auto; text-align: left; padding: 2em 4em; background-color: #C6CCD4; border: 1px solid #4A5D75\">\n" +
            "        <h3>Development login</h3>\n" +
            "        <p>\n" +
            "            <label for=\"email\" style=\"width: 4em\">Email:</label>\n" +
            "            <input type=\"text\" name=\"email\" id=\"email\" value=\"" + email + "\" style=\"padding: 0.4em\">\n" +
            "        </p>\n" +
            "        <p style=\"margin-left: 3.5em; font-size:12px\">\n" +
            "            <input type=\"checkbox\" name=\"isAdmin\" id=\"isAdmin\" value=\"true\">\n" +
            "            <label for=\"isAdmin\">Sign in as Administrator</label>\n" +
            "        </p>\n" +
            "        <input type=\"hidden\" name=\"" + AuthServlet.DESTINATION_URL_PARAM + "\" value=\"" + destinationUrl.replace("\"", "\\\"") + "\">\n" +
            "        <p style=\"margin-left: 3em\">\n" +
                "            <input name=\"action\" type=\"submit\" value=\"Log in\" id='btn-login' style=\"padding: 6px 10px\">\n" +
            "        </p>\n" +
            "    </div>\n" +
            "</form>\n" +
            "</body>\n" +
            "</html>\n");
    }
}
