# SudokuPlayer

Artificial Intelligence Assignment: Creation of an intelligent agent that solves Sudoku using backtracking search with the AC3 algorithm

How to run
------
Download the .java file. Compile using ``` javac SudokuPlayer.java ```. Run with ``` java SudokuPlayer ```
The program randomly generates a solveable sudoku board based on level of difficulty (corresponding to number of missing digits on the board). You will be prompted to run via GUI or not. Then, you choose an algorithm (AC3 or custom algorith, explained below) that will solve the sudoku board.

To learn more about the AC3 algorithm, explore the [Wikipedia](https://en.wikipedia.org/wiki/AC-3_algorithm) page

Learning objectives
----- 
	1. Practice on reading and understanding instructor's "starter code," then implementing the AC3 algorithm on top of it. 
	2. Create a customized solver using heuristics to improve the running time of the AC3 algorithm (see below).

Customized algorithm
-----
We implemented the least-constraining-value heuristic in the checkRevisions method. We checked the number of values remaining in a given cell’s neighbors’ domains for each value in the current cell’s domain. We assigned the value that was least constraining. In other words, we chose the value that left the most other values for other variables.
	
We also implemented the minimum remaining values heuristic in the toBacktrack method. This chose the next cell to backtrack on based on which cell had the minimum remaining values in its domain. 
	
Within our customBacktrack method, we used both these heuristics. Overall, it performed about the same as the AC3 solver. We don’t think time improved because of the time added to loop through neighbors and the board. We performed more operations with data structures that take O(n) time for operations such as contains and retrieve. 
	
Further improvements
-------

We believe the best way to improve the solver would be to implement forward checking, and iterative deepening to resolve conflicts (For example, with the MRV heuristic, if two cells have only 1 value left in the domain, it chooses the last cell checked as our next variable. Another heuristic could resolve this “tie” and improve the overall time). 
