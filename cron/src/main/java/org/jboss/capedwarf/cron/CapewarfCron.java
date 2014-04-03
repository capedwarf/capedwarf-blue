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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.capedwarf.common.async.WireWrapper;
import org.jboss.capedwarf.shared.config.ApplicationConfiguration;
import org.jboss.capedwarf.shared.config.CronEntry;
import org.jboss.capedwarf.shared.config.CronXml;
import org.jboss.capedwarf.shared.util.Utils;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.ScheduleBuilder;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
public class CapewarfCron {
    private static final Logger log = Logger.getLogger(CapewarfCron.class.getName());
    private static final Properties DEFAULT_PROPERTIES;

    static {
        DEFAULT_PROPERTIES = new Properties();
        URL cronProperties = CapewarfCron.class.getResource("cron.properties");
        try {
            try (InputStream stream = cronProperties.openStream()) {
                DEFAULT_PROPERTIES.load(stream);
            }
        } catch (IOException e) {
            throw Utils.toRuntimeException(e);
        }
    }

    private Scheduler scheduler;

    private CapewarfCron() {
    }

    public static CapewarfCron create(ApplicationConfiguration configuration) {
        CapewarfCron cron = new CapewarfCron();
        cron.start(configuration);
        return cron;
    }

    private void start(ApplicationConfiguration configuration) {
        final String appId = configuration.getAppEngineWebXml().getApplication();
        final String module = configuration.getAppEngineWebXml().getModule();

        final CronXml cronXml = configuration.getCronXml();
        if (cronXml.getEntries().isEmpty()) {
            log.info(String.format("No cron jobs: %s/%s", appId, module));
            return;
        }

        try {
            SchedulerFactory factory = new StdSchedulerFactory(DEFAULT_PROPERTIES) {
                @Override
                public void initialize(Properties props) throws SchedulerException {
                    props.put(StdSchedulerFactory.PROP_SCHED_INSTANCE_NAME, String.format("%s.%s", appId, module));
                    props.put("org.quartz.threadPool.module", String.format("%s", Utils.toModule().getIdentifier()));
                    super.initialize(props);
                }
            };
            scheduler = factory.getScheduler();
            scheduler.start();

            applyCrons(cronXml);
        } catch (SchedulerException e) {
            throw Utils.toRuntimeException(e);
        }
    }

    private void applyCrons(CronXml cronXml) throws SchedulerException {
        for (CronEntry entry : cronXml.getEntries()) {
            final String timezone = entry.getTimezone() != null ? entry.getTimezone() : "GMT";
            final String name = String.format("%s@%s#%s", entry.getUrl(), entry.getSchedule(), timezone);

            Callable<Void> callable = new WireWrapper<>(new CronCallable(entry.getUrl()));
            JobDataMap map = new JobDataMap();
            map.put("callable", callable);
            JobDetail jobDetail = JobBuilder.newJob(CronJob.class).withIdentity(name).usingJobData(map).build();

            ScheduleBuilder scheduleBuilder = new GrocScheduleBuilder(new GoogleGrocAdapter(entry.getSchedule(), TimeZone.getTimeZone(timezone)));
            Trigger trigger = TriggerBuilder.newTrigger().withIdentity(name).withSchedule(scheduleBuilder).build();

            scheduler.scheduleJob(jobDetail, trigger);
        }
    }

    public void destroy() {
        if (scheduler != null) {
            try {
                try {
                    scheduler.clear();
                } finally {
                    scheduler.shutdown();
                }
            } catch (SchedulerException e) {
                log.log(Level.WARNING, String.format("Error during cron destroy."), e);
            }
        }
    }
}
