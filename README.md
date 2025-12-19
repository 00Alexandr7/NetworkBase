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

##Коллекции FireStore
admin_task

- title: String
- description: String
- moduleId: String
- createdAt: Timestamp

lessons

- title: String
- content: String
- type: String
- moduleId: String
- order: Number
- isPublished: Boolean
- createdAt: Timestamp
- updatedAt: Timestamp
  
modules

- title: String
- description: String
- order: Number
  
tasks

- title: String
- type: String
- lessonId: String
- isPublished: Boolean
- createdAt: Timestamp
- updatedAt: Timestamp
  
users

- email: String
- name: String
- role: String
- xp: Number
- currentModuleId: String (может быть null)
- createdAt: Timestamp

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
