package com.example.network_base.data.model

import java.util.UUID

/**
 * Типы сетевых устройств
 */
enum class DeviceType {
    PC,
    ROUTER,
    SWITCH,
    SERVER,
    HUB
}

/**
 * Сетевой интерфейс устройства
 */
data class NetworkInterface(
    val id: String,
    val name: String,
    val deviceId: String,
    var ipAddress: String? = null,
    var subnetMask: String? = null,
    var macAddress: String = generateMacAddress(),
    var connectedTo: String? = null, // ID другого интерфейса
    var vlanId: Int? = null
) {
    companion object {
        fun generateMacAddress(): String {
            val bytes = ByteArray(6)
            java.util.Random().nextBytes(bytes)
            bytes[0] = (bytes[0].toInt() and 0xfe).toByte() // unicast
            return bytes.joinToString(":") { String.format("%02x", it) }
        }
        
        fun create(name: String, deviceId: String): NetworkInterface {
            return NetworkInterface(
                id = "$deviceId:$name",
                name = name,
                deviceId = deviceId
            )
        }
    }
}

/**
 * Сетевое устройство
 */
data class NetworkDevice(
    val id: String = UUID.randomUUID().toString(),
    val type: DeviceType,
    var name: String,
    var x: Float = 0f,
    var y: Float = 0f,
    val interfaces: MutableList<NetworkInterface> = mutableListOf(),
    var defaultGateway: String? = null // Для ПК и серверов
) {
    init {
        if (interfaces.isEmpty()) {
            initializeInterfaces()
        }
    }
    
    private fun initializeInterfaces() {
        when (type) {
            DeviceType.PC, DeviceType.SERVER -> {
                interfaces.add(NetworkInterface.create("eth0", id))
            }
            DeviceType.ROUTER -> {
                repeat(4) { i ->
                    interfaces.add(NetworkInterface.create("eth$i", id))
                }
            }
            DeviceType.SWITCH -> {
                repeat(8) { i ->
                    interfaces.add(NetworkInterface.create("port$i", id))
                }
            }
            DeviceType.HUB -> {
                repeat(4) { i ->
                    interfaces.add(NetworkInterface.create("port$i", id))
                }
            }
        }
    }
    
    /**
     * Получить первый интерфейс с IP-адресом
     */
    fun getPrimaryInterface(): NetworkInterface? {
        return interfaces.firstOrNull { it.ipAddress != null }
    }
    
    /**
     * Получить свободный интерфейс
     */
    fun getFreeInterface(): NetworkInterface? {
        return interfaces.firstOrNull { it.connectedTo == null }
    }
    
    /**
     * Проверить, является ли устройство Layer 2 (коммутатор, хаб)
     */
    fun isLayer2Device(): Boolean {
        return type == DeviceType.SWITCH || type == DeviceType.HUB
    }
    
    /**
     * Проверить, является ли устройство маршрутизатором
     */
    fun isRouter(): Boolean {
        return type == DeviceType.ROUTER
    }
    
    companion object {
        fun createPC(name: String, x: Float = 0f, y: Float = 0f): NetworkDevice {
            return NetworkDevice(type = DeviceType.PC, name = name, x = x, y = y)
        }
        
        fun createRouter(name: String, x: Float = 0f, y: Float = 0f): NetworkDevice {
            return NetworkDevice(type = DeviceType.ROUTER, name = name, x = x, y = y)
        }
        
        fun createSwitch(name: String, x: Float = 0f, y: Float = 0f): NetworkDevice {
            return NetworkDevice(type = DeviceType.SWITCH, name = name, x = x, y = y)
        }
        
        fun createServer(name: String, x: Float = 0f, y: Float = 0f): NetworkDevice {
            return NetworkDevice(type = DeviceType.SERVER, name = name, x = x, y = y)
        }
        
        fun createHub(name: String, x: Float = 0f, y: Float = 0f): NetworkDevice {
            return NetworkDevice(type = DeviceType.HUB, name = name, x = x, y = y)
        }
    }
}

