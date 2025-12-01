package com.example.network_base.data.model

import java.util.UUID

/**
 * Типы сетевых пакетов
 */
enum class PacketType {
    ICMP_ECHO_REQUEST,    // Ping request
    ICMP_ECHO_REPLY,      // Ping reply
    ARP_REQUEST,          // ARP запрос "Кто имеет этот IP?"
    ARP_REPLY,            // ARP ответ "Это я!"
    DATA,                 // Обычные данные
    BROADCAST             // Широковещательный пакет
}

/**
 * Сетевой пакет для симуляции
 */
data class Packet(
    val id: String = UUID.randomUUID().toString(),
    val type: PacketType,
    val sourceMac: String,
    val destinationMac: String,
    val sourceIp: String?,
    val destinationIp: String?,
    val payload: String = "",
    val ttl: Int = 64,
    val sequenceNumber: Int = 0
) {
    /**
     * Уменьшить TTL и вернуть новый пакет
     */
    fun decrementTtl(): Packet? {
        val newTtl = ttl - 1
        return if (newTtl > 0) copy(ttl = newTtl) else null
    }
    
    /**
     * Проверить, является ли пакет широковещательным
     */
    fun isBroadcast(): Boolean {
        return destinationMac == BROADCAST_MAC || type == PacketType.ARP_REQUEST
    }
    
    /**
     * Создать ответный пакет
     */
    fun createReply(replyType: PacketType): Packet {
        return Packet(
            type = replyType,
            sourceMac = destinationMac,
            destinationMac = sourceMac,
            sourceIp = destinationIp,
            destinationIp = sourceIp,
            sequenceNumber = sequenceNumber
        )
    }
    
    companion object {
        const val BROADCAST_MAC = "ff:ff:ff:ff:ff:ff"
        
        /**
         * Создать ICMP Echo Request (Ping)
         */
        fun createPing(
            sourceMac: String,
            destinationMac: String,
            sourceIp: String,
            destinationIp: String,
            sequenceNumber: Int = 1
        ): Packet {
            return Packet(
                type = PacketType.ICMP_ECHO_REQUEST,
                sourceMac = sourceMac,
                destinationMac = destinationMac,
                sourceIp = sourceIp,
                destinationIp = destinationIp,
                sequenceNumber = sequenceNumber
            )
        }
        
        /**
         * Создать ARP запрос
         */
        fun createArpRequest(
            sourceMac: String,
            sourceIp: String,
            targetIp: String
        ): Packet {
            return Packet(
                type = PacketType.ARP_REQUEST,
                sourceMac = sourceMac,
                destinationMac = BROADCAST_MAC,
                sourceIp = sourceIp,
                destinationIp = targetIp,
                payload = "Who has $targetIp? Tell $sourceIp"
            )
        }
        
        /**
         * Создать ARP ответ
         */
        fun createArpReply(
            sourceMac: String,
            destinationMac: String,
            sourceIp: String,
            destinationIp: String
        ): Packet {
            return Packet(
                type = PacketType.ARP_REPLY,
                sourceMac = sourceMac,
                destinationMac = destinationMac,
                sourceIp = sourceIp,
                destinationIp = destinationIp,
                payload = "$sourceIp is at $sourceMac"
            )
        }
    }
}

/**
 * Состояние пакета при анимации
 */
data class PacketAnimationState(
    val packet: Packet,
    val fromDeviceId: String,
    val toDeviceId: String,
    var progress: Float = 0f, // 0.0 - 1.0
    var status: PacketStatus = PacketStatus.IN_TRANSIT
)

/**
 * Статус пакета
 */
enum class PacketStatus {
    IN_TRANSIT,   // В пути
    DELIVERED,    // Доставлен
    DROPPED,      // Отброшен
    TIMEOUT,      // Таймаут
    PROCESSING    // Обрабатывается устройством
}

