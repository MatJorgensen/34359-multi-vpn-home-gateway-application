/*
 * Copyright 2020-present Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.meowster.app.sample;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableSet;
import org.onosproject.ui.RequestHandler;
import org.onosproject.ui.UiMessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * Skeletal ONOS UI Custom-View message handler.
 */

public class AppUiMessageHandler extends UiMessageHandler {

    private static final String SAMPLE_CUSTOM_DATA_REQ = "sampleCustomDataRequest";
    private static final String SAMPLE_CUSTOM_DATA_RESP = "sampleCustomDataResponse";

    private static final String Customer1VLAN = "Customer1VLAN";
    private static final String Customer2VLAN = "Customer2VLAN";
    private static final String Customer3VLAN = "Customer3VLAN";
    private static final int C1VLAN = 200;
    private static final int C2VLAN = 200;
    private static final int C3VLAN = 0;
    private static final int NoVlan = 0;
    private static final String MESSAGE = "message";
    private static final String MSG_FORMAT = "VLAN TAG VALUE IS: %d ";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private static boolean VlanOn = false;

    public static boolean getVlan() {
        return VlanOn;
    }
    public static int getC1vlan() {
        return C1VLAN;
    }
    public static int getC2vlan() {
        return C2VLAN;
    }
    public static int getC3vlan() {
        return C3VLAN;
    }

    @Override
    protected Collection<RequestHandler> createRequestHandlers() {
        return ImmutableSet.of(
                new SampleCustomDataRequestHandler()
        );
    }

    // handler for sample data requests
    private final class SampleCustomDataRequestHandler extends RequestHandler {

        private SampleCustomDataRequestHandler() {
            super(SAMPLE_CUSTOM_DATA_REQ);
        }

        @Override
        public void process(ObjectNode payload) {
            if(!VlanOn){
                VlanOn = true;
                log.debug("Computing data for {}...", 444);

                ObjectNode result = objectNode();
                result.put(Customer1VLAN, C1VLAN);
                result.put(Customer2VLAN, C2VLAN);
                result.put(Customer3VLAN, C3VLAN);
                result.put(MESSAGE, String.format(MSG_FORMAT, 1));
                sendMessage(SAMPLE_CUSTOM_DATA_RESP, result);

            }
            else{
                VlanOn = false;
                ObjectNode result = objectNode();
                result.put(Customer1VLAN, NoVlan);
                result.put(Customer2VLAN, NoVlan);
                result.put(Customer3VLAN, NoVlan);
                result.put(MESSAGE, String.format(MSG_FORMAT, 0));
                sendMessage(SAMPLE_CUSTOM_DATA_RESP, result);
            }
        }
    }
}
