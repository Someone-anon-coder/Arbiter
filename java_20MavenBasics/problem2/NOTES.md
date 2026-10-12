### (a) Make java -jar work

Chosen approach: **Fat (uber) jar using maven-shade-plugin**.
I chose this over `maven-dependency-plugin` because it yields a single transportable artifact that works safely from any current working directory (such as running it from the repo root), without relying on relative external folders.

Command used: `java -jar java_20MavenBasics/problem2/target/maven-basics-p2-1.0.0.jar`

**Manifest extracted from the built jar (`unzip -p target/maven-basics-p2-1.0.0.jar META-INF/MANIFEST.MF`):**
```text
Manifest-Version: 1.0
Created-By: Maven JAR Plugin 3.4.1
Build-Jdk-Spec: 21
Main-Class: com.arbiter.learning.textkit.App

```

### (b) Demonstrate the failure first

**Command & Actual Error Output:**

```text
$ java -jar target/original-maven-basics-p2-1.0.0.jar
Exception in thread "main" java.lang.NoClassDefFoundError: org/apache/commons/lang3/StringUtils
	at com.arbiter.learning.textkit.App.main(App.java:12)
Caused by: java.lang.ClassNotFoundException: org.apache.commons.lang3.StringUtils
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:641)
	at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:188)
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:526)
	... 1 more

```

**1. What did your pom have at this stage vs. the final version?**
The POM lacked the `maven-shade-plugin` execution block entirely. It possessed only the standard `maven-jar-plugin` configuration that wrote the `Main-Class` entry to the manifest.

**2. Why did mvn package succeed even though the jar can't run?**
Maven manages the compile and build lifecycles, not runtime packaging by default. The compilation succeeded because Maven successfully resolved `commons-lang3` from `~/.m2/repository` and placed it on `javac`'s compile classpath. `javac` wrote symbolic references to `StringUtils` in the `.class` files, leaving runtime classpath management as a separate concern.

**3. Did any line of the program's output print before the error?**
No. The code only loops and prints. The array initialization has no side effects, and the first print statement is inside a conditional branch executed *after* `StringUtils.isBlank(input)` evaluates. The JVM attempts to lazily load and link `StringUtils` at the exact moment it encounters `isBlank(input)` on line 12. Since it's missing from the thin jar's classpath, it crashes before `System.out.println` runs.

### (c) Dependency Tree

**Command Output (`mvn dependency:tree`):**

```text
[INFO] com.arbiter.learning:maven-basics-p2:jar:1.0.0
[INFO] \- org.apache.commons:commons-lang3:jar:3.14.0:compile

```

**Resolved Dependencies & Scopes:**
Only `org.apache.commons:commons-lang3` was resolved.
Its scope is `compile` (the default).
Are there transitive dependencies? **No**. Commons Lang3 has no internal dependencies of its own, so the tree stops at depth 1.

### (d) Lifecycle in the real log

**Execution Order and Headers:**

1. `--- maven-clean-plugin:2.5:clean (default-clean) @ maven-basics-p2 ---`
* Lifecycle: `clean` | Phase: `clean` | Source: default bindings


2. `--- maven-resources-plugin:2.6:resources (default-resources) @ maven-basics-p2 ---`
* Lifecycle: `default` | Phase: `process-resources` | Source: default bindings


3. `--- maven-compiler-plugin:3.13.0:compile (default-compile) @ maven-basics-p2 ---`
* Lifecycle: `default` | Phase: `compile` | Source: default bindings (configured version)


4. `--- maven-resources-plugin:2.6:testResources (default-testResources) @ maven-basics-p2 ---`
* Lifecycle: `default` | Phase: `process-test-resources` | Source: default bindings


5. `--- maven-compiler-plugin:3.13.0:testCompile (default-testCompile) @ maven-basics-p2 ---`
* Lifecycle: `default` | Phase: `test-compile` | Source: default bindings (configured version)


6. `--- maven-surefire-plugin:2.12.4:test (default-test) @ maven-basics-p2 ---`
* Lifecycle: `default` | Phase: `test` | Source: default bindings


7. `--- maven-jar-plugin:3.4.1:jar (default-jar) @ maven-basics-p2 ---`
* Lifecycle: `default` | Phase: `package` | Source: default bindings (configured version)


8. `--- maven-shade-plugin:3.6.0:shade (default) @ maven-basics-p2 ---`
* Lifecycle: `default` | Phase: `package` | Source: added execution



**What would not have run if using `mvn compile`?**
Everything from step 4 onwards would be skipped because `mvn compile` stops executing the default lifecycle at the `compile` phase. Additionally, step 1 (`clean:clean`) would NOT run, because calling `compile` only triggers the `default` lifecycle, completely ignoring the `clean` lifecycle.

