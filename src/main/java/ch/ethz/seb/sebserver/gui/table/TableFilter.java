/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.joda.time.DateTimeZone;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import ch.ethz.seb.sebserver.gbl.Constants;
import ch.ethz.seb.sebserver.gbl.model.Entity;
import ch.ethz.seb.sebserver.gbl.util.Tuple;
import ch.ethz.seb.sebserver.gui.service.i18n.LocTextKey;
import ch.ethz.seb.sebserver.gui.table.ColumnDefinition.TableFilterAttribute;
import ch.ethz.seb.sebserver.gui.widget.Selection;
import ch.ethz.seb.sebserver.gui.widget.WidgetFactory.ImageIcon;

public class TableFilter<ROW extends Entity> {

    private static final LocTextKey DATE_FROM_TEXT = new LocTextKey("sebserver.overall.date.from");
    private static final LocTextKey DATE_TO_TEXT = new LocTextKey("sebserver.overall.date.to");

    public static enum CriteriaType {
        TEXT,
        SINGLE_SELECTION,
        DATE,
        DATE_RANGE
    }

    private final Composite composite;
    private final EntityTable<ROW> entityTable;
    private final List<FilterComponent> components = new ArrayList<>();

    TableFilter(final EntityTable<ROW> entityTable) {
        this.composite = new Composite(entityTable.composite, SWT.NONE);
        final GridData gridData = new GridData(SWT.FILL, SWT.TOP, true, false);
        this.composite.setLayoutData(gridData);
        final RowLayout layout = new RowLayout(SWT.HORIZONTAL);
        layout.spacing = 5;
        layout.wrap = false;
        layout.center = false;
        layout.fill = true;
        this.composite.setLayout(layout);

// TODO just for debugging, remove when tested
//        this.composite.setBackground(new Color(entityTable.composite.getDisplay(), new RGB(0, 0, 200)));

        this.entityTable = entityTable;
        buildComponents();
    }

    public int size() {
        return this.components.size();
    }

    public MultiValueMap<String, String> getFilterParameter() {
        return this.components
                .stream()
                .reduce(new LinkedMultiValueMap<String, String>(),
                        (map, comp) -> comp.putFilterParameter(map),
                        (map1, map2) -> {
                            map1.putAll(map2);
                            return map1;
                        });
    }

    public void reset() {
        this.components
                .stream()
                .forEach(comp -> comp.reset());
    }

    private void buildComponents() {
        this.components.clear();
        this.components.addAll(this.entityTable.columns
                .stream()
                .map(ColumnDefinition::getFilterAttribute)
                .map(this::createFilterComponent)
                .map(comp -> comp.build(this.composite))
                .map(FilterComponent::reset)
                .collect(Collectors.toList()));

        FilterComponent lastComp = this.components.get(this.components.size() - 1);
        while (lastComp instanceof TableFilter.NullFilter) {
            this.components.remove(lastComp);
            lastComp = this.components.get(this.components.size() - 1);
        }

        addActions();
    }

    private FilterComponent createFilterComponent(final TableFilterAttribute attribute) {
        if (attribute == null) {
            return new NullFilter();
        }

        switch (attribute.type) {
            case TEXT:
                return new TextFilter(attribute);
            case SINGLE_SELECTION:
                return new SelectionFilter(attribute);
            case DATE:
                return new Date(attribute);
            case DATE_RANGE:
                return new DateRange(attribute);
            default:
                throw new IllegalArgumentException("Unsupported FilterAttributeType: " + attribute.type);
        }
    }

    boolean adaptColumnWidth(final int columnIndex, final int width) {
        if (columnIndex < this.components.size()) {
            final boolean adaptWidth = this.components.get(columnIndex).adaptWidth(width);
            if (adaptWidth) {
                this.composite.layout();
            }
            return adaptWidth;
        }

        return false;
    }

    private void addActions() {
        final Composite inner = new Composite(this.composite, SWT.NONE);
        final GridLayout gridLayout = new GridLayout(2, true);
        gridLayout.horizontalSpacing = 5;
        gridLayout.verticalSpacing = 0;
        gridLayout.marginHeight = 0;
        gridLayout.marginWidth = 0;
        inner.setLayout(gridLayout);
        inner.setLayoutData(new RowData());

        final GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, true);

