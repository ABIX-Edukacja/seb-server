/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;

import ch.ethz.seb.sebserver.webservice.servicelayer.sebconfig.SebConfigEncryptionService.Strategy;

/** Interface for a SEB Configuration encryption and decryption strategy.
 *
 * To support a new SEB Configuration encryption and decryption strategy use this interface
 * to implement a concrete strategy for encryption and decryption of SEB configurations */
public interface SebConfigCryptor {

    /** The type of strategies a concrete implementation is supporting
     *
     * @return Set of strategies a concrete implementation is supporting */
    Set<Strategy> strategies();

    void encrypt(
            final OutputStream output,
            final InputStream input,
            final SebConfigEncryptionContext context);

    void decrypt(
            final OutputStream output,
            final InputStream input,
            final SebConfigEncryptionContext context);

}
