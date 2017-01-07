# Optimal-Nonlinear-Pipeline-Latency
This project is to develop a computer-aided automatic design tool for Optimal Nonlinear Pipeline Latency.


#Input(sample):
------------------------------
A random reservation table in steady state as follows:

1 1
1 5
2 2
2 4
3 3
3 4

1 1
1 9
2 2
2 3
2 8
3 4
4 5
4 6
5 7
5 8

where the first and second column represents stage and clock cycle, respectively.


#What to do:
------------------------------
1. Simulate the collision-vector state transition diagram.
2. Find the theoretical minimum and maximum MAL.
3. Optimize the reservation table if not optimized.


#Output:
------------------------------
1. Print the resulting collision-vector state transition diagram as follows:
(for the above sample reservation table)

1011 3 1011
1011 5 1011

in any order, in each line list the source state vector, permissible latency,
and its destination state vector.

2. List all the simple and greedy cycles following the same manner in step 1.

3. Display the theoretical minimum and maximum MAL.

4. If an optimization process performed, display the resulting optimized collision-vector state transition diagram, and repeat steps 2. and 3. for the optimized diagram for verification purpose.
(If there is no optimization needed, just display "Already Optimal".)

* All the above steps is performed in a SINGLE RUN for any arbitrary input reservation table in the given format.

