# First Project - GVRP

This is the version of the same project but in *Java* for portability reasons and due to the lack of useful data structures in the *C* standard library like in the Java standard library. Also, Java offers an almost native testing environment (JUnit), which C does not offer (and the reason for the creation of the LWCTL).

## Running the application :gear:

Simply run `java -jar gvrp.jar <args...>` on your preffered terminal. If no .jar is provided, you can open the project in Eclipse and export it as an executable JAR.

## Command line arguments :scroll:

The executable jar offers many useful flags for command line tasks. You can run the application **manually**, by selecting the instance file from a *JFileChooser* or **automatically**, by providing a file with all the instance file paths.

| Flag     | Arguments | Description |
|----------|-----------|-------------|
| -input   | path      | Input file with all instance file paths (auto) |
| -persist |           | Do not stop parsing instance files if an error occurrs (auto) |
| -idir    | path      | Instance files directory (manual) |
