/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.files;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.files.AppEngineFile;
import com.google.appengine.api.files.FileReadChannel;
import com.google.appengine.api.files.FileService;
import com.google.appengine.api.files.FileWriteChannel;
import com.google.appengine.api.files.FinalizationException;
import com.google.appengine.api.files.LockException;
import org.infinispan.io.GridFilesystem;
import org.jboss.capedwarf.common.infinispan.InfinispanUtils;

/**
 * JBoss GAE File service.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class JBossFileService implements FileService
{
   public AppEngineFile createNewBlobFile(String mimeType) throws IOException
   {
      return null; // TODO -- create random name?
   }

   public AppEngineFile createNewBlobFile(String s, String s1) throws IOException
   {
      return null;  // TODO s, s1?
   }

   public FileWriteChannel openWriteChannel(AppEngineFile file, boolean b) throws FileNotFoundException, FinalizationException, LockException, IOException
   {
      GridFilesystem gfs = InfinispanUtils.getGridFilesystem();
      return new JBossFileWriteChannel(gfs.getOutput(file.getFullPath())); // TODO b?
   }

   public FileReadChannel openReadChannel(AppEngineFile file, boolean b) throws FileNotFoundException, LockException, IOException
   {
      GridFilesystem gfs = InfinispanUtils.getGridFilesystem();
      return new JBossFileReadChannel(gfs.getInput(file.getFullPath())); // TODO b?
   }

   public BlobKey getBlobKey(AppEngineFile file)
   {
      return new BlobKey(file.getFullPath());
   }

   public AppEngineFile getBlobFile(BlobKey blobKey) throws FileNotFoundException
   {
      return new AppEngineFile(blobKey.getKeyString());
   }
}
