# iFutureTaskOne

Desktop Java application for searching text inside log files within a selected directory tree.

## Overview

The application lets a user choose a root directory, specify a file extension, and enter the text to search for. It then scans the selected directory and all nested subdirectories, finds matching files, and displays the results in a tree view.

The user interface is implemented with Swing. Matching files appear on the left side of the window, while the selected file content is shown on the right side with navigation controls for moving between matches.

## Features

- Choose a root directory through a file chooser
- Filter files by extension, such as `log`
- Search file contents for a target string
- Display matched files in a tree structure
- Open a matched file and navigate through occurrences
- Highlight all occurrences in the opened file
- Use background threads for directory scanning and file reading

## Project Structure

- `Main` starts the Swing application
- `GUI` contains the desktop interface and search interactions
- `FileTreeBuilder` traverses directories and builds the result tree
- `FileScanner` checks whether a file contains the requested text

## Requirements

- Java 8 or newer
- Maven 3.9+

## Build

```bash
mvn clean package
```

The Maven configuration uses UTF-8 source encoding so the project builds consistently on systems where the default platform encoding is not UTF-8.

## Run

```bash
java -jar target/iFutureTaskOne-1.0.0.jar
```

You can also run `Main` directly from your IDE.

## Notes

- The project currently has no automated tests.
- The search UI and file reading logic are implemented as a desktop Swing workflow rather than a web application.
