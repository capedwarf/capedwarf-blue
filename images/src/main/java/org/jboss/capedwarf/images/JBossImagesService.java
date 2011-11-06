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

package org.jboss.capedwarf.images;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.images.*;
import org.jboss.capedwarf.common.threads.ExecutorFactory;
import org.jboss.capedwarf.images.transform.JBossTransform;
import org.jboss.capedwarf.images.transform.JBossTransformFactory;
import org.jboss.capedwarf.images.util.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * @author <a href="mailto:marko.luksa@gmail.com">Marko Luksa</a>
 */
public class JBossImagesService implements ImagesService {


    public Image applyTransform(Transform transform, Image image) {
        return applyTransform(transform, image, OutputEncoding.PNG);
    }

    public Image applyTransform(Transform transform, Image image, OutputEncoding outputEncoding) {
        return applyTransform(transform, image, new OutputSettings(outputEncoding));
    }

    public Image applyTransform(Transform transform, Image image, OutputSettings outputSettings) {
        return applyTransform(transform, image, new InputSettings(), outputSettings);
    }

    public Image applyTransform(Transform transform, Image image, InputSettings inputSettings, OutputSettings outputSettings) {
        BufferedImage bufferedImage = convertToBufferedImage(image);
        BufferedImage transformedBufferedImage = applyTransform(transform, bufferedImage);
        return createImage(transformedBufferedImage, outputSettings);
    }

    private Image createImage(BufferedImage transformedBufferedImage, OutputSettings outputSettings) {
        byte[] transformedImageData = getByteArray(transformedBufferedImage, outputSettings);
        return ImagesServiceFactory.makeImage(transformedImageData);
    }

    public static BufferedImage applyTransform(Transform transform, BufferedImage bufferedImage) {
        JBossTransform jBossTransform = JBossTransformFactory.createJBossTransform(transform);
        return jBossTransform.applyTo(bufferedImage);
    }

    private BufferedImage convertToBufferedImage(Image image) {
        return ImageUtils.convertToBufferedImage(image.getImageData());
    }


    public Future<Image> applyTransformAsync(Transform transform, Image image) {
        return applyTransformAsync(transform, image, OutputEncoding.PNG);
    }

    public Future<Image> applyTransformAsync(Transform transform, Image image, OutputEncoding outputEncoding) {
        return applyTransformAsync(transform, image, new OutputSettings(outputEncoding));
    }

    public Future<Image> applyTransformAsync(Transform transform, Image image, OutputSettings outputSettings) {
        return applyTransformAsync(transform, image, new InputSettings(), outputSettings);
    }

    public Future<Image> applyTransformAsync(final Transform transform, final Image image, final InputSettings inputSettings, final OutputSettings outputSettings) {
        FutureTask<Image> task = new FutureTask<Image>(new Callable<Image>() {
            public Image call() throws Exception {
                return applyTransform(transform, image, inputSettings, outputSettings);
            }
        });
        Executor executor = ExecutorFactory.getInstance();
        executor.execute(task);
        return task;
    }


    public Image composite(Collection<Composite> composites, int width, int height, long color) {
        return composite(composites, width, height, color, OutputEncoding.PNG);
    }

    public Image composite(Collection<Composite> composites, int width, int height, long color, OutputEncoding outputEncoding) {
        return composite(composites, width, height, color, new OutputSettings(outputEncoding));
    }

    public Image composite(Collection<Composite> composites, int width, int height, long color, OutputSettings outputSettings) {
        CompositeImageBuilder compositeImageBuilder = new CompositeImageBuilder(composites, width, height, color);
        BufferedImage image = compositeImageBuilder.createBufferedImage();
        return createImage(image, outputSettings);
    }


    public int[][] histogram(Image image) {
        return ImageUtils.histogram(convertToBufferedImage(image));
    }


    public String getServingUrl(BlobKey blobKey) {
        return ImageServlet.getServingUrl(blobKey);
    }

    public String getServingUrl(BlobKey blobKey, int imageSize, boolean crop) {
        return ImageServlet.getServingUrl(blobKey, imageSize, crop);
    }


    private byte[] getByteArray(BufferedImage bufferedImage, OutputSettings outputSettings) {
        String formatName = getFormatName(outputSettings.getOutputEncoding());

        return ImageUtils.getByteArray(bufferedImage, formatName);
    }

    private String getFormatName(OutputEncoding outputEncoding) {
        switch (outputEncoding) {
            case PNG:
                return "PNG";
            case JPEG:
                return "JPG";
            case WEBP:
                return "WEBP";
            default:
                throw new IllegalArgumentException("Unsupported OutputEncoding " + outputEncoding);
        }
    }


}
