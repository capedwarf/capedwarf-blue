package org.jboss.capedwarf.admin;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Matej Lazar
 */
public class UIProducers {

    @Inject
    private HttpServletRequest httpRequest;

    @Produces
    @Named("request")
    public HttpServletRequest produceRequest() {
        return httpRequest;
    }
}
