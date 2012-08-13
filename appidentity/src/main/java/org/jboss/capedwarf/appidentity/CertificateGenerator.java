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

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.asn1.x509.ExtendedKeyUsage;
import org.bouncycastle.asn1.x509.KeyPurposeId;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.asn1.x509.X509Extension;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.crypto.util.PrivateKeyFactory;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcRSAContentSignerBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;

/**
 * Generates self-signed X509 certificates and private/public key pairs.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CertificateGenerator {

    private static CertificateGenerator instance;

    private static final int KEY_SIZE = 1024;

    public CertificateGenerator() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
    }

    public KeyPair generateKeyPair() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA", "BC");
            generator.initialize(KEY_SIZE, new SecureRandom());
            return generator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Cannot generate RSA key pair", e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException("Cannot generate RSA key pair", e);
        }
    }

    public X509Certificate generateCertificate(KeyPair pair, String dn) {
        try {

            X509v3CertificateBuilder builder = new X509v3CertificateBuilder(
                new X500Name("CN=" + dn),
                BigInteger.valueOf(new SecureRandom().nextLong()),
                new Date(System.currentTimeMillis() - 10000),
                new Date(System.currentTimeMillis() + 24L*3600*1000),
                new X500Name("CN=" + dn),
                SubjectPublicKeyInfo.getInstance(pair.getPublic().getEncoded()));

            builder.addExtension(X509Extension.basicConstraints, true, new BasicConstraints(false));
            builder.addExtension(X509Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature));
            builder.addExtension(X509Extension.extendedKeyUsage, true, new ExtendedKeyUsage(KeyPurposeId.id_kp_clientAuth));

            X509CertificateHolder holder =  builder.build(createContentSigner(pair));
            Certificate certificate = holder.toASN1Structure();

            return convertToJavaCertificate(certificate);

        } catch (CertificateEncodingException e) {
            throw new RuntimeException("Cannot generate X509 certificate", e);
        } catch (OperatorCreationException e) {
            throw new RuntimeException("Cannot generate X509 certificate", e);
        } catch (CertIOException e) {
            throw new RuntimeException("Cannot generate X509 certificate", e);
        } catch (IOException e) {
            throw new RuntimeException("Cannot generate X509 certificate", e);
        } catch (CertificateException e) {
            throw new RuntimeException("Cannot generate X509 certificate", e);
        }
    }

    private X509Certificate convertToJavaCertificate(Certificate certificate) throws CertificateException, IOException {
        InputStream is = new ByteArrayInputStream(certificate.getEncoded());
        try {
            return (X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(is);
        } finally {
            is.close();
        }
    }

    private ContentSigner createContentSigner(KeyPair pair) throws IOException, OperatorCreationException {
        AlgorithmIdentifier signatureAlgorithmId = new DefaultSignatureAlgorithmIdentifierFinder().find("SHA256withRSA");
        AlgorithmIdentifier digestAlgorithmId = new DefaultDigestAlgorithmIdentifierFinder().find(signatureAlgorithmId);
        AsymmetricKeyParameter privateKey = PrivateKeyFactory.createKey(pair.getPrivate().getEncoded());

        return new BcRSAContentSignerBuilder(signatureAlgorithmId, digestAlgorithmId).build(privateKey);
    }

    public static CertificateGenerator getInstance() {
        if (instance == null) {
            instance = new CertificateGenerator();
        }
        return instance;
    }

}
