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

package org.jboss.test.capedwarf.appidentity;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.appidentity.PublicCertificate;
import junit.framework.Assert;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemReader;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 *
 */
@RunWith(Arquillian.class)
public class AppIdentityTestCase {

    @Deployment
    public static Archive getDeployment() {
        return ShrinkWrap.create(WebArchive.class)
            .setWebXML(new StringAsset("<web/>"))
            .addAsWebInfResource("jboss-deployment-structure.xml")
            .addAsWebInfResource("appengine-web.xml");
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
            PemObject pemObject = pemReader.readPemObject();

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(pemObject.getContent()));

            Signature verifier = Signature.getInstance("SHA1withRSA", "BC");
            verifier.initVerify(cert);
            verifier.update("CapeDwarf".getBytes());
            return verifier.verify(signature);

        } catch (Exception e) {
            throw new RuntimeException("Cannot verify signature", e);
        }
    }
}
