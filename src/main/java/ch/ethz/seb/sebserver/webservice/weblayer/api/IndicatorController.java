/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.weblayer.api;

import org.mybatis.dynamic.sql.SqlTable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.ethz.seb.sebserver.gbl.api.API;
import ch.ethz.seb.sebserver.gbl.api.EntityType;
import ch.ethz.seb.sebserver.gbl.api.POSTMapper;
import ch.ethz.seb.sebserver.gbl.model.Domain;
import ch.ethz.seb.sebserver.gbl.model.exam.Indicator;
import ch.ethz.seb.sebserver.gbl.profile.WebServiceProfile;
import ch.ethz.seb.sebserver.gbl.util.Result;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.mapper.IndicatorRecordDynamicSqlSupport;
import ch.ethz.seb.sebserver.webservice.servicelayer.PaginationService;
import ch.ethz.seb.sebserver.webservice.servicelayer.authorization.AuthorizationService;
import ch.ethz.seb.sebserver.webservice.servicelayer.authorization.GrantEntity;
import ch.ethz.seb.sebserver.webservice.servicelayer.bulkaction.BulkActionService;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.ExamDAO;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.IndicatorDAO;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.UserActivityLogDAO;
import ch.ethz.seb.sebserver.webservice.servicelayer.validation.BeanValidationService;

@WebServiceProfile
@RestController
@RequestMapping("/${sebserver.webservice.api.admin.endpoint}" + API.EXAM_INDICATOR_ENDPOINT)
public class IndicatorController extends EntityController<Indicator, Indicator> {

    private final ExamDAO examDao;

    protected IndicatorController(
            final AuthorizationService authorization,
            final BulkActionService bulkActionService,
            final IndicatorDAO entityDAO,
            final ExamDAO examDao,
            final UserActivityLogDAO userActivityLogDAO,
            final PaginationService paginationService,
            final BeanValidationService beanValidationService) {

        super(authorization,
                bulkActionService,
                entityDAO,
                userActivityLogDAO,
                paginationService,
                beanValidationService);

        this.examDao = examDao;
    }

    @Override
    protected Indicator createNew(final POSTMapper postParams) {
        final Long examId = postParams.getLong(Domain.INDICATOR.ATTR_EXAM_ID);

        return this.examDao
                .byPK(examId)
                .map(exam -> new Indicator(exam, postParams))
                .getOrThrow();
    }

    @Override
    protected SqlTable getSQLTableOfEntity() {
        return IndicatorRecordDynamicSqlSupport.indicatorRecord;
    }

    @Override
    protected Result<Indicator> checkCreateAccess(final Indicator entity) {
        if (entity == null) {
            return null;
        }

        this.authorization.checkWrite(this.examDao.byPK(entity.examId).getOrThrow());
        return Result.of(entity);
    }

    @Override
    protected GrantEntity toGrantEntity(final Indicator entity) {
        if (entity == null) {
            return null;
        }

        return this.examDao.byPK(entity.examId)
                .getOrThrow();
    }

    @Override
    protected EntityType getGrantEntityType() {
        return EntityType.EXAM;
    }

}
