/*
 * Copyright 2016 Open Networking Laboratory
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
package org.student.multi_vpn_app;

import org.apache.felix.scr.annotations.*;
import org.onlab.packet.*;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Host;
import org.onosproject.net.Port;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.*;
import org.onosproject.net.flowobjective.DefaultForwardingObjective;
import org.onosproject.net.flowobjective.FlowObjectiveService;
import org.onosproject.net.flowobjective.ForwardingObjective;
import org.onosproject.net.packet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class AppComponent {

    private ApplicationId appId;
    private static String H1_MAC = "00:00:00:00:00:01";
    private static String H2_MAC = "00:00:00:00:00:02";
    private static String S1_ID = "of:0000000000000001";
    private static String S2_ID = "of:0000000000000002";
    private static long H1_S1_PORT = 1;
    private static long H2_S2_PORT = 1;
    private static long S1_S2_PORT = 2;
    private static long S2_S1_PORT = 2;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    FlowObjectiveService flowObjectiveService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    private ReactivePacketProcessor processor = new ReactivePacketProcessor();

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Activate
    protected void activate() {
        appId = coreService.registerApplication("org.student.multi-vpn-app");
        TrafficSelector packetSelector = DefaultTrafficSelector.builder()
                .matchEthType(Ethernet.TYPE_IPV4).build();
        packetService.requestPackets(packetSelector, PacketPriority.REACTIVE, appId);
        packetService.addProcessor(processor, PacketProcessor.director(2));
        log.info("Started");
    }

    @Deactivate
    protected void deactivate() {
        flowRuleService.removeFlowRulesById(appId);
        packetService.removeProcessor(processor);
        processor = null;
        log.info("Stopped");
    }

    private class ReactivePacketProcessor implements PacketProcessor {
        @Override
        public void process(PacketContext context) {
            InboundPacket pkt = context.inPacket();
            Ethernet ethPkt = pkt.parsed();
            //Discard if packet is null.
            if (ethPkt == null) {
                log.info("Discarding null packet");
                return;
            }

            if (ethPkt.getEtherType() != Ethernet.TYPE_IPV4) {
                return;
            }

            log.info("Processing packet request");
            // log.info("VlanID: " + ethPkt.getVlanID());

            DeviceId deviceId = pkt.receivedFrom().deviceId();
            MacAddress dstMac = ethPkt.getDestinationMAC();
            PortNumber outPort = PortNumber.FLOOD;
            VlanId vlanId = VlanId.vlanId(VlanId.UNTAGGED);

            TrafficSelector.Builder packetSelector1 = DefaultTrafficSelector.builder();
            packetSelector1.matchEthType(Ethernet.TYPE_IPV4);

            TrafficSelector.Builder packetSelector2 = DefaultTrafficSelector.builder();
            packetSelector2.matchEthType(Ethernet.TYPE_IPV4);

            TrafficTreatment.Builder packetTreatment1 = DefaultTrafficTreatment.builder();
            TrafficTreatment.Builder packetTreatment2 = DefaultTrafficTreatment.builder();

            if (deviceId.equals(DeviceId.deviceId("of:0000000000000001"))) {
                log.info("s1");
                if (dstMac.equals(MacAddress.valueOf("00:00:00:00:00:01"))) {
                    outPort = PortNumber.portNumber(1);
                    vlanId = VlanId.vlanId(vlanId.UNTAGGED);
                    log.info("Setting output port to: " + outPort);
                    log.info("Tagging packet with vlan tag " + vlanId);
                    packetSelector1.matchVlanId(VlanId.vlanId((short) 200));
                    packetSelector1.matchEthDst(dstMac);
                    packetTreatment1.popVlan();
                    packetTreatment1.setOutput(outPort);
                    forwardRequest(context, packetSelector1, packetTreatment1, deviceId, outPort);

                } else if (dstMac.equals(MacAddress.valueOf("00:00:00:00:00:02"))) {
                    outPort = PortNumber.portNumber(2);
                    vlanId = VlanId.vlanId((short) 200);
                    log.info("Setting output port to: " + outPort);
                    log.info("Tagging packet with vlan tag " + vlanId);
                    packetSelector1.matchEthDst(dstMac);
                    packetTreatment1.pushVlan().setVlanId(vlanId).setOutput(outPort);
                    forwardRequest(context, packetSelector1, packetTreatment1, deviceId, outPort);
                } else {
                    log.info("Unknown destination host, ignoring");
                    return;
                }
            } else if (deviceId.equals(DeviceId.deviceId("of:0000000000000002"))) {
                log.info("s2");
                if (dstMac.equals(MacAddress.valueOf("00:00:00:00:00:01"))) {
                    outPort = PortNumber.portNumber(2);
                    vlanId = VlanId.vlanId((short) 200);
                    log.info("Setting output port to: " + outPort);
                    log.info("Tagging packet with vlan tag " + vlanId);
                    packetSelector2.matchEthDst(dstMac);
                    packetTreatment2.pushVlan().setVlanId(vlanId).setOutput(outPort);
                    forwardRequest(context, packetSelector2, packetTreatment2, deviceId, outPort);
                } else if (dstMac.equals(MacAddress.valueOf("00:00:00:00:00:02"))) {
                    outPort = PortNumber.portNumber(1);
                    vlanId = VlanId.vlanId(VlanId.UNTAGGED);
                    log.info("Setting output port to: " + outPort);
                    log.info("Tagging packet with vlan tag " + vlanId);
                    packetSelector2.matchVlanId(VlanId.vlanId((short) 200));
                    packetSelector2.matchEthDst(dstMac);
                    packetTreatment2.popVlan();
                    packetTreatment2.setOutput(outPort);
                    forwardRequest(context, packetSelector2, packetTreatment2, deviceId, outPort);
                } else {
                    log.info("Unknown destination host, ignoring");
                    return;
                }
            }
            //packetSelector1.matchEthDst(dstMac);

            //if (outPort != PortNumber.FLOOD) flowObjectiveService.forward(deviceId, forwardingObjective);
            // context.treatmentBuilder().addTreatment(treatment.build());
            //packetTreatment.setOutput(outPort);

            //Generate the traffic selector based on the packet that arrived.
            //TrafficSelector packetSelector = DefaultTrafficSelector.builder()
            //        .matchEthType(Ethernet.TYPE_IPV4)
            //        .matchVlanId(vlanId)
            //        .matchEthDst(dstMac).build();

            //TrafficTreatment treatment = DefaultTrafficTreatment.builder()
            //        .pushVlan()
            //        .setVlanId(vlanId)
            //        .setOutput(outPort).build();

            //ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
            //        .withSelector(packetSelector.build())
            //        .withTreatment(packetTreatment.build())
            //        .withPriority(5000)
            //        .withFlag(ForwardingObjective.Flag.VERSATILE)
            //        .fromApp(appId)
            //        .makeTemporary(5)
            //        .add();

            //if (outPort != PortNumber.FLOOD) flowObjectiveService.forward(deviceId, forwardingObjective);
            //context.treatmentBuilder().addTreatment(packetTreatment.build());
            //context.send();

        }

        public void forwardRequest(PacketContext context, TrafficSelector.Builder selector, TrafficTreatment.Builder treatment, DeviceId deviceId, PortNumber outPort) {
            ForwardingObjective forwardingObjective = DefaultForwardingObjective.builder()
                    .withSelector(selector.build())
                    .withTreatment(treatment.build())
                    .withPriority(5000)
                    .withFlag(ForwardingObjective.Flag.VERSATILE)
                    .fromApp(appId)
                    .makeTemporary(5)
                    .add();

        if (outPort != PortNumber.FLOOD) flowObjectiveService.forward(deviceId, forwardingObjective);
        context.treatmentBuilder().addTreatment(treatment.build());
	    context.send(); // TJEK DENNE...!
        }
    }
}

