/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.dao;

import ch.ethz.seb.sebserver.gbl.model.sebconfig.ConfigurationTableValue;
import ch.ethz.seb.sebserver.gbl.model.sebconfig.ConfigurationValue;
import ch.ethz.seb.sebserver.gbl.util.Result;

public interface ConfigurationValueDAO extends EntityDAO<ConfigurationValue, ConfigurationValue> {

    Result<ConfigurationTableValue> getTableValue(
            Long institutionId,
            Long attributeId,
            final Long configurationId);

    Result<ConfigurationTableValue> saveTableValue(ConfigurationTableValue value);

}
