# Task 2: DFA Minimization

## Objective
The goal of this task is to minimize a **Deterministic Finite Automaton (DFA)** by reducing the number of states without altering the language it recognizes. This process results in a **minimal DFA**, which is unique (up to state renaming) for a given language.

---

## Algorithm: DFA Minimization

### Steps:

1. **Remove Unreachable States**:
- Identify states that cannot be reached from the start state using any sequence of inputs.
- Remove these unreachable states from the DFA.

2. **Partition States into Initial Groups**:
- Separate states into two groups:
- Final (accept) states.
- Non-final states.
- These groups form the **initial partition**.

3. **Refine the Partitions**:
- Iteratively refine the state groups by checking transitions for each input symbol.
- If two states in the same group transition to states in different groups for any input symbol, split them into separate groups.
- Repeat until no further partitioning is possible.

4. **Construct the Minimized DFA**:
- Each group in the final partition represents a single state in the minimized DFA.
- Transitions between these states are determined based on the original DFA’s transitions.

---

## Data Structures

### State Representation
- Use integers to represent states for simplicity.

### Transitions
- Use `Map<Integer, Map<Character, Integer>>` to store transitions, where:
- Key: Current state.
- Value: Map of input symbols to the next state.

### Partitions
- Use a `List<Set<Integer>>` to represent partitions during refinement.

### Reachability
- Use a `Queue<Integer>` to track states during reachability analysis.

---

## Example Execution

### Input DFA (Example 1):
File: `dfa_input.txt`
```
5 # Total number of states: q0, q1, q2, q3, q4
a b # Input alphabet: 'a' and 'b'
0 # Start state: q0
4 # Final state: q4
0 a 1 # From q0, on 'a', transition to q1
0 b 3 # From q0, on 'b', transition to q3
1 a 2 # From q1, on 'a', transition to q2
1 b 4 # From q1, on 'b', transition to q4
2 a 1 # From q2, on 'a', transition back to q1
2 b 4 # From q2, on 'b', transition to q4
3 a 2 # From q3, on 'a', transition to q2
3 b 4 # From q3, on 'b', transition to q4
4 a 4 # From q4, on 'a', remain in q4 (self-loop)
4 b 4 # From q4, on 'b', remain in q4 (self-loop)
```

### Output:
- After applying minimization, states `q0` and `q3` are merged as they exhibit equivalent behavior.
- Transitions for the minimized DFA are written to `minimized_dfa_output.txt`.

---

### Example Diagram:
Below is a visual representation of the DFA minimization process:

![DFA Minimization Example](https://github.com/HoaVies/DFAMinimizer/blob/master/task1.png)
Result:
![DFA Minimization Example](https://github.com/HoaVies/DFAMinimizer/blob/master/task1sol.png)
Code Execution:
![DFA Minimization Example](https://github.com/HoaVies/DFAMinimizer/blob/master/codeexec.png)
---

## References
- [Minimization of DFA - Gate Vidyalay](https://www.gatevidyalay.com/minimization-of-dfa-minimize-dfa-example)

