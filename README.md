1. Build & Run
```console
sbt clean test stage && ./run.sh
```

2. Sanity check
```console
curl http://localhost:9000/dump
[{"account":1,"balance":100},{"account":2,"balance":100},{"account":3,"balance":0}]
```

```console
curl -d"accfrom=1&accto=3&amt=50" http://localhost:9000/xfer
{"res":"ok"}
```

```console
curl http://localhost:9000/dump
[{"account":1,"balance":50},{"account":2,"balance":100},{"account":3,"balance":50}]
```
