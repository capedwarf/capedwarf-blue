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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;

import com.google.appengine.api.files.FileWriteChannel;

/**
 * JBoss file write channel.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class JBossFileWriteChannel implements FileWriteChannel
{
   private final WritableByteChannel delegate;

   JBossFileWriteChannel(OutputStream out)
   {
      delegate = Channels.newChannel(out);
   }

   public int write(ByteBuffer byteBuffer, String s) throws IOException
   {
      return 0;  // TODO
   }

   public void closeFinally() throws IllegalStateException, IOException
   {
      // TODO
   }

   public int write(ByteBuffer src) throws IOException
   {
      return delegate.write(src);
   }

   public boolean isOpen()
   {
      return delegate.isOpen();
   }

   public void close() throws IOException
   {
      delegate.close();
   }
}
