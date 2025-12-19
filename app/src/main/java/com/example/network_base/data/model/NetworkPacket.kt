
package com.example.network_base.data.model

/**
 * Типы пакетов
 */
enum class PacketType {
    ICMP_ECHO_REQUEST,
    ICMP_ECHO_REPLY,
    ARP_REQUEST,
    ARP_REPLY,
    TCP,
    UDP,
    DATA,
    BROADCAST,
    UNKNOWN
}

/**
 * Сетевой пакет
 */
data class Packet(
    val id: String = generateId(),
    val type: PacketType,
    val sourceMac: String,
    val destinationMac: String,
    val sourceIp: String? = null,
    val destinationIp: String? = null,
    val sequenceNumber: Int? = null,
    val data: String? = null
) {
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
            sequenceNumber = sequenceNumber,
            data = data
        )
    }

    companion object {
        /**
         * Создать ICMP Echo Request пакет
         */
        fun createPing(
            sourceMac: String,
            destinationMac: String,
            sourceIp: String,
            destinationIp: String,
            sequenceNumber: Int
        ): Packet {
            return Packet(
                type = PacketType.ICMP_ECHO_REQUEST,
                sourceMac = sourceMac,
                destinationMac = destinationMac,
                sourceIp = sourceIp,
                destinationIp = destinationIp,
                sequenceNumber = sequenceNumber,
                data = "ICMP Echo Request"
            )
        }

        /**
         * Создать ARP Request пакет
         */
        fun createArpRequest(
            sourceMac: String,
            sourceIp: String,
            targetIp: String
        ): Packet {
            return Packet(
                type = PacketType.ARP_REQUEST,
                sourceMac = sourceMac,
                destinationMac = "FF:FF:FF:FF:FF:FF", // Broadcast MAC
                sourceIp = sourceIp,
                destinationIp = targetIp,
                data = "ARP Request: Who has $targetIp? Tell $sourceIp"
            )
        }

        /**
         * Создать ARP Reply пакет
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
                data = "ARP Reply: $sourceIp is at $sourceMac"
            )
        }

        private fun generateId(): String {
            return "packet_${System.currentTimeMillis()}_${(Math.random() * 1000).toInt()}"
        }
    }
}

/**
 * Состояние анимации пакета
 */
data class PacketAnimationState(
    val packet: Packet,
    val fromDeviceId: String,
    val toDeviceId: String,
    var progress: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)
