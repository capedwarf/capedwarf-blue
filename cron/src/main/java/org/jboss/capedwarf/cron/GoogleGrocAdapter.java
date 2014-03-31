/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014, Red Hat, Inc., and individual contributors
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

package org.jboss.capedwarf.cron;

import java.util.Date;
import java.util.TimeZone;

import com.google.appengine.repackaged.com.google.cron.GrocTimeSpecification;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class GoogleGrocAdapter extends GrocAdapter {
    public GoogleGrocAdapter(String schedule, TimeZone timeZone) {
        super(schedule, timeZone);
    }

    private GrocTimeSpecification create() {
        return GrocTimeSpecification.create(getSchedule(), getTimeZone());
    }

    protected Date getStartDate() {
        GrocTimeSpecification grocTimeSpecification = create();
        return grocTimeSpecification.getMatch(new Date());
    }

    Date getFireTimeAfter(Date afterTime) {
        GrocTimeSpecification grocTimeSpecification = create();
        return grocTimeSpecification.getMatch(afterTime);
    }

    int computeNumTimesFiredBetween(Date start, Date end) {
        GrocTimeSpecification grocTimeSpecification = create();
        Date next = grocTimeSpecification.getMatch(start);
        int count = 0;
        while (next != null && next.before(end)) {
            count++;
            next = grocTimeSpecification.getMatch(next);
        }
        return count;
    }

    Date getFinalFireTime() {
        return null;
    }
}
