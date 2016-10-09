# SudokuPlayer
Artificial Intelligence Assignment: Creation of an intelligent agent that solves Sudoku using backtracking search with the AC3 algorithm

Customized solver:
	We implemented the least-constraining-value heuristic in the checkRevisions method. We checked the number of values remaining in a given cell’s neighbors’ domains for each value in the current cell’s domain. We assigned the value that was least constraining. In other words, we chose the value that left the most other values for other variables.
	We also implemented the minimum remaining values heuristic in the toBacktrack method. This chose the next cell to backtrack on based on which cell had the minimum remaining values in its domain. 
	Within our customBacktrack method, we used both these heuristics. Overall, it performed about the same as the AC3 solver. We don’t think time improved because of the time added to loop through neighbors and the board. We performed more operations with data structures that take O(n) time for operations such as contains and retrieve. 
	We believe the best way to improve the solver would be to implement forward checking, and iterative deepening to resolve conflicts (For example, with the MRV heuristic, if two cells have only 1 value left in the domain, it chooses the last cell checked as our next variable. Another heuristic could resolve this “tie” and improve the overall time). 
