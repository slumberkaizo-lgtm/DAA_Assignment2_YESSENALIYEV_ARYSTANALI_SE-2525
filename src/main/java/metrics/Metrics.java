package metrics;

/**
 * Counts physical operations performed by a data structure.
 * Steps: array-cell reads or following a next link (including a null successor).
 * Moves: relocating an existing array element or assigning head, tail or next.
 * Comparisons: comparisons of element values, excluding indexes and pointers.
 * New int writes, clearing unused cells and local assignments are not moves.
 */
public class Metrics {
    private long steps;
    private long moves;
    private long comparisons;

    public void recordStep() {
        steps++;
    }

    public void recordMove() {
        moves++;
    }

    public void recordComparison() {
        comparisons++;
    }

    public long getSteps() {
        return steps;
    }

    public long getMoves() {
        return moves;
    }

    public long getComparisons() {
        return comparisons;
    }

    public void reset() {
        steps = 0;
        moves = 0;
        comparisons = 0;
    }
}
