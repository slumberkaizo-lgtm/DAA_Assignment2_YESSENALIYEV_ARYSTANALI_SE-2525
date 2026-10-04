"""Generate PNG charts from the actual benchmark CSV.

Install requirements-plots.txt, then run: python scripts/plot_results.py
"""

import csv
import math
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt


ROOT = Path(__file__).resolve().parents[1]
CSV_PATH = ROOT / "results" / "results.csv"
OUTPUT_DIR = ROOT / "results" / "plots"
SIZES = (100, 1_000, 10_000, 100_000)
FIELDS = ("workload", "variant", "structure", "n", "time_ms", "steps", "moves", "comparisons")
CASES = (
    ("W1", "-", "Random Access", ("DynamicArray", "MyLinkedList")),
    ("W2", "-", "Search", ("DynamicArray", "MyLinkedList")),
    ("W3", "head", "Insert & Remove at Head", ("DynamicArray", "MyLinkedList")),
    ("W3", "middle", "Insert & Remove at Middle", ("DynamicArray", "MyLinkedList")),
    ("W4", "-", "Priority Processing", ("MinHeap",)),
)
METRICS = (
    ("time_ms", "Time vs n", "Median time (ms)"),
    ("steps", "Steps vs n", "Steps (count)"),
    ("moves", "Moves vs n", "Moves (count)"),
    ("comparisons", "Comparisons vs n", "Comparisons (count)"),
)
STYLES = {
    "DynamicArray": {"color": "#2563eb", "marker": "o", "linestyle": "-"},
    "MyLinkedList": {"color": "#ea580c", "marker": "x", "linestyle": "--"},
    "MinHeap": {"color": "#15803d", "marker": "s", "linestyle": "-"},
}


def read_results():
    results = {}
    with CSV_PATH.open(encoding="utf-8", newline="") as source:
        reader = csv.DictReader(source)
        if tuple(reader.fieldnames or ()) != FIELDS:
            raise ValueError("Unexpected CSV header")
        for row in reader:
            key = (row["workload"], row["variant"], row["structure"], int(row["n"]))
            if key in results:
                raise ValueError(f"Duplicate benchmark case: {key}")
            values = {metric: float(row[metric]) if metric == "time_ms" else int(row[metric])
                      for metric, _, _ in METRICS}
            if any(not math.isfinite(value) or value < 0 for value in values.values()):
                raise ValueError(f"Invalid numeric value: {key}")
            if values["time_ms"] <= 0:
                raise ValueError(f"Non-positive measured time: {key}")
            results[key] = values
    expected = {(workload, variant, structure, n)
                for workload, variant, _, structures in CASES
                for structure in structures for n in SIZES}
    if set(results) != expected:
        raise ValueError("CSV must contain exactly the 36 required benchmark cases")
    return results


def main():
    results = read_results()
    OUTPUT_DIR.mkdir(parents=True, exist_ok=True)
    for workload, variant, title, structures in CASES:
        fig, axes = plt.subplots(2, 2, figsize=(12, 8), layout="constrained")
        fig.suptitle(f"{workload} - {title}", fontsize=17, fontweight="bold")
        for axis, (metric, panel_title, ylabel) in zip(axes.flat, METRICS):
            all_values = []
            for structure in structures:
                values = [results[(workload, variant, structure, n)][metric] for n in SIZES]
                all_values.extend(values)
                axis.plot(SIZES, values, label=structure, linewidth=2, markersize=7,
                          markerfacecolor="none", **STYLES[structure])
            axis.set_xscale("log")
            axis.set_xticks(SIZES, [f"{n:,}" for n in SIZES])
            if min(all_values) > 0 and max(all_values) / min(all_values) >= 100:
                axis.set_yscale("log")
                ylabel += " - log scale"
            elif min(all_values) == 0 and max(all_values) >= 100:
                axis.set_yscale("symlog", linthresh=1)
                ylabel += " - symlog scale"
            elif max(all_values) == 0:
                axis.set_ylim(-0.5, 0.5)
                axis.set_yticks([0])
                axis.text(0.5, 0.45, "Both structures: 0", transform=axis.transAxes,
                          ha="center", color="#475569")
            axis.set_title(panel_title, fontsize=12)
            axis.set_xlabel("n (initial number of elements)")
            axis.set_ylabel(ylabel)
            axis.grid(True, which="major", alpha=0.25)
            axis.legend(fontsize=9)
        fig.supxlabel("Source: results/results.csv | Time: median of 5 measured runs", fontsize=10)
        filename = f"{workload.lower()}{'_' + variant if variant != '-' else ''}.png"
        path = OUTPUT_DIR / filename
        fig.savefig(path, dpi=160)
        plt.close(fig)
        print(f"Saved {path.relative_to(ROOT)}")


if __name__ == "__main__":
    main()
