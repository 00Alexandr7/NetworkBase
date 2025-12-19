
package com.example.network_base.data.repository

import com.example.network_base.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Репозиторий для работы с курсами
 */
class CourseRepository {
    private val _modules = MutableStateFlow<List<CourseModule>>(emptyList())
    val modules: Flow<List<CourseModule>> = _modules.asStateFlow()

    private val _lessons = MutableStateFlow<List<LessonWithContent>>(emptyList())
    val lessons: Flow<List<LessonWithContent>> = _lessons.asStateFlow()

    private val _tasks = MutableStateFlow<List<TaskWithRequirements>>(emptyList())
    val tasks: Flow<List<TaskWithRequirements>> = _tasks.asStateFlow()

    init {
        // Инициализация тестовыми данными
        initializeTestData()
    }

    /**
     * Получить все модули
     */
    fun getAllModules(): List<CourseModule> {
        return _modules.value
    }

    /**
     * Получить модуль по ID
     */
    fun getModuleById(moduleId: String): CourseModule? {
        return _modules.value.find { it.id == moduleId }
    }

    /**
     * Получить урок по ID
     */
    fun getLessonById(lessonId: String): LessonWithContent? {
        return _lessons.value.find { it.id == lessonId }
    }

    /**
     * Получить задание по ID
     */
    fun getTaskById(taskId: String): TaskWithRequirements? {
        return _tasks.value.find { it.id == taskId }
    }

    /**
     * Получить топологию для задания
     */
    fun getTopologyByTaskId(taskId: String): NetworkTopology? {
        return getTaskById(taskId)?.initialTopology
    }

