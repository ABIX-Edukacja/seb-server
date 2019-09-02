/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.batch;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BatchConfig {

    @Bean
    public JobDetail jobADetails() {
        return JobBuilder
                .newJob(SimpleBatchJob.class)
                .withIdentity("sampleJobA")
                .build();
    }

    @Bean
    public Trigger jobATrigger(final JobDetail jobADetails) {

        return TriggerBuilder
                .newTrigger()
                .forJob(jobADetails)
                .withIdentity("sampleTriggerA")

                .withSchedule(CronScheduleBuilder.cronSchedule("0/30 0 0 ? * * *"))
                .startNow()
                .build();
    }

}
