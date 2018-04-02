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

package org.jboss.capedwarf.admin;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

import com.google.appengine.repackaged.com.google.cron.GrocTimeSpecification;
import org.jboss.capedwarf.shared.config.ApplicationConfiguration;
import org.jboss.capedwarf.shared.config.CronEntry;
import org.jboss.capedwarf.shared.config.CronXml;

/**
 * @author Ales Justin
 */
@Named
@RequestScoped
public class CronJobs {

    private String module;
    private List<Job> jobs;

    public String getModule() {
        if (module == null) {
            module = ApplicationConfiguration.getInstance().getAppEngineWebXml().getModule();
        }
        return module;
    }

    public List<Job> getJobs() {
        if (jobs == null) {
            jobs = new ArrayList<>();
            Date current = new Date(); // so all entries use the same current date
            ApplicationConfiguration configuration = ApplicationConfiguration.getInstance();
            CronXml cronXml = configuration.getCronXml();
            for (CronEntry ce : cronXml.getEntries()) {
                String timezone = ce.getTimezone() != null ? ce.getTimezone() : "GMT";
                jobs.add(new Job(ce.getUrl(), ce.getDescription(), ce.getSchedule(), timezone, ce.getTarget(), current));
            }
        }
        return jobs;
    }

    private static GrocTimeSpecification create(String schedule, String timezone) {
        return GrocTimeSpecification.create(schedule, TimeZone.getTimeZone(timezone));
    }

    public static class Job {
        private String url;
        private String desc;
        private String schedule;
        private String timezone;
        private String target;
        private String next;

        public Job(String url, String desc, String schedule, String timezone, String target, Date current) {
            this.url = VelocityUtils.toString(url);
            this.desc = VelocityUtils.toString(desc);
            this.schedule = VelocityUtils.toString(schedule);
            this.timezone = VelocityUtils.toString(timezone);
            this.target = VelocityUtils.toString(target);
            this.next = createNext(schedule, timezone, current);
        }

        private static String createNext(String schedule, String timezone, Date current) {
            Date next = create(schedule, timezone).getMatch(current);
            return TimeFormatter.DATE_FORMAT.format(next);
        }

        public String getUrl() {
            return url;
        }

        public String getDesc() {
            return desc;
        }

        public String getSchedule() {
            return schedule;
        }

        public String getTimezone() {
            return timezone;
        }

        public String getTarget() {
            return target;
        }

        public String getNext() {
            return next;
        }
    }
}
