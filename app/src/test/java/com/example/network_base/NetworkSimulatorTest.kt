package com.example.network_base

import com.example.network_base.data.model.*
import com.example.network_base.domain.simulation.NetworkSimulator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for NetworkSimulator
 */
class NetworkSimulatorTest {
    
    private lateinit var topology: NetworkTopology
    private lateinit var simulator: NetworkSimulator
    
    @Before
    fun setup() {
        topology = NetworkTopology(name = "Test Network")
    }
    
    @Test
    fun `test findPath between connected devices`() {
        // Create 3 devices
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.10"
        }
        val pc2 = NetworkDevice.createPC("PC-2", 300f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.20"
        }
        val switch1 = NetworkDevice.createSwitch("Switch-1", 200f, 200f)
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        topology.addDevice(switch1)
        
        // Connect devices
        topology.connectDevices(pc1.id, switch1.id)
        topology.connectDevices(pc2.id, switch1.id)
        
        simulator = NetworkSimulator(topology)
        
        // Test path finding
        val path = simulator.findPath(pc1.id, pc2.id)
        
        assertEquals(3, path.size)
        assertEquals(pc1.id, path[0])
        assertEquals(switch1.id, path[1])
        assertEquals(pc2.id, path[2])
    }
    
    @Test
    fun `test findPath returns empty for disconnected devices`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f)
        val pc2 = NetworkDevice.createPC("PC-2", 300f, 100f)
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        
        simulator = NetworkSimulator(topology)
        
        val path = simulator.findPath(pc1.id, pc2.id)
        
        assertTrue(path.isEmpty())
    }
    
    @Test
    fun `test areInSameSubnet with same subnet`() {
        simulator = NetworkSimulator(topology)
        
        val result = simulator.areInSameSubnet(
            "192.168.1.10",
            "192.168.1.20",
            "255.255.255.0"
        )
        
        assertTrue(result)
    }
    
    @Test
    fun `test areInSameSubnet with different subnets`() {
        simulator = NetworkSimulator(topology)
        
        val result = simulator.areInSameSubnet(
            "192.168.1.10",
            "192.168.2.10",
            "255.255.255.0"
        )
        
        assertFalse(result)
    }
    
    @Test
    fun `test getSubnet extraction`() {
        simulator = NetworkSimulator(topology)
        
        val subnet = simulator.getSubnet("192.168.1.100", "255.255.255.0")
        
        assertEquals("192.168.1.0", subnet)
    }
    
    @Test
    fun `test connectivity check`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.10"
        }
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.20"
        }
        val pc3 = NetworkDevice.createPC("PC-3", 300f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.30"
        }
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        topology.addDevice(pc3)
        
        // Only connect pc1 and pc2
        topology.connectDevices(pc1.id, pc2.id)
        
        simulator = NetworkSimulator(topology)
        
        val connectivity = simulator.checkConnectivity()
        
        // pc1-pc2 should be connected
        assertTrue(connectivity[pc1.id to pc2.id] == true || connectivity[pc2.id to pc1.id] == true)
        
        // pc1-pc3 should not be connected
        assertTrue(connectivity[pc1.id to pc3.id] == false || connectivity[pc3.id to pc1.id] == false)
    }
    
    @Test
    fun `test direct connection path`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f)
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f)
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        topology.connectDevices(pc1.id, pc2.id)
        
        simulator = NetworkSimulator(topology)
        
        val path = simulator.findPath(pc1.id, pc2.id)
        
        assertEquals(2, path.size)
        assertEquals(pc1.id, path[0])
        assertEquals(pc2.id, path[1])
    }
}

