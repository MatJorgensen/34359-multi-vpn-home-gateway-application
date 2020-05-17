![DTU Logo](https://images.squarespace-cdn.com/content/5b052242506fbe7ea6c0969c/1539868936426-869NHDYJ3T0P9JJE2G5J/DTU_Logo_Corporate_Red_RGB.png?format=1500w&content-type=image%2Fpng)

# Multi-VPN Home Gateway Application
Source code for the final project (exam project) for course 34359 Software Defined Networking (SDN) at the Technical University of Denmark.

The Multi-VPN Home Gateway Application is an application which enables users to dynamically connect and disconnect hosts to VLANs, thus creating a VPN, for any given network topology. Network traffic is segmented based on the VLANs, for example, a host connected only to VLAN 200 cannot reach a host connected only to VLAN 300. All hosts are by default untagged. 

## Requirements
```
java == 1.8
maven >= 3.3.9
mininet >= 2.2.2
onos >= 3.0.8
buck >= fd114fefab812fa66af16df93c665ccede53d886
```

## Launch Application
The applicaiton is comprised of multiple software components. To launch it please follow the below steps to start the ONOS controller, generate a network topology, and activate the Multi-VPN Home Gateway Applicaiton. 
### Start ONOS
1. Start ONOS by executing command  `onos-buck run onos-localhost -- clean debug`.
2. Invoke the ONOS CLI by executing command `onos localhost` (remember to navigate to directory `onos`).
### Create Mininet topology
1. Create one of the four premade test topologies using Mininet by executing the command `sudo mn --custom topos.py --topo <topo> --controller onos` where `<topo>` is either of values `topo1`, `topo2`, `topo3`, and `topo4`.
### Install & Activate Application
1. Execute command `mvn clean install -DskipTests` to compile the Maven project from `pom.xml`.
2. Install the application from the `.oar` file in the newly generated `target/` directory by executing command `onos-app localhost install target/multi-vpn-app-1.0-SNAPSHOT.oar`.
3. In the ONOS CLI execute command `app activate multi-vpn-app` to activate the applicaiton.
4. Remember to deactivate automatic forwarding of packets by executing `app deactivate fwd`.

## Authors
* Frederik Rander Andersen - [frander1](https://github.com/frander1)
* Mathias Boss Jørgensen - [MatJorgensen](https://github.com/MatJorgensen)
