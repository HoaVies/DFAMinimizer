import java.io.*;
import java.util.*;

public class DFAMinimizer {

    // DFA Representation
    static class DFA {
        Set<String> states;
        Set<Character> alphabet;
        String startState;
        Set<String> finalStates;
        Map<String, Map<Character, String>> transitions;

        public DFA() {
            states = new HashSet<>();
            alphabet = new HashSet<>();
            finalStates = new HashSet<>();
            transitions = new HashMap<>();
        }
    }

    public static void main(String[] args) {
        // Default file names
        String inputFile = "input2.txt";
        String outputFile = "output2.txt";

        if (args.length == 2) {
            inputFile = args[0];
            outputFile = args[1];
        } else if (args.length != 0) {
            System.out.println("Usage: java DFAMinimizer [inputFile outputFile]");
            System.out.println("Using default files: input.txt and output.txt");
        }

        DFA dfa = readDFAFromFile(inputFile);
        DFA minimizedDFA = minimizeDFA(dfa);
        writeDFAToFile(minimizedDFA, outputFile);
        printDFAToConsole(minimizedDFA); //print DFA to console
    }

    /**
     * Reads the DFA from the specified input file.
     *
     * @param filename The name of the input file.
     * @return The DFA object.
     */
    private static DFA readDFAFromFile(String filename) {
        DFA dfa = new DFA();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            // Read the number of states
            int numStates = Integer.parseInt(br.readLine().trim());
            for (int i = 0; i < numStates; i++) {
                dfa.states.add(String.valueOf(i));
            }
    
            // Read the alphabet
            String[] alphabetParts = br.readLine().trim().split("\\s+");
            for (String symbol : alphabetParts) {
                if (symbol.length() != 1) {
                    throw new IllegalArgumentException("Alphabet symbols must be single characters.");
                }
                dfa.alphabet.add(symbol.charAt(0));
            }
    
            // Read the start state
            dfa.startState = br.readLine().trim();
    
            // Read the final states
            String[] finalStatesParts = br.readLine().trim().split("\\s+");
            for (String state : finalStatesParts) {
                dfa.finalStates.add(state);
            }
    
            // Read transitions
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
    
                String[] parts = line.split("\\s+");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid transition format: " + line);
                }
    
                String currentState = parts[0];
                char inputSymbol = parts[1].charAt(0);
                String nextState = parts[2];
    
