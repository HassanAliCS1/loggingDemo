1. Hit http://localhost:8080/ on PostMan
2. add a dummy value for `app-version` in headers
3. Observe `traceId`, `spanId`, `dd.trace_id`, `app-version` and `mdcUser` are all populated for request and response logs

All works ! :)  


next step: set up trace id for third parties