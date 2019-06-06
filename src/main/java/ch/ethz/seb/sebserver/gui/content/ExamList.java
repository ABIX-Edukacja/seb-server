/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.content;

import java.util.function.Function;

import org.eclipse.swt.widgets.Composite;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import ch.ethz.seb.sebserver.gbl.Constants;
import ch.ethz.seb.sebserver.gbl.api.EntityType;
import ch.ethz.seb.sebserver.gbl.model.Domain;
import ch.ethz.seb.sebserver.gbl.model.exam.Exam;
import ch.ethz.seb.sebserver.gbl.model.exam.QuizData;
import ch.ethz.seb.sebserver.gbl.model.institution.LmsSetup;
import ch.ethz.seb.sebserver.gbl.profile.GuiProfile;
import ch.ethz.seb.sebserver.gui.content.action.ActionDefinition;
import ch.ethz.seb.sebserver.gui.service.ResourceService;
import ch.ethz.seb.sebserver.gui.service.i18n.I18nSupport;
import ch.ethz.seb.sebserver.gui.service.i18n.LocTextKey;
import ch.ethz.seb.sebserver.gui.service.page.PageContext;
import ch.ethz.seb.sebserver.gui.service.page.PageMessageException;
import ch.ethz.seb.sebserver.gui.service.page.PageService;
import ch.ethz.seb.sebserver.gui.service.page.PageService.PageActionBuilder;
import ch.ethz.seb.sebserver.gui.service.page.TemplateComposer;
import ch.ethz.seb.sebserver.gui.service.page.impl.PageAction;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.RestService;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.api.exam.GetExamPage;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.auth.CurrentUser;
import ch.ethz.seb.sebserver.gui.service.remote.webservice.auth.CurrentUser.GrantCheck;
import ch.ethz.seb.sebserver.gui.table.ColumnDefinition;
import ch.ethz.seb.sebserver.gui.table.ColumnDefinition.TableFilterAttribute;
import ch.ethz.seb.sebserver.gui.table.EntityTable;
import ch.ethz.seb.sebserver.gui.table.TableFilter.CriteriaType;
import ch.ethz.seb.sebserver.gui.widget.WidgetFactory;

@Lazy
@Component
@GuiProfile
public class ExamList implements TemplateComposer {

    private static final LocTextKey NO_MODIFY_PRIVILEGE_ON_OTHER_INSTITUION =
            new LocTextKey("sebserver.exam.list.action.no.modify.privilege");
    private final static LocTextKey EMPTY_SELECTION_TEXT_KEY =
            new LocTextKey("sebserver.exam.info.pleaseSelect");
    private final static LocTextKey COLUMN_TITLE_KEY =
            new LocTextKey("sebserver.exam.list.column.lmssetup");
    private final static LocTextKey COLUMN_TITLE_NAME_KEY =
            new LocTextKey("sebserver.exam.list.column.name");
    private final static LocTextKey COLUMN_TITLE_TYPE_KEY =
            new LocTextKey("sebserver.exam.list.column.type");
    private final static LocTextKey NO_MODIFY_OF_OUT_DATED_EXAMS =
            new LocTextKey("sebserver.exam.list.modify.out.dated");
    private final static LocTextKey EMPTY_LIST_TEXT_KEY =
            new LocTextKey("sebserver.exam.list.empty");

    private final TableFilterAttribute lmsFilter;
    private final TableFilterAttribute nameFilter =
            new TableFilterAttribute(CriteriaType.TEXT, QuizData.FILTER_ATTR_NAME);
    private final TableFilterAttribute startTimeFilter =
            new TableFilterAttribute(CriteriaType.DATE, QuizData.FILTER_ATTR_START_TIME);

    private final PageService pageService;
    private final ResourceService resourceService;
    private final int pageSize;

    protected ExamList(
            final PageService pageService,
            final ResourceService resourceService,
            @Value("${sebserver.gui.list.page.size}") final Integer pageSize) {

        this.pageService = pageService;
        this.resourceService = resourceService;
        this.pageSize = (pageSize != null) ? pageSize : 20;

        this.lmsFilter = new TableFilterAttribute(
                CriteriaType.SINGLE_SELECTION,
                LmsSetup.FILTER_ATTR_LMS_SETUP,
                this.resourceService::lmsSetupResource);
    }

