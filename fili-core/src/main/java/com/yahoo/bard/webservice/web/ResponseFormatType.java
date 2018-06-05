// Copyright 2017 Yahoo Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.web;

public interface ResponseFormatType {

    boolean accepts(String responseFormatValue);

    boolean accepts(ResponseFormatType formatType);
}
