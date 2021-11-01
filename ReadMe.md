HTTP4S with ZIO

You can execute next commands:
- Search phone
```
curl http://127.0.0.1:8080/phoneBook/4567
```
- Add a new phone:
```
curl -X POST http://127.0.0.1:8080/phoneBook \
-d '{"fio": "Alex Galin", "phone": "123456"}'
```
- Update existed phone:
```
curl -X PUT http://127.0.0.1:8080/phoneBook/123456 \
-d '{"fio": "Alex", "phone": "123456"}'
```
Delete existed phone:
```
curl -X DELETE http://127.0.0.1:8080/phoneBook/2
```