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

package org.jboss.capedwarf.tasks;

import java.io.Serializable;

import com.google.appengine.api.taskqueue.RetryOptions;
import com.google.appengine.api.taskqueue.TaskOptions;
import org.hibernate.search.annotations.Analyze;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.Norms;
import org.hibernate.search.annotations.NumericField;
import org.hibernate.search.annotations.ProvidedId;
import org.hibernate.search.annotations.TermVector;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@Indexed
@ProvidedId
public class TaskOptionsEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String queue;
    private String tag;
    private Long eta;
    private TaskOptions options;
    private RetryOptions retry;

    public TaskOptionsEntity() {
    }

    public TaskOptionsEntity(String name, String queue, String tag, Long eta, TaskOptions options, RetryOptions retry) {
        this.name = name;
        this.queue = queue;
        this.tag = tag;
        this.eta = eta;
        this.options = options;
        this.retry = retry;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Field(analyze = Analyze.NO, norms = Norms.NO, termVector = TermVector.NO)
    public String getQueue() {
        return queue;
    }

    public void setQueue(String queue) {
        this.queue = queue;
    }

    @Field(analyze = Analyze.NO, norms = Norms.NO, termVector = TermVector.NO)
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @NumericField
    @Field
    public Long getEta() {
        return eta;
    }

    public void setEta(Long eta) {
        this.eta = eta;
    }

    public TaskOptions getOptions() {
        return options;
    }

    public void setOptions(TaskOptions options) {
        this.options = options;
    }

    public RetryOptions getRetry() {
        return retry;
    }

    public void setRetry(RetryOptions retry) {
        this.retry = retry;
    }
}
