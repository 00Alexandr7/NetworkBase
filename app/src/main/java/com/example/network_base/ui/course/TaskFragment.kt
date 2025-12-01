package com.example.network_base.ui.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.network_base.NetworkBaseApplication
import com.example.network_base.R
import com.example.network_base.data.model.*
import com.example.network_base.data.repository.CourseRepository
import com.example.network_base.data.repository.ProgressRepository
import com.example.network_base.data.repository.TopologyRepository
import com.example.network_base.data.repository.UserRepository
import com.example.network_base.databinding.FragmentTaskBinding
import com.example.network_base.domain.simulation.NetworkSimulator
import com.example.network_base.domain.simulation.SimulationEvent
import com.example.network_base.domain.validation.TopologyValidator
import com.example.network_base.ui.canvas.CanvasMode
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class TaskFragment : Fragment() {
    
    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var courseRepository: CourseRepository
    private lateinit var progressRepository: ProgressRepository
    private lateinit var topologyRepository: TopologyRepository
    private lateinit var userRepository: UserRepository
    
    private var taskId: String? = null
    private var task: Task? = null
    private var topology: NetworkTopology = NetworkTopology()
    
    private lateinit var bottomSheetBehavior: BottomSheetBehavior<View>
    private var selectedDevice: NetworkDevice? = null
    
    private var deviceCounter = mutableMapOf<DeviceType, Int>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        taskId = arguments?.getString("taskId")
        
        val app = requireActivity().application as NetworkBaseApplication
        courseRepository = CourseRepository(requireContext())
        progressRepository = ProgressRepository(app.database.progressDao())
        topologyRepository = TopologyRepository(app.database.savedTopologyDao())
        userRepository = UserRepository(app.database.userDao(), app.database.achievementDao())
        
        setupToolbar()
        setupCanvas()
        setupToolbarButtons()
        setupBottomSheet()
        setupCheckButton()
        loadTask()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_reset -> {
                    resetTopology()
                    true
                }
                R.id.action_hint -> {
                    showHints()
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
            // Показываем подсказку, что нужно выбрать второе устройство
            Snackbar.make(binding.root, "Выбрано: ${device.name}. Теперь выберите второе устройство", Snackbar.LENGTH_SHORT).show()
        }
        
        binding.networkCanvas.onConnectionCreated = { device1, device2 ->
            topology.connectDevices(device1.id, device2.id)
            binding.networkCanvas.invalidate()
            binding.networkCanvas.mode = CanvasMode.SELECT
            updateToolbarSelection()
            Snackbar.make(binding.root, "Соединение создано: ${device1.name} ↔ ${device2.name}", Snackbar.LENGTH_SHORT).show()
        }
        
        binding.networkCanvas.onCanvasTapped = { x, y ->
            if (binding.networkCanvas.mode == CanvasMode.ADD_DEVICE) {
                // Device type determined by last clicked button
            }
        }
    }
    
    private fun setupToolbarButtons() {
        binding.buttonAddPc.setOnClickListener { addDevice(DeviceType.PC) }
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
                topology.removeDevice(device.id)
                binding.networkCanvas.invalidate()
                selectedDevice = null
                hideBottomSheet()
            }
        }
        
        binding.buttonSimulate.setOnClickListener {
            runSimulation()
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
            DeviceType.PC -> "PC-$count"
            DeviceType.ROUTER -> "Router-$count"
            DeviceType.SWITCH -> "Switch-$count"
            DeviceType.SERVER -> "Server-$count"
            DeviceType.HUB -> "Hub-$count"
        }
        
        val centerX = binding.networkCanvas.width / 2f
        val centerY = binding.networkCanvas.height / 2f
        val offset = (Math.random() * 100 - 50).toFloat()
        
        val device = NetworkDevice(
            type = type,
            name = name,
            x = centerX + offset,
            y = centerY + offset
        )
        
        topology.addDevice(device)
        binding.networkCanvas.invalidate()
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
        
        bottomSheet.findViewById<TextView>(R.id.text_device_name)?.text = device.name
        bottomSheet.findViewById<TextView>(R.id.text_device_type)?.text = when (device.type) {
            DeviceType.PC -> "Компьютер"
            DeviceType.ROUTER -> "Роутер"
            DeviceType.SWITCH -> "Коммутатор"
            DeviceType.SERVER -> "Сервер"
            DeviceType.HUB -> "Хаб"
        }
        
        bottomSheet.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_device_name)?.setText(device.name)
        
        val ip = device.getPrimaryInterface()?.ipAddress ?: ""
        bottomSheet.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_ip_address)?.setText(ip)
        
        val gateway = device.defaultGateway ?: ""
        bottomSheet.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.edit_gateway)?.setText(gateway)
        
        val mac = device.getPrimaryInterface()?.macAddress ?: ""
        bottomSheet.findViewById<TextView>(R.id.text_mac_address)?.text = mac
        
        // Show/hide IP fields for L2 devices
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
            
            newName?.let { device.name = it }
            
            if (!device.isLayer2Device()) {
                device.interfaces.firstOrNull()?.ipAddress = newIp?.ifEmpty { null }
                device.interfaces.firstOrNull()?.subnetMask = "255.255.255.0"
            }
            
            if (!device.isRouter()) {
                device.defaultGateway = newGateway?.ifEmpty { null }
            }
            
            binding.networkCanvas.invalidate()
        }
        
        hideBottomSheet()
    }
    
    private fun setupCheckButton() {
        binding.buttonCheck.setOnClickListener {
            checkSolution()
        }
        
        binding.buttonHint.setOnClickListener {
            showHints()
        }
    }
    
    private fun loadTask() {
        taskId?.let { id ->
            task = courseRepository.getTaskById(id)
            task?.let { t ->
                binding.toolbar.title = t.title
                
                // Load initial topology if exists
                t.initialTopology?.let { initial ->
                    topology = initial.copy()
                    binding.networkCanvas.topology = topology
                }
                
                // Load saved topology if exists
                viewLifecycleOwner.lifecycleScope.launch {
                    topologyRepository.getTopologyByTaskId(id)?.let { saved ->
                        topology = saved
                        binding.networkCanvas.topology = topology
                    }
                }
                
                renderObjectives(t)
            }
        }
    }
    
    private fun renderObjectives(task: Task) {
        binding.containerObjectives.removeAllViews()
        
        for (objective in task.objectives) {
            val textView = TextView(requireContext()).apply {
                text = "• $objective"
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary))
                setPadding(0, 4, 0, 4)
            }
            binding.containerObjectives.addView(textView)
        }
    }
    
    private fun checkSolution() {
        task?.let { t ->
            val validator = TopologyValidator()
            val result = validator.validateTask(topology, t)
            
            if (result.isValid) {
                // Success!
                viewLifecycleOwner.lifecycleScope.launch {
                    progressRepository.completeTask(t.moduleId, result.score)
                    userRepository.addXp(
                        userRepository.getOrCreateUser().id,
                        t.xpReward
                    )
                    
                    // Check achievements
                    if (progressRepository.getCompletedTasksCount() == 1) {
                        userRepository.unlockAchievement("first_step")
                    }
                    
                    topologyRepository.saveTopologyForTask(t.id, topology)
                }
                
                showSuccessDialog(result.score, t.xpReward)
            } else {
                // Show errors
                showErrorsDialog(result)
            }
        }
    }
    
    private fun showSuccessDialog(score: Int, xp: Int) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Отлично!")
            .setMessage("Задание выполнено!\n\nРезультат: $score%\nПолучено: +$xp XP")
            .setPositiveButton("Продолжить") { _, _ ->
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showErrorsDialog(result: TopologyValidator.ValidationResult) {
        val message = buildString {
            appendLine("Результат: ${result.score}%\n")
            
            if (result.completedRequirements.isNotEmpty()) {
                appendLine("✓ Выполнено:")
                result.completedRequirements.forEach {
                    appendLine("  • $it")
                }
                appendLine()
            }
            
            if (result.failedRequirements.isNotEmpty()) {
                appendLine("✗ Не выполнено:")
                result.failedRequirements.forEach {
                    appendLine("  • $it")
                }
            }
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Есть ошибки")
            .setMessage(message)
            .setPositiveButton("Исправить") { _, _ -> }
            .setNeutralButton("Подсказка") { _, _ -> showHints() }
            .show()
    }
    
    private fun showHints() {
        task?.hints?.let { hints ->
            if (hints.isEmpty()) {
                Snackbar.make(binding.root, "Подсказок нет", Snackbar.LENGTH_SHORT).show()
                return
            }
            
            val items = hints.map { "${it.title}: ${it.content}" }.toTypedArray()
            
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Подсказки")
                .setItems(items) { _, _ -> }
                .setPositiveButton("Закрыть") { _, _ -> }
                .show()
        }
    }
    
    private fun resetTopology() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Сбросить?")
            .setMessage("Все изменения будут потеряны.")
            .setPositiveButton("Сбросить") { _, _ ->
                topology = task?.initialTopology?.copy() ?: NetworkTopology()
                deviceCounter.clear()
                binding.networkCanvas.topology = topology
                selectedDevice = null
                hideBottomSheet()
            }
            .setNegativeButton("Отмена") { _, _ -> }
            .show()
    }
    
    private fun runSimulation() {
        val devices = topology.devices.filter { it.getPrimaryInterface()?.ipAddress != null }
        
        if (devices.size < 2) {
            Snackbar.make(binding.root, "Нужно минимум 2 устройства с IP", Snackbar.LENGTH_SHORT).show()
            return
        }
        
        val from = devices[0]
        val to = devices[1]
        val targetIp = to.getPrimaryInterface()?.ipAddress ?: return
        
        val simulator = NetworkSimulator(topology)
        
        viewLifecycleOwner.lifecycleScope.launch {
            simulator.simulatePing(from.id, targetIp, 1).collect { event ->
                when (event) {
                    is SimulationEvent.PacketInTransit -> {
                        binding.networkCanvas.animatePacket(event.state)
                    }
                    is SimulationEvent.Log -> {
                        // Could show in a console view
                    }
                    is SimulationEvent.SimulationEnded -> {
                        val msg = if (event.success) "Ping успешен!" else "Ping не удался"
                        Snackbar.make(binding.root, msg, Snackbar.LENGTH_SHORT).show()
                    }
                    else -> {}
                }
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

