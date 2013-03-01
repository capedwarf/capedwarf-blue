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

import java.util.concurrent.TimeUnit;

import com.google.appengine.api.taskqueue.LeaseOptions;
import org.jboss.capedwarf.common.reflection.ReflectionUtils;
import org.jboss.capedwarf.common.reflection.TargetInvocation;

/**
 * Lease options.
 *
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
class LeaseOptionsInternal {
    private static TargetInvocation<Long> getLease = ReflectionUtils.cacheInvocation(LeaseOptions.class, "getLease");
    private static TargetInvocation<TimeUnit> getUnit = ReflectionUtils.cacheInvocation(LeaseOptions.class, "getUnit");
    private static TargetInvocation<Long> getCountLimit = ReflectionUtils.cacheInvocation(LeaseOptions.class, "getCountLimit");
    private static TargetInvocation<byte[]> getTag = ReflectionUtils.cacheInvocation(LeaseOptions.class, "getTag");
    private static TargetInvocation<Boolean> getGroupByTag = ReflectionUtils.cacheInvocation(LeaseOptions.class, "getGroupByTag");
    private static TargetInvocation<Double> getDeadlineInSeconds = ReflectionUtils.cacheInvocation(LeaseOptions.class, "getDeadlineInSeconds");

    private long lease;
    private TimeUnit unit;
    private long countLimit;
    private byte[] tag;
    private boolean groupByTag;
    private Double deadlineInSeconds;

    static LeaseOptions toLeaseOptions(long lease, TimeUnit unit, long countLimit, byte[] tag, boolean groupByTag, Double deadlineInSeconds) {
        LeaseOptions options = LeaseOptions.Builder.withLeasePeriod(lease, unit).countLimit(countLimit);
        if (tag != null) {
            options.tag(tag);
        }
        if (groupByTag) {
            options.groupByTag();
        }
        options.deadlineInSeconds(deadlineInSeconds);
        return options;
    }

    LeaseOptionsInternal(long lease, TimeUnit unit, long countLimit, String tag) {
        this(lease, unit, countLimit, tag, false);
    }

    LeaseOptionsInternal(long lease, TimeUnit unit, long countLimit, String tag, boolean groupByTag) {
        this(lease, unit, countLimit, tag, groupByTag, null);
    }

    LeaseOptionsInternal(long lease, TimeUnit unit, long countLimit, String tag, boolean groupByTag, Double deadlineInSeconds) {
        this(lease, unit, countLimit, (tag != null) ? tag.getBytes() : null, groupByTag, deadlineInSeconds);
    }

    LeaseOptionsInternal(long lease, TimeUnit unit, long countLimit, byte[] tag, boolean groupByTag, Double deadlineInSeconds) {
        this(toLeaseOptions(lease, unit, countLimit, tag, groupByTag, deadlineInSeconds));
    }

    LeaseOptionsInternal(LeaseOptions options) {
        this.lease = invoke(getLease, options, 0L);
        this.unit = invoke(getUnit, options, TimeUnit.MILLISECONDS);
        this.countLimit = invoke(getCountLimit, options, 0L);
        this.tag = invoke(getTag, options, null);
        this.groupByTag = invoke(getGroupByTag, options, false);
        this.deadlineInSeconds = invoke(getDeadlineInSeconds, options, 0.0);
    }

    private static <T> T invoke(TargetInvocation<T> ti, LeaseOptions options, T defaultValue) {
        try {
            T result = ti.invoke(options);
            return (result != null) ? result : defaultValue;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public long getLease() {
        return lease;
    }

    public TimeUnit getUnit() {
        return unit;
    }

    public long getCountLimit() {
        return countLimit;
    }

    public byte[] getTag() {
        return tag;
    }

    public String getTagAsString() {
        return (tag != null) ? new String(tag) : null;
    }

    public boolean isGroupByTag() {
        return groupByTag;
    }

    public Double getDeadlineInSeconds() {
        return deadlineInSeconds;
    }
}
