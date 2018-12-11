# сontra cqrs
Проект реализует тестовое задание 2Gis.

## Что нужно, чтобы запустить проект?
Только постгрес и jre. Ну и надо клонировать проект, конечно.

## Как запускать?

### создать базу данных в постгресе.
Например, как-то так:
```bash
export DBNAME=contra_cqrs; \
(echo "DO \$body\$ BEGIN CREATE ROLE $DBNAME LOGIN PASSWORD '';\
EXCEPTION WHEN others THEN RAISE NOTICE 'Role $DBNAME exists, skipping recreation';\
END \$body\$; \
drop database IF EXISTS $DBNAME; \
create database $DBNAME with template template1 owner $DBNAME;" | psql -U postgres)
```
### заполнить БД таблицами
Для этого в корне проекта
```bash
cp gradle.properties.example gradle.properties
```
а затем отредактировать в gradle.properties явки/пароли для связи с БД на правах 
её хозяина. 
(Не забудьте раскомментировать отредактированное)
После чего:
```bash
./gradlew targetDb update
```
создаём структуры данных в БД
### настроить соединение с БД для автотестов или автономной работы
```bash
cp \
common/src/main/resources/contra.properties.example \
common/src/main/resources/contra.properties
```
и снова редактируем настройки связи с БД и где запускать сервис.

### протестировать работу сервиса автотестами

```bash
./gradlew test
```

### запустить сервис в автономном режиме
Заполняем БД тестовыми данными:
```bash
./gradlew :rest:test
```

После этого выполняем:
```bash
./gradlew run
```
дальше сервис можно тестировать вручную. Например, так:
```bash
curl -H "Content-Type: application/json" 127.0.0.1:8080/movie/all
```
или так:
```bash
curl -H "Content-Type: application/json" 127.0.0.1:8080/show/1/1/2018-01-01T00:00/2030-12-31T23:59
```
другие примеры запросов к сервису можно увидеть в `contra/res/integration.kt`
