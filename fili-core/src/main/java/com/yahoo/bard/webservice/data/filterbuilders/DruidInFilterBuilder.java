// Copyright 2016 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.data.filterbuilders;

import com.yahoo.bard.webservice.data.dimension.Dimension;
import com.yahoo.bard.webservice.data.dimension.DimensionRowNotFoundException;
import com.yahoo.bard.webservice.druid.model.filter.AndFilter;
import com.yahoo.bard.webservice.druid.model.filter.Filter;
import com.yahoo.bard.webservice.druid.model.filter.InFilter;
import com.yahoo.bard.webservice.druid.model.filter.NotFilter;
import com.yahoo.bard.webservice.web.ApiFilter;
import com.yahoo.bard.webservice.web.FilterOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A DruidInFilterBuilder builds a conjunction of in-filters for each Dimension, where each in-filter corresponds to a
 * filter term. So, the filter terms on dimension {@code category}:
 * <p>
 * {@code category|id-in[finance,sports],category|desc-contains[baseball]}
 * <p>
 * are translated into:
 * <pre>
 * {@code
 *     {
 *         "type": "in",
 *         "dimension": "category",
 *         "values": ["finance", "sports"]
 *     }
 * }
 * </pre>
 * Each filter term is resolved independently of the other filter terms.
 */
public class DruidInFilterBuilder extends ConjunctionDruidFilterBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(DruidInFilterBuilder.class);

    @Override
    protected Filter buildDimensionFilter(Dimension dimension, Set<ApiFilter> filters)
            throws DimensionRowNotFoundException {
        LOG.trace("Building dimension filter using dimension: {} \n\n and set of filter: {}", dimension, filters);

        List<Filter> inFilters = new ArrayList<>(); // A list with at most 2 in-filters(positive & negative)
        Set<String> positiveInValues = new HashSet<>(); // contains values for positive in-filter
        Set<String> negativeInValues = new HashSet<>(); // contains values for negative in-filter

        for (ApiFilter filter : filters) {
            ApiFilter normalizedFilter = filter;
            if (normalizedFilter.getOperation().equals(FilterOperation.notin)) {
                normalizedFilter = filter.withOperation(FilterOperation.in);
            }

            List<String> values = getFilteredDimensionRows(dimension, Collections.singleton(normalizedFilter)).stream()
                    .map(row -> row.get(dimension.getKey()))
                    .collect(Collectors.toList());

            if (normalizedFilter == filter) {
                positiveInValues.addAll(values);
            } else {
                negativeInValues.addAll(values);
            }
        }

        if (!positiveInValues.isEmpty()) {
            inFilters.add(new InFilter(dimension, new ArrayList<>(positiveInValues)));
        }
        if (!negativeInValues.isEmpty()) {
            inFilters.add(new NotFilter(new InFilter(dimension, new ArrayList<>(negativeInValues))));
        }

        Filter newFilter = inFilters.size() == 1 ? inFilters.get(0) : new AndFilter(inFilters);

        LOG.trace("Filter: {}", newFilter);
        return newFilter;
    }
}