                dfa.transitions.putIfAbsent(currentState, new HashMap<>());
                dfa.transitions.get(currentState).put(inputSymbol, nextState);
            }
    
        } catch (IOException e) {
            System.err.println("Error reading the input file: " + e.getMessage());
            System.exit(1);
        }
        return dfa;
    }
    

    /**
     * Minimizes the given DFA using the partition refinement algorithm.
     *
     * @param dfa The original DFA.
     * @return The minimized DFA.
     */
    private static DFA minimizeDFA(DFA dfa) {
        //Remove unreachable states
        Set<String> reachable = getReachableStates(dfa);
        dfa.states.retainAll(reachable);
        dfa.finalStates.retainAll(reachable);

        //Remove transitions of unreachable states
        dfa.transitions.keySet().retainAll(reachable);
        for (Map<Character, String> trans : dfa.transitions.values()) {
            trans.keySet().retainAll(dfa.alphabet);
        }

        //Initialize partitions
        Set<Set<String>> partitions = new HashSet<>();
        Set<String> nonFinal = new HashSet<>(dfa.states);
        nonFinal.removeAll(dfa.finalStates);
        if (!nonFinal.isEmpty()) {
            partitions.add(nonFinal);
        }
        if (!dfa.finalStates.isEmpty()) {
            partitions.add(new HashSet<>(dfa.finalStates));
        }

        boolean updated;
        do {
            updated = false;
            Set<Set<String>> newPartitions = new HashSet<>();

            for (Set<String> group : partitions) {
                Map<Map<Character, Set<String>>, Set<String>> splitter = new HashMap<>();

                for (String state : group) {
                    Map<Character, Set<String>> key = new HashMap<>();
                    for (char symbol : dfa.alphabet) {
                        String target = dfa.transitions.get(state).get(symbol);
                        // Find the partition that contains the target state
                        Set<String> targetPartition = null;
                        for (Set<String> p : partitions) {
                            if (p.contains(target)) {
                                targetPartition = p;
                                break;
                            }
                        }
                        key.put(symbol, targetPartition);
                    }
                    splitter.putIfAbsent(key, new HashSet<>());
                    splitter.get(key).add(state);
                }

                if (splitter.size() > 1) {
                    updated = true;
                    newPartitions.addAll(splitter.values());
                } else {
                    newPartitions.add(group);
                }
            }

            partitions = newPartitions;
        } while (updated);

        //Create a mapping from old states to new states
        Map<String, String> stateMapping = new HashMap<>();
        int stateCounter = 0;
        for (Set<String> group : partitions) {
            String newState = "S" + stateCounter++;
            for (String state : group) {
                stateMapping.put(state, newState);
            }
        }

        //Build the minimized DFA
        DFA minimized = new DFA();
        minimized.alphabet = dfa.alphabet;

        // Define states
        minimized.states.addAll(new HashSet<>(stateMapping.values()));

        // Define start state
        minimized.startState = stateMapping.get(dfa.startState);

        // Define final states
        for (Set<String> group : partitions) {
            for (String state : group) {
                if (dfa.finalStates.contains(state)) {
                    minimized.finalStates.add(stateMapping.get(state));
                    break;
                }
            }
        }

        // Define transitions
        for (Set<String> group : partitions) {
            String representative = group.iterator().next();
            String newState = stateMapping.get(representative);
            minimized.transitions.putIfAbsent(newState, new HashMap<>());

            for (char symbol : dfa.alphabet) {
                String target = dfa.transitions.get(representative).get(symbol);
                String mappedTarget = stateMapping.get(target);
                minimized.transitions.get(newState).put(symbol, mappedTarget);
            }
        }

        return minimized;
    }

    /**
     * Retrieves the set of reachable states from the start state.
     *
     * @param dfa The DFA.
     * @return A set of reachable states.
     */
    private static Set<String> getReachableStates(DFA dfa) {
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        reachable.add(dfa.startState);
        queue.add(dfa.startState);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (char symbol : dfa.alphabet) {
                String next = dfa.transitions.get(current).get(symbol);
                if (!reachable.contains(next)) {
                    reachable.add(next);
                    queue.add(next);
                }
            }
        }

        return reachable;
    }

    /**
     * Writes the DFA to the specified output file
     *
     * @param dfa      The DFA to write.
     * @param filename The name of the output file.
     */
    private static void writeDFAToFile(DFA dfa, String filename) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename))) {
            // Write States
            bw.write("States: ");
            bw.write(String.join(",", dfa.states));
            bw.newLine();

            // Write Alphabet
            bw.write("Alphabet: ");
            List<String> alphabetList = new ArrayList<>();
            for (char c : dfa.alphabet) {
                alphabetList.add(String.valueOf(c));
            }
            bw.write(String.join(",", alphabetList));
            bw.newLine();

            // Write StartState
            bw.write("StartState: ");
            bw.write(dfa.startState);
            bw.newLine();

            // Write FinalStates
            bw.write("FinalStates: ");
            bw.write(String.join(",", dfa.finalStates));
            bw.newLine();

            // Write Transitions
            bw.write("Transitions:");
            bw.newLine();
            for (String state : dfa.states) {
                for (char symbol : dfa.alphabet) {
                    String nextState = dfa.transitions.get(state).get(symbol);
                    bw.write(state + " " + symbol + " " + nextState);
                    bw.newLine();
                }
            }

            System.out.println("Minimized DFA has been written to " + filename);
        } catch (IOException e) {
            System.err.println("Error writing the output file: " + e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Prints the DFA to the console 
     *
     * @param dfa The DFA to print.
     */
    private static void printDFAToConsole(DFA dfa) {
        System.out.println("\n--- Minimized DFA ---\n");

        // Print States
        System.out.println("States: " + String.join(", ", dfa.states));

        // Print Alphabet
        List<String> alphabetList = new ArrayList<>();
        for (char c : dfa.alphabet) {
            alphabetList.add(String.valueOf(c));
        }
        System.out.println("Alphabet: " + String.join(", ", alphabetList));

        // Print Start State
        System.out.println("Start State: " + dfa.startState);

        // Print Final States
        System.out.println("Final States: " + String.join(", ", dfa.finalStates));

        // Print Transitions
        System.out.println("Transitions:");
        for (String state : dfa.states) {
            for (char symbol : dfa.alphabet) {
                String nextState = dfa.transitions.get(state).get(symbol);
                System.out.println("  " + state + " --" + symbol + "--> " + nextState);
            }
        }

        System.out.println("\n--- End of Minimized DFA ---");
    }
}
