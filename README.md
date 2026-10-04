# Assignment 2 - Data Structures

Design and Analysis of Algorithms assignment implementing three data structures
from scratch, measuring physical operations, and comparing their performance.

## Structures

| Class | Storage | Operations |
| --- | --- | --- |
| `DynamicArray` | `int[]`, capacity doubles when full | `add(x)`, `add(index, x)`, `remove(index)`, `get(index)`, `contains(x)`, `size()` |
| `MyLinkedList` | Own singly linked `Node` with `int value`, head and tail | Same sequence operations as DynamicArray |
| `MinHeap` | Binary min heap on a growing `int[]` | `insert(x)`, `peekMin()`, `extractMin()`, `size()` |

Indexed insertion accepts `0 <= index <= size`; reading and removal require
`0 <= index < size`. Invalid indexes throw `IndexOutOfBoundsException`.
`remove(index)` returns the removed value. Empty-heap reads and extraction throw
`IllegalStateException`. Production structures use primitive `int` values and
do not wrap Java collection implementations; reference collections appear only
in tests.

Each structure exposes `getMetrics()` with `long` steps, moves and comparisons.
Call `getMetrics().reset()` to exclude setup work from an experiment. Steps count
array reads or following `next` links, including a null successor. Moves count
relocation of existing array elements and assignments to head, tail or next.
Comparisons count value comparisons; index and pointer checks do not count.
Writing a new int, clearing unused array cells and assigning local variables
do not count as moves.

## Project Layout

```text
src/main/java/structures/   Three data structures
src/main/java/metrics/      Physical operation counters
src/main/java/benchmark/    W1-W4 runner and CSV export
src/test/java/             JUnit 5 correctness and metrics tests
scripts/                   Chart generator and Python dependencies
results/results.csv        Measured benchmark results
results/plots/             Five PNG charts, four panels each
gradle/wrapper/             Gradle Wrapper
build.gradle               Java, test, benchmark and chart tasks
settings.gradle            Project name
REPORT.md                  Complexity, proofs and performance discussion
```

## Requirements

- JDK 17, available to Gradle; set `JAVA_HOME` to the JDK 17 installation when
  the default Java is older. A JRE alone is insufficient.
- Gradle Wrapper 8.14.3 is included; no separate Gradle installation is needed.
- Internet access on first use to download the wrapper distribution and JUnit
  dependencies, unless they are already cached.
- Optional for regenerating charts: Python 3.10 or newer with pip and Matplotlib.
  Existing PNGs can be viewed without Python.

Run the following commands from the repository root. Java uses the Gradle
toolchain configured in `build.gradle`.

## Build and Test

Windows PowerShell:

```powershell
.\gradlew.bat clean build
.\gradlew.bat test
```

Linux/macOS:

```sh
./gradlew clean build
./gradlew test
```

If the Unix wrapper lacks executable permission after checkout, use
`sh gradlew clean build` and `sh gradlew test`.

The 49 JUnit 5 tests cover empty structures, boundary positions, invalid indexes,
duplicates, negative and extreme int values, resizing, reuse after removal,
reference comparisons, exact metric counts, heap property after mutations and
sorted heap extraction. The HTML test report is `build/reports/tests/test/index.html`.

## Benchmark

Windows PowerShell:

```powershell
.\gradlew.bat benchmark
```

Linux/macOS:

```sh
./gradlew benchmark
```

The task compiles the Java code, runs all cases and overwrites
`results/results.csv` with 36 results. Each case uses one discarded warm-up,
five measured runs and the median elapsed time from `System.nanoTime()`.
Inputs come from `Random(42)` and are identical for compared structures.
Operation counts are deterministic for these inputs; timings vary across runs
and machines. Data generation, result validation and file output are outside
the timed section. W1-W3 also exclude initial structure filling; W4 measures
both insertion and extraction, including any capacity growth.

| Workload | Structures | Measured operations for each n |
| --- | --- | --- |
| W1 Random Access | DynamicArray, MyLinkedList | 10,000 valid random `get(index)` calls |
| W2 Search | DynamicArray, MyLinkedList | 1,000 `contains(x)` calls: 500 present, 500 absent |
| W3 Insert & Remove | DynamicArray, MyLinkedList | 1,000 insertions followed by 1,000 removals, separately at head and current middle |
| W4 Priority Processing | MinHeap | Insert n values, then extract n minima; verify non-decreasing order and exact values |

Every workload uses `n = 100, 1000, 10000, 100000`. W3 computes the middle index
from the current size on every operation. Incorrect answers, final contents,
heap extraction order or checked metric counts terminate the benchmark with an
error rather than exporting those results.

CSV header:

```csv
workload,variant,structure,n,time_ms,steps,moves,comparisons
```

`variant` is `head` or `middle` for W3 and `-` otherwise. `time_ms` is the median
in milliseconds; steps, moves and comparisons are actual per-run counters,
not estimates or sums over repetitions. Zero counters are expected when the
workload performs no operation of that category.

## Charts and Report

To install chart dependencies into the Python environment selected by the
`python` command, then regenerate charts from the existing CSV:

Windows PowerShell:

```powershell
.\gradlew.bat plotDependencies
.\gradlew.bat plots
```

Linux/macOS:

```sh
./gradlew plotDependencies -PpythonExecutable=python3
./gradlew plots -PpythonExecutable=python3
```

Both tasks accept `-PpythonExecutable` with an interpreter path, for example
`'-PpythonExecutable=build/plot-venv/Scripts/python.exe'` for a Windows virtual
environment. Install dependencies once per Python environment. The `plots` task
reads the existing CSV and does not rerun the benchmark. After a new benchmark,
rerun `plots` and update the report's numeric tables to match the new CSV.

[Results charts](results/plots/) include `w1.png`, `w2.png`, `w3_head.png`,
`w3_middle.png` and `w4.png`. Every image shows time, steps, moves and comparisons
versus n, with labels, units and legends. Logarithmic scales are labelled;
zero counters remain visible on linear axes.

[REPORT.md](REPORT.md) contains complexity tables, two loop-invariant proofs,
benchmark methodology, measured results and a discussion of locality, pointer
chasing, runtime limitations and appropriate use cases.

## Final Verification

Windows PowerShell:

```powershell
.\gradlew.bat clean test
.\gradlew.bat clean build
.\gradlew.bat benchmark
.\gradlew.bat plots
```

Linux/macOS:

```sh
./gradlew clean test
./gradlew clean build
./gradlew benchmark
./gradlew plots -PpythonExecutable=python3
```

`clean` removes `build/`, including a Python environment stored there; use an
environment outside `build/` or recreate it before chart generation. Generated
Gradle caches and build files are ignored. The CSV and PNG results are tracked
assignment deliverables. Commits, merges and release tags are controlled manually.
