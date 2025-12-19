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
    fun validateTask(topology: NetworkTopology, task: TaskWithRequirements): ValidationResult {
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
                completedRequirements.add(getRequirementDescription(requirement))
            } else {
                failedRequirements.add(getRequirementDescription(requirement))
                errors.add(ValidationError(
                    message = getRequirementErrorMessage(requirement),
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
                val count = topology.getDeviceCountByType(requirement.deviceType)
                val minOk = count >= requirement.minCount
                val maxOk = requirement.maxCount?.let { count <= it } ?: true
                minOk && maxOk
            }
            
            is TaskRequirement.DevicesConnected -> {
                // Проверяем, что все устройства связаны
                val devicesWithType = requirement.deviceNames.flatMap { typeName ->
                    topology.devices.filter { it.type().toString().contains(typeName, ignoreCase = true) }
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

            // Новые классы требований
            is SpecificDevicesRequirement -> {
                val devices = topology.devices.filter { device ->
                    requirement.deviceNames.any { device.name.equals(it, ignoreCase = true) } &&
                    (requirement.type == null || device.type() == requirement.type)
                }
                devices.size == requirement.deviceNames.size
            }

            is ConnectivityRequirement -> {
                val fromDevice = topology.devices.find {
                    it.name.equals(requirement.fromDeviceName, ignoreCase = true)
                } ?: return false

                val toDevice = topology.devices.find {
                    it.name.equals(requirement.toDeviceName, ignoreCase = true)
                } ?: return false

                val simulator = NetworkSimulator(topology)
                val path = simulator.findPath(fromDevice.id, toDevice.id)
                path.isNotEmpty()
            }

            is IpConfigurationRequirement -> {
                val device = topology.devices.find {
                    it.name.equals(requirement.deviceName, ignoreCase = true)
                } ?: return false

                val ip = device.getPrimaryInterface()?.ipAddress ?: return false
                ip.startsWith(requirement.subnet)
            }

            is SubnetCountRequirement -> {
                val subnets = topology.getSubnets()
                subnets.size >= requirement.count
            }

            is VlanRequirement -> {
                val devices = topology.devices.filter { device ->
                    requirement.deviceNames.any { device.name.equals(it, ignoreCase = true) }
                }

                devices.all { device ->
                    device.interfaces.all { networkInterface ->
                        networkInterface.vlanId == requirement.vlanId
                    }
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
            it.type() == DeviceType.PC || it.type() == DeviceType.SERVER 
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
                    DeviceType.COMPUTER -> "компьютер(ов)"
                    DeviceType.PC -> "компьютер(ов)"
                    DeviceType.ROUTER -> "роутер(ов)"
                    DeviceType.SWITCH -> "коммутатор(ов)"
                    DeviceType.SERVER -> "сервер(ов)"
                    DeviceType.FIREWALL -> "межсетевых экран(ов)"
                    DeviceType.ACCESS_POINT -> "точек доступа"
                    DeviceType.HUB -> "хаб(ов)"
                    else -> "устройств"
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

            // Новые классы требований
            is SpecificDevicesRequirement -> {
                val typeName = requirement.type?.let { 
                    when (it) {
                        DeviceType.COMPUTER -> "компьютер(ов)"
                        DeviceType.PC -> "компьютер(ов)"
                        DeviceType.ROUTER -> "роутер(ов)"
                        DeviceType.SWITCH -> "коммутатор(ов)"
                        DeviceType.SERVER -> "сервер(ов)"
                        DeviceType.FIREWALL -> "межсетевых экран(ов)"
                        DeviceType.ACCESS_POINT -> "точек доступа"
                        DeviceType.HUB -> "хаб(ов)"
                        else -> "устройств"
                    }
                } ?: "устройств"

                "Добавьте следующие устройства: ${requirement.deviceNames.joinToString(", ")}"
            }

            is ConnectivityRequirement -> {
                "Убедитесь, что ${requirement.fromDeviceName} и ${requirement.toDeviceName} соединены"
            }

            is IpConfigurationRequirement -> {
                "Настройте IP-адрес для устройства ${requirement.deviceName} в подсети ${requirement.subnet}"
            }

            is SubnetCountRequirement -> {
                "Создайте ${requirement.count} разных подсетей"
            }

            is VlanRequirement -> {
                "Настройте VLAN ${requirement.vlanId} для устройств: ${requirement.deviceNames.joinToString(", ")}"
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
     * Получить описание требования
     */
    private fun getRequirementDescription(requirement: TaskRequirement): String {
        return when (requirement) {
            is TaskRequirement.DeviceCount -> "Добавить ${requirement.minCount} ${requirement.deviceType}"
            is TaskRequirement.DevicesConnected -> "Соединить устройства"
            is TaskRequirement.IpConfigured -> "Настроить IP-адреса"
            is TaskRequirement.SubnetCount -> "Создать ${requirement.count} подсетей"
            is TaskRequirement.PingSuccessful -> "Проверить связность"
            is TaskRequirement.Custom -> "Выполнить требование"
            is SpecificDevicesRequirement -> "Добавить устройства"
            is ConnectivityRequirement -> "Соединить устройства"
            is IpConfigurationRequirement -> "Настроить IP-адрес"
            is SubnetCountRequirement -> "Создать подсети"
            is VlanRequirement -> "Настроить VLAN"
        }
    }

    /**
     * Получить сообщение об ошибке требования
     */
    private fun getRequirementErrorMessage(requirement: TaskRequirement): String {
        return when (requirement) {
            is TaskRequirement.DeviceCount -> "Недостаточное или избыточное количество устройств"
            is TaskRequirement.DevicesConnected -> "Устройства не соединены"
            is TaskRequirement.IpConfigured -> "Некорректная IP-конфигурация"
            is TaskRequirement.SubnetCount -> "Недостаточное количество подсетей"
            is TaskRequirement.PingSuccessful -> "Устройства не могут обмениваться пакетами"
            is TaskRequirement.Custom -> "Требование не выполнено"
            is SpecificDevicesRequirement -> "Требуемые устройства не найдены"
            is ConnectivityRequirement -> "Устройства не соединены"
            is IpConfigurationRequirement -> "Некорректная IP-конфигурация"
            is SubnetCountRequirement -> "Недостаточное количество подсетей"
            is VlanRequirement -> "Некорректная VLAN-конфигурация"
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

