/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.session.impl;

import java.util.Map;
import java.util.stream.Collectors;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import ch.ethz.seb.sebserver.gbl.Constants;
import ch.ethz.seb.sebserver.gbl.model.exam.Exam;
import ch.ethz.seb.sebserver.gbl.profile.WebServiceProfile;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.ExamDAO;
import ch.ethz.seb.sebserver.webservice.servicelayer.session.SebClientConnectionService;

@Service
@WebServiceProfile
class ExamSessionControlTask {

    private static final Logger log = LoggerFactory.getLogger(ExamSessionControlTask.class);

    private final ExamDAO examDAO;
    private final SebClientConnectionService sebClientConnectionService;
    private final ExamUpdateHandler examUpdateHandler;
    private final Long examTimePrefix;
    private final Long examTimeSuffix;

    // TODO for distributed systems we need a data-base based priority and flag that the individual
    //      tasks can check and set so that only one task is actually processing and the other just checks
    //      if a task is still processing and backup of not.
    //      Possibly this can be done with a overall master service setting on the DB in table webservice_server_info

    // TODO considering to have some caching of running exams end dates here to improve performance
    //      the end check task has than only to first update missing running exams and then
    //      check the exam ending within the cached end date of the exam. if an exam has ended by
    //      applying the check to the cached value, the process can double-check if the end date has
    //      no change and update if needed or end the exam and remove from cache.

    protected ExamSessionControlTask(
            final ExamDAO examDAO,
            final SebClientConnectionService sebClientConnectionService,
            final ExamUpdateHandler examUpdateHandler,
            @Value("${sebserver.webservice.api.exam.time-prefix:3600000}") final Long examTimePrefix,
            @Value("${sebserver.webservice.api.exam.time-suffix:3600000}") final Long examTimeSuffix) {

        this.examDAO = examDAO;
        this.sebClientConnectionService = sebClientConnectionService;
        this.examUpdateHandler = examUpdateHandler;
        this.examTimePrefix = examTimePrefix;
        this.examTimeSuffix = examTimeSuffix;
    }

    @Scheduled(cron = "${sebserver.webservice.api.exam.update-interval:1 * * * * *}")
    public void examRunUpdateTask() {

        final String updateId = this.examUpdateHandler.createUpdateId();

        if (log.isDebugEnabled()) {
            log.debug("Run exam runtime update task with Id: {}", updateId);
        }

        controlExamStart(updateId);
        controlExamEnd(updateId);
    }

    @Scheduled(fixedRate = 15 * Constants.SECOND_IN_MILLIS)
    public void pingEventUpdateTask() {
        this.sebClientConnectionService.updatePingEvents();
    }

    private void controlExamStart(final String updateId) {
        if (log.isDebugEnabled()) {
            log.debug("Check starting exams: {}", updateId);
        }

        try {

            final DateTime now = DateTime.now(DateTimeZone.UTC);
            final Map<Long, String> updated = this.examDAO.allForRunCheck()
                    .getOrThrow()
                    .stream()
                    .filter(exam -> exam.startTime.minus(this.examTimePrefix).isBefore(now))
                    .map(exam -> this.examUpdateHandler.setRunning(exam, updateId))
                    .collect(Collectors.toMap(Exam::getId, Exam::getName));

            if (!updated.isEmpty()) {
                log.info("Updated exams to running state: {}", updated);
            }

        } catch (final Exception e) {
            log.error("Unexpected error while trying to update exams: ", e);
        }
    }

    private void controlExamEnd(final String updateId) {
        if (log.isDebugEnabled()) {
            log.debug("Check ending exams: {}", updateId);
        }

        try {

            final DateTime now = DateTime.now(DateTimeZone.UTC);

            final Map<Long, String> updated = this.examDAO.allForEndCheck()
                    .getOrThrow()
                    .stream()
                    .filter(exam -> exam.endTime.plus(this.examTimeSuffix).isBefore(now))
                    .map(exam -> this.examUpdateHandler.setFinished(exam, updateId))
                    .collect(Collectors.toMap(Exam::getId, Exam::getName));

            if (!updated.isEmpty()) {
                log.info("Updated exams to finished state: {}", updated);
            }

        } catch (final Exception e) {
            log.error("Unexpected error while trying to update exams: ", e);
        }
    }

}
