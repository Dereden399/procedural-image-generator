# Procedural Image Generator
This program, written in ScalaFX, allows users to easily generate images procedurally.

I created this program as a course project during my studies at university. It uses ScalaFX, a JavaFX analog, to handle the UI part, and generates the images using the Wave Function Collapse (WFC) algorithm.

The main goal of the project was to enable users without any programming experience to easily generate images. Therefore, the program has a very minimalistic user interface and receives the rules for generation in easy-to-read JSON format.
# How to run
To run this project, follow these steps:
1. If you don't have it yet, install sbt on your machine first.
2. Donwload the project from this repository
3. In the root of the project, run the command:
```shell
$ sbt run
```
# Testing
You can find unit tests in the folder src/test. The unit tests only cover the Generate and Reader classes as there is no need to test the others. To start the tests, run the following command in the root of the project:
```shell
$ sbt test
```
# Instructions
You can find a brief instruction on how to use the program on the main screen after it has started. See the examples folder to find more information about the rules format. Note: only the fields "tileName" and "tilesUp" (as well as other directions) are mandatory.
# Bugs 
1. **High memory consumption** - The program consumes a lot of memory and can crash when generating large images (30x30 is the maximum stable size). For some reason, if the provided tileset/rules lead to contradictions, the program starts to use up memory very quickly and eventually crashes.
2. **Tileset with contradictions may remain ungenerated** - If the tileset and rules lead to contradictions, for images larger than 30x30, there is a significant possibility that the image will not be generated. To reduce memory consumption, the grid state is saved to the backtracking every x amount of collapsing. As a result, sometimes the program cannot resolve the found contradiction.
