package org.student.multi_vpn_app;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Copyright 2020-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Skeletal ONOS UI Custom-View message handler.
 */

public class AppUiMessageHandler extends UiMessageHandler {

    private static final String SAMPLE_CUSTOM_DATA_REQ = "sampleCustomDataRequest";
    private static final String SAMPLE_CUSTOM_DATA_RESP = "sampleCustomDataResponse";
    private static final String CUST_DATA_REQ = "macAndVlan";

    private static final String macHost = "";
    private static final String vlanTagId = "";

    private static final int NoVlan = 0;
    private static final String MESSAGE = "message";
    private static final String MSG_FORMAT = "VLAN %s applied for host %s";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static boolean VlanOn = false;

    public static boolean getVlan() { return VlanOn; }

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new SampleCustomDataRequestHandler()
        );
    }

    // handler for sample data requests
    private final class SampleCustomDataRequestHandler extends RequestHandler {

        private SampleCustomDataRequestHandler() {
            super(CUST_DATA_REQ);
            log.info("Hello");
            log.info("MacHost: " + macHost.toString());
            log.info("sample Custom data req: " + CUST_DATA_REQ);
        }
        @Override
        public void process(ObjectNode payload) {
            log.info("payload: " + payload);
            String macHost2 = payload.get("host").toString();
            String vlanTagId2 = payload.get("vlanId").toString();
            log.info("Host: " + macHost2 + "VlanId: " + vlanTagId2);
            ObjectNode result = objectNode();
            //log.info(payload.get("payload").toString());
            result.put(MESSAGE, String.format(MSG_FORMAT, payload.get("vlanId").toString(), payload.get("host").toString()));
            //check if tuple of MAC/VLAN exists if it does -> delete else -> create
            log.info("result: " + result);
            sendMessage(result);

        }
    }
}