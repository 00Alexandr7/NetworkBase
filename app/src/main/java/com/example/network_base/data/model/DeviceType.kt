
package com.example.network_base.data.model

/**
 * Типы сетевых устройств
 */
enum class DeviceType {
    COMPUTER,
    PC, // Синоним для COMPUTER
    SWITCH,
    ROUTER,
    SERVER,
    FIREWALL,
    ACCESS_POINT,
    HUB
}

/**
 * Расширение для проверки, является ли устройство устройством уровня 2 (коммутатор)
 */
fun NetworkDevice.isLayer2Device(): Boolean {
    return this is Switch
}

/**
 * Расширение для проверки, является ли устройство маршрутизатором
 */
fun NetworkDevice.isRouter(): Boolean {
    return this is Router
}

/**
 * Расширение для получения IP-адреса шлюза по умолчанию
 */
fun NetworkDevice.defaultGateway(): String? {
    return if (this is Router) {
        interfaces.firstOrNull()?.ipAddress
    } else {
        interfaces.firstOrNull()?.ipAddress?.let { ip ->
            val parts = ip.split(".")
            if (parts.size == 4) {
                "${parts[0]}.${parts[1]}.${parts[2]}.1"
            } else null
        }
    }
}

/**
 * Расширение для получения типа устройства
 */
fun NetworkDevice.type(): DeviceType {
    return when (this) {
        is Computer -> DeviceType.COMPUTER
        is Switch -> DeviceType.SWITCH
        is Router -> DeviceType.ROUTER
        else -> DeviceType.COMPUTER
    }
}
