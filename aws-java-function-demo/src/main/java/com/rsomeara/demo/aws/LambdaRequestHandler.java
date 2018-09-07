/*
 * Copyright (c) Sep 6, 2018 StarChart Labs Authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    romeara - initial API and implementation and/or initial documentation
 */
package com.rsomeara.demo.aws;

import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class LambdaRequestHandler implements RequestHandler<Map<String, Object>, String> {

    @Override
    public String handleRequest(Map<String, Object> input, Context context) {
        context.getLogger().log("Input: " + input);
        return "Hello World - " + input;
    }

}
