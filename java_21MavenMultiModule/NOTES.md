# Session 21: Multi-Module Projects and Dependency Management

## Problem 1 (Simple)

### 3. & 5. Build Execution and Reactor Order
Running `mvn clean package` from the `problem1/` root produced:

```text
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] problem1-parent                                                    [pom]
[INFO] lib                                                                [jar]
[INFO] app                                                                [jar]
[INFO] 
[INFO] ----------------< com.arbiter.learning:problem1-parent >----------------

```

And closed with the exact block:

```text
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Summary for problem1-parent 1.0-SNAPSHOT:
[INFO] 
[INFO] problem1-parent .................................... SUCCESS [  0.134 s]
[INFO] lib ................................................ SUCCESS [  0.875 s]
[INFO] app ................................................ SUCCESS [  0.231 s]
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------

```

**Why the build order differs from `<modules>` order:**
Even though `app` was listed *before* `lib` in the parent's `<modules>` section, Maven identifies the inter-module dependency where `app` depends on `lib`. Maven constructs a directed acyclic graph (DAG) of the reactor project and performs a topological sort. Since `app` requires `lib` to compile, `lib` is pushed earlier in the reactor build order. The `<modules>` list is merely a tie-breaker for completely unrelated siblings.

### 4. Running Main via CLI

```bash
$ java -cp "app/target/classes:lib/target/classes" com.arbiter.learning.app.Main
multi-module works

```

---

## Problem 2 (Hard)

### (a) Central Management

**Inter-module versioning decision:**
For inter-module dependencies (`app` depending on `service`), I used the inherited `${project.version}` property. Internal modules are released in lockstep with the exact same version, so using the built-in dynamic property prevents having to redundantly list every internal module inside `<dependencyManagement>`.

**Plugin Management vs Plugins:**
I pinned all default lifecycle plugins (clean, compiler, resources, surefire, jar, dependency) in the parent's `<pluginManagement>`. This sets the version to be used *if* the plugin binds to a phase, but does not force the plugin into the active build cycle of every child module. Had we placed them directly in `<plugins>`, Maven would forcibly apply and run the plugin on every child module whether needed or not.

### (b) The Version Conflict

**1. Initial `dependency:tree -Dverbose` for `app**`

```text
[INFO] --- maven-dependency-plugin:3.6.1:tree (default-cli) @ app ---
[INFO] com.arbiter.learning2:app:jar:1.0-SNAPSHOT
[INFO] \- com.arbiter.learning2:service:jar:1.0-SNAPSHOT:compile
[INFO]    +- com.arbiter.learning2:core:jar:1.0-SNAPSHOT:compile
[INFO]    |  \- org.apache.commons:commons-compress:jar:1.26.0:compile (version managed from 1.26.0)
[INFO]    |     +- commons-io:commons-io:jar:2.15.1:compile
[INFO]    |     \- (org.apache.commons:commons-lang3:jar:3.14.0:compile - omitted for conflict with 3.13.0)
[INFO]    \- org.apache.commons:commons-text:jar:1.11.0:compile (version managed from 1.11.0)
[INFO]       \- org.apache.commons:commons-lang3:jar:3.13.0:compile

```

* **Resolved version:** `3.13.0`
* **Omitted candidate:** `3.14.0`
* **Path Depths:**
* Path to `3.14.0` (`app` -> `service` -> `core` -> `commons-compress` -> `commons-lang3`) = **Depth 4**
* Path to `3.13.0` (`app` -> `service` -> `commons-text` -> `commons-lang3`) = **Depth 3**



**2 & 3. Resolution (Aiming for 3.14.0)**

**Way 1: `dependencyManagement` in Parent POM**

```diff
--- problem2/pom.xml
+++ problem2/pom.xml
@@ -34,6 +34,11 @@
                 <groupId>org.apache.commons</groupId>
                 <artifactId>commons-text</artifactId>
                 <version>1.11.0</version>
             </dependency>
+            <dependency>
+                <groupId>org.apache.commons</groupId>
+                <artifactId>commons-lang3</artifactId>
+                <version>3.14.0</version>
+            </dependency>

```

