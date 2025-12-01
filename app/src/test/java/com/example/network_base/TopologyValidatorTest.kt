package com.example.network_base

import com.example.network_base.data.model.*
import com.example.network_base.domain.validation.TopologyValidator
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for TopologyValidator
 */
class TopologyValidatorTest {
    
    private lateinit var validator: TopologyValidator
    private lateinit var topology: NetworkTopology
    
    @Before
    fun setup() {
        validator = TopologyValidator()
        topology = NetworkTopology(name = "Test Network")
    }
    
    @Test
    fun `test empty topology validation fails`() {
        val result = validator.validateBasic(topology)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.isNotEmpty())
        assertEquals(0, result.score)
    }
    
    @Test
    fun `test single device without IP has warning`() {
        val pc = NetworkDevice.createPC("PC-1", 100f, 100f)
        topology.addDevice(pc)
        
        val result = validator.validateBasic(topology)
        
        assertTrue(result.isValid)
        assertTrue(result.warnings.isNotEmpty())
    }
    
    @Test
    fun `test duplicate IP detection`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.10"
        }
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.10" // Same IP
        }
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        
        val result = validator.validateBasic(topology)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.message.contains("Дублирующийся") })
    }
    
    @Test
    fun `test valid network with unique IPs`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.10"
        }
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.20"
        }
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        topology.connectDevices(pc1.id, pc2.id)
        
        val result = validator.validateBasic(topology)
        
        assertTrue(result.isValid)
        assertEquals(100, result.score)
    }
    
    @Test
    fun `test invalid IP format detection`() {
        val pc = NetworkDevice.createPC("PC-1", 100f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.999" // Invalid
        }
        
        topology.addDevice(pc)
        
        val result = validator.validateBasic(topology)
        
        assertFalse(result.isValid)
        assertTrue(result.errors.any { it.message.contains("Некорректный") })
    }
    
    @Test
    fun `test disconnected network warning`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.10"
        }
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.20"
        }
        
        // Not connected
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        
        val result = validator.validateBasic(topology)
        
        assertTrue(result.warnings.any { it.contains("разделена") })
    }
    
    @Test
    fun `test task validation with device count requirement`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f)
        topology.addDevice(pc1)
        
        val task = Task(
            id = "test_task",
            moduleId = "test_module",
            title = "Test",
            description = "Test task",
            requirements = listOf(
                TaskRequirement.DeviceCount(
                    deviceType = DeviceType.PC,
                    minCount = 2,
                    description = "Need 2 PCs",
                    errorMessage = "Add more PCs"
                )
            )
        )
        
        val result = validator.validateTask(topology, task)
        
        assertFalse(result.isValid)
        assertEquals(0, result.score)
        assertTrue(result.failedRequirements.contains("Need 2 PCs"))
    }
    
    @Test
    fun `test task validation passes with all requirements met`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.10"
        }
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.20"
        }
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        topology.connectDevices(pc1.id, pc2.id)
        
        val task = Task(
            id = "test_task",
            moduleId = "test_module",
            title = "Test",
            description = "Test task",
            requirements = listOf(
                TaskRequirement.DeviceCount(
                    deviceType = DeviceType.PC,
                    minCount = 2,
                    description = "Need 2 PCs",
                    errorMessage = "Add more PCs"
                ),
                TaskRequirement.DevicesConnected(
                    deviceNames = listOf("PC"),
                    description = "PCs connected",
                    errorMessage = "Connect PCs"
                )
            )
        )
        
        val result = validator.validateTask(topology, task)
        
        assertTrue(result.isValid)
        assertEquals(100, result.score)
        assertEquals(2, result.completedRequirements.size)
    }
    
    @Test
    fun `test canCommunicate returns true for connected devices`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f)
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f)
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        topology.connectDevices(pc1.id, pc2.id)
        
        val result = validator.canCommunicate(topology, pc1.id, pc2.id)
        
        assertTrue(result)
    }
    
    @Test
    fun `test canCommunicate returns false for disconnected devices`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f)
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f)
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        // Not connected
        
        val result = validator.canCommunicate(topology, pc1.id, pc2.id)
        
        assertFalse(result)
    }
    
    @Test
    fun `test subnet count requirement`() {
        val pc1 = NetworkDevice.createPC("PC-1", 100f, 100f).apply {
            interfaces[0].ipAddress = "192.168.1.10"
        }
        val pc2 = NetworkDevice.createPC("PC-2", 200f, 100f).apply {
            interfaces[0].ipAddress = "192.168.2.10"
        }
        
        topology.addDevice(pc1)
        topology.addDevice(pc2)
        
        val task = Task(
            id = "test_task",
            moduleId = "test_module",
            title = "Test",
            description = "Test task",
            requirements = listOf(
                TaskRequirement.SubnetCount(
                    count = 2,
                    description = "Create 2 subnets",
                    errorMessage = "Need 2 subnets"
                )
            )
        )
        
        val result = validator.validateTask(topology, task)
        
        assertTrue(result.isValid)
        assertEquals(100, result.score)
    }
}

