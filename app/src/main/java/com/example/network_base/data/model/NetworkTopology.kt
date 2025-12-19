
package com.example.network_base.data.model

/**
 * Класс, представляющий топологию сети
 */
data class NetworkTopology(
    val name: String = "",
    val devices: List<NetworkDevice> = emptyList(),
    val connections: List<NetworkConnection> = emptyList()
) {
    /**
     * Найти устройство по ID
     */
    fun findDevice(deviceId: String): NetworkDevice? {
        return devices.find { it.id == deviceId }
    }

    /**
     * Найти устройство по IP-адресу
     */
    fun findDeviceByIp(ip: String): NetworkDevice? {
        return devices.find { device ->
            device.interfaces.any { networkInterface ->
                networkInterface.ipAddress == ip
            }
        }
    }

    /**
     * Получить количество устройств по типу
     */
    fun getDeviceCountByType(deviceType: DeviceType): Int {
        return devices.count { it.type() == deviceType }
    }

    /**
     * Получить соседние устройства для указанного устройства
     */
    fun getNeighbors(deviceId: String): List<NetworkDevice> {
        val connectedInterfaceIds = connections
            .filter { it.sourceDeviceId == deviceId || it.targetDeviceId == deviceId }
            .map { conn ->
                if (conn.sourceDeviceId == deviceId) conn.targetInterfaceId
                else conn.sourceInterfaceId
            }

        return devices.filter { device ->
            device.interfaces.any { networkInterface ->
                networkInterface.id in connectedInterfaceIds
            }
        }
    }

    /**
     * Получить список ID всех устройств
     */
    fun getDeviceIds(): List<String> {
        return devices.map { it.id }
    }

    /**
     * Получить все подсети в топологии
     */
    fun getSubnets(): Set<String> {
        return devices
            .flatMap { it.interfaces }
            .mapNotNull { networkInterface ->
                networkInterface.ipAddress?.let { ip ->
                    // Получаем подсеть из IP (упрощенно)
                    val parts = ip.split(".")
                    if (parts.size == 4) {
                        "${parts[0]}.${parts[1]}.${parts[2]}.0/24"
                    } else null
                }
            }
            .toSet()
    }

    /**
     * Получить количество устройств определенного типа (альтернативный метод)
     */
    fun getDeviceCountByTypeAlt(type: DeviceType): Int {
        return devices.count { device ->
            when (device) {
                is Computer -> device.type() == DeviceType.COMPUTER
                is Switch -> device.type() == DeviceType.SWITCH
                is Router -> device.type() == DeviceType.ROUTER
                else -> false
            }
        }
    }

    /**
     * Получить устройства определенного типа
     */
    fun getDevicesByType(type: DeviceType): List<NetworkDevice> {
        return devices.filter { device ->
            when (device) {
                is Computer -> device.type() == DeviceType.COMPUTER
                is Switch -> device.type() == DeviceType.SWITCH
                is Router -> device.type() == DeviceType.ROUTER
                else -> false
            }
        }
    }

    /**
     * Добавить устройство в топологию
     */
    fun addDevice(device: NetworkDevice): NetworkTopology {
        return copy(devices = devices + device)
    }

    /**
     * Удалить устройство из топологии
     */
    fun removeDevice(deviceId: String): NetworkTopology {
        return copy(
            devices = devices.filter { it.id != deviceId },
            connections = connections.filter { 
                it.sourceDeviceId != deviceId && it.targetDeviceId != deviceId 
            }
        )
    }

    /**
     * Добавить соединение в топологию
     */
    fun addConnection(connection: NetworkConnection): NetworkTopology {
        return copy(connections = connections + connection)
    }

    /**
     * Удалить соединение из топологии
     */
    fun removeConnection(connectionId: String): NetworkTopology {
        return copy(connections = connections.filter { it.id != connectionId })
    }

    /**
     * Соединить два устройства
     */
    fun connectDevices(
        sourceDeviceId: String, 
        sourceInterfaceId: String,
        targetDeviceId: String,
        targetInterfaceId: String
    ): NetworkTopology {
        val connection = NetworkConnection(
            id = generateId(),
            sourceDeviceId = sourceDeviceId,
            sourceInterfaceId = sourceInterfaceId,
            targetDeviceId = targetDeviceId,
            targetInterfaceId = targetInterfaceId
        )
        return addConnection(connection)
    }

    private fun generateId(): String {
        return "conn_${System.currentTimeMillis()}_${(Math.random() * 1000).toInt()}"
    }
}

/**
 * Базовый класс для сетевых устройств
 */
sealed class NetworkDevice {
    abstract val id: String
    abstract val name: String
    abstract var x: Float
    abstract var y: Float
    abstract val interfaces: List<NetworkInterface>

    /**
     * Получить основной интерфейс устройства (первый в списке)
     */
    fun getPrimaryInterface(): NetworkInterface? {
        return interfaces.firstOrNull()
    }

    /**
     * Проверить, является ли устройство устройством уровня 2 (коммутатор)
     */
    fun isLayer2Device(): Boolean {
        return this is Switch
    }

    /**
     * Получить тип устройства
     */
    fun type(): DeviceType {
        return when (this) {
            is Computer -> DeviceType.COMPUTER
            is Switch -> DeviceType.SWITCH
            is Router -> DeviceType.ROUTER
            else -> DeviceType.COMPUTER
        }
    }

    /**
     * Получить шлюз по умолчанию
     */
    var defaultGateway: String? = null
}

/**
 * Компьютер/конечное устройство
 */
data class Computer(
    override val id: String,
    override val name: String,
    override var x: Float,
    override var y: Float,
    override val interfaces: List<NetworkInterface> = listOf(NetworkInterface.generate(id)),
    val isActive: Boolean = true
) : NetworkDevice()

/**
 * Коммутатор
 */
data class Switch(
    override val id: String,
    override val name: String,
    override var x: Float,
    override var y: Float,
    val portCount: Int = 8,
    override val interfaces: List<NetworkInterface> = (1..portCount).map { 
        NetworkInterface.generate("${id}_$it") 
    }
) : NetworkDevice()

/**
 * Маршрутизатор
 */
data class Router(
    override val id: String,
    override val name: String,
    override var x: Float,
    override var y: Float,
    val portCount: Int = 4,
    override val interfaces: List<NetworkInterface> = (1..portCount).map { 
        NetworkInterface.generate("${id}_$it") 
    }
) : NetworkDevice()

/**
 * Сетевой интерфейс
 */
data class NetworkInterface(
    val id: String,
    val name: String,
    val macAddress: String = generateMacAddress(),
    val ipAddress: String? = null,
    val subnetMask: String = "255.255.255.0",
    val vlanId: Int? = null
) {
    companion object {
        fun generate(id: String, name: String = "eth0"): NetworkInterface {
            return NetworkInterface(
                id = id,
                name = name,
                macAddress = generateMacAddress(),
                ipAddress = null,
                subnetMask = "255.255.255.0",
                vlanId = null
            )
        }

        private fun generateMacAddress(): String {
            val macBytes = ByteArray(6)
            (0..5).forEach { i ->
                macBytes[i] = ((Math.random() * 256).toInt()).toByte()
            }
            // Устанавливаем бит локально администрируемого адреса
            macBytes[0] = (macBytes[0].toInt() or 0x02).toByte()

            return macBytes.joinToString(":") { String.format("%02X", it) }
        }
    }
}

/**
 * Соединение между устройствами
 */
data class NetworkConnection(
    val id: String,
    val sourceDeviceId: String,
    val sourceInterfaceId: String,
    val targetDeviceId: String,
    val targetInterfaceId: String
)
