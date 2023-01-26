## Дипломная работа курса Нетологии "Android-разработчик с нуля"

### Социальная сеть формата LinkedIn.

### Задача
- Ваша ключевая задача - разработать полностью функционирующее приложение, самостоятельно выбрав, какую часть функциональности вы реализуете (можно реализовывать не всё).

### Функционал приложения: 
- Регистрация и аутентификация
- Аутентифицированный пользователь может создавать посты (как с медиа-вложениями, так и без),
редактировать и удалять их, а также лайкать посты других пользователей.
- Реализована возможность добавления событий с дополнительными полями (дата события, тип события (онлайн, офлайн).
- Вожможность просмотра всех зарегистрированных пользователей.
- Реализована логика просмотра профиля каждого пользователя с отображением его мест работ.
- "Проигрывание" медиа-вложений (для изображений - показ, для аудио и видео - воспроизведение).
-  Переход по внешним ссылкам (поля link, если ссылаются на внешние ресурсы).
- Темная и светлая темы

### Скриншоты:

- Экран профиля
![Screenshot1](https://github.com/AnTomS/NeWork/blob/14026b2bca289f00bf10575a0e8248588d2d7181/app/src/main/res/Screenshot__profile.png)

- Экран списка событий 
![Screenshot2](https://github.com/AnTomS/NeWork/blob/14026b2bca289f00bf10575a0e8248588d2d7181/app/src/main/res/Screenshot_events.png)

- Экран списка постов
![Screenshot3](https://github.com/AnTomS/NeWork/blob/14026b2bca289f00bf10575a0e8248588d2d7181/app/src/main/res/Screenshot_post.png)


Экран списка пользователей
![Screenshot4](https://github.com/AnTomS/NeWork/blob/14026b2bca289f00bf10575a0e8248588d2d7181/app/src/main/res/Screenshot_list_users.png)


Экран регистрации
![Screenshot5](https://github.com/AnTomS/NeWork/blob/14026b2bca289f00bf10575a0e8248588d2d7181/app/src/main/res/Screenshot_register.png)

### Реализация приложения
- Kotlin for all code
- Single Activity Application
- Navigation Component for Fragments
- MVVM for presentation layer
- Coroutines for async work
- Dagger 2 (Hilt) for DI
- Glide для загрузки изображений
- Material Components for styling UI components
- okhttp3 для сетевых запросов
- Room для хранения данных в БД