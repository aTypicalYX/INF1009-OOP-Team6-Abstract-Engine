# AbstractEngine

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a template including simple application launchers and an `ApplicationAdapter` extension that draws libGDX logo.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## How to set up

This project was created and is designed to run using VSCode. As it uses the LibGDX and Gradle libraries, and is also written in Java, you will need to first ensure that you have these extensions downloaded for VSCode:


- Gradle for Java: https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-gradle
- Extension Pack for Java: https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack

It is recommended to follow the instructions in this website link to set up VSCode to run Java files first: https://code.visualstudio.com/docs/java/java-tutorial

1. After doing all that, you can then proceed to clone the repository to an empty folder on your local machine.
2. Then, open VSCode on that folder.  Ensure you have opened the ROOT folder (the one containing build.gradle), not the core folder
3. Watch the bottom middle to right status bar. Wait until it says "Java: Ready". (This can take 2-5 minutes the first time).
4. Run: Click the Elephant Icon (left sidebar) → lwjgl3 → Tasks → application → run.
