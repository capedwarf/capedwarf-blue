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

package org.jboss.capedwarf.appidentity;

import java.io.IOException;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFailureException;
import com.google.appengine.api.appidentity.PublicCertificate;
import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;
import org.jboss.capedwarf.common.app.Application;
import org.jboss.capedwarf.common.compatibility.CompatibilityUtils;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.environment.Environment;
import org.jboss.capedwarf.environment.EnvironmentFactory;
import org.jboss.capedwarf.shared.compatibility.Compatibility;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CapedwarfAppIdentityService implements AppIdentityService {
    public static final String MEMCACHE_NAMESPACE = "_ah_";
    public static final String MEMCACHE_KEY_PREFIX = "_ah_app_identity_";
    public static final long OFFSET = 300000L;
    public static final long VALID = 6 * 30 * 24 * 60 * 60 * 1000L;

    public SigningResult signForApp(byte[] bytes) {
        rotateCertificatesIfNeeded();
        CertificateBundle bundle = getCertificateStore().getCurrentBundle();
        byte[] signature = sign(bytes, bundle.getPrivateKey());
        return new SigningResult(bundle.getName(), signature);
    }

    public Collection<PublicCertificate> getPublicCertificatesForApp() {
        rotateCertificatesIfNeeded();
        Collection<PublicCertificate> certificates = new ArrayList<PublicCertificate>();
        for (CertificateBundle bundle : getCertificateStore().getAllBundles()) {
            if (!bundle.isStale()) {
                certificates.add(new PublicCertificate(bundle.getName(), convertToPEMFormat(bundle.getCertificate())));
            }
        }
        return certificates;
    }

    private void rotateCertificatesIfNeeded() {
        if (rotationNeeded()) {
            rotateCertificates();
        }
    }

    private boolean rotationNeeded() {
        CertificateBundle bundle = getCertificateStore().getCurrentBundle();
        return bundle == null || bundle.isStale();
    }

    public void rotateCertificates() {
        CertificateStore certificateStore = getCertificateStore();
        certificateStore.store(createNewCertificate());
        certificateStore.removeStaleCertificates();
    }

    private CertificateStore getCertificateStore() {
        return CertificateStoreFactory.getCertificateStore();
    }

    private CertificateBundle createNewCertificate() {
        KeyPair keyPair = CertificateGenerator.getInstance().generateKeyPair();
        String domain = EnvironmentFactory.getEnvironment().getDomain();
        X509Certificate certificate = CertificateGenerator.getInstance().generateCertificate(keyPair, domain);
        return new CertificateBundle(certificate.getSerialNumber().toString(), keyPair, certificate);
    }

    private byte[] sign(byte[] bytes, PrivateKey privateKey) {
        try {
            Signature dsa = Signature.getInstance("SHA256WithRSA", "BC");
            dsa.initSign(privateKey);
            dsa.update(bytes);
            return dsa.sign();

        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | SignatureException e) {
            throw new AppIdentityServiceFailureException("Cannot sign: " + e);
        }
    }

    public String convertToPEMFormat(final X509Certificate certificate) {
        try {
            StringWriter stringWriter = new StringWriter();
            PemWriter pemWriter = new PemWriter(stringWriter);
            pemWriter.writeObject(new PemObject(certificate.getType(), certificate.getEncoded()));
            pemWriter.flush();
            return stringWriter.toString();

        } catch (IOException | CertificateEncodingException e) {
            throw new RuntimeException("Cannot format certificate to PEM format", e);
        }
    }

    public String getServiceAccountName() {
        final String appId = Application.getAppId();
        final String domain = EnvironmentFactory.getEnvironment().getDomain();
        return appId + "@" + domain;
    }

    public GetAccessTokenResult getAccessTokenUncached(final Iterable<String> scopes) {
        // TODO -- proper token, expDate generation
        final String token = UUID.randomUUID().toString();
        final Date now = new Date();
        final Date expDate = new Date(now.getTime() + VALID);
        return new GetAccessTokenResult(token, expDate);
    }

    public GetAccessTokenResult getAccessToken(final Iterable<String> scopes) {
        final MemcacheService cache = MemcacheServiceFactory.getMemcacheService(MEMCACHE_NAMESPACE);
        final String key = toKey(scopes);
        GetAccessTokenResult result = (GetAccessTokenResult) cache.get(key);
        if (result == null) {
            result = getAccessTokenUncached(scopes);
            final Date expDate = new Date(result.getExpirationTime().getTime() - OFFSET);
            cache.put(key, result, Expiration.onDate(expDate));
        }
        return result;
    }

    public ParsedAppId parseFullAppId(String fullAppId) {
        Environment env = EnvironmentFactory.getEnvironment();
        final String partition = env.getPartition();
        final String domain = env.getDomain();
        final String appId = Application.getAppId();
        return ReflectionUtils.newInstance(
                ParsedAppId.class,
                new Class[]{String.class, String.class, String.class},
                new Object[]{partition, domain, appId}
        );
    }

    public String getDefaultGcsBucketName() {
        String value = CompatibilityUtils.getInstance().getValue(Compatibility.Feature.DEFAULT_GCS_BUCKET_NAME);
        return (value != null) ? value : "CAPEDWARF_GCS_BUCKET";
    }

    protected static String toKey(final Iterable<String> scopes) {
        final Iterator<String> iter = scopes.iterator();
        if (iter.hasNext() == false)
            return MEMCACHE_KEY_PREFIX + "[]";

        final StringBuilder builder = new StringBuilder(MEMCACHE_KEY_PREFIX + "[");
        while (true) {
            builder.append(iter.next());
            if (iter.hasNext() == false)
                return builder.append(']').toString();
            builder.append(", ");
        }
    }
}
