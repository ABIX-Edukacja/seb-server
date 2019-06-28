/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import ch.ethz.seb.sebserver.gbl.model.sebconfig.AttributeType;
import ch.ethz.seb.sebserver.gbl.model.sebconfig.ConfigurationAttribute;
import ch.ethz.seb.sebserver.gbl.profile.WebServiceProfile;
import ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.XMLValueConverter;
import ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.XMLValueConverterService;

@Lazy
@Service
@WebServiceProfile
public class XMLValueConverterServiceImpl implements XMLValueConverterService {

    private static final Logger log = LoggerFactory.getLogger(XMLValueConverterServiceImpl.class);

    private final Map<String, XMLValueConverter> convertersByAttributeName;
    private final Map<AttributeType, XMLValueConverter> convertersByAttributeType;

    public XMLValueConverterServiceImpl(final Collection<XMLValueConverter> converters) {
        this.convertersByAttributeName = new HashMap<>();
        this.convertersByAttributeType = new HashMap<>();
        for (final XMLValueConverter converter : converters) {
            converter.init(this);
            if (StringUtils.isNotBlank(converter.name())) {
                this.convertersByAttributeName.put(converter.name(), converter);
            }

            for (final AttributeType aType : converter.types()) {
                if (this.convertersByAttributeType.containsKey(aType)) {
                    log.warn(
                            "Unexpected state in inititalization: A XMLValueConverter for AttributeType {} exists already: {}",
                            aType,
                            converter);
                }
                this.convertersByAttributeType.put(aType, converter);
            }
        }
    }

    @Override
    public XMLValueConverter getXMLConverter(final ConfigurationAttribute attribute) {
        if (this.convertersByAttributeName.containsKey(attribute.name)) {
            return this.convertersByAttributeName.get(attribute.name);
        }

        if (this.convertersByAttributeType.containsKey(attribute.type)) {
            return this.convertersByAttributeType.get(attribute.type);
        }

        throw new IllegalStateException("No XMLValueConverter found for attribute: " + attribute);
    }

}