    /**
     * Инициализация тестовыми данными
     */
    private fun initializeTestData() {
        val modules = listOf(
            CourseModule(
                id = "module1",
                title = "Введение в сети",
                description = "Основы компьютерных сетей, модели OSI и TCP/IP",
                order = 1,
                lessons = listOf(
                    LessonWithContent(
                        id = "lesson1",
                        moduleId = "module1",
                        title = "Что такое компьютерная сеть",
                        description = "Введение в компьютерные сети",
                        order = 1,
                        estimatedMinutes = 15,
                        contentBlocks = listOf(
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Что такое компьютерная сеть?",
                                style = TextStyle.HEADING_1
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Компьютерная сеть — это система, состоящая из двух или более устройств, соединённых между собой для обмена данными и общим доступом к ресурсам.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Зачем нужны сети",
                                style = TextStyle.HEADING_2
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Сети позволяют совместно использовать интернет‑подключение, файловые хранилища, принтеры и другие ресурсы. Без сети каждый компьютер жил бы сам по себе.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Типы сетей",
                                style = TextStyle.HEADING_2
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Сети можно классифицировать по разным признакам: по территории (LAN, MAN, WAN), по топологии (шина, звезда, кольцо, mesh), по способу управления (одноранговые, клиент‑серверные).",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.NOTE,
                                content = "LAN (локальная сеть) — это офис, дом или аудитория. WAN — глобальные сети вроде интернета.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.EXAMPLE,
                                content = "Примеры реальных сетей",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "Домашняя Wi‑Fi сеть",
                                    "Сеть кампуса/колледжа",
                                    "Корпоративная сеть офиса"
                                )
                            ),
                            ContentBlock(
                                type = InfoType.EXERCISE,
                                content = "Небольшое упражнение",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "Вспомните как минимум 2 сети, с которыми вы сталкиваетесь каждый день",
                                    "Попробуйте отнести их к типам LAN/MAN/WAN",
                                    "Подумайте, какие устройства входят в каждую из этих сетей"
                                )
                            )
                        )
                    ),
                    LessonWithContent(
                        id = "lesson1_2",
                        moduleId = "module1",
                        title = "Модели OSI и TCP/IP",
                        description = "Слои, инкапсуляция и зачем это нужно",
                        order = 2,
                        estimatedMinutes = 20,
                        contentBlocks = listOf(
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Модели OSI и TCP/IP",
                                style = TextStyle.HEADING_1
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Модель OSI описывает сетевое взаимодействие в виде 7 уровней: от физического до прикладного. Модель TCP/IP проще — в ней обычно выделяют 4 уровня.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Сравнение уровней",
                                style = TextStyle.HEADING_2
                            ),
                            ContentBlock(
                                type = InfoType.EXAMPLE,
                                content = "Привязка уровней OSI к реальным устройствам",
                                items = listOf(
                                    "L1 (физический) — кабели, коннекторы",
                                    "L2 (канальный) — свитчи, MAC‑адреса",
                                    "L3 (сетевой) — роутеры, IP‑адреса",
                                    "L7 (прикладной) — приложения (браузер, почта и т.п.)"
                                )
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Инкапсуляция",
                                style = TextStyle.HEADING_2
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Данные прикладного уровня последовательно оборачиваются в заголовки нижележащих уровней. На принимающей стороне заголовки снимаются в обратном порядке — это деинкапсуляция.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.TIP,
                                content = "Представьте конверт в конверте: письмо → конверт города → конверт страны. Так работают заголовки протоколов.",
                                style = TextStyle.NORMAL
                            )
                        )
                    )
                ),
                task = TaskWithRequirements(
                    id = "task1",
                    moduleId = "module1",
                    title = "Создание простой сети",
                    description = "Создайте сеть из двух компьютеров, соединенных напрямую",
                    xpReward = 25,
                    requirements = listOf(
                        TaskRequirement.DeviceCount(
                            deviceType = DeviceType.COMPUTER,
                            minCount = 2,
                            maxCount = 2
                        )
                    ),
                    hints = listOf(
                        "Добавь 2 компьютера кнопкой 'ПК'.",
                        "Нажми 'Соединить' и кликни по первому ПК, затем по второму — появится линия.",
                        "Если проверка не проходит: убедись, что устройств ровно 2 и они соединены между собой."
                    ),
                    objectives = listOf(
                        "Добавить два компьютера",
                        "Соединить компьютеры напрямую"
                    )
                )
            ),
            CourseModule(
                id = "module2",
                title = "IP-адресация",
                description = "Основы IP-адресации, подсети, маски",
                order = 2,
                lessons = listOf(
                    LessonWithContent(
                        id = "lesson2",
                        moduleId = "module2",
                        title = "IP-адреса и маски подсетей",
                        description = "Основы IP-адресации",
                        order = 1,
                        estimatedMinutes = 20,
                        contentBlocks = listOf(
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "IPv4: адресация и маска",
                                style = TextStyle.HEADING_1
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "IP-адрес - это уникальный идентификатор устройства в сети. В IPv4 адрес состоит из 32 бит (4 байта) и обычно записывается в виде четырех десятичных чисел от 0 до 255, разделенных точками.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.NOTE,
                                content = "IPv4 адрес — это 32 бита. Точка-разделённая запись (например 192.168.1.10) — это просто удобный вид четырёх байт.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Маска подсети",
                                style = TextStyle.HEADING_2
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Маска подсети определяет, какая часть IP-адреса относится к сети, а какая - к хосту. Она также состоит из 32 бит и записывается в том же формате, что и IP-адрес.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Сетевой адрес и broadcast",
                                style = TextStyle.HEADING_2
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "У любой подсети есть: сетевой адрес (все биты хостовой части = 0) и broadcast (все биты хостовой части = 1). Эти адреса обычно не назначаются хостам.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.EXAMPLE,
                                content = "Пример: 192.168.1.0/24\nСеть: 192.168.1.0\nBroadcast: 192.168.1.255\nХосты: 192.168.1.1 – 192.168.1.254",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "Сеть: 192.168.1.0",
                                    "Broadcast: 192.168.1.255",
                                    "Хосты: 192.168.1.1 – 192.168.1.254"
                                )
                            ),
                            ContentBlock(
                                type = InfoType.TIP,
                                content = "Быстрый лайфхак: /24 почти всегда значит 255.255.255.0 — это самый популярный вариант в домашних/учебных сетях.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.EXERCISE,
                                content = "Мини‑практика",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "Сколько usable-хостов в сети /24?",
                                    "Назови network и broadcast для 10.0.5.0/24.",
                                    "Почему адрес 192.168.1.0 обычно не назначают компьютеру?"
                                )
                            )
                        )
                    ),
                    LessonWithContent(
                        id = "lesson2_2",
                        moduleId = "module2",
                        title = "Подсети и CIDR",
                        description = "Понимание /24, /26 и разбиения сети",
                        order = 2,
                        estimatedMinutes = 25,
                        contentBlocks = listOf(
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "CIDR и разбиение подсетей",
                                style = TextStyle.HEADING_1
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "CIDR-нотация (/24, /26 и т.д.) показывает количество бит сетевой части. Чем больше число, тем меньше подсеть.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.EXAMPLE,
                                content = "Пример: 192.168.10.0/24 — 256 адресов (254 usable), 192.168.10.0/26 — 64 адреса (62 usable).",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Сколько адресов даёт префикс",
                                style = TextStyle.HEADING_2
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Количество адресов в подсети = 2^(32 - prefix). Usable‑адресов обычно на 2 меньше (network и broadcast).",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.EXAMPLE,
                                content = "Подсказка по популярным префиксам",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "/24: 256 адресов, 254 usable",
                                    "/25: 128 адресов, 126 usable",
                                    "/26: 64 адреса, 62 usable",
                                    "/27: 32 адреса, 30 usable",
                                    "/28: 16 адресов, 14 usable"
                                )
                            ),
                            ContentBlock(
                                type = InfoType.WARNING,
                                content = "Важно: не всегда “usable-2”. В некоторых кейсах (например /31 в P2P) правила отличаются, но для базовой IPv4 практики — “-2” почти всегда верно.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.EXERCISE,
                                content = "Мини‑практика",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "Сколько usable‑адресов у /26?",
                                    "Во сколько подсетей можно разбить 192.168.1.0/24 на /26?",
                                    "Какие будут первые две подсети /26 внутри 192.168.1.0/24?"
                                )
                            )
                        )
                    )
                ),
                task = TaskWithRequirements(
                    id = "task2",
                    moduleId = "module2",
                    title = "Настройка IP-адресов",
                    description = "Настройте IP-адреса для устройств в сети",
                    xpReward = 50,
                    requirements = listOf(
                        TaskRequirement.DeviceCount(
                            deviceType = DeviceType.COMPUTER,
                            minCount = 3
                        ),
                        TaskRequirement.IpConfigured(
                            subnet = "192.168.1."
                        )
                    ),
                    hints = listOf(
                        "Добавь 3 ПК и соедини их (напрямую или через свитч — зависит от требований проверки).",
                        "Выбери ПК → открой свойства → задай IP: 192.168.1.2, 192.168.1.3, 192.168.1.4 (маска /24).",
                        "Проверь, что у каждого устройства IP начинается с 192.168.1.",
                        "Для проверки связности можешь нажать 'Симуляция' (ping)."
                    ),
                    objectives = listOf(
                        "Добавить три компьютера",
                        "Настроить IP-адреса в одной подсети",
                        "Проверить связность"
                    )
                )
            ),
            CourseModule(
                id = "module3",
                title = "Коммутаторы и MAC",
                description = "Канальный уровень, MAC-адреса, таблица коммутации",
                order = 3,
                lessons = listOf(
                    LessonWithContent(
                        id = "lesson3",
                        moduleId = "module3",
                        title = "MAC-адрес и Ethernet",
                        description = "Как работает передача на L2",
                        order = 1,
                        estimatedMinutes = 18,
                        contentBlocks = listOf(
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Канальный уровень: Ethernet и MAC",
                                style = TextStyle.HEADING_1
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "MAC-адрес — уникальный адрес сетевого интерфейса на канальном уровне. Ethernet-кадр содержит MAC источника и назначения.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "MAC vs IP",
                                style = TextStyle.HEADING_2
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "IP нужен для маршрутизации между подсетями (L3), MAC — для доставки внутри L2 домена. Внутри одной LAN кадры ходят по MAC, а IP используется “для логики” протоколов.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.NOTE,
                                content = "Коммутатор обучается MAC-адресам, анализируя входящие кадры.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "ARP: как узнаём MAC по IP",
                                style = TextStyle.HEADING_2
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Чтобы отправить кадр на IP в своей подсети, компьютер сначала должен узнать MAC адрес назначения. Для этого используется ARP (Address Resolution Protocol).",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.EXAMPLE,
                                content = "Упрощённый сценарий ARP",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "ПК1 хочет отправить пакет на 192.168.10.3",
                                    "ПК1 делает ARP Request (broadcast): “кто 192.168.10.3?”",
                                    "ПК2 отвечает ARP Reply (unicast): “это я, мой MAC = …”",
                                    "ПК1 сохраняет запись в ARP cache и отправляет Ethernet кадр на MAC ПК2"
                                )
                            ),
                            ContentBlock(
                                type = InfoType.EXERCISE,
                                content = "Мини‑практика",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "Почему ARP Request — широковещательный?",
                                    "Что произойдёт, если очистить ARP cache?",
                                    "Почему MAC не помогает маршрутизировать между подсетями?"
                                )
                            )
                        )
                    ),
                    LessonWithContent(
                        id = "lesson3_2",
                        moduleId = "module3",
                        title = "Как коммутатор принимает решения",
                        description = "Flooding, learning, forwarding",
                        order = 2,
                        estimatedMinutes = 22,
                        contentBlocks = listOf(
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Логика работы коммутатора",
                                style = TextStyle.HEADING_1
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Если MAC назначения неизвестен — коммутатор делает flooding (рассылает по всем портам кроме входящего). Когда узнаёт — начинает forwarding на конкретный порт.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Таблица коммутации (MAC table)",
                                style = TextStyle.HEADING_2
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Коммутатор хранит соответствие: MAC → порт. При получении кадра он “учится”: запоминает MAC источника и порт, откуда пришёл кадр.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.EXAMPLE,
                                content = "Что делает свитч при получении кадра",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "1) Записать MAC источника в таблицу (learning)",
                                    "2) Посмотреть MAC назначения",
                                    "3) Если MAC назначения известен → отправить только на нужный порт (forwarding)",
                                    "4) Если неизвестен → разослать по всем портам кроме входящего (flooding)"
                                )
                            ),
                            ContentBlock(
                                type = InfoType.TIP,
                                content = "Flooding — это нормально на старте. Через несколько кадров таблица заполнится и трафик станет “точечным”.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.EXERCISE,
                                content = "Мини‑практика",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "Почему после подключения нового ПК первое время может быть flooding?",
                                    "Что будет, если MAC адрес устройства сменился, а таблица ещё старая?",
                                    "Зачем вообще VLAN, если свитч уже умеет учиться MAC?"
                                )
                            )
                        )
                    )
                ),
                task = TaskWithRequirements(
                    id = "task3",
                    moduleId = "module3",
                    title = "Сеть через коммутатор",
                    description = "Соберите LAN: 3 ПК через коммутатор и проверьте связность",
                    xpReward = 60,
                    requirements = listOf(
                        TaskRequirement.DeviceCount(deviceType = DeviceType.SWITCH, minCount = 1, maxCount = 1),
                        TaskRequirement.DeviceCount(deviceType = DeviceType.PC, minCount = 3),
                        TaskRequirement.IpConfigured(subnet = "192.168.10.")
                    ),
                    hints = listOf(
                        "Добавь 1 коммутатор и 3 ПК.",
                        "Соедини каждый ПК с коммутатором (3 отдельных кабеля).",
                        "Задай IP всем ПК в одной подсети: 192.168.10.2/24, 192.168.10.3/24, 192.168.10.4/24.",
                        "Если проверка не проходит — проверь, что коммутатор ровно один и ПК не меньше 3."
                    ),
                    objectives = listOf(
                        "Добавить коммутатор",
                        "Добавить 3 ПК",
                        "Соединить ПК с коммутатором",
                        "Настроить IP в одной подсети"
                    )
                )
            ),
            CourseModule(
                id = "module4",
                title = "Маршрутизация",
                description = "Связь подсетей, шлюз по умолчанию, маршрутизатор",
                order = 4,
                lessons = listOf(
                    LessonWithContent(
                        id = "lesson4",
                        moduleId = "module4",
                        title = "Зачем нужен маршрутизатор",
                        description = "Связь разных сетей",
                        order = 1,
                        estimatedMinutes = 20,
                        contentBlocks = listOf(
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Маршрутизация и шлюз по умолчанию",
                                style = TextStyle.HEADING_1
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Маршрутизатор работает на сетевом уровне (L3) и соединяет разные подсети. Хосты отправляют трафик вне своей сети на шлюз по умолчанию.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.TIP,
                                content = "Если два ПК в разных подсетях — без роутера они не смогут обмениваться трафиком.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Как ПК решает: “в свою сеть” или “в другую”",
                                style = TextStyle.HEADING_2
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Компьютер сравнивает свой IP и IP назначения с учётом маски. Если сеть совпадает — отправляет напрямую (узнав MAC через ARP). Если сеть другая — отправляет на default gateway.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.EXAMPLE,
                                content = "Пример решения",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "ПК: 192.168.1.10/24",
                                    "Назначение: 192.168.1.50 → та же сеть → отправка напрямую",
                                    "Назначение: 10.0.0.20 → другая сеть → отправка на шлюз (например 192.168.1.1)"
                                )
                            ),
                            ContentBlock(
                                type = InfoType.WARNING,
                                content = "Частая ошибка: IP адреса настроены, но не указан шлюз по умолчанию. Тогда пакеты “в другие сети” просто не уходят.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.EXERCISE,
                                content = "Мини‑практика",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "Для ПК 10.0.0.2/24 какой должен быть шлюз, чтобы уйти в интернет через роутер?",
                                    "Почему роутеру нужны IP адреса на каждом интерфейсе?",
                                    "Что произойдёт, если на двух интерфейсах роутера одна и та же подсеть?"
                                )
                            )
                        )
                    )
                ),
                task = TaskWithRequirements(
                    id = "task4",
                    moduleId = "module4",
                    title = "Соединить две подсети",
                    description = "Создайте две подсети и обеспечьте обмен трафиком через маршрутизатор",
                    xpReward = 90,
                    requirements = listOf(
                        TaskRequirement.DeviceCount(deviceType = DeviceType.ROUTER, minCount = 1, maxCount = 1),
                        TaskRequirement.SubnetCount(count = 2)
                    ),
                    hints = listOf(
                        "Добавь 1 роутер и 2 ПК (по одному в каждую подсеть).",
                        "Соедини ПК1 ↔ роутер (порт 1) и ПК2 ↔ роутер (порт 2).",
                        "Настрой IP: ПК1 = 192.168.1.2/24, роутер(порт1)=192.168.1.1/24; ПК2=10.0.0.2/24, роутер(порт2)=10.0.0.1/24.",
                        "На ПК укажи default gateway: для первой подсети 192.168.1.1, для второй 10.0.0.1."
                    ),
                    objectives = listOf(
                        "Добавить маршрутизатор",
                        "Создать две подсети (разные IP диапазоны)",
                        "Настроить IP на интерфейсах роутера",
                        "Настроить default gateway на ПК"
                    )
                )
            ),
            CourseModule(
                id = "module5",
                title = "VLAN (практика)",
                description = "Сегментация L2, изоляция широковещательного домена",
                order = 5,
                lessons = listOf(
                    LessonWithContent(
                        id = "lesson5",
                        moduleId = "module5",
                        title = "Что такое VLAN",
                        description = "Разделение сети на уровне коммутатора",
                        order = 1,
                        estimatedMinutes = 18,
                        contentBlocks = listOf(
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "VLAN: логическая сегментация сети",
                                style = TextStyle.HEADING_1
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "VLAN позволяет логически разделить сеть на несколько L2 доменов внутри одного (или нескольких) коммутаторов.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.WARNING,
                                content = "Устройства в разных VLAN не общаются напрямую на L2 без маршрутизации между VLAN.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "Зачем VLAN",
                                style = TextStyle.HEADING_2
                            ),
                            ContentBlock(
                                type = InfoType.TEXT,
                                content = "VLAN используют для изоляции отделов/групп, снижения широковещательного трафика и повышения безопасности. Это как “несколько виртуальных коммутаторов” внутри одного.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.EXAMPLE,
                                content = "Пример",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "VLAN 10 — бухгалтерия",
                                    "VLAN 20 — разработка",
                                    "Широковещательные кадры VLAN 10 не попадут в VLAN 20"
                                )
                            ),
                            ContentBlock(
                                type = InfoType.TIP,
                                content = "В учебной практике достаточно помнить: VLAN ID назначается порту/интерфейсу. Устройство “попадает” в VLAN через порт, к которому подключено.",
                                style = TextStyle.NORMAL
                            ),
                            ContentBlock(
                                type = InfoType.EXERCISE,
                                content = "Мини‑практика",
                                style = TextStyle.NORMAL,
                                items = listOf(
                                    "Почему VLAN снижает broadcast нагрузку?",
                                    "Можно ли ПК из VLAN 10 общаться с VLAN 20 без роутера?",
                                    "Что нужно добавить, чтобы VLAN 10 и VLAN 20 могли обмениваться трафиком (подсказка: L3)?"
                                )
                            )
                        )
                    )
                ),
                task = TaskWithRequirements(
                    id = "task5",
                    moduleId = "module5",
                    title = "Изоляция VLAN",
                    description = "Создайте 2 VLAN и распределите ПК по VLAN",
                    xpReward = 100,
                    requirements = listOf(
                        TaskRequirement.DeviceCount(deviceType = DeviceType.SWITCH, minCount = 1),
                        VlanRequirement(vlanId = 10, deviceNames = listOf(), description = "Настройте VLAN 10"),
                        VlanRequirement(vlanId = 20, deviceNames = listOf(), description = "Настройте VLAN 20")
                    ),
                    hints = listOf(
                        "Добавь коммутатор и несколько ПК.",
                        "Открой свойства интерфейса ПК и назначь VLAN ID (например, двум ПК VLAN 10, двум — VLAN 20).",
                        "Проверь, что устройства из разных VLAN изолированы на L2.",
                        "Если проверка не проходит — убедись, что в интерфейсах действительно выставлены VLAN 10 и VLAN 20."
                    ),
                    objectives = listOf(
                        "Добавить коммутатор",
                        "Добавить ПК и назначить VLAN 10/20 на их интерфейсах",
                        "Проверить изоляцию между VLAN"
                    )
                )
            )
        )

        _modules.value = modules
        _lessons.value = modules.flatMap { it.lessons }
        _tasks.value = modules.mapNotNull { it.task }
    }
}
