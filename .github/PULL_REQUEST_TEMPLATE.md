## Summary

Describe the behavior changed and why.

## Validation

- [ ] `./gradlew clean check build --warning-mode=all` passes on Java 25
- [ ] `find src -type f -name '*.java'` prints nothing
- [ ] New behavior has Kotlin tests
- [ ] Folia internals remain isolated in the metric provider
- [ ] No server, generated, secret, or environment files are included

## Folia compatibility

List the exact Folia 26.2 build tested and any internal API touched.
