## Краткое описание

Это приложение было разработано в качестве димпломного проекта. Приложение сделано по типу социальной сети.

## Стек
* MVVM
* Fragments
* Material Design
* Coroutines
* Room
* Retrofit
* LiveData, Flow
* Paging
* Workmanager
* Hilt
* ImagePicker, Glide, Intent

## Возможности приложения
Для входа в приложение вам необходимо аутетифицироваться. Вы можете зайти под тестовым пользователем:
__Логин__ 111111
__Пароль__ 111111

![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/signIn.jpg)

 Так же Вы можете зарегистрировать нового пользователя с добавлением аватара или без него. После регистрации вам необходимо пройти процедуру аутентификации.

 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/transitionToRegistration.png)
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/signUp.jpg)


 После аутентификации вам станут доступны: 
 1. Лента постов.
 2. Лента событий.
 3. Список существующих пользователей.
 4. Ваш профиль.

 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/posts.jpg)
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/events.jpg)
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/users.jpg)
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/myProfile.jpg)

__Лента постов__

Вы можете просмотреть все посты пользователей, в том числе и свои. 
 - Используйте меню, чтобы удалить и отредактировать свой пост или скройте чужой пост.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/myMenuPost.jpg)
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/ownerMenuPost.jpg)
 - Проставляйте лайки на постах, которые вам понравились. Под постом вы увидите первых двух пользователей, которые оценили пост. Нажмите на список, чтобы просмотреть всех пользователей, которым понравился пост.
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/likePost.jpg)
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/listLike.jpg)
 - Просматривайте отмеченных менторов в постах. Нажмите на список, чтобы просмотреть всех отмеченных пользователей.
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/mentors.jpg)
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/listMentors.jpg)
 - Просматривайте координаты, оставленные в посте в "Яндекс. Карты". Нажмите на иконку, чтобы просмотреть.
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/CoordPost.jpg)
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/mapPost.jpg)
 - Вы можете репостнуть пост себе на стену или отправить его другу с помощью другого приложения.
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/repostAndShare.jpg)
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/share.jpg)
 - Ознакамливайтесь с рекламой прямо в ленте постов.
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/ad.jpg)

 __Лента событий__

 Вы можете просмотреть все события пользователей, в том числе и свои. Совершайте те же действия в событиях, как и в постах. А еще:
 - Нажмитие на иконки форматов событий и перейдите на сайт встречи или просмотрите координаты.
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/coordAndWebEvent.jpg)
 - Нажмите на иконку, чтобы присоединиться к событию. Под событием вы увидите первых двух пользователей, которые будут участвовать в мероприятии. Нажмите на список, чтобы просмотреть всех участников.
 ![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/joinEvent.jpg)
 
 __Список пользователей__

Просматривайте список пользователей и переходите к ним в профиль.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/users.jpg)
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/userProfile.jpg)

__Мой профиль__

- Просмотрите список работ и свои посты
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/profileSections.jpg)
- Создавайте новое: Место работы, Событие, Пост.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/new.jpg)
- При необходимости выйдите из профиля.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/exit.jpg)

__Создание работы__

- Заполните поля.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/newJob.jpg)
- Указывая период работы, нажмите на поле и выберите дату в календаре.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/calendar.jpg)
- Если вы укажите некорректную ссылку в поле "Сайт", приложение сообщит об этом.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/linkErrorJob.jpg)

__Создание события__

- Заполните поля.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/newEvent.jpg)
- Указывая дату и время, нажмите на поле и выберите необходимое в календаре и часах.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/time.jpg)
- Выберите формат из списка. В зависимости от варианта, укажите ссылку на событие или координаты на карте.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/formatEvent.jpg)
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/coordEvent.jpg)
- Для добавления координатов нажмите на место в картах, а затем на знак плюс или добавьте место длительным нажатием на место. Если вам необходимо найти свое место положение, надмите на кнопку "моя локация"
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/mapAddPlace.jpg)
- Укажите спикеров. Нажмите на поле и установите флажок на юзерах.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/listSpeakers.jpg)
- Добавьте вложение.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/attachmentChooser.jpg)
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/imageChooser.jpg)
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/videoChooser.jpg)
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/attachmentVideo.jpg)
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/attachmentAudio.jpg)
- Если обязательные поля не заполненные, приложение сообщит об этом при сохранении.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/errorEvent.jpg)

__Создание поста__

Напиши о том, что для вас важно. Вы можете сделать вложение (фото, аудио, видео), оставить координаты и отметить людей.
![Image alt](https://github.com/ERokhmanko/Diploma/blob/master/screenshots/newPost.jpg)


