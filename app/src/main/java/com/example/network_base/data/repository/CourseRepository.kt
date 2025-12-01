package com.example.network_base.data.repository

import android.content.Context
import com.example.network_base.data.model.*

/**
 * Репозиторий для получения контента курса
 */
class CourseRepository(private val context: Context) {
    
    private val modules: List<CourseModule> by lazy { createCourseModules() }
    
    /**
     * Получить все модули курса
     */
    fun getAllModules(): List<CourseModule> = modules
    
    /**
     * Получить модуль по ID
     */
    fun getModuleById(moduleId: String): CourseModule? {
        return modules.find { it.id == moduleId }
    }
    
    /**
     * Получить урок по ID
     */
    fun getLessonById(lessonId: String): Lesson? {
        for (module in modules) {
            module.lessons.find { it.id == lessonId }?.let { return it }
        }
        return null
    }
    
    /**
     * Получить задание по ID
     */
    fun getTaskById(taskId: String): Task? {
        for (module in modules) {
            if (module.task?.id == taskId) {
                return module.task
            }
        }
        return null
    }
    
    /**
     * Получить следующий модуль
     */
    fun getNextModule(currentModuleId: String): CourseModule? {
        val currentIndex = modules.indexOfFirst { it.id == currentModuleId }
        return if (currentIndex >= 0 && currentIndex < modules.size - 1) {
            modules[currentIndex + 1]
        } else {
            null
        }
    }
    
    /**
     * Создание контента курса
     */
    private fun createCourseModules(): List<CourseModule> {
        return listOf(
            createModule1(),
            createModule2(),
            createModule3(),
            createModule4(),
            createModule5()
        )
    }
    
    // ==================== МОДУЛЬ 1: Введение в сети ====================
    private fun createModule1(): CourseModule {
        return CourseModule(
            id = "module_1",
            title = "Введение в сети",
            description = "Базовые понятия компьютерных сетей",
            iconName = "ic_module_intro",
            order = 1,
            lessons = listOf(
                createLesson1_1(),
                createLesson1_2()
            ),
            task = createTask1()
        )
    }
    
