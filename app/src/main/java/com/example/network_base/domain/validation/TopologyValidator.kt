package com.example.network_base.domain.validation

import com.example.network_base.data.model.*
import com.example.network_base.domain.simulation.NetworkSimulator

/**
 * Валидатор топологии сети для проверки заданий
 */
class TopologyValidator {
    
    /**
     * Результат валидации
     */
    data class ValidationResult(
        val isValid: Boolean,
        val score: Int = 0, // 0-100
        val errors: List<ValidationError> = emptyList(),
        val warnings: List<String> = emptyList(),
        val completedRequirements: List<String> = emptyList(),
        val failedRequirements: List<String> = emptyList()
    )
    
    /**
     * Ошибка валидации
     */
    data class ValidationError(
        val message: String,
        val hint: String? = null,
        val severity: ErrorSeverity = ErrorSeverity.ERROR
    )
    
    enum class ErrorSeverity {
        ERROR,      // Критическая ошибка
        WARNING,    // Предупреждение
        INFO        // Информация
    }
    
    /**
     * Проверить базовую корректность топологии
     */
    fun validateBasic(topology: NetworkTopology): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val warnings = mutableListOf<String>()
        
        // Проверка: есть ли устройства
        if (topology.devices.isEmpty()) {
            errors.add(ValidationError(
                message = "Сеть пуста",
                hint = "Добавьте хотя бы одно устройство"
            ))
            return ValidationResult(false, 0, errors)
        }
        
        // Проверка: устройства имеют IP (кроме L2 устройств)
        for (device in topology.devices) {
            if (!device.isLayer2Device() && device.getPrimaryInterface()?.ipAddress == null) {
                warnings.add("${device.name} не имеет IP-адреса")
            }
        }
        
        // Проверка: нет дублирующихся IP
        val allIps = mutableMapOf<String, String>() // IP -> device name
        for (device in topology.devices) {
            for (iface in device.interfaces) {
                iface.ipAddress?.let { ip ->
                    val existing = allIps[ip]
                    if (existing != null) {
                        errors.add(ValidationError(
                            message = "Дублирующийся IP-адрес: $ip",
                            hint = "IP-адрес $ip используется на ${existing} и ${device.name}"
                        ))
                    } else {
                        allIps[ip] = device.name
                    }
                }
            }
        }
        
        // Проверка: связность сети
        val components = findConnectedComponents(topology)
        if (components.size > 1) {
            warnings.add("Сеть разделена на ${components.size} отдельных сегментов")
        }
        
        // Проверка: валидные IP-адреса
        for (device in topology.devices) {
            for (iface in device.interfaces) {
                iface.ipAddress?.let { ip ->
                    if (!isValidIpAddress(ip)) {
                        errors.add(ValidationError(
                            message = "Некорректный IP-адрес: $ip",
                            hint = "IP должен быть в формате x.x.x.x, где x от 0 до 255"
                        ))
                    }
                }
            }
        }
        
        val hasErrors = errors.any { it.severity == ErrorSeverity.ERROR }
        val score = when {
            hasErrors -> 0
            warnings.isEmpty() -> 100
            else -> maxOf(0, 100 - warnings.size * 10)
        }
        