    @Override
    public void compose(final PageContext pageContext) {

        final WidgetFactory widgetFactory = this.pageService.getWidgetFactory();
        final CurrentUser currentUser = this.resourceService.getCurrentUser();
        final RestService restService = this.resourceService.getRestService();
        final I18nSupport i18nSupport = this.resourceService.getI18nSupport();

        // content page layout with title
        final Composite content = widgetFactory.defaultPageLayout(
                pageContext.getParent(),
                new LocTextKey("sebserver.exam.list.title"));

        final PageActionBuilder actionBuilder = this.pageService.pageActionBuilder(pageContext.clearEntityKeys());

        // table
        final EntityTable<Exam> table =
                this.pageService.entityTableBuilder(restService.getRestCall(GetExamPage.class))
                        .withEmptyMessage(EMPTY_LIST_TEXT_KEY)
                        .withPaging(this.pageSize)
                        .withColumn(new ColumnDefinition<>(
                                Domain.EXAM.ATTR_LMS_SETUP_ID,
                                COLUMN_TITLE_KEY,
                                examLmsSetupNameFunction(this.resourceService))
                                        .withFilter(this.lmsFilter)
                                        .sortable())
                        .withColumn(new ColumnDefinition<>(
                                QuizData.QUIZ_ATTR_NAME,
                                COLUMN_TITLE_NAME_KEY,
                                Exam::getName)
                                        .withFilter(this.nameFilter)
                                        .sortable())
                        .withColumn(new ColumnDefinition<>(
                                QuizData.QUIZ_ATTR_START_TIME,
                                new LocTextKey(
                                        "sebserver.exam.list.column.starttime",
                                        i18nSupport.getUsersTimeZoneTitleSuffix()),
                                Exam::getStartTime)
                                        .withFilter(this.startTimeFilter)
                                        .sortable())
                        .withColumn(new ColumnDefinition<>(
                                Domain.EXAM.ATTR_TYPE,
                                COLUMN_TITLE_TYPE_KEY,
                                this::examTypeName)
                                        .sortable())
                        .withDefaultAction(actionBuilder
                                .newAction(ActionDefinition.EXAM_VIEW_FROM_LIST)
                                .create())
                        .compose(content);

        // propagate content actions to action-pane
        final GrantCheck userGrant = currentUser.grantCheck(EntityType.EXAM);
        actionBuilder

                .newAction(ActionDefinition.EXAM_IMPORT)
                .publishIf(userGrant::im)

                .newAction(ActionDefinition.EXAM_VIEW_FROM_LIST)
                .withSelect(table::getSelection, PageAction::applySingleSelection, EMPTY_SELECTION_TEXT_KEY)
                .publishIf(table::hasAnyContent)

                .newAction(ActionDefinition.EXAM_MODIFY_FROM_LIST)
                .withSelect(
                        table.getGrantedSelection(currentUser, NO_MODIFY_PRIVILEGE_ON_OTHER_INSTITUION),
                        action -> this.modifyExam(action, table),
                        EMPTY_SELECTION_TEXT_KEY)
                .publishIf(() -> userGrant.im() && table.hasAnyContent());

    }

    private PageAction modifyExam(final PageAction action, final EntityTable<Exam> table) {
        final Exam exam = table.getSelectedROWData();

        if (exam.startTime != null) {
            final DateTime now = DateTime.now(DateTimeZone.UTC);
            if (exam.startTime.isBefore(now)) {
                throw new PageMessageException(NO_MODIFY_OF_OUT_DATED_EXAMS);
            }
        }

        return action.withEntityKey(action.getSingleSelection());
    }

    private static Function<Exam, String> examLmsSetupNameFunction(final ResourceService resourceService) {
        return exam -> resourceService.getLmsSetupNameFunction()
                .apply(String.valueOf(exam.lmsSetupId));
    }

    private String examTypeName(final Exam exam) {
        if (exam.type == null) {
            return Constants.EMPTY_NOTE;
        }

        return this.resourceService.getI18nSupport()
                .getText(ResourceService.EXAM_TYPE_PREFIX + exam.type.name());
    }

}
