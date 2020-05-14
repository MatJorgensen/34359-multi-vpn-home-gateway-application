# 34359_multi-vpn-home-gateway-application
Source code for the final project (exam project) for course 34359 Software Defined Networking (SDN) at the Technical University of Denmark. 


# Links & resources
1. https://wiki.onosproject.org/display/ONOS/Basic+ONOS+Tutorial
2. https://wiki.onosproject.org/display/ONOS/Template+Application+Tutorial
3. https://wiki.onosproject.org/display/ONOS/Web+UI+Tutorial+-+Creating+a+Custom+View
4. https://wiki.onosproject.org/display/ONOS/Learning+Switch+Tutorial 


# HOW-TO: Run h1-s1-s2-h2 topology framework to test vlan tags
1. Create topology using mininet CLI command `sudo mn --controller=remote --topo linear,2 --mac --switch=ovs,protocol=OpenFlow13`
2. Start ONOS by executing command `onos-buck run onos-localhost -- clean debug`
3. Start ONOS CLI by executing command `onos localhost` (remember to navigate to directory `onos`)
4. Run Maven to compile the application `mvn clean install -DskipTests`
5. Install the application by executing`onos-app localhost install target/multi-vpn-app-1.0-SNAPSHOT.oar`
6. Deactivate automatic forwarding of packets by executing `app deactivate fwd`
7. Activate the multip-vpn-app application by executing `app activate multi-vpn-app`


# How to run Mininet topologies
To create the test topologies through Mininet execute the command `sudo mn --custom topos.py --topo <topo> --controller onos` where `<topo>` is either of values `topo1`, `topo2`, `topo3`, and `topo4`.
