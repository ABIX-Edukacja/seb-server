/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.dao.impl;

import java.util.Collection;
import java.util.Optional;

import org.mybatis.dynamic.sql.SqlBuilder;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ch.ethz.seb.sebserver.gbl.api.EntityType;
import ch.ethz.seb.sebserver.gbl.profile.WebServiceProfile;
import ch.ethz.seb.sebserver.gbl.util.Result;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.mapper.AdditionalAttributeRecordDynamicSqlSupport;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.mapper.AdditionalAttributeRecordMapper;
import ch.ethz.seb.sebserver.webservice.datalayer.batis.model.AdditionalAttributeRecord;
import ch.ethz.seb.sebserver.webservice.servicelayer.dao.AdditionalAttributesDAO;

@Lazy
@Component
@WebServiceProfile
public class AdditionalAttributesDAOImpl implements AdditionalAttributesDAO {

    private final AdditionalAttributeRecordMapper additionalAttributeRecordMapperer;

    protected AdditionalAttributesDAOImpl(final AdditionalAttributeRecordMapper additionalAttributeRecordMapperer) {
        this.additionalAttributeRecordMapperer = additionalAttributeRecordMapperer;
    }

    @Override
    @Transactional(readOnly = true)
    public Result<Collection<AdditionalAttributeRecord>> getAdditionalAttributes(
            final EntityType type,
            final Long entityId) {

        return Result.tryCatch(() -> {

            return this.additionalAttributeRecordMapperer
                    .selectByExample()
                    .where(
                            AdditionalAttributeRecordDynamicSqlSupport.entityType,
                            SqlBuilder.isEqualTo(type.name()))
                    .and(
                            AdditionalAttributeRecordDynamicSqlSupport.entityId,
                            SqlBuilder.isEqualTo(entityId))
                    .build()
                    .execute();

        });
    }

    @Override
    @Transactional
    public void saveAdditionalAttribute(
            final EntityType type,
            final Long entityId,
            final String name,
            final String value) {

        if (value == null) {
            this.delete(entityId, name);
            return;
        }

        final Optional<Long> id = this.additionalAttributeRecordMapperer
                .selectIdsByExample()
                .where(
                        AdditionalAttributeRecordDynamicSqlSupport.entityType,
                        SqlBuilder.isEqualTo(type.name()))
                .and(
                        AdditionalAttributeRecordDynamicSqlSupport.entityId,
                        SqlBuilder.isEqualTo(entityId))
                .and(
                        AdditionalAttributeRecordDynamicSqlSupport.name,
                        SqlBuilder.isEqualTo(name))
                .build()
                .execute()
                .stream()
                .findFirst();

        if (id.isPresent()) {
            final AdditionalAttributeRecord rec = new AdditionalAttributeRecord(
                    id.get(),
                    type.name(),
                    entityId,
                    name,
                    value);
            this.additionalAttributeRecordMapperer
                    .updateByPrimaryKeySelective(rec);
        } else {
            final AdditionalAttributeRecord rec = new AdditionalAttributeRecord(
                    null,
                    type.name(),
                    entityId,
                    name,
                    value);
            this.additionalAttributeRecordMapperer
                    .insert(rec);
        }

    }

    @Override
    @Transactional
    public void delete(final Long id) {
        this.additionalAttributeRecordMapperer
                .deleteByPrimaryKey(id);
    }

    @Override
    @Transactional
    public void delete(final Long entityId, final String name) {
        this.additionalAttributeRecordMapperer
                .deleteByExample()
                .where(
                        AdditionalAttributeRecordDynamicSqlSupport.entityId,
                        SqlBuilder.isEqualTo(entityId))
                .and(
                        AdditionalAttributeRecordDynamicSqlSupport.name,
                        SqlBuilder.isEqualTo(name))
                .build()
                .execute();

    }

    @Override
    @Transactional
    public void deleteAll(final Long entityId) {
        this.additionalAttributeRecordMapperer
                .deleteByExample()
                .where(
                        AdditionalAttributeRecordDynamicSqlSupport.entityId,
                        SqlBuilder.isEqualTo(entityId))
                .build()
                .execute();
    }

}
