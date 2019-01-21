/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.weblayer.api;

import java.util.Collection;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import ch.ethz.seb.sebserver.gbl.model.Entity;
import ch.ethz.seb.sebserver.gbl.model.EntityKey;
import ch.ethz.seb.sebserver.gbl.model.EntityProcessingReport;
import ch.ethz.seb.sebserver.gbl.model.EntityType;
import ch.ethz.seb.sebserver.gbl.model.Page;
import ch.ethz.seb.sebserver.gbl.util.Result;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.mapper.UserRecordDynamicSqlSupport;
import ch.ethz.seb.sebserver.webservice.servicelayer.PaginationService;
import ch.ethz.seb.sebserver.webservice.servicelayer.authorization.AuthorizationGrantService;
import ch.ethz.seb.sebserver.webservice.servicelayer.authorization.GrantEntity;
import ch.ethz.seb.sebserver.webservice.servicelayer.authorization.PrivilegeType;
import ch.ethz.seb.sebserver.webservice.servicelayer.authorization.UserService;
import ch.ethz.seb.sebserver.webservice.servicelayer.bulkaction.BulkAction;
import ch.ethz.seb.sebserver.webservice.servicelayer.bulkaction.BulkAction.Type;
import ch.ethz.seb.sebserver.webservice.servicelayer.bulkaction.BulkActionService;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.ActivatableEntityDAO;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.UserActivityLogDAO;

public abstract class ActivatableEntityController<T extends GrantEntity, M extends GrantEntity>
        extends EntityController<T, M> {

    private final ActivatableEntityDAO<T, M> activatableEntityDAO;

    public ActivatableEntityController(
            final AuthorizationGrantService authorizationGrantService,
            final BulkActionService bulkActionService,
            final ActivatableEntityDAO<T, M> entityDAO,
            final UserActivityLogDAO userActivityLogDAO,
            final PaginationService paginationService) {

        super(authorizationGrantService, bulkActionService, entityDAO, userActivityLogDAO, paginationService);
        this.activatableEntityDAO = entityDAO;
    }

    @RequestMapping(path = "/active", method = RequestMethod.GET)
    public Page<T> allActive(
            @RequestParam(
                    name = Entity.ATTR_INSTITUTION,
                    required = true,
                    defaultValue = UserService.USERS_INSTITUTION_AS_DEFAULT) final Long institutionId,
            @RequestParam(name = Page.ATTR_PAGE_NUMBER, required = false) final Integer pageNumber,
            @RequestParam(name = Page.ATTR_PAGE_SIZE, required = false) final Integer pageSize,
            @RequestParam(name = Page.ATTR_SORT_BY, required = false) final String sortBy,
            @RequestParam(name = Page.ATTR_SORT_ORDER, required = false) final Page.SortOrder sortOrder) {

        checkReadPrivilege(institutionId);
        return this.paginationService.getPage(
                pageNumber,
                pageSize,
                sortBy,
                sortOrder,
                UserRecordDynamicSqlSupport.userRecord,
                () -> this.activatableEntityDAO.all(institutionId, true).getOrThrow());
    }

    @RequestMapping(path = "/inactive", method = RequestMethod.GET)
    public Page<T> allInactive(
            @RequestParam(
                    name = Entity.ATTR_INSTITUTION,
                    required = true,
                    defaultValue = UserService.USERS_INSTITUTION_AS_DEFAULT) final Long institutionId,
            @RequestParam(name = Page.ATTR_PAGE_NUMBER, required = false) final Integer pageNumber,
            @RequestParam(name = Page.ATTR_PAGE_SIZE, required = false) final Integer pageSize,
            @RequestParam(name = Page.ATTR_SORT_BY, required = false) final String sortBy,
            @RequestParam(name = Page.ATTR_SORT_ORDER, required = false) final Page.SortOrder sortOrder) {

        checkReadPrivilege(institutionId);
        return this.paginationService.getPage(
                pageNumber,
                pageSize,
                sortBy,
                sortOrder,
                UserRecordDynamicSqlSupport.userRecord,
                () -> this.activatableEntityDAO.all(institutionId, false).getOrThrow());
    }

    @RequestMapping(path = "/{id}/activate", method = RequestMethod.POST)
    public EntityProcessingReport activate(@PathVariable final String id) {
        return setActive(id, true)
                .getOrThrow();
    }

    @RequestMapping(value = "/{id}/deactivate", method = RequestMethod.POST)
    public EntityProcessingReport deactivate(@PathVariable final String id) {
        return setActive(id, false)
                .getOrThrow();
    }

    @RequestMapping(path = "/{id}/delete", method = RequestMethod.DELETE)
    public EntityProcessingReport delete(@PathVariable final String id) {
        return deactivate(id);
    }

    @Override
    protected Result<Collection<T>> getAll(final Long institutionId, final Boolean active) {
        return this.activatableEntityDAO.all(institutionId, active);
    }

    private Result<EntityProcessingReport> setActive(final String id, final boolean active) {
        final EntityType entityType = this.entityDAO.entityType();
        final BulkAction bulkAction = new BulkAction(
                (active) ? Type.ACTIVATE : Type.DEACTIVATE,
                entityType,
                new EntityKey(id, entityType));

        return this.entityDAO.byModelId(id)
                .flatMap(entity -> this.authorizationGrantService.checkGrantOnEntity(
                        entity,
                        PrivilegeType.WRITE))
                .flatMap(entity -> this.bulkActionService.createReport(bulkAction));
    }

}