*Way 1 Tree Output:*

```text
[INFO] --- maven-dependency-plugin:3.6.1:tree (default-cli) @ app ---
[INFO] com.arbiter.learning2:app:jar:1.0-SNAPSHOT
[INFO] \- com.arbiter.learning2:service:jar:1.0-SNAPSHOT:compile
[INFO]    +- com.arbiter.learning2:core:jar:1.0-SNAPSHOT:compile
[INFO]    |  \- org.apache.commons:commons-compress:jar:1.26.0:compile (version managed from 1.26.0)
[INFO]    |     +- commons-io:commons-io:jar:2.15.1:compile
[INFO]    |     \- (org.apache.commons:commons-lang3:jar:3.14.0:compile - version managed from 3.14.0; omitted for duplicate)
[INFO]    \- org.apache.commons:commons-text:jar:1.11.0:compile (version managed from 1.11.0)
[INFO]       \- org.apache.commons:commons-lang3:jar:3.14.0:compile (version managed from 3.13.0)

```

*Notice what happened:* Management rewrites the version on every branch to `3.14.0`, destroying the version conflict entirely. Because Maven evaluates the shortest path first, it resolves the depth 3 `commons-text` branch, and the depth 4 `commons-compress` branch correctly registers as `omitted for duplicate`.

**Way 2: `<exclusion>` in a Child POM**
I placed the exclusion in `service/pom.xml` on the `commons-text` dependency. This intentionally breaks the Depth 3 path, leaving the Depth 4 path (3.14.0) to natively resolve.

```diff
--- problem2/service/pom.xml
+++ problem2/service/pom.xml
@@ -20,6 +20,12 @@
         <dependency>
             <groupId>org.apache.commons</groupId>
             <artifactId>commons-text</artifactId>
+            <exclusions>
+                <exclusion>
+                    <groupId>org.apache.commons</groupId>
+                    <artifactId>commons-lang3</artifactId>
+                </exclusion>
+            </exclusions>
         </dependency>

```

*Way 2 Tree Output:*

```text
[INFO] --- maven-dependency-plugin:3.6.1:tree (default-cli) @ app ---
[INFO] com.arbiter.learning2:app:jar:1.0-SNAPSHOT
[INFO] \- com.arbiter.learning2:service:jar:1.0-SNAPSHOT:compile
[INFO]    +- com.arbiter.learning2:core:jar:1.0-SNAPSHOT:compile
[INFO]    |  \- org.apache.commons:commons-compress:jar:1.26.0:compile (version managed from 1.26.0)
[INFO]    |     +- commons-io:commons-io:jar:2.15.1:compile
[INFO]    |     \- org.apache.commons:commons-lang3:jar:3.14.0:compile
[INFO]    \- org.apache.commons:commons-text:jar:1.11.0:compile (version managed from 1.11.0)

```

**5. Why the initial resolution mattered:**
By defaulting to the older `3.13.0` due to depth, our build silently dropped `3.14.0`. If `commons-compress:1.26.0` internally relied on newly introduced bug fixes or APIs in `3.14.0`, attempting to execute those code paths would crash at runtime with a `NoSuchMethodError`, without any compilation warnings to warn us.

### (c) Reactor vs. Local Repository

**1. Proof of absence in `~/.m2`:**

```bash
$ ls -la ~/.m2/repository/com/arbiter/learning2
ls: cannot access '/home/user1/.m2/repository/com/arbiter/learning2': No such file or directory

```

**2. Failing execution with `-pl`:**

```bash
$ mvn -pl app package
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Building app 1.0-SNAPSHOT
[INFO] --------------------------------[ jar ]---------------------------------
[WARNING] The POM for com.arbiter.learning2:service:jar:1.0-SNAPSHOT is missing, no dependency information available
[INFO] ------------------------------------------------------------------------
[INFO] BUILD FAILURE
[INFO] ------------------------------------------------------------------------
[ERROR] Failed to execute goal on project app: Could not resolve dependencies for project com.arbiter.learning2:app:jar:1.0-SNAPSHOT: Could not find artifact com.arbiter.learning2:service:jar:1.0-SNAPSHOT -> [Help 1]

```

