/*
 * Copyright (c) 2018 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.service.page.event;

import ch.ethz.seb.sebserver.gui.service.page.impl.PageAction;

/** This action is used to publish an Action to the Action-Pane for a specified context.
 * The ActionPane is listening to this events and render specified actions on notify */
public class ActionPublishEvent implements PageEvent {

    public final boolean active;
    public final PageAction action;

    public ActionPublishEvent(final PageAction action, final boolean active) {
        this.action = action;
        this.active = active;
    }

}