    private fun createLesson1_1(): Lesson {
        return Lesson(
            id = "lesson_1_1",
            moduleId = "module_1",
            title = "Что такое компьютерная сеть",
            order = 1,
            estimatedMinutes = 5,
            contentBlocks = listOf(
                ContentBlock.Text(
                    "Что такое компьютерная сеть?",
                    TextStyle.HEADING1
                ),
                ContentBlock.Text(
                    "Компьютерная сеть — это группа компьютеров и других устройств, соединённых между собой для обмена данными и совместного использования ресурсов."
                ),
                ContentBlock.InfoBox(
                    "Простой пример: когда вы подключаете телефон к Wi-Fi дома — вы подключаетесь к домашней сети!",
                    InfoType.TIP
                ),
                ContentBlock.Text(
                    "Зачем нужны сети?",
                    TextStyle.HEADING2
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "Обмен файлами между компьютерами",
                        "Совместное использование принтеров и других устройств",
                        "Доступ в интернет",
                        "Обмен сообщениями и видеосвязь",
                        "Совместная работа над документами"
                    ),
                    ordered = false
                ),
                ContentBlock.Text(
                    "Основные компоненты сети",
                    TextStyle.HEADING2
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "Узлы (nodes) — устройства в сети: компьютеры, телефоны, серверы",
                        "Каналы связи — кабели или беспроводные соединения",
                        "Сетевое оборудование — роутеры, коммутаторы, точки доступа"
                    ),
                    ordered = true
                ),
                ContentBlock.InfoBox(
                    "В этом приложении вы будете строить виртуальные сети, добавляя устройства и соединяя их между собой.",
                    InfoType.INFO
                )
            )
        )
    }
    
    private fun createLesson1_2(): Lesson {
        return Lesson(
            id = "lesson_1_2",
            moduleId = "module_1",
            title = "Типы сетей",
            order = 2,
            estimatedMinutes = 7,
            contentBlocks = listOf(
                ContentBlock.Text(
                    "Типы компьютерных сетей",
                    TextStyle.HEADING1
                ),
                ContentBlock.Text(
                    "Сети классифицируют по размеру и географическому охвату:"
                ),
                ContentBlock.Text(
                    "LAN — Локальная сеть",
                    TextStyle.HEADING2
                ),
                ContentBlock.Text(
                    "Local Area Network — сеть небольшого размера, обычно в пределах одного здания или офиса."
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "Домашняя сеть с Wi-Fi роутером",
                        "Офисная сеть компании",
                        "Сеть в учебном классе"
                    ),
                    ordered = false
                ),
                ContentBlock.InfoBox(
                    "Характеристики LAN: высокая скорость (до 10 Гбит/с), низкая задержка, ограниченная территория.",
                    InfoType.INFO
                ),
                ContentBlock.Text(
                    "WAN — Глобальная сеть",
                    TextStyle.HEADING2
                ),
                ContentBlock.Text(
                    "Wide Area Network — сеть, объединяющая устройства на большом расстоянии, часто в разных городах или странах."
                ),
                ContentBlock.InfoBox(
                    "Интернет — самая большая WAN в мире!",
                    InfoType.TIP
                ),
                ContentBlock.Text(
                    "MAN — Городская сеть",
                    TextStyle.HEADING2
                ),
                ContentBlock.Text(
                    "Metropolitan Area Network — сеть, охватывающая город или район. Например, сеть кабельного телевидения или сеть филиалов банка в городе."
                ),
                ContentBlock.Text(
                    "Другие типы сетей",
                    TextStyle.HEADING2
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "PAN (Personal Area Network) — персональная сеть (Bluetooth)",
                        "WLAN — беспроводная локальная сеть (Wi-Fi)",
                        "VPN — виртуальная частная сеть"
                    ),
                    ordered = false
                )
            )
        )
    }
    
    private fun createTask1(): Task {
        return Task(
            id = "task_1",
            moduleId = "module_1",
            title = "Соединить два компьютера",
            description = "Создайте простейшую сеть из двух компьютеров и соедините их между собой.",
            objectives = listOf(
                "Добавьте два компьютера (PC) на рабочую область",
                "Соедините их кабелем"
            ),
            requirements = listOf(
                TaskRequirement.DeviceCount(
                    deviceType = DeviceType.PC,
                    minCount = 2,
                    description = "Добавить минимум 2 компьютера",
                    errorMessage = "Нужно добавить минимум 2 компьютера"
                ),
                TaskRequirement.DevicesConnected(
                    deviceNames = listOf("PC"),
                    description = "Компьютеры должны быть соединены",
                    errorMessage = "Компьютеры не соединены между собой"
                )
            ),
            hints = listOf(
                TaskHint(
                    id = "hint_1_1",
                    order = 1,
                    type = HintType.PRACTICAL,
                    title = "Как добавить устройство",
                    content = "Нажмите на кнопку PC в панели инструментов, затем коснитесь рабочей области для размещения устройства."
                ),
                TaskHint(
                    id = "hint_1_2",
                    order = 2,
                    type = HintType.PRACTICAL,
                    title = "Как соединить устройства",
                    content = "Нажмите кнопку 'Соединить', затем выберите первое устройство и второе — линия соединения появится автоматически."
                )
            ),
            maxScore = 100,
            xpReward = 30
        )
    }
    
    // ==================== МОДУЛЬ 2: IP-адресация ====================
    private fun createModule2(): CourseModule {
        return CourseModule(
            id = "module_2",
            title = "IP-адресация",
            description = "Как устройства находят друг друга в сети",
            iconName = "ic_module_ip",
            order = 2,
            requiredModuleId = "module_1",
            lessons = listOf(
                createLesson2_1(),
                createLesson2_2(),
                createLesson2_3()
            ),
            task = createTask2()
        )
    }
    
    private fun createLesson2_1(): Lesson {
        return Lesson(
            id = "lesson_2_1",
            moduleId = "module_2",
            title = "Что такое IP-адрес",
            order = 1,
            estimatedMinutes = 6,
            contentBlocks = listOf(
                ContentBlock.Text(
                    "IP-адрес — уникальный идентификатор",
                    TextStyle.HEADING1
                ),
                ContentBlock.Text(
                    "IP-адрес (Internet Protocol address) — это уникальный числовой идентификатор устройства в сети, подобно почтовому адресу дома."
                ),
                ContentBlock.InfoBox(
                    "Без IP-адреса компьютер не сможет отправлять или получать данные в сети!",
                    InfoType.IMPORTANT
                ),
                ContentBlock.Text(
                    "Зачем нужен IP-адрес?",
                    TextStyle.HEADING2
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "Идентификация устройства в сети",
                        "Маршрутизация данных к нужному получателю",
                        "Организация логической структуры сети"
                    ),
                    ordered = false
                ),
                ContentBlock.Text(
                    "Пример IP-адреса",
                    TextStyle.HEADING2
                ),
                ContentBlock.Code(
                    "192.168.1.100"
                ),
                ContentBlock.Text(
                    "IP-адрес состоит из 4 чисел (октетов), разделённых точками. Каждое число — от 0 до 255."
                ),
                ContentBlock.InfoBox(
                    "Это как номер дома: город.район.улица.дом — каждая часть уточняет местоположение.",
                    InfoType.TIP
                )
            )
        )
    }
    
    private fun createLesson2_2(): Lesson {
        return Lesson(
            id = "lesson_2_2",
            moduleId = "module_2",
            title = "IPv4: формат и классы",
            order = 2,
            estimatedMinutes = 8,
            contentBlocks = listOf(
                ContentBlock.Text(
                    "Формат IPv4 адреса",
                    TextStyle.HEADING1
                ),
                ContentBlock.Text(
                    "IPv4 — это 32-битный адрес, записываемый как 4 десятичных числа от 0 до 255:"
                ),
                ContentBlock.Code(
                    "┌────────┬────────┬────────┬────────┐\n│  192   │  168   │   1    │  100   │\n└────────┴────────┴────────┴────────┘\n   8 бит    8 бит    8 бит    8 бит"
                ),
                ContentBlock.Text(
                    "Частные (приватные) адреса",
                    TextStyle.HEADING2
                ),
                ContentBlock.Text(
                    "Некоторые диапазоны адресов зарезервированы для использования в локальных сетях:"
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "10.0.0.0 — 10.255.255.255 (класс A)",
                        "172.16.0.0 — 172.31.255.255 (класс B)",
                        "192.168.0.0 — 192.168.255.255 (класс C)"
                    ),
                    ordered = false
                ),
                ContentBlock.InfoBox(
                    "Адреса 192.168.x.x чаще всего используются в домашних и офисных сетях.",
                    InfoType.TIP
                ),
                ContentBlock.Text(
                    "Специальные адреса",
                    TextStyle.HEADING2
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "127.0.0.1 — localhost (ваш собственный компьютер)",
                        "0.0.0.0 — все интерфейсы",
                        "255.255.255.255 — широковещательный адрес"
                    ),
                    ordered = false
                )
            )
        )
    }
    
    private fun createLesson2_3(): Lesson {
        return Lesson(
            id = "lesson_2_3",
            moduleId = "module_2",
            title = "Маска подсети",
            order = 3,
            estimatedMinutes = 7,
            contentBlocks = listOf(
                ContentBlock.Text(
                    "Что такое маска подсети?",
                    TextStyle.HEADING1
                ),
                ContentBlock.Text(
                    "Маска подсети определяет, какая часть IP-адреса относится к сети, а какая — к конкретному устройству."
                ),
                ContentBlock.Code(
                    "IP-адрес:    192.168.1.100\nМаска:       255.255.255.0\n─────────────────────────────\nСеть:        192.168.1.x\nУстройство:  x.x.x.100"
                ),
                ContentBlock.Text(
                    "Как это работает?",
                    TextStyle.HEADING2
                ),
                ContentBlock.Text(
                    "Маска 255.255.255.0 означает:"
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "Первые 3 октета (255.255.255) — адрес сети",
                        "Последний октет (0) — адреса устройств в сети",
                        "В такой сети может быть до 254 устройств"
                    ),
                    ordered = true
                ),
                ContentBlock.InfoBox(
                    "Устройства могут общаться напрямую, только если они в одной подсети!",
                    InfoType.IMPORTANT
                ),
                ContentBlock.Text(
                    "Пример",
                    TextStyle.HEADING2
                ),
                ContentBlock.Code(
                    "PC1: 192.168.1.10 / 255.255.255.0\nPC2: 192.168.1.20 / 255.255.255.0\n→ В одной сети, могут общаться\n\nPC1: 192.168.1.10 / 255.255.255.0\nPC3: 192.168.2.10 / 255.255.255.0\n→ В разных сетях, нужен роутер"
                )
            )
        )
    }
    
    private fun createTask2(): Task {
        return Task(
            id = "task_2",
            moduleId = "module_2",
            title = "Настроить IP для сети",
            description = "Создайте сеть из 3 компьютеров и настройте им IP-адреса в одной подсети.",
            objectives = listOf(
                "Добавьте 3 компьютера",
                "Настройте каждому IP-адрес в сети 192.168.1.x",
                "Соедините компьютеры через коммутатор"
            ),
            requirements = listOf(
                TaskRequirement.DeviceCount(
                    deviceType = DeviceType.PC,
                    minCount = 3,
                    description = "Добавить 3 компьютера",
                    errorMessage = "Нужно добавить минимум 3 компьютера"
                ),
                TaskRequirement.DeviceCount(
                    deviceType = DeviceType.SWITCH,
                    minCount = 1,
                    description = "Добавить коммутатор",
                    errorMessage = "Нужно добавить коммутатор для соединения устройств"
                ),
                TaskRequirement.IpConfigured(
                    subnet = "192.168.1",
                    deviceType = DeviceType.PC,
                    description = "Настроить IP в подсети 192.168.1.x",
                    errorMessage = "IP-адреса должны быть в подсети 192.168.1.x"
                )
            ),
            hints = listOf(
                TaskHint(
                    id = "hint_2_1",
                    order = 1,
                    type = HintType.THEORY,
                    title = "Что такое подсеть?",
                    content = "Подсеть — это группа устройств с общей частью IP-адреса. Например, 192.168.1.10 и 192.168.1.20 в одной подсети.",
                    relatedLessonId = "lesson_2_3"
                ),
                TaskHint(
                    id = "hint_2_2",
                    order = 2,
                    type = HintType.PRACTICAL,
                    title = "Как настроить IP",
                    content = "Выберите устройство → откройте свойства → введите IP-адрес, например: 192.168.1.10, 192.168.1.20, 192.168.1.30"
                )
            ),
            maxScore = 100,
            xpReward = 50
        )
    }
    
    // ==================== МОДУЛЬ 3: Коммутаторы и MAC ====================
    private fun createModule3(): CourseModule {
        return CourseModule(
            id = "module_3",
            title = "Коммутаторы и MAC",
            description = "Как работают коммутаторы и MAC-адреса",
            iconName = "ic_module_switch",
            order = 3,
            requiredModuleId = "module_2",
            lessons = listOf(
                createLesson3_1(),
                createLesson3_2(),
                createLesson3_3()
            ),
            task = createTask3()
        )
    }
    
    private fun createLesson3_1(): Lesson {
        return Lesson(
            id = "lesson_3_1",
            moduleId = "module_3",
            title = "MAC-адреса",
            order = 1,
            estimatedMinutes = 6,
            contentBlocks = listOf(
                ContentBlock.Text(
                    "Что такое MAC-адрес?",
                    TextStyle.HEADING1
                ),
                ContentBlock.Text(
                    "MAC-адрес (Media Access Control) — это уникальный физический адрес сетевого устройства, записанный в его оборудование."
                ),
                ContentBlock.Code(
                    "Пример: 00:1A:2B:3C:4D:5E"
                ),
                ContentBlock.Text(
                    "Отличия MAC от IP",
                    TextStyle.HEADING2
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "MAC-адрес постоянный (прошит в устройство)",
                        "IP-адрес можно менять",
                        "MAC работает на канальном уровне (Layer 2)",
                        "IP работает на сетевом уровне (Layer 3)"
                    ),
                    ordered = false
                ),
                ContentBlock.InfoBox(
                    "MAC-адрес — как серийный номер устройства, IP-адрес — как временный номер в сети.",
                    InfoType.TIP
                ),
                ContentBlock.Text(
                    "Структура MAC-адреса",
                    TextStyle.HEADING2
                ),
                ContentBlock.Code(
                    "00:1A:2B : 3C:4D:5E\n─────────   ─────────\n  OUI        NIC\n(vendor)   (unique)"
                ),
                ContentBlock.Text(
                    "Первые 3 байта — код производителя (OUI), последние 3 — уникальный номер устройства."
                )
            )
        )
    }
    
    private fun createLesson3_2(): Lesson {
        return Lesson(
            id = "lesson_3_2",
            moduleId = "module_3",
            title = "Как работает коммутатор",
            order = 2,
            estimatedMinutes = 8,
            contentBlocks = listOf(
                ContentBlock.Text(
                    "Коммутатор (Switch)",
                    TextStyle.HEADING1
                ),
                ContentBlock.Text(
                    "Коммутатор — устройство для соединения нескольких устройств в локальной сети. В отличие от хаба, коммутатор умный — он отправляет данные только нужному получателю."
                ),
                ContentBlock.Text(
                    "MAC-таблица",
                    TextStyle.HEADING2
                ),
                ContentBlock.Text(
                    "Коммутатор запоминает, какой MAC-адрес на каком порту:"
                ),
                ContentBlock.Code(
                    "┌──────────────────┬──────┐\n│    MAC-адрес     │ Порт │\n├──────────────────┼──────┤\n│ 00:1A:2B:3C:4D:5E│  1   │\n│ 00:1A:2B:3C:4D:5F│  2   │\n│ 00:1A:2B:3C:4D:60│  3   │\n└──────────────────┴──────┘"
                ),
                ContentBlock.Text(
                    "Алгоритм работы",
                    TextStyle.HEADING2
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "Получает кадр (frame) с данными",
                        "Запоминает MAC отправителя и порт",
                        "Ищет MAC получателя в таблице",
                        "Если нашёл — отправляет на нужный порт",
                        "Если не нашёл — отправляет на все порты (flood)"
                    ),
                    ordered = true
                ),
                ContentBlock.InfoBox(
                    "Благодаря MAC-таблице коммутатор работает эффективнее хаба — меньше лишнего трафика.",
                    InfoType.INFO
                )
            )
        )
    }
    
    private fun createLesson3_3(): Lesson {
        return Lesson(
            id = "lesson_3_3",
            moduleId = "module_3",
            title = "ARP протокол",
            order = 3,
            estimatedMinutes = 7,
            contentBlocks = listOf(
                ContentBlock.Text(
                    "ARP — Address Resolution Protocol",
                    TextStyle.HEADING1
                ),
                ContentBlock.Text(
                    "ARP связывает IP-адреса с MAC-адресами. Когда компьютер хочет отправить данные, он знает IP получателя, но не знает его MAC."
                ),
                ContentBlock.Text(
                    "Как работает ARP?",
                    TextStyle.HEADING2
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "PC1 хочет отправить данные на 192.168.1.20",
                        "PC1 отправляет ARP-запрос: \"Кто имеет 192.168.1.20?\"",
                        "Запрос идёт всем (broadcast)",
                        "PC2 (владелец IP) отвечает: \"Это я, мой MAC: xx:xx:xx\"",
                        "PC1 сохраняет ответ в ARP-таблицу",
                        "Теперь PC1 может отправить данные напрямую"
                    ),
                    ordered = true
                ),
                ContentBlock.Code(
                    "ARP Request (broadcast):\n\"Who has 192.168.1.20? Tell 192.168.1.10\"\n\nARP Reply (unicast):\n\"192.168.1.20 is at 00:1A:2B:3C:4D:5F\""
                ),
                ContentBlock.InfoBox(
                    "ARP-таблица кэшируется, чтобы не спрашивать каждый раз. Записи живут несколько минут.",
                    InfoType.TIP
                )
            )
        )
    }
    
    private fun createTask3(): Task {
        return Task(
            id = "task_3",
            moduleId = "module_3",
            title = "Сеть из 4 ПК через коммутатор",
            description = "Постройте сеть из 4 компьютеров, соединённых через один коммутатор, и убедитесь, что они могут обмениваться данными.",
            objectives = listOf(
                "Добавьте 4 компьютера и 1 коммутатор",
                "Настройте IP-адреса для всех ПК",
                "Соедините все ПК через коммутатор",
                "Проверьте связь с помощью ping"
            ),
            requirements = listOf(
                TaskRequirement.DeviceCount(
                    deviceType = DeviceType.PC,
                    minCount = 4,
                    description = "Добавить 4 компьютера",
                    errorMessage = "Нужно добавить 4 компьютера"
                ),
                TaskRequirement.DeviceCount(
                    deviceType = DeviceType.SWITCH,
                    minCount = 1,
                    description = "Добавить коммутатор",
                    errorMessage = "Нужен коммутатор для соединения"
                ),
                TaskRequirement.IpConfigured(
                    subnet = "192.168.1",
                    deviceType = DeviceType.PC,
                    description = "Все ПК в одной подсети",
                    errorMessage = "Все компьютеры должны быть в одной подсети"
                )
            ),
            hints = listOf(
                TaskHint(
                    id = "hint_3_1",
                    order = 1,
                    type = HintType.THEORY,
                    title = "Роль коммутатора",
                    content = "Коммутатор соединяет устройства в одной сети. Каждый ПК подключается к отдельному порту коммутатора.",
                    relatedLessonId = "lesson_3_2"
                )
            ),
            maxScore = 100,
            xpReward = 60
        )
    }
    
    // ==================== МОДУЛЬ 4: Маршрутизация ====================
    private fun createModule4(): CourseModule {
        return CourseModule(
            id = "module_4",
            title = "Маршрутизация",
            description = "Как данные передаются между сетями",
            iconName = "ic_module_router",
            order = 4,
            requiredModuleId = "module_3",
            lessons = listOf(
                createLesson4_1(),
                createLesson4_2(),
                createLesson4_3()
            ),
            task = createTask4()
        )
    }
    
    private fun createLesson4_1(): Lesson {
        return Lesson(
            id = "lesson_4_1",
            moduleId = "module_4",
            title = "Что такое роутер",
            order = 1,
            estimatedMinutes = 6,
            contentBlocks = listOf(
                ContentBlock.Text(
                    "Роутер (маршрутизатор)",
                    TextStyle.HEADING1
                ),
                ContentBlock.Text(
                    "Роутер — устройство, которое соединяет разные сети и направляет данные между ними. Он работает на сетевом уровне (Layer 3) и понимает IP-адреса."
                ),
                ContentBlock.InfoBox(
                    "Если коммутатор — это почтальон в одном доме, то роутер — это почтовая служба между городами.",
                    InfoType.TIP
                ),
                ContentBlock.Text(
                    "Зачем нужен роутер?",
                    TextStyle.HEADING2
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "Соединение разных подсетей",
                        "Выбор лучшего маршрута для данных",
                        "Разделение широковещательных доменов",
                        "Подключение к интернету"
                    ),
                    ordered = false
                ),
                ContentBlock.Text(
                    "Отличие от коммутатора",
                    TextStyle.HEADING2
                ),
                ContentBlock.Code(
                    "Коммутатор:\n- Работает с MAC-адресами\n- Соединяет устройства в ОДНОЙ сети\n- Не понимает IP\n\nРоутер:\n- Работает с IP-адресами\n- Соединяет РАЗНЫЕ сети\n- Принимает решения о маршруте"
                )
            )
        )
    }
    
    private fun createLesson4_2(): Lesson {
        return Lesson(
            id = "lesson_4_2",
            moduleId = "module_4",
            title = "Таблица маршрутизации",
            order = 2,
            estimatedMinutes = 8,
            contentBlocks = listOf(
                ContentBlock.Text(
                    "Таблица маршрутизации",
                    TextStyle.HEADING1
                ),
                ContentBlock.Text(
                    "Роутер использует таблицу маршрутизации, чтобы знать, куда отправлять пакеты:"
                ),
                ContentBlock.Code(
                    "┌─────────────────┬───────────────┬───────────┐\n│   Назначение    │    Шлюз       │ Интерфейс │\n├─────────────────┼───────────────┼───────────┤\n│ 192.168.1.0/24  │ Напрямую      │   eth0    │\n│ 192.168.2.0/24  │ Напрямую      │   eth1    │\n│ 0.0.0.0/0       │ 10.0.0.1      │   eth2    │\n└─────────────────┴───────────────┴───────────┘"
                ),
                ContentBlock.Text(
                    "Как роутер выбирает маршрут?",
                    TextStyle.HEADING2
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "Получает пакет с IP-адресом назначения",
                        "Ищет совпадение в таблице маршрутизации",
                        "Выбирает наиболее специфичный маршрут",
                        "Пересылает пакет через нужный интерфейс"
                    ),
                    ordered = true
                ),
                ContentBlock.InfoBox(
                    "Маршрут 0.0.0.0/0 — это маршрут по умолчанию (default route). Используется, если нет более подходящего.",
                    InfoType.INFO
                )
            )
        )
    }
    
    private fun createLesson4_3(): Lesson {
        return Lesson(
            id = "lesson_4_3",
            moduleId = "module_4",
            title = "Шлюз по умолчанию",
            order = 3,
            estimatedMinutes = 5,
            contentBlocks = listOf(
                ContentBlock.Text(
                    "Default Gateway — Шлюз по умолчанию",
                    TextStyle.HEADING1
                ),
                ContentBlock.Text(
                    "Шлюз по умолчанию — это IP-адрес роутера, на который устройство отправляет пакеты для адресов вне своей сети."
                ),
                ContentBlock.Code(
                    "PC настройки:\nIP:      192.168.1.10\nМаска:   255.255.255.0\nШлюз:    192.168.1.1  ← IP роутера"
                ),
                ContentBlock.Text(
                    "Как это работает?",
                    TextStyle.HEADING2
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "PC хочет связаться с 8.8.8.8 (Google DNS)",
                        "PC видит: 8.8.8.8 не в моей сети 192.168.1.x",
                        "PC отправляет пакет на шлюз (192.168.1.1)",
                        "Роутер принимает пакет и пересылает дальше"
                    ),
                    ordered = true
                ),
                ContentBlock.InfoBox(
                    "Без настроенного шлюза компьютер не сможет связаться с устройствами в других сетях!",
                    InfoType.WARNING
                )
            )
        )
    }
    
    private fun createTask4(): Task {
        return Task(
            id = "task_4",
            moduleId = "module_4",
            title = "Соединить 2 подсети",
            description = "Создайте две отдельные подсети и соедините их через роутер.",
            objectives = listOf(
                "Создайте подсеть 192.168.1.x с 2 ПК",
                "Создайте подсеть 192.168.2.x с 2 ПК",
                "Соедините подсети через роутер",
                "Настройте шлюз на всех ПК"
            ),
            requirements = listOf(
                TaskRequirement.DeviceCount(
                    deviceType = DeviceType.PC,
                    minCount = 4,
                    description = "Добавить 4 компьютера",
                    errorMessage = "Нужно 4 компьютера (по 2 в каждой подсети)"
                ),
                TaskRequirement.DeviceCount(
                    deviceType = DeviceType.ROUTER,
                    minCount = 1,
                    description = "Добавить роутер",
                    errorMessage = "Нужен роутер для соединения подсетей"
                ),
                TaskRequirement.SubnetCount(
                    count = 2,
                    description = "Создать 2 разные подсети",
                    errorMessage = "Должно быть 2 разные подсети"
                )
            ),
            hints = listOf(
                TaskHint(
                    id = "hint_4_1",
                    order = 1,
                    type = HintType.PRACTICAL,
                    title = "Настройка роутера",
                    content = "Роутер должен иметь IP в каждой подсети: например, 192.168.1.1 и 192.168.2.1 на разных интерфейсах."
                ),
                TaskHint(
                    id = "hint_4_2",
                    order = 2,
                    type = HintType.THEORY,
                    title = "Шлюз для ПК",
                    content = "Для ПК в сети 192.168.1.x шлюз = 192.168.1.1, для ПК в сети 192.168.2.x шлюз = 192.168.2.1",
                    relatedLessonId = "lesson_4_3"
                )
            ),
            maxScore = 100,
            xpReward = 75
        )
    }
    
    // ==================== МОДУЛЬ 5: VLAN ====================
    private fun createModule5(): CourseModule {
        return CourseModule(
            id = "module_5",
            title = "Практика VLAN",
            description = "Виртуальные локальные сети",
            iconName = "ic_module_vlan",
            order = 5,
            requiredModuleId = "module_4",
            lessons = listOf(
                createLesson5_1(),
                createLesson5_2()
            ),
            task = createTask5()
        )
    }
    
    private fun createLesson5_1(): Lesson {
        return Lesson(
            id = "lesson_5_1",
            moduleId = "module_5",
            title = "Что такое VLAN",
            order = 1,
            estimatedMinutes = 7,
            contentBlocks = listOf(
                ContentBlock.Text(
                    "VLAN — Virtual LAN",
                    TextStyle.HEADING1
                ),
                ContentBlock.Text(
                    "VLAN (Virtual Local Area Network) — технология, позволяющая разделить физическую сеть на несколько логических сетей."
                ),
                ContentBlock.InfoBox(
                    "Представьте офис: бухгалтерия и IT-отдел подключены к одному коммутатору, но не должны видеть трафик друг друга. VLAN решает эту задачу!",
                    InfoType.TIP
                ),
                ContentBlock.Text(
                    "Преимущества VLAN",
                    TextStyle.HEADING2
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "Безопасность: изоляция трафика между группами",
                        "Гибкость: группировка по функции, а не по расположению",
                        "Производительность: уменьшение широковещательного трафика",
                        "Простота управления: легко переносить пользователей между VLAN"
                    ),
                    ordered = false
                ),
                ContentBlock.Code(
                    "Физически:\n[PC1]─┐\n[PC2]─┼──[Switch]──[PC3]\n[PC4]─┘\n\nЛогически (с VLAN):\nVLAN 10: PC1, PC2 (Бухгалтерия)\nVLAN 20: PC3, PC4 (IT-отдел)"
                )
            )
        )
    }
    
    private fun createLesson5_2(): Lesson {
        return Lesson(
            id = "lesson_5_2",
            moduleId = "module_5",
            title = "Зачем нужны VLAN",
            order = 2,
            estimatedMinutes = 6,
            contentBlocks = listOf(
                ContentBlock.Text(
                    "Практические сценарии VLAN",
                    TextStyle.HEADING1
                ),
                ContentBlock.Text(
                    "Сценарий 1: Офис",
                    TextStyle.HEADING2
                ),
                ContentBlock.Text(
                    "В офисе есть разные отделы: продажи, бухгалтерия, разработка. Каждый отдел — отдельный VLAN для безопасности."
                ),
                ContentBlock.Text(
                    "Сценарий 2: Гостевой Wi-Fi",
                    TextStyle.HEADING2
                ),
                ContentBlock.Text(
                    "Гости подключаются к отдельному VLAN, изолированному от корпоративной сети."
                ),
                ContentBlock.Text(
                    "Сценарий 3: VoIP телефония",
                    TextStyle.HEADING2
                ),
                ContentBlock.Text(
                    "Голосовой трафик выделяется в отдельный VLAN с приоритетом для качества связи."
                ),
                ContentBlock.InfoBox(
                    "Для связи между VLAN нужен роутер или L3-коммутатор (Inter-VLAN routing).",
                    InfoType.IMPORTANT
                ),
                ContentBlock.Text(
                    "Типы портов коммутатора",
                    TextStyle.HEADING2
                ),
                ContentBlock.ListBlock(
                    listOf(
                        "Access port — принадлежит одному VLAN (для ПК)",
                        "Trunk port — передаёт трафик нескольких VLAN (между коммутаторами)"
                    ),
                    ordered = false
                )
            )
        )
    }
    
    private fun createTask5(): Task {
        return Task(
            id = "task_5",
            moduleId = "module_5",
            title = "Разделить сеть на отделы",
            description = "Создайте сеть офиса с двумя отделами, изолированными с помощью VLAN.",
            objectives = listOf(
                "Создайте 4 ПК: 2 для отдела продаж, 2 для IT",
                "Настройте VLAN 10 для продаж",
                "Настройте VLAN 20 для IT",
                "Добавьте роутер для связи между VLAN"
            ),
            requirements = listOf(
                TaskRequirement.DeviceCount(
                    deviceType = DeviceType.PC,
                    minCount = 4,
                    description = "Добавить 4 компьютера",
                    errorMessage = "Нужно 4 компьютера (по 2 на отдел)"
                ),
                TaskRequirement.DeviceCount(
                    deviceType = DeviceType.SWITCH,
                    minCount = 1,
                    description = "Добавить коммутатор",
                    errorMessage = "Нужен коммутатор для VLAN"
                ),
                TaskRequirement.DeviceCount(
                    deviceType = DeviceType.ROUTER,
                    minCount = 1,
                    description = "Добавить роутер",
                    errorMessage = "Нужен роутер для связи между VLAN"
                )
            ),
            hints = listOf(
                TaskHint(
                    id = "hint_5_1",
                    order = 1,
                    type = HintType.THEORY,
                    title = "Что такое VLAN?",
                    content = "VLAN разделяет сеть логически. Устройства в разных VLAN не видят друг друга без роутера.",
                    relatedLessonId = "lesson_5_1"
                ),
                TaskHint(
                    id = "hint_5_2",
                    order = 2,
                    type = HintType.PRACTICAL,
                    title = "Настройка VLAN",
                    content = "Выберите интерфейс устройства и укажите VLAN ID: 10 для продаж, 20 для IT."
                )
            ),
            maxScore = 100,
            xpReward = 100
        )
    }
}

