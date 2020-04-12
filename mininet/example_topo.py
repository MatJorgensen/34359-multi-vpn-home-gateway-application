#!/usr/bin/python                                                                            
                                                                                             
from mininet.topo import Topo
from mininet.net import Mininet
from mininet.util import dumpNodeConnections
from mininet.log import setLogLevel
from mininet.net import Mininet
from mininet.node import Controller, RemoteController, OVSController
from mininet.node import CPULimitedHost, Host, Node
from mininet.node import OVSKernelSwitch, UserSwitch
from mininet.node import IVSSwitch
from mininet.cli import CLI
from mininet.log import setLogLevel, info
from mininet.link import TCLink, Intf
from subprocess import call

class ExampleTopo(Topo):
    "Example topology from assignment description with 4 switches and 1 host."
    def build(self, n=4):
	# Create four switches
	switches = [self.addSwitch('s%s' % (s + 1), cls=OVSKernelSwitch) for s in range(n)]
	
	# Create a host
	host = self.addHost('h1', cls=Host, ip='10.0.0.1', mac='00:00:00:00:00:01')

	# Add links between switches and the host
	self.addLink(host, switches[0], cls=TCLink, **h1s1)
	self.addLink(switches[0], switches[2], cls=TCLink, **s1s3)
	self.addLink(switches[0], switches[3], cls=TCLink, **s1s4)
	self.addLink(switches[1], switches[2], cls=TCLink, **s2s3)
	self.addLink(switches[1], switches[3], cls=TCLink, **s2s4)

def simpleTest():
    "Create and test the example network"
    topo = ExampleTopo()
    net = Mininet(topo=topo,
		  controller=lambda name: RemoteController(name,
							   ip='127.0.0.1',
							   protocol='tcp',
							   port=6653),
		  ipBase='10.0.0.0/8')

    net.start()
    print "Dumping host connections"
    dumpNodeConnections(net.hosts)
    print "Testing network connectivity"
    CLI(net)
    net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    simpleTest()