### (e) Wrong scope breaks the compile

**Error lines:**

```text
[ERROR] COMPILATION ERROR : 
[ERROR] /java_20MavenBasics/problem2/src/main/java/com/arbiter/learning/textkit/App.java:[2,32] package org.apache.commons.lang3 does not exist
[ERROR] /java_20MavenBasics/problem2/src/main/java/com/arbiter/learning/textkit/App.java:[12,17] cannot find symbol
  symbol:   variable StringUtils
  location: class com.arbiter.learning.textkit.App
[ERROR] /java_20MavenBasics/problem2/src/main/java/com/arbiter/learning/textkit/App.java:[16,34] cannot find symbol
  symbol:   variable StringUtils
  location: class com.arbiter.learning.textkit.App
[ERROR] /java_20MavenBasics/problem2/src/main/java/com/arbiter/learning/textkit/App.java:[18,36] cannot find symbol
  symbol:   variable StringUtils
  location: class com.arbiter.learning.textkit.App
[ERROR] /java_20MavenBasics/problem2/src/main/java/com/arbiter/learning/textkit/App.java:[19,38] cannot find symbol
  symbol:   variable StringUtils
  location: class com.arbiter.learning.textkit.App
[INFO] 5 errors 

```

**Phase that failed:** `compile`
**Plugin goal that failed:** `maven-compiler-plugin:compile`
**Explanation:**
Setting the scope to `test` places the `commons-lang3` jar onto the *test-compile* and *test-runtime* classpaths only. The `maven-compiler-plugin:compile` goal compiles `src/main/java` using the *main* compile classpath. Since the dependency is restricted to testing scopes, `javac` cannot find the `StringUtils` class for the main application source.

### (f) Predict, then verify

**Prediction:**

1. `mvn clean package` WILL succeed. The `provided` scope puts the dependency on the main compile classpath, so `javac` is happy.
2. `java -jar` WILL FAIL. The shade plugin specifically excludes `provided` dependencies when building the fat jar, under the assumption that the deployment environment provides those classes.

**Actual result:**

1. The build succeeded. However, unlike the `compile` scope where the shade plugin explicitly prints `Including org.apache.commons:commons-lang3:jar:3.14.0 in the shaded jar.`, the plugin simply silently ignores the `provided` dependency and prints nothing about it.
2. `java -jar target/maven-basics-p2-1.0.0.jar` failed with `java.lang.NoClassDefFoundError: org/apache/commons/lang3/StringUtils`.

---

### Questions on the plugins you configured

**1. maven-compiler-plugin: what does pinning 3.13.0 change, given that you added no `<configuration>`?**
It dictates whether Maven respects the `maven.compiler.release` property. Maven 3.8.7's built-in default is `maven-compiler-plugin` version `3.1`. Version `3.1` is too old to understand the `release` property, silently ignores it, and uses ancient Java default targets. Pinning to `3.13.0` ensures the plugin actually understands Java 21 and the `release` property.

**2. maven-jar-plugin: Is the jar-plugin entry still needed in the final configuration? What would break in part (b) if it were missing?**
No, it is no longer strictly needed in the *final* fat-jar configuration, because the `maven-shade-plugin`'s `ManifestResourceTransformer` is entirely responsible for writing the `Main-Class` attribute into the uber jar's manifest. However, if we didn't have the jar-plugin writing it in part (b), `java -jar` against the original thin jar would have failed immediately with `"no main manifest attribute"` instead of successfully launching and crashing with `NoClassDefFoundError`, breaking our ability to demonstrate the runtime classpath failure.

**3. maven-shade-plugin: why does it need an `<execution>` block when the compiler and jar plugins didn't?**
The compiler and jar plugins are natively bound to the default lifecycle (to the `compile` and `package` phases, respectively) when a project specifies `<packaging>jar</packaging>`. Because they are already scheduled to run, we only need to provide their versions or configurations. The `maven-shade-plugin` has no default bindings in the standard Maven lifecycle, so we must explicitly attach its `shade` goal to the `package` phase via an `<execution>` block.

**4. Build warnings: why does the MANIFEST.MF overlap happen, and why is it harmless here?**
It happens because both the thin project jar being shaded (which contains a `MANIFEST.MF`) and the unpacked `commons-lang3.jar` dependency (which also contains its own `MANIFEST.MF`) are being merged into the same output destination. It is harmless because the `ManifestResourceTransformer` we configured intercepts this exact file, discards the colliding ones, and generates a fresh, valid manifest containing our `Main-Class` entry for the final fat jar.
