package com.example.network_base.data.model

import java.util.UUID

/**
 * Топология сети - содержит все устройства и соединения
 */
data class NetworkTopology(
    val id: String = UUID.randomUUID().toString(),
    var name: String = "Новая сеть",
    val devices: MutableList<NetworkDevice> = mutableListOf(),
    val connections: MutableList<Connection> = mutableListOf()
) {
    /**
     * Добавить устройство в топологию
     */
    fun addDevice(device: NetworkDevice) {
        devices.add(device)
    }
    
    /**
     * Удалить устройство и все его соединения
     */
    fun removeDevice(deviceId: String) {
        // Сначала удаляем все соединения устройства
        val deviceConnections = connections.filter { conn ->
            val (dev1, dev2) = conn.getDeviceIds()
            dev1 == deviceId || dev2 == deviceId
        }
        
        // Очищаем connectedTo в связанных интерфейсах
        deviceConnections.forEach { conn ->
            findInterface(conn.interface1Id)?.connectedTo = null
            findInterface(conn.interface2Id)?.connectedTo = null
        }
        
        connections.removeAll(deviceConnections.toSet())
        devices.removeAll { it.id == deviceId }
    }
    
    /**
     * Создать соединение между двумя интерфейсами
     */
    fun connect(interface1Id: String, interface2Id: String): Boolean {
        val iface1 = findInterface(interface1Id) ?: return false
        val iface2 = findInterface(interface2Id) ?: return false
        
        // Проверяем, что интерфейсы свободны
        if (iface1.connectedTo != null || iface2.connectedTo != null) {
            return false
        }
        
        // Проверяем, что это разные устройства
        if (iface1.deviceId == iface2.deviceId) {
            return false
        }
        
        iface1.connectedTo = interface2Id
        iface2.connectedTo = interface1Id
        
        connections.add(Connection(interface1Id = interface1Id, interface2Id = interface2Id))
        return true
    }
    
    /**
     * Удалить соединение
     */
    fun disconnect(connectionId: String): Boolean {
        val connection = connections.find { it.id == connectionId } ?: return false
        
        findInterface(connection.interface1Id)?.connectedTo = null
        findInterface(connection.interface2Id)?.connectedTo = null
        
        return connections.remove(connection)
    }
    
    /**
     * Соединить два устройства по первым свободным интерфейсам
     */
    fun connectDevices(device1Id: String, device2Id: String): Boolean {
        val device1 = findDevice(device1Id) ?: return false
        val device2 = findDevice(device2Id) ?: return false
        
        val freeIface1 = device1.getFreeInterface() ?: return false
        val freeIface2 = device2.getFreeInterface() ?: return false
        
        return connect(freeIface1.id, freeIface2.id)
    }
    
    /**
     * Найти интерфейс по ID
     */
    fun findInterface(interfaceId: String): NetworkInterface? {
        for (device in devices) {
            device.interfaces.find { it.id == interfaceId }?.let { return it }
        }
        return null
    }
    
    /**
     * Найти устройство по ID
     */
    fun findDevice(deviceId: String): NetworkDevice? {
        return devices.find { it.id == deviceId }
    }
    
    /**
     * Найти устройство по IP-адресу
     */
    fun findDeviceByIp(ipAddress: String): NetworkDevice? {
        return devices.find { device ->
            device.interfaces.any { it.ipAddress == ipAddress }
        }
    }
    
    /**
     * Получить все соседние устройства
     */
    fun getNeighbors(deviceId: String): List<NetworkDevice> {
        val device = findDevice(deviceId) ?: return emptyList()
        val neighborIds = mutableSetOf<String>()
        
        for (iface in device.interfaces) {
            iface.connectedTo?.let { connectedIfaceId ->
                val neighborDeviceId = connectedIfaceId.substringBefore(":")
                neighborIds.add(neighborDeviceId)
            }
        }
        
        return neighborIds.mapNotNull { findDevice(it) }
    }
    
    /**
     * Получить соединение между двумя устройствами
     */
    fun getConnectionBetween(device1Id: String, device2Id: String): Connection? {
        return connections.find { conn ->
            val (dev1, dev2) = conn.getDeviceIds()
            (dev1 == device1Id && dev2 == device2Id) || (dev1 == device2Id && dev2 == device1Id)
        }
    }
    
    /**
     * Получить все устройства определённого типа
     */
    fun getDevicesByType(type: DeviceType): List<NetworkDevice> {
        return devices.filter { it.type == type }
    }
    
    /**
     * Получить все IP-адреса в топологии
     */
    fun getAllIpAddresses(): List<String> {
        return devices.flatMap { device ->
            device.interfaces.mapNotNull { it.ipAddress }
        }
    }
    
    /**
     * Получить все уникальные подсети (по первым 3 октетам)
     */
    fun getSubnets(): Set<String> {
        return getAllIpAddresses()
            .map { it.substringBeforeLast(".") }
            .toSet()
    }
    
    /**
     * Проверить, есть ли циклы в топологии
     */
    fun hasCycles(): Boolean {
        if (devices.isEmpty()) return false
        
        val visited = mutableSetOf<String>()
        val parent = mutableMapOf<String, String?>()
        
        fun dfs(deviceId: String, parentId: String?): Boolean {
            visited.add(deviceId)
            parent[deviceId] = parentId
            
            for (neighbor in getNeighbors(deviceId)) {
                if (neighbor.id !in visited) {
                    if (dfs(neighbor.id, deviceId)) return true
                } else if (neighbor.id != parentId) {
                    return true // Нашли цикл
                }
            }
            return false
        }
        
        for (device in devices) {
            if (device.id !in visited) {
                if (dfs(device.id, null)) return true
            }
        }
        
        return false
    }
    
    /**
     * Создать копию топологии
     */
    fun copy(): NetworkTopology {
        val newTopology = NetworkTopology(
            id = UUID.randomUUID().toString(),
            name = "$name (копия)"
        )
        
        // Копируем устройства
        val deviceMapping = mutableMapOf<String, String>() // old id -> new id
        for (device in devices) {
            val newDevice = NetworkDevice(
                type = device.type,
                name = device.name,
                x = device.x,
                y = device.y,
                defaultGateway = device.defaultGateway
            )
            deviceMapping[device.id] = newDevice.id
            
            // Копируем интерфейсы
            newDevice.interfaces.clear()
            for (iface in device.interfaces) {
                newDevice.interfaces.add(
                    NetworkInterface(
                        id = "${newDevice.id}:${iface.name}",
                        name = iface.name,
                        deviceId = newDevice.id,
                        ipAddress = iface.ipAddress,
                        subnetMask = iface.subnetMask,
                        vlanId = iface.vlanId
                    )
                )
            }
            
            newTopology.addDevice(newDevice)
        }
        
        // Копируем соединения
        for (conn in connections) {
            val oldDevice1Id = conn.interface1Id.substringBefore(":")
            val oldDevice2Id = conn.interface2Id.substringBefore(":")
            val ifaceName1 = conn.interface1Id.substringAfter(":")
            val ifaceName2 = conn.interface2Id.substringAfter(":")
            
            val newDevice1Id = deviceMapping[oldDevice1Id] ?: continue
            val newDevice2Id = deviceMapping[oldDevice2Id] ?: continue
            
            newTopology.connect("$newDevice1Id:$ifaceName1", "$newDevice2Id:$ifaceName2")
        }
        
        return newTopology
    }
}

