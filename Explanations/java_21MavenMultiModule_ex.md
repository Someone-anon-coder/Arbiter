# java_21MavenMultiModule — Multi-module builds, the reactor, dependency management, and conflict resolution

## Parent, children, and what is inherited
The parent has `<packaging>pom</packaging>` and lists children in `<modules>`; each child points back with `<parent>`. Children inherit `groupId`, `version`, properties, `<dependencies>`, plugin configuration, and the *contents* of `dependencyManagement`/`pluginManagement`. They do not inherit `artifactId`, `packaging`, or `<modules>`. A plain `<dependencies>` block in the parent would push that dependency into every module, which is why versions go in `dependencyManagement` instead.

## The reactor orders by dependencies, not by `<modules>`
Problem 1 listed `app` before `lib`. The real log still printed `problem1-parent`, `lib`, `app`: Maven topologically sorts the inter-module dependency graph, and `<modules>` only breaks ties between unrelated siblings. `-pl app` selects only `app`; `-am` also builds what it depends on.

## `dependencyManagement` vs `dependencies`; `pluginManagement` vs `plugins`
Management is a lookup table: it puts nothing on any classpath. A module that wants the library declares it **without a version**, and the version comes from the parent. The same split applies to plugins. Pinning in `pluginManagement` fixes the version without forcing the plugin into modules that never use it (lifecycle-bound plugins like compiler and jar still run in every jar module, at the pinned version). The default `maven-dependency-plugin` on Maven 3.8.7 is 2.8, so it was pinned to 3.6.1. A version property is only text substitution. It does not override a transitive version, but management does.

When an artifact is managed, `dependency:tree -Dverbose` annotates it `(version managed from X)`, even when X equals the managed version (e.g. `commons-compress` 1.26.0 and `commons-text` 1.11.0 here). That annotation is evidence the parent's table was consulted.

## Nearest wins, shown on a real conflict
`commons-text` 1.11.0 needs `commons-lang3` 3.13.0. `core`'s `commons-compress` 1.26.0 needs 3.14.0. Seen from `app` (depth 0): lang3 3.13.0 via service → commons-text is **depth 3**, while 3.14.0 via service → core → commons-compress is **depth 4**. The nearer one wins, so Maven resolves the *older* 3.13.0 and prints `omitted for conflict with 3.13.0` against 3.14.0. Nothing warns that `commons-compress` now runs against a library older than the one it declared. Depth ties go to the first declared dependency.

## Dependency tree before and after each resolution (`app`, lang3 lines only)
| State | `service → core → commons-compress → lang3` | `service → commons-text → lang3` | Resolved |
|---|---|---|---|
| Before (lang3 not managed) | `(3.14.0 - omitted for conflict with 3.13.0)` | `3.13.0` | **3.13.0** |
| Way 1: parent manages lang3 at 3.14.0 | `(3.14.0 - version managed from 3.14.0; omitted for duplicate)` | `3.14.0 (version managed from 3.13.0)` | **3.14.0** |
| Way 2: `<exclusion>` of lang3 on commons-text in `service` | `3.14.0` (now the only path) | path removed | **3.14.0** |

Way 1 rewrites the version on *every* path, so the conflict disappears: both paths agree. Way 2 cuts one path, so the other becomes the only candidate. Management is global and explicit. An exclusion is local, and it only works because another path still supplies the library. Excluding from a library that nothing else brings in would leave a missing dependency.

## Reactor vs. local repository
`mvn -pl app package` from a state with no project artifacts in `~/.m2` fails with `Could not find artifact ...:service:jar:1.0-SNAPSHOT`, because `service` is outside the selected reactor and was never installed. `-pl app -am package` builds `problem2-parent`, `core`, `service`, then `app`, and `app` receives `service` from the reactor's `target/`. `mvn install` would also have fixed the first command by copying the artifacts into `~/.m2`, at the cost of leaving stale SNAPSHOTs there that can mask build problems later.
