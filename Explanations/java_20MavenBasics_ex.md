# java_20MavenBasics — Maven fundamentals: layout, pom, lifecycle, dependencies, thin vs. fat jars

## Pinning plugin versions is what makes `maven.compiler.release` work
```xml
<maven.compiler.release>21</maven.compiler.release>
...
<artifactId>maven-compiler-plugin</artifactId>
<version>3.13.0</version>
```
The property does nothing by itself. The compiler plugin reads it, and only versions 3.6 and later understand `release`. The installed Maven (3.8.7) binds `maven-compiler-plugin:3.1` by default, which ignores the property and falls back to an obsolete source/target level that JDK 21 rejects. Pinning the version needs no `<configuration>`, because the default `compile` binding already exists and the pin only changes which plugin version that binding runs. `javap -v` showing major version 65 (Java 21) confirms the property took effect.

## Phases vs. goals, as observed in the real log
A phase is only a slot in the lifecycle; the work comes from the plugin goals bound to it. Phases don't appear in the log. Each `--- plugin:version:goal (execution-id) ---` header is a goal execution, and an id of `default-*` means the `jar` packaging bound it. Goals you bind yourself in the pom get the id you chose; shade's execution had no `<id>`, so Maven called it `default`. The real `mvn clean package` run for problem 2 on Maven 3.8.7:

| Lifecycle | Phase | Plugin goal observed | Bound by |
|---|---|---|---|
| clean | `clean` | `maven-clean-plugin:2.5:clean (default-clean)` | default |
| default | `process-resources` | `maven-resources-plugin:2.6:resources (default-resources)` | `jar` packaging |
| default | `compile` | `maven-compiler-plugin:3.13.0:compile (default-compile)` | `jar` packaging (version pinned in pom) |
| default | `process-test-resources` | `maven-resources-plugin:2.6:testResources (default-testResources)` | `jar` packaging |
| default | `test-compile` | `maven-compiler-plugin:3.13.0:testCompile (default-testCompile)` | `jar` packaging |
| default | `test` | `maven-surefire-plugin:2.12.4:test (default-test)`, prints "No tests to run." | `jar` packaging |
| default | `package` | `maven-jar-plugin:3.4.1:jar (default-jar)` | `jar` packaging (manifest configured in pom) |
| default | `package` | `maven-shade-plugin:3.6.0:shade (default)` | `<execution>` added in pom |

`mvn compile` would run only the first three `default`-lifecycle rows, and **not** `clean:clean`, because `clean` is a separate lifecycle that runs only when you invoke it.

## Why the thin jar failed and the shaded one works
The jar plugin packages only `target/classes`. Commons Lang3 was on the *compile* classpath (from `~/.m2`), so `javac` succeeded and wrote a symbolic reference to `StringUtils` into `App.class`. `java -jar` uses only the jar plus its manifest `Class-Path`, so at runtime the class was nowhere. Classes load lazily, so `main` started and the first `StringUtils.isBlank` call threw `NoClassDefFoundError`. Nothing printed first, because every print comes after that call. That error means "present at compile time, missing at run time," which is different from `ClassNotFoundException` from a `Class.forName` lookup. Shade fixed it by merging the dependency's classes into the jar. Its `ManifestResourceTransformer` sets `Main-Class` on the manifest it keeps (the project's own; `Created-By: Maven JAR Plugin` survives in the final manifest). That's also why the overlapping `META-INF/MANIFEST.MF` warning is harmless here. The jar plugin's `mainClass` is redundant for the final fat jar, but it was needed to get the thin jar to *start* in the failure demonstration. Without it, `java -jar` stops at "no main manifest attribute."

## Scopes, tested rather than recited
| Scope | Main compile | Shaded into jar | Observed |
|---|---|---|---|
| `compile` | ✅ | ✅ | Builds and runs |
| `test` | ❌ | ❌ | Fails in `compile` phase, `compiler:compile`: "package org.apache.commons.lang3 does not exist", 5 errors |
| `provided` | ✅ | ❌ (silently, no "Including" line) | Builds; `java -jar` throws `NoClassDefFoundError` |

## Repo hygiene
`target/` is in `.gitignore`. Shade's `dependency-reduced-pom.xml` is written to the **project root**, outside `target/`, so the `target/` rule doesn't catch it. It was switched off with `<createDependencyReducedPom>false</createDependencyReducedPom>`, since nothing consumes this jar as a dependency.
