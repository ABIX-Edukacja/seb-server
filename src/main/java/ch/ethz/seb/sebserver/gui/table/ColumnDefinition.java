/*
 * Copyright (c) 2019 ETH Zürich, Educational Development and Technology (LET)
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ch.ethz.seb.sebserver.gui.table;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import ch.ethz.seb.sebserver.gbl.model.Entity;
import ch.ethz.seb.sebserver.gbl.util.Tuple;
import ch.ethz.seb.sebserver.gui.service.i18n.LocTextKey;
import ch.ethz.seb.sebserver.gui.table.TableFilter.CriteriaType;

public final class ColumnDefinition<ROW extends Entity> {

    final String columnName;
    final LocTextKey displayName;
    final LocTextKey tooltip;
    final int widthPercent;
    final Function<ROW, ?> valueSupplier;
    final boolean sortable;
    final TableFilterAttribute filterAttribute;
    final boolean localized;

    public ColumnDefinition(final String columnName, final LocTextKey displayName) {
        this(columnName, displayName, null, -1);
    }

    public ColumnDefinition(
            final String columnName,
            final LocTextKey displayName,
            final LocTextKey tooltip,
            final int widthPercent) {

        this(columnName, displayName, tooltip, widthPercent, null, null, false, false);
    }

    public ColumnDefinition(
            final String columnName,
            final LocTextKey displayName,
            final int widthPercent) {

        this(columnName, displayName, null, widthPercent, null, null, false, false);
    }

    public ColumnDefinition(
            final String columnName,
            final LocTextKey displayName,
            final Function<ROW, ?> valueSupplier,
            final boolean sortable) {

        this(columnName, displayName, null, -1, valueSupplier, null, sortable, false);
    }

    public ColumnDefinition(
            final String columnName,
            final LocTextKey displayName,
            final Function<ROW, ?> valueSupplier,
            final boolean sortable,
            final boolean localized) {

        this(columnName, displayName, null, -1, valueSupplier, null, sortable, localized);
    }

    public ColumnDefinition(
            final String columnName,
            final LocTextKey displayName,
            final Function<ROW, ?> valueSupplier,
            final TableFilterAttribute tableFilterAttribute,
            final boolean sortable) {

        this(columnName, displayName, null, -1, valueSupplier, tableFilterAttribute, sortable, false);
    }

    public ColumnDefinition(
            final String columnName,
            final LocTextKey displayName,
            final Function<ROW, ?> valueSupplier,
            final TableFilterAttribute tableFilterAttribute,
            final boolean sortable,
            final boolean localized) {

        this(columnName, displayName, null, -1, valueSupplier, tableFilterAttribute, sortable, localized);
    }

    public ColumnDefinition(
            final String columnName,
            final LocTextKey displayName,
            final LocTextKey tooltip,
            final int widthPercent,
            final Function<ROW, ?> valueSupplier,
            final TableFilterAttribute filterAttribute,
            final boolean sortable,
            final boolean localized) {

        this.columnName = columnName;
        this.displayName = displayName;
        this.tooltip = tooltip;
        this.widthPercent = widthPercent;
        this.valueSupplier = valueSupplier;
        this.filterAttribute = filterAttribute;
        this.sortable = sortable;
        this.localized = localized;
    }

    public static final class TableFilterAttribute {

        public final CriteriaType type;
        public final String columnName;
        public final String initValue;
        public final Supplier<List<Tuple<String>>> resourceSupplier;

        public TableFilterAttribute(final CriteriaType type, final String columnName) {
            this(type, columnName, "", null);
        }

        public TableFilterAttribute(
                final CriteriaType type,
                final String columnName,
                final Supplier<List<Tuple<String>>> resourceSupplier) {

            this(type, columnName, "", resourceSupplier);
        }

        public TableFilterAttribute(
                final CriteriaType type,
                final String columnName,
                final String initValue) {

            this(type, columnName, initValue, null);
        }

        public TableFilterAttribute(
                final CriteriaType type,
                final String columnName,
                final String initValue,
                final Supplier<List<Tuple<String>>> resourceSupplier) {

            this.type = type;
            this.columnName = columnName;
            this.initValue = initValue;
            this.resourceSupplier = resourceSupplier;
        }

    }
}
