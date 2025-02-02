### Building

The project can be built by downloading it, navigating into the directory, and running `./gradlew build` or
`gradlew.bat build` on Windows.

The compiled jar be `build/Ion.jar`.

### Testing

This repository includes a script that will set up a functioning system comprising of Ion, IonCore, and its required
dependencies. To use it simply ensure that Docker and Docker Compose are installed and running, and then use
`testServer`. This script is a bash script, using it on Windows will require Windows Subsystem for Linux or Git Bash.

To use the test server run `sh testServer setup` and then start it with `sh testServer run`, if there are any issues try
`sh testServer run-fallback`.

You can also start, view logs, and stop the server independently with `sh testServer start`, `sh testServer logs`,
and `sh testServer stop`.

If the test server breaks, use `sh testServer reset` to reset it back to it's default state.

### Contributing

Contributions must follow the following rules:

1) Lines should be 120 characters long at most, this is not a strict requirement, lines *can* be longer.

2) Never use wildcard imports.

3) Sometimes there can be name conflicts when importing, import them with a custom name, prefixed by the source. For
   example "LibAListener" and "LibBListener".

4) Avoid excessive use of `.apply {}` or similar.

5) Do not statically import individual elements from enums or objects.

6) If there is a large block of mostly similar code, align it with spaces, as it makes things more readable.

7) All event listeners must specify a priority based on the criteria below:
	- Does the listener unconditionally cancel the event? If so, use LOWEST.
	- Does the listener conditionally cancel the event? If so, use LOW.
	- Does the listener alter the events data? If so, use NORMAL.
	- Does the listener simply act on the result of the event? If so, use MONITOR.
	- HIGH and HIGHEST should not be used right now.

8) To prevent IntelliJ from complaining, please `@Suppress("Unused")`for any entry points. Don't just tell IntelliJ to
   ignore them for that class as that only applies to you, not everyone else.