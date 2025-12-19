# NetworkBase

Android-приложение на Kotlin — учебный симулятор компьютерных сетей (в духе Packet Tracer) с курсами, уроками и практическими заданиями.

Проект объединяет учебный контент (модули/уроки), практические задания на построение топологий и систему прогресса (XP/уровни/достижения).

## Ключевые возможности

### Обучение

- Список модулей курса и экран деталей модуля
- Уроки (теория) и практические задания
- Поиск по модулям курса (SearchView в тулбаре)

### Симулятор и задачи

- Построение топологии на Canvas (устройства/соединения)
- Проверка решения задания (валидатор требований)
- Песочница для свободного построения и экспериментов

### Профиль и прогресс

- Прогресс по модулям, завершённые уроки/задания
- XP, уровни, достижения
- Локальный аватар в профиле (выбор фото из галереи)

### Авторизация и роли

- Firebase Auth (Email/Password)
- Роли пользователей `GUEST` / `USER` / `ADMIN`
- Админ-панель доступна только при роли `ADMIN`

### UI/UX

- Переключение светлой/тёмной темы (луна/солнце)
- Актуальные иконки под тёмную тему

## Технологии

- Kotlin, Coroutines / Flow
- Fragments + ViewBinding
- Navigation Component
- Material Components
- Room (локальные данные)
- Firebase Auth + Firestore

## Архитектура (кратко)

- **UI**: фрагменты + Navigation, адаптеры RecyclerView
- **Data**: Room для локального состояния + Firebase Auth/Firestore для авторизации и синхронизации
- **Domain**: логика симуляции/валидации топологии вынесена отдельно от UI

## Структура проекта (ориентиры)

- `app/src/main/java/.../ui/`
  - `course/` — экраны курса (модули, уроки, задания)
  - `sandbox/` — песочница
  - `profile/` — профиль и прогресс
  - `admin/` — админ-панель и управление
  - `auth/` — вход/регистрация
- `app/src/main/java/.../data/`
  - `local/` — Room (БД, DAO, Entities)
  - `model/` — модели предметной области
  - `repository/` — репозитории (курсы/прогресс/пользователи/авторизация)
- `app/src/main/java/.../domain/` — валидация/логика, не привязанная к UI

## Коллекции в FireStore
# Коллекция пользователей
users/{userId}
- name: string
- email: string
- role: string
- xp: number
- currentModuleId: string
- createdAt: timestamp

# Подколлекция достижений пользователя
users/{userId}/achievements/{achievementId}
- achievementId: string
- unlockedAt: timestamp

# Подколлекция прогресса пользователя по модулям
users/{userId}/progress/{moduleId}
- moduleId: string
- lessonsCompleted: array
- taskCompleted: boolean
- taskScore: number
- taskAttempts: number
- bestScore: number
- updatedAt: timestamp

# Подколлекция сохраненных топологий пользователя
users/{userId}/topologies/{topologyId}
- name: string
- createdAt: timestamp
- updatedAt: timestamp

# Подколлекция версий топологии
users/{userId}/topologies/{topologyId}/versions/{versionId}
- versionNumber: number
- taskId: string
- topologyJson: object
- createdAt: timestamp

# Подколлекция устройств в версии топологии
users/{userId}/topologies/{topologyId}/versions/{versionId}/devices/{deviceId}
- name: string
- deviceType: string
- x: number
- y: number
- isActive: boolean
- portCount: number
- defaultGateway: string

# Подколлекция интерфейсов устройства
users/{userId}/topologies/{topologyId}/versions/{versionId}/devices/{deviceId}/interfaces/{interfaceId}
- name: string
- macAddress: string
- ipAddress: string
- subnetMask: string
- vlanId: number

# Подколлекция соединений в версии топологии
users/{userId}/topologies/{topologyId}/versions/{versionId}/connections/{connectionId}
- sourceDeviceId: string
- sourceInterfaceId: string
- targetDeviceId: string
- targetInterfaceId: string

# Коллекция ролей пользователей
userRoles/{roleId}
- name: string
- description: string
- permissions: array

# Коллекция определений достижений
achievementDefinitions/{achievementId}
- title: string
- description: string
- iconName: string
- xpReward: number
- isSecret: boolean

# Коллекция модулей курса
modules/{moduleId}
- title: string
- description: string
- order: number

# Подколлекция уроков модуля
modules/{moduleId}/lessons/{lessonId}
- title: string
- description: string
- content: string
- type: string
- order: number
- isPublished: boolean
- createdAt: timestamp
- updatedAt: timestamp
- estimatedMinutes: number

# Подколлекция теоретических материалов урока
modules/{moduleId}/lessons/{lessonId}/theories/{theoryId}
- title: string
- content: string
- order: number

# Подколлекция блоков контента урока
modules/{moduleId}/lessons/{lessonId}/contentBlocks/{blockId}
- type: string
- content: string
- style: string
- caption: string
- resourceName: string
- order: number

# Подколлекция заданий урока
modules/{moduleId}/lessons/{lessonId}/tasks/{taskId}
- title: string
- description: string
- type: string
- isPublished: boolean
- createdAt: timestamp
- updatedAt: timestamp
- xpReward: number

# Подколлекция требований к заданию
modules/{moduleId}/lessons/{lessonId}/tasks/{taskId}/requirements/{requirementId}
- requirementType: string
- deviceType: string
- minCount: number
- maxCount: number
- description: string
- errorMessage: string
- subnet: string
- vlanId: number


## Запуск проекта

### Требования

- Android Studio (рекомендуется актуальная версия)
- Android SDK:
  - `minSdk = 24`
  - `targetSdk = 35`

### Настройка Firebase

Проект использует Firebase (Auth + Firestore). Чтобы собрать и запустить проект локально, нужно подключить свой Firebase-проект.

1. Создай проект в Firebase Console
2. Добавь Android-приложение с package name:

### ER-Диаграмма
<img width="970" height="1182" alt="image" src="https://github.com/user-attachments/assets/2f8393d1-8094-4001-b19a-86eb51e1c5dd" />


```text
com.example.network_base
```

3. Включи Email/Password Authentication и Firestore
4. Подключи конфигурацию Firebase для Android-приложения (генерируется в Firebase Console)

### Сборка

```bash
./gradlew assembleDebug
```

## APK

- **Готовые сборки**: см. раздел **Releases** в репозитории
- **Собрать самому**:

```bash
./gradlew assembleDebug
```

Файл APК появляется в:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Примечания

- Проект использует DayNight-тему и хранит выбор темы локально.
- Для гостевого режима прогресс/данные могут храниться локально; при входе возможно перенесение прогресса (зависит от сценария).
