package com.example.network_base.ui.sandbox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.network_base.NetworkBaseApplication
import com.example.network_base.R
import com.example.network_base.data.model.*
import com.example.network_base.data.repository.TopologyRepository
import com.example.network_base.data.repository.UserRepository
import com.example.network_base.databinding.FragmentSandboxBinding
import com.example.network_base.domain.simulation.NetworkSimulator
import com.example.network_base.domain.simulation.SimulationEvent
import com.example.network_base.ui.canvas.CanvasMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class SandboxFragment : Fragment() {
    
    private var _binding: FragmentSandboxBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var topologyRepository: TopologyRepository
    private lateinit var userRepository: UserRepository
    
    private var topology: NetworkTopology = NetworkTopology(name = "Песочница")
    private var selectedDevice: NetworkDevice? = null
    
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    
    private var deviceCounter = mutableMapOf<DeviceType, Int>()
    private val consoleLog = StringBuilder()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSandboxBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val app = requireActivity().application as NetworkBaseApplication
        topologyRepository = TopologyRepository(app.database.savedTopologyDao())
        userRepository = UserRepository(app.database.userDao(), app.database.achievementDao())
        
        setupToolbar()
        setupCanvas()
        setupToolbarButtons()
        setupBottomSheet()
        setupSimulationButtons()
        setupConsole()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_save -> {
                    saveTopology()
                    true
                }
                R.id.action_load -> {
                    loadTopology()
                    true
                }
                R.id.action_clear -> {
                    clearTopology()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupCanvas() {
        binding.networkCanvas.topology = topology
        
        binding.networkCanvas.onDeviceSelected = { device ->
            selectedDevice = device
            if (device != null) {
                showDeviceProperties(device)
            } else {
                hideBottomSheet()
            }
        }
        
        binding.networkCanvas.onConnectionStartSelected = { device ->
            Snackbar.make(binding.root, "Выбрано: ${device.name}. Теперь выберите второе устройство", Snackbar.LENGTH_SHORT).show()
        }
        
        binding.networkCanvas.onConnectionCreated = { device1, device2 ->
            val sourceInterfaceId = device1.getPrimaryInterface()?.id
            val targetInterfaceId = device2.getPrimaryInterface()?.id

            if (sourceInterfaceId != null && targetInterfaceId != null) {
                topology = topology.connectDevices(
                    sourceDeviceId = device1.id,
                    sourceInterfaceId = sourceInterfaceId,
                    targetDeviceId = device2.id,
                    targetInterfaceId = targetInterfaceId
                )
                binding.networkCanvas.topology = topology
                binding.networkCanvas.invalidate()
                binding.networkCanvas.mode = CanvasMode.SELECT
                updateToolbarSelection()
                Snackbar.make(
                    binding.root,
                    "Соединение создано: ${device1.name} ↔ ${device2.name}",
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    private fun setupToolbarButtons() {
        binding.buttonAddPc.setOnClickListener { addDevice(DeviceType.COMPUTER) }
        binding.buttonAddServer.setOnClickListener { addDevice(DeviceType.SERVER) }
        binding.buttonAddSwitch.setOnClickListener { addDevice(DeviceType.SWITCH) }
        binding.buttonAddRouter.setOnClickListener { addDevice(DeviceType.ROUTER) }
        
        binding.buttonConnect.setOnClickListener {
            binding.networkCanvas.mode = if (binding.networkCanvas.mode == CanvasMode.CONNECT) {
                CanvasMode.SELECT
            } else {
                CanvasMode.CONNECT
            }
            updateToolbarSelection()
        }
        
        binding.buttonDelete.setOnClickListener {
            selectedDevice?.let { device ->
                topology = topology.removeDevice(device.id)
                binding.networkCanvas.topology = topology
                binding.networkCanvas.invalidate()
                selectedDevice = null
                hideBottomSheet()
            }
        }
    }
    
    private fun updateToolbarSelection() {
        val connectColor = if (binding.networkCanvas.mode == CanvasMode.CONNECT) {
            R.color.primary
        } else {
            R.color.text_secondary
        }
        binding.buttonConnect.setColorFilter(ContextCompat.getColor(requireContext(), connectColor))
    }
    
    private fun addDevice(type: DeviceType) {
        val count = deviceCounter.getOrDefault(type, 0) + 1
        deviceCounter[type] = count
        
        val name = when (type) {
            DeviceType.COMPUTER, DeviceType.PC -> "PC-$count"
            DeviceType.ROUTER -> "Router-$count"
            DeviceType.SWITCH -> "Switch-$count"
            DeviceType.SERVER -> "Server-$count"
            DeviceType.FIREWALL -> "Firewall-$count"
            DeviceType.ACCESS_POINT -> "AP-$count"
            DeviceType.HUB -> "Hub-$count"
        }
        
        val centerX = binding.networkCanvas.width / 2f
        val centerY = binding.networkCanvas.height / 2f
        val offset = (Math.random() * 100 - 50).toFloat()
        
        val device = when (type) {
            DeviceType.COMPUTER, DeviceType.PC -> Computer(
                id = "device_${System.currentTimeMillis()}",
                name = name,
                x = centerX + offset,
                y = centerY + offset
            )
            DeviceType.ROUTER -> Router(
                id = "device_${System.currentTimeMillis()}",
                name = name,
                x = centerX + offset,
                y = centerY + offset
            )
            DeviceType.SWITCH, DeviceType.HUB -> Switch(
                id = "device_${System.currentTimeMillis()}",
                name = name,
                x = centerX + offset,
                y = centerY + offset
            )
            DeviceType.SERVER -> Computer(
                id = "device_${System.currentTimeMillis()}",
                name = name,
                x = centerX + offset,
                y = centerY + offset
            )
            DeviceType.FIREWALL, DeviceType.ACCESS_POINT -> Computer(
                id = "device_${System.currentTimeMillis()}",
                name = name,
                x = centerX + offset,
                y = centerY + offset
            )
        }
        
        topology = topology.addDevice(device)
        binding.networkCanvas.topology = topology
        binding.networkCanvas.invalidate()
        
        // Check explorer achievement
        if (topology.devices.size >= 10) {
            viewLifecycleOwner.lifecycleScope.launch {
                userRepository.unlockAchievement("explorer")
            }
        }
    }
    
    private fun setupBottomSheet() {
        val bottomSheet = binding.root.findViewById<View>(R.id.bottom_sheet)
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
        
        bottomSheet.findViewById<View>(R.id.button_apply)?.setOnClickListener {
            applyDeviceChanges()
        }
    }
    
    private fun showDeviceProperties(device: NetworkDevice) {
        val bottomSheet = binding.root.findViewById<View>(R.id.bottom_sheet)
        
        bottomSheet.findViewById<android.widget.TextView>(R.id.text_device_name)?.text = device.name
        bottomSheet.findViewById<android.widget.TextView>(R.id.text_device_type)?.text = when (device.type()) {
            DeviceType.COMPUTER, DeviceType.PC -> "Компьютер"
            DeviceType.ROUTER -> "Роутер"
            DeviceType.SWITCH -> "Коммутатор"
            DeviceType.SERVER -> "Сервер"
            DeviceType.FIREWALL -> "Межсетевой экран"
            DeviceType.ACCESS_POINT -> "Точка доступа"
            DeviceType.HUB -> "Хаб"
        }
        
        bottomSheet.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_device_name)?.setText(device.name)
        
        val ip = device.getPrimaryInterface()?.ipAddress ?: ""
        bottomSheet.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_ip_address)?.setText(ip)
        
        val gateway = device.defaultGateway ?: ""
        bottomSheet.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_gateway)?.setText(gateway)
        
        val mac = device.getPrimaryInterface()?.macAddress ?: ""
        bottomSheet.findViewById<android.widget.TextView>(R.id.text_mac_address)?.text = mac
        
        val showIp = !device.isLayer2Device()
        bottomSheet.findViewById<View>(R.id.layout_ip)?.visibility = if (showIp) View.VISIBLE else View.GONE
        bottomSheet.findViewById<View>(R.id.layout_gateway)?.visibility = if (showIp && !device.isRouter()) View.VISIBLE else View.GONE
        
        bottomSheet.visibility = View.VISIBLE
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }
    
    private fun hideBottomSheet() {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_HIDDEN
    }
    
    private fun applyDeviceChanges() {
        val bottomSheet = binding.root.findViewById<View>(R.id.bottom_sheet)
        
        selectedDevice?.let { device ->
            val newName = bottomSheet.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_device_name)?.text?.toString()
            val newIp = bottomSheet.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_ip_address)?.text?.toString()
            val newGateway = bottomSheet.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_gateway)?.text?.toString()
            
            val updatedInterfaces = if (!device.isLayer2Device()) {
                device.interfaces.mapIndexed { index, iface ->
                    if (index == 0) {
                        iface.copy(
                            ipAddress = newIp?.ifEmpty { null },
                            subnetMask = "255.255.255.0"
                        )
                    } else {
                        iface
                    }
                }
            } else {
                device.interfaces
            }

            val updatedDevice: NetworkDevice = when (device) {
                is Computer -> device.copy(
                    name = newName?.takeIf { it.isNotBlank() } ?: device.name,
                    interfaces = updatedInterfaces
                )
                is Switch -> device.copy(
                    name = newName?.takeIf { it.isNotBlank() } ?: device.name,
                    interfaces = updatedInterfaces
                )
                is Router -> device.copy(
                    name = newName?.takeIf { it.isNotBlank() } ?: device.name,
                    interfaces = updatedInterfaces
                )
            }

            if (!updatedDevice.isRouter()) {
                updatedDevice.defaultGateway = newGateway?.ifEmpty { null }
            }

            topology = topology.copy(
                devices = topology.devices.map { existing ->
                    if (existing.id == device.id) updatedDevice else existing
                }
            )
            binding.networkCanvas.topology = topology
            binding.networkCanvas.invalidate()
            selectedDevice = updatedDevice
        }
        
        hideBottomSheet()
    }
    
    private fun setupSimulationButtons() {
        binding.buttonPing.setOnClickListener {
            showPingDialog()
        }
        
        binding.buttonSimulate.setOnClickListener {
            runQuickSimulation()
        }
    }
    
    private fun setupConsole() {
        binding.buttonCloseConsole.setOnClickListener {
            binding.cardConsole.visibility = View.GONE
        }
    }
    
    private fun showConsole() {
        binding.cardConsole.visibility = View.VISIBLE
    }
    
    private fun appendToConsole(text: String) {
        consoleLog.appendLine(text)
        binding.textConsole.text = consoleLog.toString()
    }
    
    private fun clearConsole() {
        consoleLog.clear()
        binding.textConsole.text = ""
    }
    
    private fun showPingDialog() {
        val devicesWithIp = topology.devices.filter { 
            it.getPrimaryInterface()?.ipAddress != null 
        }
        
        if (devicesWithIp.size < 2) {
            Snackbar.make(binding.root, "Нужно минимум 2 устройства с IP для ping", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        val deviceNames = devicesWithIp.map { "${it.name} (${it.getPrimaryInterface()?.ipAddress})" }.toTypedArray()
        
        var fromIndex = 0
        var toIndex = 1
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Ping: выберите источник")
            .setSingleChoiceItems(deviceNames, 0) { _, which ->
                fromIndex = which
            }
            .setPositiveButton("Далее") { _, _ ->
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Ping: выберите цель")
                    .setSingleChoiceItems(deviceNames, 1) { _, which ->
                        toIndex = which
                    }
                    .setPositiveButton("Ping") { _, _ ->
                        if (fromIndex != toIndex) {
                            runPing(devicesWithIp[fromIndex], devicesWithIp[toIndex])
                        }
                    }
                    .setNegativeButton("Отмена") { _, _ -> }
                    .show()
            }
            .setNegativeButton("Отмена") { _, _ -> }
            .show()
    }
    
    private fun runPing(from: NetworkDevice, to: NetworkDevice) {
        val targetIp = to.getPrimaryInterface()?.ipAddress ?: return
        
        clearConsole()
        showConsole()
        appendToConsole("$ ping $targetIp")
        appendToConsole("PING $targetIp from ${from.getPrimaryInterface()?.ipAddress}")
        appendToConsole("")
        
        val simulator = NetworkSimulator(topology)
        
        viewLifecycleOwner.lifecycleScope.launch {
            simulator.simulatePing(from.id, targetIp, 3).collect { event ->
                when (event) {
                    is SimulationEvent.PacketInTransit -> {
                        binding.networkCanvas.animatePacket(event.state, 300)
                    }
                    is SimulationEvent.Log -> {
                        appendToConsole(event.message)
                    }
                    is SimulationEvent.Error -> {
                        appendToConsole("Error: ${event.message}")
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun runQuickSimulation() {
        val devicesWithIp = topology.devices.filter { 
            it.getPrimaryInterface()?.ipAddress != null 
        }
        
        if (devicesWithIp.size < 2) {
            Snackbar.make(binding.root, "Добавьте минимум 2 устройства с IP", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        runPing(devicesWithIp[0], devicesWithIp[1])
    }
    
    private fun saveTopology() {
        viewLifecycleOwner.lifecycleScope.launch {
            // Обновляем currentTopology в репозитории перед сохранением
            topologyRepository.updateTopology(topology)
            val savedTopology = topologyRepository.saveCurrentTopology(topology.name)
            Snackbar.make(binding.root, "Топология сохранена: ${savedTopology.name}", Snackbar.LENGTH_SHORT).show()
        }
    }
    
    private fun loadTopology() {
        viewLifecycleOwner.lifecycleScope.launch {
            val topologies = topologyRepository.getAllSavedTopologies()
            
            if (topologies.isEmpty()) {
                Snackbar.make(binding.root, "Нет сохранённых топологий", Snackbar.LENGTH_SHORT).show()
                return@launch
            }
            
            val names = topologies.map { it.name }.toTypedArray()
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Загрузить топологию")
                .setItems(names) { _, which ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        val selected = topologies[which]
                        val topologyObj = com.google.gson.Gson().fromJson(selected.topologyJson, NetworkTopology::class.java)
                        topology = topologyObj
                        binding.networkCanvas.topology = topology
                        deviceCounter.clear()
                        Snackbar.make(binding.root, "Загружено: ${selected.name}", Snackbar.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton("Отмена") { _, _ -> }
                .show()
        }
    }
    
    private fun clearTopology() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Очистить?")
            .setMessage("Все устройства будут удалены.")
            .setPositiveButton("Очистить") { _, _ ->
                topology = NetworkTopology(name = "Песочница")
                binding.networkCanvas.topology = topology
                deviceCounter.clear()
                selectedDevice = null
                hideBottomSheet()
            }
            .setNegativeButton("Отмена") { _, _ -> }
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

