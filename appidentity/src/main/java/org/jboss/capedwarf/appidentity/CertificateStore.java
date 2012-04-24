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

import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Stores CertificateBundles in Memcache.
 *
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class CertificateStore {

    public static final String CURRENT_BUNDLE_KEY = "_ah_current_bundle";
    public static final String ALL_BUNDLES_KEY = "_ah_all_bundles";


    public CertificateBundle getCurrentBundle() {
        return (CertificateBundle) getMemcacheService().get(CURRENT_BUNDLE_KEY);
    }

    public void putCurrentBundle(CertificateBundle bundle) {
        getMemcacheService().put(CURRENT_BUNDLE_KEY, bundle);
    }

    @SuppressWarnings("unchecked")
    public Collection<CertificateBundle> getAllBundles() {
        Collection<CertificateBundle> bundles = (Collection<CertificateBundle>) getMemcacheService().get(ALL_BUNDLES_KEY);
        return bundles == null ? new ArrayList<CertificateBundle>() : bundles;
    }

    private void putAllBundles(Collection<CertificateBundle> bundles) {
        getMemcacheService().put(ALL_BUNDLES_KEY, bundles);
    }

    private MemcacheService getMemcacheService() {
        return MemcacheServiceFactory.getMemcacheService(JBossAppIdentityService.MEMCACHE_NAMESPACE);
    }

    public void store(CertificateBundle bundle) {
        Collection<CertificateBundle> bundles = getAllBundles();
        bundles.add(bundle);
        putAllBundles(bundles);
        putCurrentBundle(bundle);
    }

    public void removeStaleCertificates() {
        Collection<CertificateBundle> bundles = getAllBundles();
        for (Iterator<CertificateBundle> iterator = bundles.iterator(); iterator.hasNext(); ) {
            CertificateBundle bundle = iterator.next();
            if (bundle.isStale()) {
                iterator.remove();
            }
        }
        putAllBundles(bundles);
    }

}