        final Label imageButton = this.entityTable.widgetFactory.imageButton(
                ImageIcon.SEARCH,
                inner,
                new LocTextKey("sebserver.overall.action.filter"),
                event -> {
                    this.entityTable.applyFilter();
                });
        imageButton.setLayoutData(gridData);
        final Label imageButton2 = this.entityTable.widgetFactory.imageButton(
                ImageIcon.CANCEL,
                inner,
                new LocTextKey("sebserver.overall.action.filter.clear"),
                event -> {
                    reset();
                    this.entityTable.applyFilter();
                });
        imageButton2.setLayoutData(gridData);
    }

    private static abstract class FilterComponent {

        static final int CELL_WIDTH_ADJUSTMENT = -5;

        protected final RowData rowData;
        final TableFilterAttribute attribute;

        FilterComponent(final TableFilterAttribute attribute) {
            this.attribute = attribute;
            this.rowData = new RowData();
        }

        LinkedMultiValueMap<String, String> putFilterParameter(
                final LinkedMultiValueMap<String, String> filterParameter) {

            final String value = getValue();
            if (StringUtils.isNotBlank(value)) {
                filterParameter.put(this.attribute.columnName, Arrays.asList(value));
            }
            return filterParameter;
        }

        abstract FilterComponent build(Composite parent);

        abstract FilterComponent reset();

        abstract String getValue();

        boolean adaptWidth(final int width) {
            final int _width = width + CELL_WIDTH_ADJUSTMENT;
            if (_width != this.rowData.width) {
                this.rowData.width = _width;
                return true;
            }

            return false;
        }

        protected Composite createInnerComposite(final Composite parent) {
            final Composite inner = new Composite(parent, SWT.NONE);
            final GridLayout gridLayout = new GridLayout(1, true);
            gridLayout.horizontalSpacing = 0;
            gridLayout.verticalSpacing = 0;
            gridLayout.marginHeight = 0;
            gridLayout.marginWidth = 0;
            inner.setLayout(gridLayout);
            inner.setLayoutData(this.rowData);
            return inner;
        }
    }

    private class NullFilter extends FilterComponent {

        private Label label;

        NullFilter() {
            super(null);
        }

        @Override
        FilterComponent build(final Composite parent) {
            this.label = new Label(parent, SWT.NONE);
            this.label.setLayoutData(this.rowData);
            return this;
        }

        @Override
        boolean adaptWidth(final int width) {
            return super.adaptWidth(width - 2 * CELL_WIDTH_ADJUSTMENT);
        }

        @Override
        FilterComponent reset() {
            return this;
        }

        @Override
        String getValue() {
            return null;
        }

    }

    private class TextFilter extends FilterComponent {

        private Text textInput;

        TextFilter(final TableFilterAttribute attribute) {
            super(attribute);
        }

        @Override
        FilterComponent reset() {
            if (this.textInput != null) {
                this.textInput.setText(super.attribute.initValue);
            }
            return this;
        }

        @Override
        FilterComponent build(final Composite parent) {
            final Composite innerComposite = createInnerComposite(parent);
            final GridData gridData = new GridData(SWT.FILL, SWT.END, true, true);

            this.textInput = TableFilter.this.entityTable.widgetFactory.textInput(innerComposite);
            this.textInput.setLayoutData(gridData);
            return this;
        }

        @Override
        String getValue() {
            if (this.textInput != null) {
                return this.textInput.getText();
            }

            return null;
        }

    }

    private class SelectionFilter extends FilterComponent {

        protected Selection selector;

        SelectionFilter(final TableFilterAttribute attribute) {
            super(attribute);
        }

        @Override
        FilterComponent build(final Composite parent) {
            final Composite innerComposite = createInnerComposite(parent);
            final GridData gridData = new GridData(SWT.FILL, SWT.END, true, true);

            Supplier<List<Tuple<String>>> resourceSupplier = this.attribute.resourceSupplier;
            if (this.attribute.resourceFunction != null) {
                resourceSupplier = () -> this.attribute.resourceFunction.apply(TableFilter.this.entityTable);
            }

            this.selector = TableFilter.this.entityTable.widgetFactory
                    .selectionLocalized(
                            ch.ethz.seb.sebserver.gui.widget.Selection.Type.SINGLE,
                            innerComposite,
                            resourceSupplier);

            this.selector
                    .adaptToControl()
                    .setLayoutData(gridData);
            return this;
        }

        @Override
        FilterComponent reset() {
            if (this.selector != null) {
                this.selector.clear();
            }
            return this;
        }

        @Override
        String getValue() {
            if (this.selector != null) {
                return this.selector.getSelectionValue();
            }

            return null;
        }
    }

    // NOTE: SWT DateTime month-number starting with 0 and joda DateTime with 1!
    private class Date extends FilterComponent {

        private DateTime selector;

        Date(final TableFilterAttribute attribute) {
            super(attribute);
        }

        @Override
        FilterComponent build(final Composite parent) {
            final Composite innerComposite = createInnerComposite(parent);
            final GridData gridData = new GridData(SWT.FILL, SWT.END, true, true);

            this.selector = new DateTime(innerComposite, SWT.DATE | SWT.BORDER);
            this.selector.setLayoutData(gridData);
            return this;
        }

        @Override
        FilterComponent reset() {
            try {
                final org.joda.time.DateTime parse = org.joda.time.DateTime.parse(this.attribute.initValue);
                this.selector.setDate(parse.getYear(), parse.getMonthOfYear() - 1, parse.getDayOfMonth());
            } catch (final Exception e) {
                final org.joda.time.DateTime now = org.joda.time.DateTime.now(DateTimeZone.UTC);
                this.selector.setDate(now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth());
            }
            return this;
        }

        @Override
        String getValue() {
            if (this.selector != null) {
                final org.joda.time.DateTime date = org.joda.time.DateTime.now(DateTimeZone.UTC)
                        .withYear(this.selector.getYear())
                        .withMonthOfYear(this.selector.getMonth() + 1)
                        .withDayOfMonth(this.selector.getDay())
                        .withTimeAtStartOfDay();

                return date.toString(Constants.STANDARD_DATE_TIME_FORMATTER);
            } else {
                return null;
            }
        }

        @Override
        boolean adaptWidth(final int width) {
            // NOTE: for some unknown reason RWT acts differently on width-property for date selector
            //       this is to adjust date filter criteria to the list column width
            return super.adaptWidth(width - 5);
        }

    }

    // NOTE: SWT DateTime month-number starting with 0 and joda DateTime with 1!
    private class DateRange extends FilterComponent {

        private Composite innerComposite;
        private final GridData rw1 = new GridData(SWT.FILL, SWT.FILL, true, true);
        private DateTime fromSelector;
        private DateTime toSelector;

        DateRange(final TableFilterAttribute attribute) {
            super(attribute);
        }

        @Override
        FilterComponent build(final Composite parent) {
            this.innerComposite = new Composite(parent, SWT.NONE);
            final GridLayout gridLayout = new GridLayout(2, false);
            gridLayout.marginHeight = 0;
            gridLayout.marginWidth = 0;
            gridLayout.horizontalSpacing = 5;
            gridLayout.verticalSpacing = 3;
            this.innerComposite.setLayout(gridLayout);
            this.innerComposite.setLayoutData(this.rowData);

            TableFilter.this.entityTable.widgetFactory
                    .labelLocalized(this.innerComposite, DATE_FROM_TEXT);
            this.fromSelector = new DateTime(this.innerComposite, SWT.DATE | SWT.BORDER);
            this.fromSelector.setLayoutData(this.rw1);

            TableFilter.this.entityTable.widgetFactory
                    .labelLocalized(this.innerComposite, DATE_TO_TEXT);
            this.toSelector = new DateTime(this.innerComposite, SWT.DATE | SWT.BORDER);
            this.toSelector.setLayoutData(this.rw1);

            return this;
        }

        @Override
        FilterComponent reset() {
            final org.joda.time.DateTime now = org.joda.time.DateTime.now(DateTimeZone.UTC);
            try {
                final org.joda.time.DateTime parse = org.joda.time.DateTime.parse(this.attribute.initValue);
                this.fromSelector.setDate(parse.getYear(), parse.getMonthOfYear() - 1, parse.getDayOfMonth());
            } catch (final Exception e) {
                this.fromSelector.setDate(now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth());
            }
            this.toSelector.setDate(now.getYear(), now.getMonthOfYear() - 1, now.getDayOfMonth());
            return this;
        }

        @Override
        String getValue() {
            if (this.fromSelector != null && this.toSelector != null) {
                final org.joda.time.DateTime fromDate = org.joda.time.DateTime.now(DateTimeZone.UTC)
                        .withYear(this.fromSelector.getYear())
                        .withMonthOfYear(this.fromSelector.getMonth() + 1)
                        .withDayOfMonth(this.fromSelector.getDay())
                        .withTimeAtStartOfDay();
                final org.joda.time.DateTime toDate = org.joda.time.DateTime.now(DateTimeZone.UTC)
                        .withYear(this.toSelector.getYear())
                        .withMonthOfYear(this.toSelector.getMonth() + 1)
                        .withDayOfMonth(this.toSelector.getDay())
                        .withTime(23, 59, 59, 0);

                return fromDate.toString(Constants.STANDARD_DATE_TIME_FORMATTER) +
                        Constants.EMBEDDED_LIST_SEPARATOR +
                        toDate.toString(Constants.STANDARD_DATE_TIME_FORMATTER);
            } else {
                return null;
            }
        }
    }

}
