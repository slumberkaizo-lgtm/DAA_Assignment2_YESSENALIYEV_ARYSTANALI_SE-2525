# Assignment 2 - Data Structures

## Complexity Analysis

Let `n` be the number of stored elements before an operation and `i` a valid index.
The tables describe successful operations; invalid indexes and empty-heap access
throw an exception in Θ(1) time. Bounds concern algorithmic work, excluding JVM
garbage collection and operating-system scheduling. Allocating and initializing
an array of length proportional to `n` takes Θ(n) time.

Θ denotes a tight bound, O an upper bound, and Ω a lower bound. For example, a
full unsuccessful linear search takes both O(n) and Ω(n), hence Θ(n).
Expressions involving logarithms use `log(n + 1)` to cover small heaps.

Average indexed-operation costs assume a uniformly selected valid index.
Average search costs assume either a successful search with a uniformly
distributed first matching position or an unsuccessful search; both give Θ(n).
For operations that grow an array, the average column explicitly gives an
amortized bound over a sequence starting from an empty structure, rather than
assuming a probability that the array is full. Heap value distributions are
unspecified, so its average entries are upper bounds rather than tight expected
costs for a particular distribution.

Auxiliary space means additional memory needed during an operation, excluding
the existing structure. A newly allocated array counts during resizing because
the old and new arrays coexist while elements are copied. All methods are
iterative; metrics use a constant number of `long` fields.

### DynamicArray

| Operation | Best time | Average / amortized time | Worst time | Auxiliary space |
| --- | --- | --- | --- | --- |
| `add(x)` | Θ(1) | Θ(1) amortized | Θ(n) | Θ(1) without resize; Θ(n) with resize |
| `add(i, x)` | Θ(1) | Θ(n) for a uniform insertion index | Θ(n) | Θ(1) without resize; Θ(n) with resize |
| `remove(i)` | Θ(1) | Θ(n) | Θ(n) | Θ(1) |
| `get(i)` | Θ(1) | Θ(1) | Θ(1) | Θ(1) |
| `contains(x)` | Θ(1) | Θ(n) | Θ(n) | Θ(1) |

Appending with spare capacity writes the new element and increments `size`, so
the normal case is Θ(1). When full, `ensureCapacity()` allocates an array twice
as large and copies all `n` elements, giving a Θ(n) resize case. With initial
capacity 10, copied capacities form the geometric series `10 + 20 + 40 + ...`.
Across `m` appends starting empty, total copying and array initialization cost
O(m); the `m` new-element writes also give Ω(m). Total work is therefore Θ(m),
and each append costs Θ(1) amortized, despite individual Θ(n) resizes.

Indexed insertion shifts exactly `n - i` elements, so it takes Θ(n - i + 1)
without resizing and Θ(n) when resizing. Its best case is insertion at `i = n`
with spare capacity. Under a uniform index in `0..n`, the expected shift count
is `n / 2`, giving Θ(n).

Removal shifts `n - i - 1` elements and takes Θ(n - i). Removing the last
element is Θ(1); removing the first is Θ(n). The array never shrinks, so removal
requires only constant additional space. Direct indexing makes `get(i)` Θ(1).
`contains(x)` stops at the first match or scans all `n` live elements: a first
match gives Θ(1), while a last match or absence gives Θ(n).

### MyLinkedList

| Operation | Best time | Average time | Worst time | Auxiliary space |
| --- | --- | --- | --- | --- |
| `add(x)` | Θ(1) | Θ(1) | Θ(1) | Θ(1), one new node |
| `add(i, x)` | Θ(1) | Θ(n) | Θ(n) | Θ(1), one new node |
| `remove(i)` | Θ(1) | Θ(n) | Θ(n) | Θ(1) |
| `get(i)` | Θ(1) | Θ(n) | Θ(n) | Θ(1) |
| `contains(x)` | Θ(1) | Θ(n) | Θ(n) | Θ(1) |

The implementation is singly linked and stores both `head` and `tail`.
Appending uses `tail` directly and updates a constant number of links, so
`add(x)` is Θ(1). Indexed insertion at `i = n` delegates to append, and insertion
at `i = 0` updates the head directly; both cases are Θ(1). For `0 < i < n`,
`nodeAt(i - 1)` follows `i - 1` links before inserting the new node, giving
Θ(i + 1). A uniformly selected insertion index still gives Θ(n) average time.

Removing the head is Θ(1). Removing any other node must first find its
predecessor, giving Θ(i + 1), including Θ(n) removal of the tail. The tail pointer
does not supply a predecessor in a singly linked list. `get(i)` follows exactly
`i` links from the head and takes Θ(i + 1); even `get(n - 1)` traverses the list
in this implementation. Search compares node values until the first match or
the end, giving the same search-time bounds as DynamicArray. Traversal uses
local references rather than an additional list or a recursion stack.

### MinHeap

| Operation | Best time | Average / amortized upper bound | Worst time | Auxiliary space |
| --- | --- | --- | --- | --- |
| `insert(x)` | Θ(1) | O(log(n + 1)) amortized; value-distribution dependent | Θ(n) with resize; Θ(log(n + 1)) worst without resize | Θ(1) without resize; Θ(n) with resize |
| `peekMin()` | Θ(1) | Θ(1) | Θ(1) | Θ(1) |
| `extractMin()` | Θ(1) | O(log(n + 1)); value-distribution dependent | Θ(log(n + 1)) | Θ(1) |

Insertion appends an element and calls `bubbleUp()`. With spare capacity and
no violated parent relation, it stops after constant work. A new smallest
element may rise through the heap's Θ(log(n + 1)) levels. This implementation
also doubles its backing array when full, so the worst individual insertion is
Θ(n), rather than O(log n). Geometric growth contributes O(1) amortized copying
and allocation cost per insertion; combining it with bubble-up gives an
O(log(n + 1)) amortized upper bound.

The minimum is stored at index 0, making `peekMin()` Θ(1). Extraction replaces
the root with the last element and calls `bubbleDown()`. Each iteration chooses
the smaller existing child and performs at most one swap, moving down one
level. The worst case visits Θ(log(n + 1)) levels. The best case stops at the
root, for example when all values are equal. No resizing or shrinking occurs
during extraction, so auxiliary space is Θ(1). Without assumptions about heap
values, a tight Θ(log n) average claim is unjustified: equal-value heaps can
perform both bubble-up and bubble-down in Θ(1).

### Storage and Metric Overhead

During growth from empty without removals, each structure uses Θ(n + 1) storage.
DynamicArray and MinHeap have geometrically grown `int[]` buffers; MyLinkedList
has one node per element plus head and tail references and retains Θ(n + 1)
storage after removals as well. The fixed initial array capacity explains the
`+1` for empty arrays. Arrays retain their capacity after removals, so after
shrinking the logical size their storage is Θ(c), where `c` is the retained
capacity, not necessarily Θ(n + 1). Each structure's Metrics object uses Θ(1) space, and each
counter update adds Θ(1) work at its operation site without changing any of
the asymptotic bounds above.
