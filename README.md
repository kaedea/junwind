# junwind
Get Java thread's stacktrace by native tid.


## Getting Started

```java
// 1. Get current thread's stacktrace.
String callStack = JUnwind.jUnwindCurr();

// 2. Get other thread's stacktrace by tid.
int tid = ...;
String callStack = JUnwind.jUnwind(tid);
```
