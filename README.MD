POM-to-DOT
==========

## License

This project is licensed under the [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.html).

The copyright owner is Dieter Bogdoll.

## Overview
This project provides a command line tool, to visualize the hierarchical POM
structure with the help of [DOT](https://en.wikipedia.org/wiki/DOT_%28graph_description_language%29)
and [GraphViz](http://graphviz.org/) or [PlantUML](http://plantuml.com/).

All dependencies are assumed to be in your local Maven repository.
 
There is also plugins to use PlantUML with some IDEs:
* IntelliJ: https://plugins.jetbrains.com/plugin/7017-plantuml-integration
* Eclipse: http://plantuml.com/eclipse
* Emacs: http://plantuml.com/emacs
* (add yours here and send me the link) 

## Command line 

After you have build the project with maven, you can rename `target/pom-to-dot-<VERSION>-jar-with-dependencies.jar`
into something shorter like `pom-to-dot.jar`:

    Usage: pom-to-dot [OPTIONS]
    
    Options:
      --coord MAVEN_COORD  Maven coordinates. Format:
                           groupId:artifactId:version[:packaging][:classifier]
      --internal-dep RE    Regular Expression for highlighting the internal
                           dependencies
      --internal-only      Shows only the internal dependencies
      --exclude-dep RE     Regular Expression for excluding dependencies
      --out-file PATH      File into which dot output is written
      --plant-uml          Should format it for plantUml
      -h, --help           Show this message and exit

The shortest form to call this is like

    java -jar pom-to-dot.jar --coord io.github.dibog:pom-to-dot:1.0.0
    
which would create a rather big dot file as all the dependencies which
are transitively used will be printed onto the system console.

Whereas
    
    java -jar pom-to-dot.jar --coord io.github.dibog:pom-to-dot:1.0.0 --out-file test.dot
    
stores the output into a file called `test.dot`.

Lets go a bit more in detail over each parameter:

* `--coord` here you specify the root of the graph to be displayed

* `--internal-dep` here you can specify a regular expression which will
be matched against the group id. The idea is to display your internal dependencies 
differently then the other dependencies.

* `--internal-only` is a flag and is set in addition to `--internal-dep` it
will only display your dependencies.

* `--exclude-dep` lets you specify a regular expression which will again be
matched with the groupId. Every such matched dependecy won't appear in the
DOT file.

* `--out-file` if set the output will be written in that file.

* `--plant-uml` if set the output will be formatted to be used with PlantUML.