        return ValidationResult(!hasErrors, score, errors, warnings)
    }
    
    /**
     * Проверить задание
     */
    fun validateTask(topology: NetworkTopology, task: Task): ValidationResult {
        val errors = mutableListOf<ValidationError>()
        val warnings = mutableListOf<String>()
        val completedRequirements = mutableListOf<String>()
        val failedRequirements = mutableListOf<String>()
        
        // Сначала проверяем базовые правила
        val basicResult = validateBasic(topology)
        errors.addAll(basicResult.errors)
        warnings.addAll(basicResult.warnings)
        
        // Проверяем каждое требование задания
        for (requirement in task.requirements) {
            val passed = checkRequirement(topology, requirement)
            
            if (passed) {
                completedRequirements.add(requirement.description)
            } else {
                failedRequirements.add(requirement.description)
                errors.add(ValidationError(
                    message = requirement.errorMessage,
                    hint = getHintForRequirement(requirement),
                    severity = ErrorSeverity.ERROR
                ))
            }
        }
        
        // Вычисляем счёт
        val totalRequirements = task.requirements.size
        val passedRequirements = completedRequirements.size
        
        val score = if (totalRequirements > 0) {
            (passedRequirements * 100 / totalRequirements)
        } else {
            if (basicResult.isValid) 100 else 0
        }
        
        val isValid = failedRequirements.isEmpty() && basicResult.errors.isEmpty()
        
        return ValidationResult(
            isValid = isValid,
            score = score,
            errors = errors,
            warnings = warnings,
            completedRequirements = completedRequirements,
            failedRequirements = failedRequirements
        )
    }
    
    /**
     * Проверить отдельное требование
     */
    private fun checkRequirement(topology: NetworkTopology, requirement: TaskRequirement): Boolean {
        return when (requirement) {
            is TaskRequirement.DeviceCount -> {
                val count = topology.getDevicesByType(requirement.deviceType).size
                val minOk = count >= requirement.minCount
                val maxOk = requirement.maxCount?.let { count <= it } ?: true
                minOk && maxOk
            }
            
            is TaskRequirement.DevicesConnected -> {
                // Проверяем, что все устройства связаны
                val devicesWithType = requirement.deviceNames.flatMap { typeName ->
                    topology.devices.filter { it.type.name.contains(typeName, ignoreCase = true) }
                }
                
                if (devicesWithType.size < 2) return true
                
                val simulator = NetworkSimulator(topology)
                for (i in devicesWithType.indices) {
                    for (j in i + 1 until devicesWithType.size) {
                        val path = simulator.findPath(devicesWithType[i].id, devicesWithType[j].id)
                        if (path.isEmpty()) return false
                    }
                }
                true
            }
            
            is TaskRequirement.IpConfigured -> {
                val devices = if (requirement.deviceType != null) {
                    topology.getDevicesByType(requirement.deviceType)
                } else {
                    topology.devices.filter { !it.isLayer2Device() }
                }
                
                for (device in devices) {
                    val ip = device.getPrimaryInterface()?.ipAddress
                    if (ip == null) return false
                    
                    requirement.subnet?.let { subnet ->
                        if (!ip.startsWith(subnet)) return false
                    }
                }
                true
            }
            
            is TaskRequirement.SubnetCount -> {
                val subnets = topology.getSubnets()
                subnets.size >= requirement.count
            }
            
            is TaskRequirement.PingSuccessful -> {
                val fromDevice = topology.devices.find { 
                    it.name.equals(requirement.fromDeviceName, ignoreCase = true) 
                } ?: return false
                
                val toDevice = topology.devices.find {
                    it.name.equals(requirement.toDeviceName, ignoreCase = true)
                } ?: return false
                
                val toIp = toDevice.getPrimaryInterface()?.ipAddress ?: return false
                
                val simulator = NetworkSimulator(topology)
                val path = simulator.findPath(fromDevice.id, toDevice.id)
                path.isNotEmpty()
            }
            
            is TaskRequirement.Custom -> {
                // Кастомные проверки
                when (requirement.checkerId) {
                    "vlan_configured" -> checkVlanConfigured(topology)
                    "gateway_configured" -> checkGatewayConfigured(topology)
                    else -> true
                }
            }
        }
    }
    
    /**
     * Проверить конфигурацию VLAN
     */
    private fun checkVlanConfigured(topology: NetworkTopology): Boolean {
        val vlans = mutableSetOf<Int>()
        for (device in topology.devices) {
            for (iface in device.interfaces) {
                iface.vlanId?.let { vlans.add(it) }
            }
        }
        return vlans.size >= 2
    }
    
    /**
     * Проверить настройку шлюзов
     */
    private fun checkGatewayConfigured(topology: NetworkTopology): Boolean {
        val pcsAndServers = topology.devices.filter { 
            it.type == DeviceType.PC || it.type == DeviceType.SERVER 
        }
        
        return pcsAndServers.all { it.defaultGateway != null }
    }
    
    /**
     * Получить подсказку для требования
     */
    private fun getHintForRequirement(requirement: TaskRequirement): String {
        return when (requirement) {
            is TaskRequirement.DeviceCount -> {
                val typeName = when (requirement.deviceType) {
                    DeviceType.PC -> "компьютер(ов)"
                    DeviceType.ROUTER -> "роутер(ов)"
                    DeviceType.SWITCH -> "коммутатор(ов)"
                    DeviceType.SERVER -> "сервер(ов)"
                    DeviceType.HUB -> "хаб(ов)"
                }
                "Добавьте минимум ${requirement.minCount} $typeName"
            }
            is TaskRequirement.DevicesConnected -> {
                "Убедитесь, что все устройства соединены между собой"
            }
            is TaskRequirement.IpConfigured -> {
                requirement.subnet?.let {
                    "Настройте IP-адреса в подсети $it.x"
                } ?: "Настройте IP-адреса на всех устройствах"
            }
            is TaskRequirement.SubnetCount -> {
                "Создайте ${requirement.count} разных подсетей"
            }
            is TaskRequirement.PingSuccessful -> {
                "Проверьте связность между ${requirement.fromDeviceName} и ${requirement.toDeviceName}"
            }
            is TaskRequirement.Custom -> {
                "Проверьте настройки согласно заданию"
            }
        }
    }
    
    /**
     * Найти компоненты связности
     */
    private fun findConnectedComponents(topology: NetworkTopology): List<Set<String>> {
        val visited = mutableSetOf<String>()
        val components = mutableListOf<Set<String>>()
        
        for (device in topology.devices) {
            if (device.id in visited) continue
            
            val component = mutableSetOf<String>()
            val queue = ArrayDeque<String>()
            queue.add(device.id)
            
            while (queue.isNotEmpty()) {
                val current = queue.removeFirst()
                if (current in visited) continue
                visited.add(current)
                component.add(current)
                
                topology.getNeighbors(current).forEach { neighbor ->
                    if (neighbor.id !in visited) {
                        queue.add(neighbor.id)
                    }
                }
            }
            
            components.add(component)
        }
        
        return components
    }
    
    /**
     * Проверить валидность IP-адреса
     */
    private fun isValidIpAddress(ip: String): Boolean {
        val parts = ip.split(".")
        if (parts.size != 4) return false
        
        return parts.all { part ->
            val num = part.toIntOrNull()
            num != null && num in 0..255
        }
    }
    
    /**
     * Проверить, могут ли два устройства обмениваться данными
     */
    fun canCommunicate(
        topology: NetworkTopology,
        device1Id: String,
        device2Id: String
    ): Boolean {
        val simulator = NetworkSimulator(topology)
        val path = simulator.findPath(device1Id, device2Id)
        return path.isNotEmpty()
    }
}

