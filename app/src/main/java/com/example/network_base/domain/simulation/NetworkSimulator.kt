package com.example.network_base.domain.simulation

import com.example.network_base.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Симулятор сетевых операций
 */
class NetworkSimulator(
    private val topology: NetworkTopology
) {
    // ARP таблицы для каждого устройства (IP -> MAC)
    private val arpTables = mutableMapOf<String, MutableMap<String, String>>()
    
    // MAC таблицы для коммутаторов (MAC -> Port/Interface)
    private val macTables = mutableMapOf<String, MutableMap<String, String>>()
    
    // Таблицы маршрутизации для роутеров (subnet -> interfaceId)
    private val routingTables = mutableMapOf<String, MutableMap<String, String>>()
    
    // Задержка между шагами симуляции (мс)
    var stepDelay: Long = 400
    
    /**
     * Симуляция ping между двумя устройствами
     */
    fun simulatePing(
        sourceDeviceId: String,
        destinationIp: String,
        count: Int = 1
    ): Flow<SimulationEvent> = flow {
        val sourceDevice = topology.findDevice(sourceDeviceId)
            ?: run {
                emit(SimulationEvent.Error("Устройство-источник не найдено"))
                return@flow
            }
        
        val sourceInterface = sourceDevice.getPrimaryInterface()
            ?: run {
                emit(SimulationEvent.Error("У устройства нет настроенного IP-адреса"))
                return@flow
            }
        
        val sourceIp = sourceInterface.ipAddress
            ?: run {
                emit(SimulationEvent.Error("IP-адрес не настроен"))
                return@flow
            }
        
        emit(SimulationEvent.Log("PING $destinationIp из $sourceIp"))
        emit(SimulationEvent.SimulationStarted)
        
        // Проверяем, есть ли устройство с этим IP
        val destinationDevice = topology.findDeviceByIp(destinationIp)
        if (destinationDevice == null) {
            emit(SimulationEvent.Error("Хост $destinationIp недоступен: устройство не найдено"))
            emit(SimulationEvent.SimulationEnded(false))
            return@flow
        }
        
        var successCount = 0
        var failCount = 0
        
        for (seq in 1..count) {
            emit(SimulationEvent.Log("--- Пакет $seq из $count ---"))
            
            // Шаг 1: ARP разрешение (если нужно)
            val destinationMac = resolveArp(sourceDeviceId, destinationIp, sourceIp, sourceInterface.macAddress)
            
            if (destinationMac == null) {
                emit(SimulationEvent.Log("ARP: Не удалось получить MAC для $destinationIp"))
                failCount++
            } else {
                emit(SimulationEvent.Log("ARP: $destinationIp -> $destinationMac"))
                delay(stepDelay / 2)
                
                // Шаг 2: Создаём ICMP Echo Request
                val pingRequest = Packet.createPing(
                    sourceMac = sourceInterface.macAddress,
                    destinationMac = destinationMac,
                    sourceIp = sourceIp,
                    destinationIp = destinationIp,
                    sequenceNumber = seq
                )
                
                emit(SimulationEvent.PacketCreated(pingRequest, sourceDeviceId))
                emit(SimulationEvent.Log("ICMP: Отправляем Echo Request seq=$seq"))
                
                // Шаг 3: Находим путь и отправляем пакет
                val path = findPath(sourceDeviceId, destinationDevice.id)
                
                if (path.isEmpty()) {
                    emit(SimulationEvent.Error("Маршрут до $destinationIp не найден"))
                    failCount++
                } else {
                    // Анимация пути запроса
                    for (i in 0 until path.size - 1) {
                        val from = path[i]
                        val to = path[i + 1]
                        
                        val animState = PacketAnimationState(pingRequest, from, to)
                        emit(SimulationEvent.PacketInTransit(animState))
                        delay(stepDelay)
                    }
                    
                    emit(SimulationEvent.PacketDelivered(pingRequest, destinationDevice.id))
                    emit(SimulationEvent.Log("ICMP: Echo Request доставлен"))
                    delay(stepDelay / 2)
                    
                    // Шаг 4: Создаём и отправляем Reply
                    val pingReply = pingRequest.createReply(PacketType.ICMP_ECHO_REPLY)
                    emit(SimulationEvent.PacketCreated(pingReply, destinationDevice.id))
                    emit(SimulationEvent.Log("ICMP: Отправляем Echo Reply"))
                    
                    // Обратный путь
                    val reversePath = path.reversed()
                    for (i in 0 until reversePath.size - 1) {
                        val from = reversePath[i]
                        val to = reversePath[i + 1]
                        
                        val animState = PacketAnimationState(pingReply, from, to)
                        emit(SimulationEvent.PacketInTransit(animState))
                        delay(stepDelay)
                    }
                    
                    emit(SimulationEvent.PacketDelivered(pingReply, sourceDeviceId))
                    
                    val rtt = (path.size - 1) * 2 * 10 // Примерный RTT в мс
                    emit(SimulationEvent.Log("✓ Ответ от $destinationIp: seq=$seq time=${rtt}ms"))
                    successCount++
                }
            }
            
            if (seq < count) {
                delay(stepDelay)
            }
        }
        
        emit(SimulationEvent.Log("--- Статистика ---"))
        emit(SimulationEvent.Log("Отправлено: $count, Получено: $successCount, Потеряно: $failCount"))
        emit(SimulationEvent.SimulationEnded(failCount == 0))
    }
    
    /**
     * Симуляция ARP запроса
     */
    fun simulateArp(
        sourceDeviceId: String,
        targetIp: String
    ): Flow<SimulationEvent> = flow {
        val sourceDevice = topology.findDevice(sourceDeviceId)
            ?: run {
                emit(SimulationEvent.Error("Устройство-источник не найдено"))
                return@flow
            }
        
        val sourceInterface = sourceDevice.getPrimaryInterface()
            ?: run {
                emit(SimulationEvent.Error("У устройства нет настроенного IP-адреса"))
                return@flow
            }
        
        val sourceIp = sourceInterface.ipAddress ?: run {
            emit(SimulationEvent.Error("IP-адрес не настроен"))
            return@flow
        }
        
        emit(SimulationEvent.SimulationStarted)
        emit(SimulationEvent.Log("ARP: Who has $targetIp? Tell $sourceIp"))
        
        // Создаём ARP запрос (broadcast)
        val arpRequest = Packet.createArpRequest(
            sourceMac = sourceInterface.macAddress,
            sourceIp = sourceIp,
            targetIp = targetIp
        )
        
        emit(SimulationEvent.PacketCreated(arpRequest, sourceDeviceId))
        
        // Находим целевое устройство
        val targetDevice = topology.findDeviceByIp(targetIp)
        if (targetDevice == null) {
            emit(SimulationEvent.Log("ARP: Нет ответа - хост не найден"))
            emit(SimulationEvent.SimulationEnded(false))
            return@flow
        }
        
        // Отправляем broadcast на все соседние устройства
        val neighbors = topology.getNeighbors(sourceDeviceId)
        for (neighbor in neighbors) {
            val animState = PacketAnimationState(arpRequest, sourceDeviceId, neighbor.id)
            emit(SimulationEvent.PacketInTransit(animState))
        }
        delay(stepDelay)
        
        // Если target - это сосед, он отвечает
        val targetInterface = targetDevice.getPrimaryInterface()
        if (targetInterface != null) {
            val arpReply = Packet.createArpReply(
                sourceMac = targetInterface.macAddress,
                destinationMac = sourceInterface.macAddress,
                sourceIp = targetIp,
                destinationIp = sourceIp
            )
            
            emit(SimulationEvent.Log("ARP Reply: $targetIp is at ${targetInterface.macAddress}"))
            emit(SimulationEvent.PacketCreated(arpReply, targetDevice.id))
            
            val animState = PacketAnimationState(arpReply, targetDevice.id, sourceDeviceId)
            emit(SimulationEvent.PacketInTransit(animState))
            delay(stepDelay)
            
            emit(SimulationEvent.PacketDelivered(arpReply, sourceDeviceId))
            
            // Сохраняем в ARP таблицу
            arpTables.getOrPut(sourceDeviceId) { mutableMapOf() }[targetIp] = targetInterface.macAddress
            
            emit(SimulationEvent.Log("ARP: Добавлено в таблицу: $targetIp -> ${targetInterface.macAddress}"))
            emit(SimulationEvent.SimulationEnded(true))
        } else {
            emit(SimulationEvent.Log("ARP: Нет ответа"))
            emit(SimulationEvent.SimulationEnded(false))
        }
    }
    
    /**
     * Получить ARP таблицу устройства
     */
    fun getArpTable(deviceId: String): Map<String, String> {
        return arpTables[deviceId]?.toMap() ?: emptyMap()
    }
    
    /**
     * Очистить ARP таблицу устройства
     */
    fun clearArpTable(deviceId: String) {
        arpTables[deviceId]?.clear()
    }
    
    /**
     * Очистить все таблицы
     */
    fun clearAllTables() {
        arpTables.clear()
        macTables.clear()
        routingTables.clear()
    }
    
    /**
     * Разрешение ARP (получение MAC по IP)
     */
    private fun resolveArp(
        fromDeviceId: String,
        targetIp: String,
        sourceIp: String,
        sourceMac: String
    ): String? {
        // Проверяем кэш
        arpTables[fromDeviceId]?.get(targetIp)?.let { return it }
        
        // Ищем устройство с этим IP
        val targetDevice = topology.findDeviceByIp(targetIp)
        val targetInterface = targetDevice?.interfaces?.find { it.ipAddress == targetIp }
        
        targetInterface?.let {
            // Записываем в ARP таблицу
            arpTables.getOrPut(fromDeviceId) { mutableMapOf() }[targetIp] = it.macAddress
            return it.macAddress
        }
        
        return null
    }
    
    /**
     * Поиск пути от устройства к устройству (BFS)
     */
    fun findPath(fromDeviceId: String, toDeviceId: String): List<String> {
        if (fromDeviceId == toDeviceId) {
            return listOf(fromDeviceId)
        }
        
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<List<String>>()
        queue.add(listOf(fromDeviceId))
        
        while (queue.isNotEmpty()) {
            val path = queue.removeFirst()
            val current = path.last()
            
            if (current == toDeviceId) {
                return path
            }
            
            if (current in visited) {
                // Skip already visited
            } else {
                visited.add(current)
                
                for (neighbor in topology.getNeighbors(current)) {
                    if (neighbor.id !in visited) {
                        queue.add(path + neighbor.id)
                    }
                }
            }
        }
        
        return emptyList()
    }
    
    /**
     * Проверка связности сети
     */
    fun checkConnectivity(): Map<Pair<String, String>, Boolean> {
        val results = mutableMapOf<Pair<String, String>, Boolean>()
        
        val devicesWithIp = topology.devices.filter { device ->
            device.getPrimaryInterface()?.ipAddress != null
        }
        
        for (i in devicesWithIp.indices) {
            for (j in i + 1 until devicesWithIp.size) {
                val device1 = devicesWithIp[i]
                val device2 = devicesWithIp[j]
                
                val path = findPath(device1.id, device2.id)
                results[device1.id to device2.id] = path.isNotEmpty()
            }
        }
        
        return results
    }
    
    /**
     * Проверить, в одной ли подсети два IP адреса
     */
    fun areInSameSubnet(ip1: String, ip2: String, mask: String = "255.255.255.0"): Boolean {
        val parts1 = ip1.split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = ip2.split(".").map { it.toIntOrNull() ?: 0 }
        val maskParts = mask.split(".").map { it.toIntOrNull() ?: 0 }
        
        if (parts1.size != 4 || parts2.size != 4 || maskParts.size != 4) {
            return false
        }
        
        for (i in 0..3) {
            if ((parts1[i] and maskParts[i]) != (parts2[i] and maskParts[i])) {
                return false
            }
        }
        
        return true
    }
    
    /**
     * Получить подсеть для IP адреса
     */
    fun getSubnet(ip: String, mask: String = "255.255.255.0"): String {
        val parts = ip.split(".").map { it.toIntOrNull() ?: 0 }
        val maskParts = mask.split(".").map { it.toIntOrNull() ?: 0 }
        
        return parts.zip(maskParts)
            .map { (p, m) -> p and m }
            .joinToString(".")
    }
}

/**
 * События симуляции для UI
 */
sealed class SimulationEvent {
    object SimulationStarted : SimulationEvent()
    data class SimulationEnded(val success: Boolean) : SimulationEvent()
    data class Log(val message: String) : SimulationEvent()
    data class PacketCreated(val packet: Packet, val deviceId: String) : SimulationEvent()
    data class PacketInTransit(val state: PacketAnimationState) : SimulationEvent()
    data class PacketDelivered(val packet: Packet, val toDeviceId: String) : SimulationEvent()
    data class PacketDropped(val packet: Packet, val reason: String) : SimulationEvent()
    data class Error(val message: String) : SimulationEvent()
}
