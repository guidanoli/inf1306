# Assignment

Please find enclosed the benchmark instances associated with the project `(GVRP3.zip)`, as well as a paper listing the current "best known solutions" for these instances `(see column "BKS" of Tables 14 to 16)`. Here are some important conventions:

* Euclidean distances rounded to the nearest integer
	* Triangular inequalities are very likely to occur
	* If d < 0.5, rounded d will be 0 (which is weird, but a convention...)
* The depot is represented by node 1 in "NODE_COORD_SECTION", so we have its coordinates
* The number of routes should exactly match the number of vehicles indicated in the field "VEHICLES" (no empty route is allowed)

The deliverables of the project, to be sent by email on the 08/10 at 23:59:59 include

* The detailed results of your method per instance (Solution value, Time, and Gap) in an Excel file
* The slides of your presentation
* Your source code (Python, Java, C++, C or Julia)