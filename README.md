1. Build & Run
```console
./run.sh
```

2. Sanity check
```console
curl http://localhost:9000/dump
[{"account":1,"balance":10000},{"account":2,"balance":10000},{"account":3,"balance":0}]
```

```console
curl -d"accfrom=1&accto=3&amt=5000" http://localhost:9000/xfer
{"res":"ok"}
```

```console
curl http://localhost:9000/dump
[{"account":1,"balance":5000},{"account":2,"balance":10000},{"account":3,"balance":5000}]
```

3. Deadlock test
```console
wrk -s post_1_to_2.lua http://localhost:9000/xfer & \
wrk -s post_2_to_1.lua http://localhost:9000/xfer ; \
wait
```
where
```lua
-- post_1_to_2.lua
wrk.method = "POST"
wrk.body   = "accfrom=1&accto=2&amt=1"
wrk.headers["Content-Type"] = "application/x-www-form-urlencoded"

-- post_2_to_1.lua
wrk.method = "POST"
wrk.body   = "accfrom=2&accto=1&amt=1"
wrk.headers["Content-Type"] = "application/x-www-form-urlencoded"
```