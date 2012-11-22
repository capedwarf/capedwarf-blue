/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.test.capedwarf.appidentity.test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.appidentity.PublicCertificate;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import junit.framework.Assert;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.test.capedwarf.common.support.All;
import org.jboss.test.capedwarf.common.test.BaseTest;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author Marko Luksa
 */
@RunWith(Arquillian.class)
@Category(All.class)
public class AppIdentityTestCase extends BaseTest {

    @Deployment
    public static Archive getDeployment() {
        final WebArchive war = getCapedwarfDeployment();
        war.addAsWebInfResource("jboss-deployment-structure.xml");
        return war;
    }

    @Test
    public void test() {
        AppIdentityService service = AppIdentityServiceFactory.getAppIdentityService();

        AppIdentityService.SigningResult signingResult = service.signForApp("CapeDwarf".getBytes());
        byte[] signature = signingResult.getSignature();

        Assert.assertTrue(isSignedByAny(signature, service.getPublicCertificatesForApp()));
    }

    private boolean isSignedByAny(byte[] signature, Collection<PublicCertificate> certificates) {
        for (PublicCertificate certificate : certificates) {
            if (isSignedBy(signature, certificate)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSignedBy(byte[] signature, PublicCertificate certificate) {
        try {
            PemReader pemReader = new PemReader(new StringReader(certificate.getX509CertificateInPemFormat()));
            byte[] content = pemReader.readPemObject();

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(content));

            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(cert);
            verifier.update("CapeDwarf".getBytes());
            return verifier.verify(signature);

        } catch (Exception e) {
            throw new RuntimeException("Cannot verify signature", e);
        }
    }

    // Copyright (c) 2000 - 2011 The Legion Of The Bouncy Castle (http://www.bouncycastle.org)
    private static class PemReader extends BufferedReader {
        private static final String BEGIN = "-----BEGIN ";
        private static final String END = "-----END ";

        private PemReader(Reader reader) {
            super(reader);
        }

        private byte[] readPemObject() throws Exception {
            String line = readLine();

            while (line != null && line.startsWith(BEGIN) == false) {
                line = readLine();
            }

            if (line != null) {
                line = line.substring(BEGIN.length());
                int index = line.indexOf('-');
                String type = line.substring(0, index);

                if (index > 0) {
                    String endMarker = END + type;
                    StringBuilder buf = new StringBuilder();

                    while ((line = readLine()) != null) {
                        if (line.contains(":")) {
                            continue;
                        }

                        if (line.contains(endMarker)) {
                            break;
                        }

                        buf.append(line.trim());
                    }

                    if (line == null) {
                        throw new IOException(endMarker + " not found");
                    }

                    return Base64.decode(buf.toString());
                }
            }

            return null;
        }
    }
}
