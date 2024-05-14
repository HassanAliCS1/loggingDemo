run both demo and x1
Hit http://localhost:8080/ on PostMan 
add a dummy value for app-version in headers 
Observe `traceId`, `spanId`, `dd.trace_id`, `app-version` and `mdcUser` are all populated for request and response logs
