#!/usr/bin/python                                                                            
                                                                                             
from mininet.topo import Topo
from mininet.net import Mininet
from mininet.util import dumpNodeConnections
from mininet.log import setLogLevel

class ExampleTopo(Topo):
    "Example topology from assignment description with 4 switches and 1 host."
    def build(self, n=4):
	switches = [self.addSwitch('s%s' % (s + 1)) for s in range(n)]

	for i in range(2):
	    self.addLink(switches[i], switches[2])
	    self.addLink(switches[i], switches[3])

	host = self.addHost('h1')
	self.addLink(host, switches[0])

def simpleTest():
    "Create and test a simple network"
    topo = ExampleTopo()
    net = Mininet(topo)
    net.start()
    print "Dumping host connections"
    dumpNodeConnections(net.hosts)
    print "Testing network connectivity"
    net.pingAll()
    net.stop()

if __name__ == '__main__':
    setLogLevel('info')
    simpleTest()
