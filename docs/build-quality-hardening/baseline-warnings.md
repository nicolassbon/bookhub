# Build Quality Hardening — Baseline Warnings

This fixture captures the warning strings that were present before the hardening changes.

## Compiler plugin warning (to eliminate)

```text
Parameter 'testAnnotationProcessorPaths' is unknown for plugin 'maven-compiler-plugin:3.14.0:compile (default-compile)'
Parameter 'testAnnotationProcessorPaths' is unknown for plugin 'maven-compiler-plugin:3.14.0:testCompile (default-testCompile)'
```

## JDK 21 dynamic agent warning (to mitigate)

```text
WARNING: A Java agent has been loaded dynamically
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: Dynamic loading of agents will be disallowed by default in a future release
Mockito is currently self-attaching to enable the inline-mock-maker.
```
