package org.jboss.capedwarf.users;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jboss.capedwarf.common.url.URLUtils;
import org.jboss.capedwarf.shared.compatibility.Compatibility;
import org.picketlink.social.standalone.openid.api.OpenIDManager;
import org.picketlink.social.standalone.openid.api.OpenIDProtocolAdapter;
import org.picketlink.social.standalone.openid.api.OpenIDRequest;
import org.picketlink.social.standalone.openid.api.exceptions.OpenIDGeneralException;

/**
 * @author <a href="mailto:mluksa@redhat.com">Marko Luksa</a>
 */
public class OpenIdProductionAuthHandler extends AuthHandler {

    private final Logger log = Logger.getLogger(getClass().getName());

    private static final String GOOGLE_OPEN_ID_SERVICE_URL = "https://www.google.com/accounts/o8/id";
    private static final String OPENID_MANAGER_KEY = "openid_manager";

    public void handleLoginRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Compatibility.enable(Compatibility.Feature.IGNORE_CAPEDWARF_SOCKETS);
        try {
            String authDomain = request.getParameter(AuthServlet.AUTH_DOMAIN_PARAM);    // TODO: what is authDomain _exactly_?

            OpenIDManager manager = getOpenIdManager(request);
            try {
                CapedwarfOpenIDProtocolAdaptor adapter = createOpenIdProtocolAdapter(request, response);
                OpenIDManager.OpenIDProviderList providers = manager.discoverProviders();
                OpenIDManager.OpenIDProviderInformation providerInfo = manager.associate(adapter, providers);
                manager.authenticate(adapter, providerInfo);

            } catch (OpenIDGeneralException e) {
                log.log(Level.SEVERE, "[OpenIDConsumerServlet] Exception in dealing with the provider:", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        } finally {
            Compatibility.disable(Compatibility.Feature.IGNORE_CAPEDWARF_SOCKETS);
        }
    }

    @Override
    public void handleOpenIDCallBackRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Compatibility.enable(Compatibility.Feature.IGNORE_CAPEDWARF_SOCKETS);
        // extract the receiving URL from the HTTP request
        try {
            OpenIDProtocolAdapter adapter = createOpenIdProtocolAdapter(request, response);
            OpenIDManager manager = getOpenIdManager(request.getSession());
            boolean authenticated = manager.verify(adapter, getStringToStringParameterMap(request), getFullRequestURL(request));
            if (authenticated) {
                try {
                    response.sendRedirect(request.getParameter(AuthServlet.DESTINATION_URL_PARAM));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (OpenIDGeneralException e) {
            throw new RuntimeException(e);
        } finally {
            Compatibility.disable(Compatibility.Feature.IGNORE_CAPEDWARF_SOCKETS);
        }
    }

    private CapedwarfOpenIDProtocolAdaptor createOpenIdProtocolAdapter(HttpServletRequest request, HttpServletResponse response) {
        return new CapedwarfOpenIDProtocolAdaptor(request, response, getReturnUrl(request));
    }

    private String getReturnUrl(HttpServletRequest request) {
        String destinationURL = request.getParameter(AuthServlet.DESTINATION_URL_PARAM);
        return AuthServlet.getServletUrl()
                + AuthServlet.CALLBACK_PATH
                + "?" + AuthServlet.DESTINATION_URL_PARAM + "=" + URLUtils.encode(destinationURL);
    }

    private String getFullRequestURL(HttpServletRequest request) {
        String queryString = request.getQueryString();
        return request.getRequestURL().toString()
            + ((queryString == null || queryString.isEmpty()) ? "" : ("?" + queryString));
    }

    private Map<String, String> getStringToStringParameterMap(HttpServletRequest request) {
        Map<String, String> map = new HashMap<String, String>();
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            map.put(entry.getKey(), entry.getValue().length > 0 ? entry.getValue()[0] : null);
        }
        return map;
    }

    private OpenIDManager getOpenIdManager(HttpServletRequest req) {
        OpenIDManager manager = getOpenIdManager(req.getSession());
        if (manager == null) {
            manager = new OpenIDManager(createOpenIdRequest(req));
            req.getSession().setAttribute(OPENID_MANAGER_KEY, manager);
        }
        return manager;
    }

    private OpenIDRequest createOpenIdRequest(HttpServletRequest req) {
        String federatedIdentity = req.getParameter(AuthServlet.FEDERATED_IDENTITY_PARAM);
        String openIdUrl = federatedIdentity == null || federatedIdentity.isEmpty() ? GOOGLE_OPEN_ID_SERVICE_URL : federatedIdentity;
        return new OpenIDRequest(openIdUrl);
    }

    private OpenIDManager getOpenIdManager(HttpSession session) {
        return (OpenIDManager) session.getAttribute(OPENID_MANAGER_KEY);
    }


}
