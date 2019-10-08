# First Project - GVRP

This is the version of the same project but in *Java* for portability reasons and due to the lack of useful data structures in the *C* standard library like in the Java standard library. Also, Java offers an almost native testing environment (JUnit), which C does not offer (and the reason for the creation of the LWCTL).

## Running the application :gear:

Simply run `java -jar gvrp.jar <args...>` on your preffered terminal. The latest .JAR executable can be downloaded in the GitHub repository page. It is recommended to place the .JAR in the project root (where this very file is located at), since some default paths are relative to this root.

You can also parse a file with the arguments by running `java -jar gvrp.jar @args.txt`. Beware that in these files, arguments are separater by a new line! Thankfully there is already one of those files as a demo in the project directory.

**Disclaimer:** Using @filepath on Powershell will not work because of the tokenization of @ in the Powershell lex. Use the escape character tick (\`) before the @.

## Command line arguments :scroll:

To list all valid arguments, type `java -jar gvrp.jar -help`. Here are some of the main commands.

### Manual/Automatic

The executable jar offers many useful flags for command line tasks. You can run the application **manually**, by selecting the instance file from a *JFileChooser* or **automatically**, by providing a file with all the instance file paths. If `all.txt` is the file with the instance file names, and `data/GVRP` the folder where these files are located, then the following command runs for each instance automatically.

``` bash
java -jar gvrp.jar -idir data/GVRP3 -ifile all.txt [-mode auto]
```

If you want to select one instance file, manually, then, use the following arguments:

``` bash
java -jar gvrp.jar -mode manual [-idir default_path]
```

### CSV

In order to generate a .csv file with informations about the the simulation, add the `-csv` flag and, if necessary, define the CSV folder with the `-csvdir` flag.

``` bash
java -jar gvrp.jar -csv [-csvdir data/results]
```

You can also generate a .csv file with the time step informations, that is, each improvement % to BKS and time (ms). For that, use the `-csvts` flag.

``` bash
java -jar gvrp.jar -csvts [-csvdir data/results]
```

### Verbosity

Use `-verbose` or ,`-v` for short, to display more information.

You can also display detailed information about the solutions/instances:
* `-iinfo` for instance info
* `-isinfo` for initial solution info
* `-fsinfo` for final solution info
* `-dmatrix` for distance matrix
* `-gamma` for gamma set

### Calibration

To fiddle around with parameters, you can define these constants:
* `-gammak` for Gamma set k (size)
* `-perturbation` for ILS perturbation fraction (n/4 would be 0.25)

### Termination criteria

Use `-seconds` for defining maximum number of seconds for each instance until termination, and `-threshold` to define minimum %BKS for termination (in decimal representation, that is, 50% would be 0.5)