**Why it fails:** `-pl app` instructs Maven to *only* build the `app` module. Since `service` isn't included in this localized reactor run, Maven attempts to resolve it by looking in the local `.m2` repository (and ultimately remote repos). Because we haven't run `mvn install`, `service` doesn't exist there, and resolution crashes.

**3. Fixing with `-am`:**

```bash
$ mvn -pl app -am package
[INFO] Scanning for projects...
[INFO] ------------------------------------------------------------------------
[INFO] Reactor Build Order:
[INFO] 
[INFO] problem2-parent                                                    [pom]
[INFO] core                                                               [jar]
[INFO] service                                                            [jar]
[INFO] app                                                                [jar]

```

**Built modules selected explicitly:** `app`.
**Built due to `-am` addition:** `problem2-parent`, `core`, `service`.

**4. Short Comparison:**
In the failing run (`-pl`), `app` attempted to look up `service` in `~/.m2` (the local repo). In the passing run (`-am`), `app` resolved `service` natively from the Reactor's active build process. Specifically, because we ran the `package` phase, the reactor supplied `service/target/service-1.0-SNAPSHOT.jar` directly to `app`'s classpath without touching `.m2`.
If we had run `mvn install` globally first, the failing run would have passed because `service` would have been copied to `~/.m2`. The negative side effect of relying on that is polluting your local repo with unstable `SNAPSHOT` artifacts that mask build isolation issues. *(Note: I did not leave artifacts installed).*

### (d) Final Run

Executing Main by explicitly pointing the classpath sequentially to our internally packaged JARs and the resolved third-party JAR paths from the local repository:

```bash
$ java -cp "app/target/app-1.0-SNAPSHOT.jar:service/target/service-1.0-SNAPSHOT.jar:core/target/core-1.0-SNAPSHOT.jar:$HOME/.m2/repository/org/apache/commons/commons-text/1.11.0/commons-text-1.11.0.jar:$HOME/.m2/repository/org/apache/commons/commons-compress/1.26.0/commons-compress-1.26.0.jar:$HOME/.m2/repository/commons-io/commons-io/2.15.1/commons-io-2.15.1.jar:$HOME/.m2/repository/org/apache/commons/commons-lang3/3.14.0/commons-lang3-3.14.0.jar" com.arbiter.learning2.app.Main

HELLO MULTI MODULE!
Hello Multi Module
4
olleH

```

**Classpath Note:** `commons-lang3-3.14.0.jar` was correctly utilized here, directly mirroring the finalized dependency tree.

---

### Final Concept Summaries

**1. Nearest-wins and the first-declared tiebreak:**
When two conflicting versions of a transitive dependency arrive, Maven calculates the hop count ("depth") from the current module down to the transitive dependency. The version with the shortest path natively wins. If there is an exact tie in depth, Maven resolves the conflict strictly based on the order dependencies are written in the POM (`first-declared wins`).

**2. `dependencyManagement` vs `dependencies`:**
`<dependencies>` actively adds a library to a module's classpath. `<dependencyManagement>` adds *nothing* to the classpath; it acts exclusively as a lookup table. If a child defines a dependency without a version, Maven pulls the version from the parent's `dependencyManagement`. If the parent manages a dependency but the child *never* mentions it in its local `<dependencies>`, that jar is never downloaded nor placed on the child's classpath.

**3. Reactor order vs `<modules>` order:**
The `<modules>` block simply tells Maven which subdirectories it should ingest into the build process. Maven generates its Reactor Build Order independently by mapping out inter-module `<dependencies>` into a graph and sorting it topologically. Therefore, swapping the string order of modules in the text of the `<modules>` list does not alter the compilation sequence; Maven dictates the order solely based on dependencies.
