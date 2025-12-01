package com.example.network_base.data.model

import java.util.UUID

/**
 * Соединение между двумя сетевыми интерфейсами
 */
data class Connection(
    val id: String = UUID.randomUUID().toString(),
    val interface1Id: String,
    val interface2Id: String,
    var isActive: Boolean = true,
    var bandwidth: Int = 100 // Mbps
) {
    /**
     * Получить ID устройства на другом конце соединения
     */
    fun getOtherInterfaceId(interfaceId: String): String? {
        return when (interfaceId) {
            interface1Id -> interface2Id
            interface2Id -> interface1Id
            else -> null
        }
    }
    
    /**
     * Проверить, содержит ли соединение указанный интерфейс
     */
    fun containsInterface(interfaceId: String): Boolean {
        return interface1Id == interfaceId || interface2Id == interfaceId
    }
    
    /**
     * Получить ID устройств, участвующих в соединении
     */
    fun getDeviceIds(): Pair<String, String> {
        return interface1Id.substringBefore(":") to interface2Id.substringBefore(":")
    }
}

