
package com.example.network_base.data.model

/**
 * Требования к заданию (старая версия для совместимости)
 */
sealed class TaskRequirement {
    data class DeviceCount(
        val deviceType: DeviceType,
        val minCount: Int,
        val maxCount: Int? = null,
        val description: String = "",
        val errorMessage: String = "Недостаточное или избыточное количество устройств"
    ) : TaskRequirement()

    data class DevicesConnected(
        val deviceNames: List<String>,
        val description: String = "",
        val errorMessage: String = "Устройства не соединены"
    ) : TaskRequirement()

    data class IpConfigured(
        val deviceType: DeviceType? = null,
        val subnet: String? = null,
        val description: String = "",
        val errorMessage: String = "Некорректная IP-конфигурация"
    ) : TaskRequirement()

    data class SubnetCount(
        val count: Int,
        val description: String = "",
        val errorMessage: String = "Недостаточное количество подсетей"
    ) : TaskRequirement()

    data class PingSuccessful(
        val fromDeviceName: String,
        val toDeviceName: String,
        val description: String = "",
        val errorMessage: String = "Устройства не могут обмениваться пакетами"
    ) : TaskRequirement()

    data class Custom(
        val checkerId: String,
        val description: String = "",
        val errorMessage: String = "Требование не выполнено"
    ) : TaskRequirement()
}

// Новые классы требований
data class SpecificDevicesRequirement(
    val deviceNames: List<String>,
    val type: DeviceType? = null,
    val description: String = "",
    val errorMessage: String = "Требуемые устройства не найдены"
) : TaskRequirement()

data class ConnectivityRequirement(
    val fromDeviceName: String,
    val toDeviceName: String,
    val description: String = "",
    val errorMessage: String = "Устройства не соединены"
) : TaskRequirement()

data class IpConfigurationRequirement(
    val deviceName: String,
    val subnet: String,
    val description: String = "",
    val errorMessage: String = "Некорректная IP-конфигурация"
) : TaskRequirement()

data class SubnetCountRequirement(
    val count: Int,
    val description: String = "",
    val errorMessage: String = "Недостаточное количество подсетей"
) : TaskRequirement()

data class VlanRequirement(
    val vlanId: Int,
    val deviceNames: List<String>,
    val description: String = "",
    val errorMessage: String = "Некорректная VLAN-конфигурация"
) : TaskRequirement()
