package com.example.network_base.ui.canvas

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.example.network_base.data.model.*
import kotlin.math.sqrt

/**
 * Кастомный View для визуального конструктора сетей
 * Поддерживает drag-and-drop, масштабирование и анимацию пакетов
 */
class NetworkCanvasView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // Топология сети
    var topology: NetworkTopology? = null
        set(value) {
            field = value
            invalidate()
        }
    
    // Режим работы
    var mode: CanvasMode = CanvasMode.SELECT
        set(value) {
            field = value
            connectionStartDevice = null
            invalidate()
        }
    
    // Текущее выбранное устройство
    var selectedDevice: NetworkDevice? = null
        private set
    
    // Устройство, которое перетаскивают
    private var draggingDevice: NetworkDevice? = null
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    
    // Для режима создания соединений
    private var connectionStartDevice: NetworkDevice? = null
    private var connectionEndPoint: PointF? = null
    
    // Анимация пакетов
    private val activePackets = mutableListOf<PacketAnimationState>()
    
    // Масштаб и смещение
    private var scale = 1f
    private var translateX = 0f
    private var translateY = 0f
    
    // Размеры
    private val deviceRadius = 45f
    private val packetRadius = 12f
    private val touchRadius = 60f // Область касания больше визуальной
    
    // Краски для рисования
    private val devicePaints = mapOf(
        DeviceType.PC to Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2196F3") // Синий
            style = Paint.Style.FILL
        },
        DeviceType.ROUTER to Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#9C27B0") // Фиолетовый
            style = Paint.Style.FILL
        },
        DeviceType.SWITCH to Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FF9800") // Оранжевый
            style = Paint.Style.FILL
        },
        DeviceType.SERVER to Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#4CAF50") // Зеленый
            style = Paint.Style.FILL
        },
        DeviceType.HUB to Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#795548") // Коричневый
            style = Paint.Style.FILL
        }
    )
    
    private val selectedStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700") // Золотой
        style = Paint.Style.STROKE
        strokeWidth = 4f
    }
    
    private val connectionPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#455A64")
        style = Paint.Style.STROKE
        strokeWidth = 4f
        strokeCap = Paint.Cap.ROUND
    }
    
    private val connectionPreviewPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#81C784")
        style = Paint.Style.STROKE
        strokeWidth = 3f
        strokeCap = Paint.Cap.ROUND
        pathEffect = DashPathEffect(floatArrayOf(15f, 10f), 0f)
    }
    
    private val packetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    
    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#37474F")
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }
    
    private val ipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#607D8B")
        textSize = 20f
        textAlign = Paint.Align.CENTER
    }
    
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E0E0E0")
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    
    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#FAFAFA")
    }
    
    // Callbacks
    var onDeviceSelected: ((NetworkDevice?) -> Unit)? = null
    var onDeviceMoved: ((NetworkDevice) -> Unit)? = null
    var onConnectionCreated: ((NetworkDevice, NetworkDevice) -> Unit)? = null
    var onConnectionStartSelected: ((NetworkDevice) -> Unit)? = null
    var onCanvasTapped: ((Float, Float) -> Unit)? = null
    
    // Детекторы жестов
    private val gestureDetector = GestureDetector(context, GestureListener())
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    
    // Матрица для обратного преобразования координат
    private val inverseMatrix = Matrix()
    private val transformedPoint = FloatArray(2)
    
    init {
        // Делаем View кликабельным
        isClickable = true
        isFocusable = true
    }
    
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        // Фон
        canvas.drawPaint(backgroundPaint)
        
        canvas.save()
        canvas.translate(translateX, translateY)
        canvas.scale(scale, scale)
        
        // Сетка
        drawGrid(canvas)
        
        topology?.let { topo ->
            // Соединения
            drawConnections(canvas, topo)
            
            // Предпросмотр соединения
            drawConnectionPreview(canvas)
            
            // Устройства
            for (device in topo.devices) {
                drawDevice(canvas, device)
            }
            
            // Анимированные пакеты
            drawPackets(canvas, topo)
        }
        
        canvas.restore()
    }
    
    private fun drawGrid(canvas: Canvas) {
        val gridSize = 50f
        val startX = -translateX / scale - 1000
        val startY = -translateY / scale - 1000
        val endX = (width - translateX) / scale + 1000
        val endY = (height - translateY) / scale + 1000
        
        var x = (startX / gridSize).toInt() * gridSize
        while (x < endX) {
            canvas.drawLine(x, startY, x, endY, gridPaint)
            x += gridSize
        }
        
        var y = (startY / gridSize).toInt() * gridSize
        while (y < endY) {
            canvas.drawLine(startX, y, endX, y, gridPaint)
            y += gridSize
        }
    }
    
    private fun drawConnections(canvas: Canvas, topology: NetworkTopology) {
        for (connection in topology.connections) {
            val (device1Id, device2Id) = connection.getDeviceIds()
            val device1 = topology.findDevice(device1Id) ?: continue
            val device2 = topology.findDevice(device2Id) ?: continue
            
            connectionPaint.color = if (connection.isActive) {
                Color.parseColor("#455A64")
            } else {
                Color.parseColor("#BDBDBD")
            }
            
            canvas.drawLine(
                device1.x, device1.y,
                device2.x, device2.y,
                connectionPaint
            )
        }
    }
    
    private fun drawConnectionPreview(canvas: Canvas) {
        connectionStartDevice?.let { startDevice ->
            connectionEndPoint?.let { endPoint ->
                canvas.drawLine(
                    startDevice.x, startDevice.y,
                    endPoint.x, endPoint.y,
                    connectionPreviewPaint
                )
            }
        }
    }
    
    private fun drawDevice(canvas: Canvas, device: NetworkDevice) {
        val paint = devicePaints[device.type] ?: devicePaints[DeviceType.PC]!!
        
        // Тень
        val shadowPaint = Paint(paint).apply {
            color = Color.parseColor("#40000000")
        }
        canvas.drawCircle(device.x + 3, device.y + 3, deviceRadius, shadowPaint)
        
        // Основной круг
        canvas.drawCircle(device.x, device.y, deviceRadius, paint)
        
        // Выделение, если выбрано или это начало соединения
        if (device == selectedDevice || device == connectionStartDevice) {
            canvas.drawCircle(device.x, device.y, deviceRadius + 5, selectedStrokePaint)
        }
        
        // Иконка типа (буква)
        val label = when (device.type) {
            DeviceType.PC -> "PC"
            DeviceType.ROUTER -> "R"
            DeviceType.SWITCH -> "SW"
            DeviceType.SERVER -> "S"
            DeviceType.HUB -> "H"
        }
        canvas.drawText(label, device.x, device.y + 10, textPaint)
        
        // Имя устройства под иконкой
        canvas.drawText(device.name, device.x, device.y + deviceRadius + 25, labelPaint)
        
        // IP адрес (если есть)
        device.getPrimaryInterface()?.ipAddress?.let { ip ->
            canvas.drawText(ip, device.x, device.y + deviceRadius + 48, ipPaint)
        }
    }
    
    private fun drawPackets(canvas: Canvas, topology: NetworkTopology) {
        for (packetState in activePackets.toList()) {
            val fromDevice = topology.findDevice(packetState.fromDeviceId) ?: continue
            val toDevice = topology.findDevice(packetState.toDeviceId) ?: continue
            
            // Интерполяция позиции
            val x = fromDevice.x + (toDevice.x - fromDevice.x) * packetState.progress
            val y = fromDevice.y + (toDevice.y - fromDevice.y) * packetState.progress
            
            // Цвет в зависимости от типа пакета
            packetPaint.color = when (packetState.packet.type) {
                PacketType.ICMP_ECHO_REQUEST -> Color.parseColor("#4CAF50") // Зеленый
                PacketType.ICMP_ECHO_REPLY -> Color.parseColor("#8BC34A") // Светло-зеленый
                PacketType.ARP_REQUEST -> Color.parseColor("#FFC107") // Желтый
                PacketType.ARP_REPLY -> Color.parseColor("#FFEB3B") // Светло-желтый
                PacketType.DATA -> Color.parseColor("#03A9F4") // Голубой
                PacketType.BROADCAST -> Color.parseColor("#FF5722") // Оранжево-красный
            }
            
            // Свечение
            val glowPaint = Paint(packetPaint).apply {
                alpha = 80
            }
            canvas.drawCircle(x, y, packetRadius * 1.5f, glowPaint)
            
            // Пакет
            canvas.drawCircle(x, y, packetRadius, packetPaint)
        }
    }
    
    /**
     * Анимировать пакет между устройствами
     */
    fun animatePacket(state: PacketAnimationState, duration: Long = 500) {
        activePackets.add(state)
        
        ValueAnimator.ofFloat(0f, 1f).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener { animator ->
                state.progress = animator.animatedValue as Float
                invalidate()
            }
            start()
        }
        
        postDelayed({
            activePackets.remove(state)
            invalidate()
        }, duration + 100)
    }
    
    /**
     * Очистить все анимации пакетов
     */
    fun clearPacketAnimations() {
        activePackets.clear()
        invalidate()
    }
    
    /**
     * Центрировать view на топологии
     */
    fun centerOnTopology() {
        topology?.let { topo ->
            if (topo.devices.isEmpty()) return
            
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE
            
            for (device in topo.devices) {
                minX = minOf(minX, device.x)
                minY = minOf(minY, device.y)
                maxX = maxOf(maxX, device.x)
                maxY = maxOf(maxY, device.y)
            }
            
            val centerX = (minX + maxX) / 2
            val centerY = (minY + maxY) / 2
            
            translateX = width / 2f - centerX * scale
            translateY = height / 2f - centerY * scale
            
            invalidate()
        }
    }
    
    /**
     * Сбросить масштаб и позицию
     */
    fun resetView() {
        scale = 1f
        translateX = 0f
        translateY = 0f
        invalidate()
    }
    
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)
        
        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (mode == CanvasMode.CONNECT && connectionStartDevice != null) {
                    val canvasPoint = screenToCanvas(event.x, event.y)
                    connectionEndPoint = PointF(canvasPoint.x, canvasPoint.y)
                    invalidate()
                } else if (draggingDevice != null) {
                    val canvasPoint = screenToCanvas(event.x, event.y)
                    draggingDevice?.let { device ->
                        device.x = canvasPoint.x - dragOffsetX
                        device.y = canvasPoint.y - dragOffsetY
                        invalidate()
                    }
                }
            }
            
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                // Очищаем preview линии при отпускании, если соединение не было создано
                if (mode == CanvasMode.CONNECT && connectionStartDevice != null) {
                    val canvasPoint = screenToCanvas(event.x, event.y)
                    val endDevice = findDeviceAt(canvasPoint.x, canvasPoint.y)
                    
                    // Если соединение не было создано в onSingleTapUp, очищаем preview
                    if (endDevice == null || endDevice == connectionStartDevice) {
                        connectionEndPoint = null
                        invalidate()
                    }
                }
                
                draggingDevice?.let {
                    onDeviceMoved?.invoke(it)
                }
                draggingDevice = null
            }
        }
        
        return true
    }
    
    private fun screenToCanvas(screenX: Float, screenY: Float): PointF {
        val x = (screenX - translateX) / scale
        val y = (screenY - translateY) / scale
        return PointF(x, y)
    }
    
    private fun findDeviceAt(canvasX: Float, canvasY: Float): NetworkDevice? {
        topology?.devices?.forEach { device ->
            val dx = canvasX - device.x
            val dy = canvasY - device.y
            if (sqrt(dx * dx + dy * dy) <= touchRadius) {
                return device
            }
        }
        return null
    }
    
    inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        
        override fun onDown(e: MotionEvent): Boolean = true
        
        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val canvasPoint = screenToCanvas(e.x, e.y)
            val device = findDeviceAt(canvasPoint.x, canvasPoint.y)
            
            when (mode) {
                CanvasMode.SELECT -> {
                    selectedDevice = device
                    onDeviceSelected?.invoke(device)
                    
                    if (device == null) {
                        onCanvasTapped?.invoke(canvasPoint.x, canvasPoint.y)
                    }
                }
                CanvasMode.CONNECT -> {
                    if (connectionStartDevice == null) {
                        // Первый клик - выбираем начальное устройство
                        if (device != null) {
                            connectionStartDevice = device
                            selectedDevice = device
                            onDeviceSelected?.invoke(device)
                            onConnectionStartSelected?.invoke(device)
                        }
                    } else {
                        // Второй клик - создаём соединение
                        if (device != null && device != connectionStartDevice) {
                            onConnectionCreated?.invoke(connectionStartDevice!!, device)
                            connectionStartDevice = null
                            connectionEndPoint = null
                            selectedDevice = null
                            onDeviceSelected?.invoke(null)
                            // Возвращаемся в режим выбора
                            mode = CanvasMode.SELECT
                        } else {
                            // Клик мимо устройства или на то же - сбрасываем выбор
                            connectionStartDevice = null
                            connectionEndPoint = null
                            selectedDevice = null
                            onDeviceSelected?.invoke(null)
                        }
                    }
                }
                CanvasMode.ADD_DEVICE -> {
                    if (device == null) {
                        onCanvasTapped?.invoke(canvasPoint.x, canvasPoint.y)
                    }
                }
            }
            
            invalidate()
            return true
        }
        
        override fun onLongPress(e: MotionEvent) {
            if (mode != CanvasMode.SELECT) return
            
            val canvasPoint = screenToCanvas(e.x, e.y)
            val device = findDeviceAt(canvasPoint.x, canvasPoint.y)
            
            device?.let {
                draggingDevice = it
                dragOffsetX = canvasPoint.x - it.x
                dragOffsetY = canvasPoint.y - it.y
                selectedDevice = it
                onDeviceSelected?.invoke(it)
                invalidate()
            }
        }
        
        override fun onScroll(
            e1: MotionEvent?,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            // Если перетаскиваем устройство, не двигаем холст
            if (draggingDevice != null) return false
            if (mode == CanvasMode.CONNECT && connectionStartDevice != null) return false
            
            // Перемещение холста
            translateX -= distanceX
            translateY -= distanceY
            invalidate()
            return true
        }
        
        override fun onDoubleTap(e: MotionEvent): Boolean {
            val canvasPoint = screenToCanvas(e.x, e.y)
            val device = findDeviceAt(canvasPoint.x, canvasPoint.y)
            
            if (device != null) {
                selectedDevice = device
                onDeviceSelected?.invoke(device)
            } else {
                // Сбросить масштаб при двойном тапе на пустом месте
                centerOnTopology()
            }
            
            return true
        }
    }
    
    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val oldScale = scale
            scale *= detector.scaleFactor
            scale = scale.coerceIn(0.3f, 3f)
            
            // Масштабировать относительно центра жеста
            val focusX = detector.focusX
            val focusY = detector.focusY
            
            translateX += (focusX - translateX) * (1 - scale / oldScale)
            translateY += (focusY - translateY) * (1 - scale / oldScale)
            
            invalidate()
            return true
        }
    }
}

/**
 * Режим работы Canvas
 */
enum class CanvasMode {
    SELECT,      // Выбор и перемещение устройств
    CONNECT,     // Создание соединений
    ADD_DEVICE   // Добавление устройств
}

