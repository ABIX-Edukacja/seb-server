/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.content;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import ch.ethz.seb.sebserver.gbl.profile.GuiProfile;
import ch.ethz.seb.sebserver.gui.service.ResourceService;
import ch.ethz.seb.sebserver.gui.service.page.PageContext;
import ch.ethz.seb.sebserver.gui.service.page.TemplateComposer;
import ch.ethz.seb.sebserver.gui.widget.WidgetFactory;

@Lazy
@Component
@GuiProfile
public class QuizDiscoveryList implements TemplateComposer {

    private final WidgetFactory widgetFactory;
    private final ResourceService resourceService;
    private final int pageSize;

    protected QuizDiscoveryList(
            final WidgetFactory widgetFactory,
            final ResourceService resourceService,
            @Value("${sebserver.gui.list.page.size}") final Integer pageSize) {

        this.widgetFactory = widgetFactory;
        this.resourceService = resourceService;
        this.pageSize = (pageSize != null) ? pageSize : 20;
    }

    @Override
    public void compose(final PageContext pageContext) {
        // TODO Auto-generated method stub

    }

}
