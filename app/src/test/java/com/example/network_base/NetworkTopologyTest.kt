package com.example.network_base

import com.example.network_base.data.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NetworkTopology
 */
class NetworkTopologyTest {
    
    private lateinit var topology: NetworkTopology
    
    @Before
    fun setup() {
        topology = NetworkTopology(name = "Test Network")
    }
    
    @Test
    fun `test add device`() {
        val pc = NetworkDevice.createPC("PC-1", 100f, 100f)
        
        topology.addDevice(pc)
        
        assertEquals(1, topology.devices.size)
        assertEquals(pc, topology.devices[0])
    }
    
    @Test
    fun `test remove device`() {
        val pc = NetworkDevice.createPC("PC-1", 100f, 100f)
        topology.addDevice(pc)
        
        topology.removeDevice(pc.id)
        
        assertTrue(topology.devices.isEmpty())
    }
    
    @Test
    fun `test connect devices`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f)
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f)
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        
        val result = topology.connectDevices(pc1.id, pc2.id)
        
        assertTrue(result)
        assertEquals(1, topology.connections.size)
        assertNotNull(pc1.interfaces[0].connectedTo)
        assertNotNull(pc2.interfaces[0].connectedTo)
    }
    
    @Test
    fun `test cannot connect same device to itself`() {
        val pc = NetworkDevice.createPC("PC-1", 100f, 100f)
        topology.addDevice(pc)
        
        val result = topology.connect(pc.interfaces[0].id, pc.interfaces[0].id)
        
        assertFalse(result)
    }
    
    @Test
    fun `test find device by id`() {
        val pc = NetworkDevice.createPC("PC-1", 100f, 100f)
        topology.addDevice(pc)
        
        val found = topology.findDevice(pc.id)
        
        assertEquals(pc, found)
    }
    
    @Test
    fun `test find device by IP`() {
        val pc = NetworkDevice.createPC("PC-1", 100f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.10"
        }
        topology.addDevice(pc)
        
        val found = topology.findDeviceByIp("192.168.1.10")
        
        assertEquals(pc, found)
    }
    
    @Test
    fun `test get neighbors`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f)
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f)
        val pc3 = NetworkDevice.createPC("PC-3", 300f, 100f)
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        topology.addDevice(pc3)
        
        topology.connectDevices(pc1.id, pc2.id)
        // pc3 is not connected
        
        val neighbors = topology.getNeighbors(pc1.id)
        
        assertEquals(1, neighbors.size)
        assertEquals(pc2, neighbors[0])
    }
    
    @Test
    fun `test remove device removes connections`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f)
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f)
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        topology.connectDevices(pc1.id, pc2.id)
        
        topology.removeDevice(pc1.id)
        
        assertTrue(topology.connections.isEmpty())
        assertNull(pc2.interfaces[0].connectedTo)
    }
    
    @Test
    fun `test get all IP addresses`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.10"
        }
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.20"
        }
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        
        val ips = topology.getAllIpAddresses()
        
        assertEquals(2, ips.size)
        assertTrue(ips.contains("192.168.1.10"))
        assertTrue(ips.contains("192.168.1.20"))
    }
    
    @Test
    fun `test get subnets`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.10"
        }
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f).apply {
            interfaces[0].ipAddress = "192.168.2.10"
        }
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        
        val subnets = topology.getSubnets()
        
        assertEquals(2, subnets.size)
        assertTrue(subnets.contains("192.168.1"))
        assertTrue(subnets.contains("192.168.2"))
    }
    
    @Test
    fun `test copy topology`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.10"
        }
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.20"
        }
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        topology.connectDevices(pc1.id, pc2.id)
        
        val copy = topology.copy()
        
        assertNotEquals(topology.id, copy.id)
        assertEquals(2, copy.devices.size)
        assertEquals(1, copy.connections.size)
    }
    
    @Test
    fun `test devices by type`() {
        val pc = NetworkDevice.createPC("PC-1", 100f, 100f)
        val router = NetworkDevice.createRouter("Router-1", 200f, 100f)
        val switch = NetworkDevice.createSwitch("Switch-1", 300f, 100f)
        
        topology.addDevice(pc)
        topology.addDevice(router)
        topology.addDevice(switch)
        
        val pcs = topology.getDevicesByType(DeviceType.PC)
        val routers = topology.getDevicesByType(DeviceType.ROUTER)
        
        assertEquals(1, pcs.size)
        assertEquals(1, routers.size)
    }
}

