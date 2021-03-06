/*
 * Copyright (c) 2020 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.webservice.servicelayer.lms;

import ch.ethz.seb.sebserver.gbl.model.exam.Exam;
import ch.ethz.seb.sebserver.gbl.model.exam.SebRestriction;
import ch.ethz.seb.sebserver.gbl.util.Result;

public interface SebRestrictionService {

    /** Used as name prefix to store additional SEB restriction properties within AdditionalAttribute domain. */
    String SEB_RESTRICTION_ADDITIONAL_PROPERTY_NAME_PREFIX = "sebRestrictionProp_";

    /** Get the SebRestriction properties for specified Exam.
     *
     * @param exam the Exam
     * @return the SebRestriction properties for specified Exam */
    Result<SebRestriction> getSebRestrictionFromExam(Exam exam);

    /** Saves the given SebRestriction for the given Exam.
     *
     * The webservice saves the given browser Exam keys within the Exam record
     * and given additional restriction properties within the Additional attributes linked
     * to the given Exam.
     *
     * @param exam the Exam instance to save the SEB restrictions for
     * @param sebRestriction SebRestriction data containing generic and LMS specific restriction attributes
     * @return Result refer to the given Exam instance or to an error if happened */
    Result<Exam> saveSebRestrictionToExam(Exam exam, SebRestriction sebRestriction);

    /** Used to apply SEB Client restriction within the LMS API for a specified Exam.
     * If the underling LMS Setup API didn't support the SEB restriction feature
     * the apply will be just ignored and no error is returned
     *
     * @param exam the Exam instance
     * @return Result refer to the Exam instance or to an error if happened */
    Result<Exam> applySebClientRestriction(Exam exam);

    /** Release SEB Client restriction within the LMS API for a specified Exam.
     *
     * @param exam the Exam instance
     * @return Result refer to the Exam instance or to an error if happened */
    Result<Exam> releaseSebClientRestriction(Exam exam);

}
