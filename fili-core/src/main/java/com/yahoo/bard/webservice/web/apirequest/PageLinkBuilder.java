// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.web.apirequest;

import com.yahoo.bard.webservice.util.Pagination;
import com.yahoo.bard.webservice.web.util.PaginationLink;

import java.util.Arrays;
import java.util.stream.Stream;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

public class PageLinkBuilder {

    /**
     * Add page links to the header of the response builder.
     *
     * @param link  The type of the link to add.
     * @param uriInfo The uri info for building page links
     * @param pages  The paginated set of results containing the pages being linked to.
     */
    protected static void addPageLink(
            Response.ResponseBuilder builder,
            PaginationLink link,
            UriInfo uriInfo,
            Pagination<?> pages
    ) {
        link.getPage(pages).ifPresent(page -> addPageLink(builder, link, uriInfo, page));
    }

    /**
     * Add page links to the header of the response builder.
     *
     * @param link  The type of the link to add.
     * @param uriInfo The uri info for building page links
     * @param pageNumber  Number of the page to add the link for.
     */
    protected static void addPageLink(Response.ResponseBuilder builder, PaginationLink link, UriInfo uriInfo, int pageNumber) {
        UriBuilder uriBuilder = uriInfo.getRequestUriBuilder().replaceQueryParam("page", pageNumber);
        builder.header(HttpHeaders.LINK, Link.fromUriBuilder(uriBuilder).rel(link.getHeaderName()).build());
    }


    /**
     * Add links to the response builder and return a stream with the requested page from the raw data
     *
     *
     * @param responseBuilder  The Response Builder to be updated
     * @param paginationParameters  The request parameters for paginating
     * @param data  The collection of data to be paginated
     * @param uriInfo  The UriInfo used to produce links
     * @param <T>  The type of the paging collection
     *
     * @return  A stream from the subcollection of the data collection corresponding to the page described


    public static <T> Stream<T> paginate(
            Response.ResponseBuilder responseBuilder,
            PaginationParameters paginationParameters,
            Collection<T> data,
            UriInfo uriInfo
    ) {
        return PageLinkBuilder.paginate(
                responseBuilder,
                new AllPagesPagination<>(data, paginationParameters),
                uriInfo
        );
    }
     */
    /**
     * Add links to the response builder and return a stream with the requested page from the raw data.
     *
     * @param <T>  The type of the collection elements
     * @param pagination  The pagination object
     *
     * @return  A stream from the subcollection of the data collection corresponding to the page described
     */

    public static <T> Stream<T> paginate(
            Response.ResponseBuilder responseBuilder,
            Pagination<T> pagination,
            UriInfo uriInfo
    ) {
        Arrays.stream(PaginationLink.values())
                .forEachOrdered(link -> addPageLink(responseBuilder, link, uriInfo, pagination));

        return pagination.getPageOfData().stream();
    }

}
