/*
    Rachel Collins
    Nosagie Asaolu

    Group Honor Code: - All group members were present and contributing during all work on this project

    Middlebury Honor Code: - We have neither given nor received unauthorized aid on this assignment
*/

import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.text.DecimalFormat;
import java.util.List;

public class SudokuPlayer implements Runnable, ActionListener {

    // final values must be assigned in vals[][]
    int[][] vals = new int[9][9];
    Board board = null;


    //Helpers
    static final int BOARDDIMENSION = 9;
    // for generality


    /// --- AC-3 Constraint Satisfication --- ///


    // Useful but not required Data-Structures;
    ArrayList<Integer>[] globalDomains = new ArrayList[81];
    ArrayList<Integer>[] neighbors = new ArrayList[81];
    Queue<Arc> globalQueue = new LinkedList<Arc>();

	/*
 	* This method sets up the data structures and the initial global constraints
 	* (by calling allDiff()) and makes the initial call to backtrack().
 	*/

    private final void init(){
        //Do NOT remove these 3 lines (required for the GUI)
        board.Clear();
        ops = 0;
        recursions = 0;

        /**
         *  Populate data structures defined above
         *  These will be the data structures necessary for AC-3.
         **/

        for (int row = 0; row < BOARDDIMENSION; row++){
            for (int col = 0; col < BOARDDIMENSION; col++){

                int gDIndex = (row * BOARDDIMENSION) + col;
                globalDomains[gDIndex] = new ArrayList<Integer>();

                if (vals[row][col] == 0){
                    // value has not been assigned to board yet
                    // all values added to domain
                    globalDomains[gDIndex].addAll(Arrays.asList(1,2,3,4,5,6,7,8,9));
                }
                else{
                    // value has been assigned to board, it's domain size is 1
                    globalDomains[gDIndex].add(vals[row][col]);
                }
            }
        }

        // Define constraints between set of variables
        allDiff();

        // Initial call to backtrack() on cell 0 (top left)
        boolean success = backtrack(0,globalDomains);

        // Prints evaluation of run
        Finished(success);

    }



    // This defines constraints between a set of variables
    private final void allDiff(){

        List<Integer> neighborsToAdd;

        // Set up neighbors data structure
        for (int row = 0; row < BOARDDIMENSION; row++){
            for (int col = 0; col < BOARDDIMENSION; col++){

                int currentCellIndex = (row * BOARDDIMENSION) + col;
                neighbors[currentCellIndex] = new ArrayList<Integer>();

                //Row Inclusions
                //Add cells to the right of current cell
                int lastColIndex = BOARDDIMENSION-col;
                for (int colIndex = 1; colIndex < lastColIndex; colIndex++)
                {
                    neighbors[currentCellIndex].add((currentCellIndex) + colIndex);
                }
                //Add cells to the left of current cell, in same row
                for (int colIndex = 1; colIndex < col+1;colIndex++){
                    neighbors[currentCellIndex].add(currentCellIndex - colIndex);
                }

                //Column Inclusions
                //Adds cells above current cell, in same column
                for (int rowIndex = 0; rowIndex < row;rowIndex++){
                    neighbors[currentCellIndex].add((rowIndex*BOARDDIMENSION)+col);
                }
                //Adds cells below current cell, in same column
                for (int rowIndex = row+1; rowIndex < BOARDDIMENSION;rowIndex++){
                    neighbors[currentCellIndex].add((rowIndex * BOARDDIMENSION)+col);
                }

                //Box Inclusions
                //Adds cells in same box as current cell
                neighborsToAdd = evaluateBox(row,col);
                for (Integer neigh : neighborsToAdd){
                    neighbors[currentCellIndex].add(neigh);
                }
            }
        }


        //Create Arcs between neighbors and set up Global Queue
        for (int indexInNeigh = 0; indexInNeigh<BOARDDIMENSION*BOARDDIMENSION;indexInNeigh++){

            ArrayList<Integer> currentListForArcs = neighbors[indexInNeigh];

            //Create Arcs between neighbors and current cell, and add to global Queue
            for (int neighborIndex : currentListForArcs){
                Arc toAdd = new Arc(indexInNeigh,neighborIndex);
                globalQueue.add(toAdd);
            }
        }
    }

    // Helper Method to evaluate boxes (manually), in context of 3x3 box
    // (not generalized for different board dimensions)
    private ArrayList<Integer> evaluateBox(int row, int col){
        int modRow = row % 3;
        int modCol = col % 3;
        int cell = (row*9) + col;

        ArrayList<Integer> neighboursToAdd = new ArrayList<Integer>();

        switch (modRow){
            case 0:
                switch (modCol){
                    case 0:
                        addValues(10+cell,11+cell,19+cell,20+cell,neighboursToAdd);
                        break;
                    case 1:
                        addValues(8+cell,10+cell,17+cell,19+cell,neighboursToAdd);
                        break;
                    case 2:
                        addValues(7+cell,8+cell,16+cell,17+cell,neighboursToAdd);
                        break;
                }
                break;

            case 1:
                switch (modCol){
                    case 0:
                        addValues(cell-8,cell-7,10+cell,11+cell,neighboursToAdd);
                        break;
                    case 1:
                        addValues(cell-10,cell-8,cell+10,cell+8,neighboursToAdd);
                        break;
                    case 2:
                        addValues(cell-11,cell-10,cell+7,cell+8,neighboursToAdd);
                        break;
                }
                break;

            case 2:
                switch (modCol){
                    case 0:
                        addValues(cell-17,cell-16,cell-8,cell-7,neighboursToAdd);
                        break;
                    case 1:
                        addValues(cell-19,cell-17,cell-10,cell-8,neighboursToAdd);
                        break;
                    case 2:
                        addValues(cell-20,cell-19,cell-11,cell-10,neighboursToAdd);
                        break;
                }
                break;

            default: return neighboursToAdd;
        }
        return neighboursToAdd;
    }

    // Helper method to add values to an array list
    private void addValues(int a,int b,int c, int d,ArrayList<Integer> ntoadd){
        ntoadd.add(a);
        ntoadd.add(b);
        ntoadd.add(c);
        ntoadd.add(d);
    }


    // This is the Recursive AC3.
    private final boolean backtrack(int cell, ArrayList<Integer>[] Domains) {
        recursions += 1;

        // corresponding row and col in board, based on cell number
        int row = cell / 9;
        int col = cell % 9;

        if (cell > 80){ // done; all previous assignments made
            return true;
        }

        if (Domains[cell].size() == 1){
            // one value in cell's domain: call backtrack on next cell
            boolean isSatif = backtrack(cell+1,Domains);
            if (isSatif){
                // assignment worked, can update board
                vals[row][col] = Domains[cell].get(0);
                return true;
            }
            return false; // assignment didn't work
        }

        boolean canBeSatisfied = AC3(Domains);

        if (!canBeSatisfied){ // AC3 returned false: a cell's domain
            return false;	// became 0, so this assignment doesn't
        }					// work

        for (int j=0; j < Domains[cell].size(); j++){
            // iterates through cell's domain, to check each value if necessary

            // copies Domains
            ArrayList<Integer>[] cDomains = new ArrayList[81];
            for (int val = 0; val < cDomains.length; val++) {
                ArrayList<Integer> p = Domains[val];
                if (p != null) {
                    cDomains[val] = new ArrayList(p);
                }
            }

            // "try" the next value by setting the cell's domain
            // to be that value
            Integer valToTry = cDomains[cell].get(j);

            cDomains[cell] = new ArrayList<Integer>();
            cDomains[cell].add(valToTry);

            //calls backtrack to check next cells recursively
            boolean isSatif = backtrack(cell+1,cDomains);

            if (isSatif) { // assignment worked, update board
                vals[row][col] = valToTry;
                return true;
            }
        } // end for loop; no values left to try
        return false;
    }



    // This is the AC-3 Algorithm
    private final boolean AC3(ArrayList<Integer>[] Domains) {

        // copy queue
        Queue tempQueue = new LinkedList(globalQueue);

        while (true){
            if(tempQueue.isEmpty()){
                return true; // domain consistent
            }

            Arc temp = (Arc)tempQueue.poll();

            // update domains
            boolean isRevised = Revise(temp, Domains);

            if (Domains[temp.Xi].isEmpty()){
                // domain NOT consistent
                return false;
            }

            if (isRevised){
                // domain was updated for current Arc
                // add all neighbors to queue
                for (int neigh:neighbors[temp.Xi]){
                    Arc n = new Arc(neigh,temp.Xi);
                    tempQueue.add(n);
                }
            }
        }
    }



    // This is the Revise() method
    private final boolean Revise(Arc t, ArrayList<Integer>[] Domains){
        ops += 1;

        boolean revised = false;
        boolean valueExists;

        if (Domains[t.Xi].size() == 0){
            // if Domain size becomes 0, domain NOT consistent
            return false;
        }

        for (int val = 0;val < Domains[t.Xi].size(); val++){

            valueExists = false;

            for (int val2 : Domains[t.Xj]){
                // checks if value exists in Xj's domain that is
                // NOT equal to current value in Xi's domain
                if (Domains[t.Xi].get(val) != val2){
                    valueExists = true;
                }
            }

            if (!valueExists){
                // if no value in Xj's domain that satisfies
                // constraints, remove value from X's domain
                Domains[t.Xi].remove(val);
                revised = true;
            }
        }
        return revised;
    }




    private final void customSolver(){

        //’success’ should be set to true if a successful board
        //is found and false otherwise.
        board.Clear();
        ops = 0;
        recursions = 0;

        /**
         *  Populate data structures defined above
         *  These will be the data structures necessary for AC-3.
         **/

        for (int row = 0; row < BOARDDIMENSION; row++){
            for (int col = 0; col < BOARDDIMENSION; col++){

                int gDIndex = (row * BOARDDIMENSION) + col;
                globalDomains[gDIndex] = new ArrayList<Integer>();

                if (vals[row][col] == 0){
                    // value has not been assigned to board yet
                    // all values added to domain
                    globalDomains[gDIndex].addAll(Arrays.asList(1,2,3,4,5,6,7,8,9));
                }
                else{
                    // value has been assigned to board, it's domain size is 1
                    globalDomains[gDIndex].add(vals[row][col]);
                }
            }
        }

        // Define constraints between set of variables
        allDiff();

        System.out.println("Running custom algorithm");

        //-- Your Code Here --
        boolean success = customBacktrack(0,globalDomains);

        Finished(success);



    }

    private final boolean customBacktrack(int cell, ArrayList<Integer>[] Domains){
        recursions += 1;

        // corresponding row and col in board, based on cell number
        int row = cell / 9;
        int col = cell % 9;

        if (boardSolved()){ // done; all previous assignments made
            return true;
        }

        if (Domains[cell].size() == 1){
            // one value in cell's domain: call backtrack on next cell
            int nextCell = toBacktrack(Domains);
            boolean isSatif = backtrack(nextCell,Domains);
            if (isSatif){
                // assignment worked, can update board
                vals[row][col] = Domains[cell].get(0);
                return true;
            }
            return false; // assignment didn't work
        }

        boolean canBeSatisfied = AC3(Domains);

        if (!canBeSatisfied){ // AC3 returned false: a cell's domain
            return false;	// became 0, so this assignment doesn't
        }					// work

        //Check least constraining values in Domain
        PriorityQueue<Integer[]> domainVals = leastConstrainingValue(cell,Domains[cell],Domains);


        //for (int j=0; j < Domains[cell].size(); j++){
        while (!domainVals.isEmpty())    {
            // iterates through cell's domain, to check each value if necessary

            // copies Domains
            ArrayList<Integer>[] cDomains = new ArrayList[81];
            for (int val = 0; val < cDomains.length; val++) {
                ArrayList<Integer> p = Domains[val];
                if (p != null) {
                    cDomains[val] = new ArrayList(p);
                }
            }

            // "try" the next value by setting the cell's domain
            // to be that value
            Integer valToTry = domainVals.poll()[0];

            cDomains[cell] = new ArrayList<Integer>();
            cDomains[cell].add(valToTry);

            //calls backtrack to check next cells recursively
            int nextCell = toBacktrack(cDomains);
            boolean isSatif = backtrack(nextCell,cDomains);

            if (isSatif) { // assignment worked, update board
                vals[row][col] = valToTry;
                return true;
            }

        } // end for loop; no values left to try
        return false;
    }

    //Returns value that will allow maximum number of vlues for all other variables
    private PriorityQueue<Integer[]> leastConstrainingValue(int cell, ArrayList<Integer> cellDomain, ArrayList<Integer>[] Domains){

        //PriorityQueue orders elements according to leastConstrainingValue
        PriorityQueue<Integer[]> valsAndRevisions = new PriorityQueue<Integer[]>(10, new Comparator<Integer[]>() {
            @Override
            public int compare(Integer[] o1, Integer[] o2) {

                if (o1[1] > o2[1] ) {return -1;}
                else if (o2[1] > o2[1] ) {return 1;}

                return 0;
            }
        });

        //Call checkRevisions which returns total number of permissible values in other domains
        for (int val : cellDomain){

            int revisions = checkRevisions(cell,val,Domains);
            Integer[] temp = new Integer[2];
            temp[0] = val;
            temp[1] = revisions;
            valsAndRevisions.add(temp);
        }


        return valsAndRevisions;//returns PriorityQueue with elements according to leastConstrainingValues
    }

    private int checkRevisions(int cell, int possibleVal,ArrayList<Integer>[] Domains){

        ops += 1;

        int revisions;
        ArrayList<Integer> cellNeighbors = neighbors[cell];
            revisions = 0;
            //try val and check consistency with all neighbors
            for (int neighbor: cellNeighbors){
                //Get neighbor Domain
                ArrayList<Integer> neighborDomain = Domains[neighbor];

                //Check possible values in neighboring Domain
                if (neighborDomain.contains(Integer.valueOf(possibleVal))){
                    revisions += neighborDomain.size()-1;
                }

            }

        return revisions;
    }

  private int toBacktrack(ArrayList<Integer>[] Domains){
	  // implements minimum remaining values heuristic  
        int size;
        Integer min = Integer.MAX_VALUE;
        int minCell = -1;

      for (int i = 0; i < BOARDDIMENSION*BOARDDIMENSION; i++){

          if (vals[i/BOARDDIMENSION][i%BOARDDIMENSION] == 0){
              size = Domains[i].size();

              if (size < min){
            	  // if domain size is less than current minimum
            	  //assign new minimum cell 
                  minCell = i;
                  min = size;
              }
          }

      }

        return minCell;
    }

    //Brute force method to check if board is solved
    private Boolean boardSolved(){
        for (int i = 0; i < BOARDDIMENSION * BOARDDIMENSION;i++){
            if (vals[i/BOARDDIMENSION][i%BOARDDIMENSION] == 0) return false;
        }
        return true;
    }
    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    /// ---------- HELPER FUNCTIONS --------- ///
    /// ----   DO NOT EDIT REST OF FILE   --- ///
    public final boolean valid(int x, int y, int val){
        ops +=1;
        if (vals[x][y] == val)
            return true;
        if (rowContains(x,val))
            return false;
        if (colContains(y,val))
            return false;
        if (blockContains(x,y,val))
            return false;
        return true;
    }

    public final boolean blockContains(int x, int y, int val){
        int block_x = x / 3;
        int block_y = y / 3;
        for(int r = (block_x)*3; r < (block_x+1)*3; r++){
            for(int c = (block_y)*3; c < (block_y+1)*3; c++){
                if (vals[r][c] == val)
                    return true;
            }
        }
        return false;
    }

    public final boolean colContains(int c, int val){
        for (int r = 0; r < 9; r++){
            if (vals[r][c] == val)
                return true;
        }
        return false;
    }

    public final boolean rowContains(int r, int val) {
        for (int c = 0; c < 9; c++)
        {
            if(vals[r][c] == val)
                return true;
        }
        return false;
    }

    private void CheckSolution() {
        // If played by hand, need to grab vals
        board.updateVals(vals);

        /*for(int i=0; i<9; i++){
	        for(int j=0; j<9; j++)
	        	System.out.print(vals[i][j]+" ");
	        System.out.println();
        }*/

        for (int v = 1; v <= 9; v++){
            // Every row is valid
            for (int r = 0; r < 9; r++)
            {
                if (!rowContains(r,v))
                {
                    board.showMessage("Value "+v+" missing from row: " + (r+1));// + " val: " + v);
                    return;
                }
            }
            // Every column is valid
            for (int c = 0; c < 9; c++)
            {
                if (!colContains(c,v))
                {
                    board.showMessage("Value "+v+" missing from column: " + (c+1));// + " val: " + v);
                    return;
                }
            }
            // Every block is valid
            for (int r = 0; r < 3; r++){
                for (int c = 0; c < 3; c++){
                    if(!blockContains(r, c, v))
                    {
                        return;
                    }
                }
            }
        }
        board.showMessage("Success!");
    }



    /// ---- GUI + APP Code --- ////
    /// ----   DO NOT EDIT  --- ////
    enum algorithm {
        AC3, Custom
    }
    class Arc implements Comparable<Object>{
        int Xi, Xj;
        public Arc(int cell_i, int cell_j){
            if (cell_i == cell_j){
                try {
                    throw new Exception(cell_i+ "=" + cell_j);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.exit(1);
                }
            }
            Xi = cell_i;      Xj = cell_j;
        }

        public int compareTo(Object o){
            return this.toString().compareTo(o.toString());
        }

        public String toString(){
            return "(" + Xi + "," + Xj + ")";
        }
    }

    enum difficulty {
        easy, medium, hard, random
    }

    public void actionPerformed(ActionEvent e){
        String label = ((JButton)e.getSource()).getText();
        if (label.equals("AC-3"))
            init();
        else if (label.equals("Clear"))
            board.Clear();
        else if (label.equals("Check"))
            CheckSolution();
            //added
        else if(label.equals("Custom"))
            customSolver();
    }

    public void run() {
        board = new Board(gui,this);

        long start=0, end=0;

        while(!initialize());
        if (gui)
            board.initVals(vals);
        else {
            board.writeVals();
            System.out.println("Algorithm: " + alg);
            switch(alg) {
                default:
                case AC3:
                    start = System.currentTimeMillis();
                    init();
                    end = System.currentTimeMillis();
                    break;
                case Custom: //added
                    start = System.currentTimeMillis();
                    customSolver();
                    end = System.currentTimeMillis();
                    break;
            }

            CheckSolution();

            if(!gui)
                System.out.println("time to run: "+(end-start));
        }
    }

    public final boolean initialize(){
        switch(level) {
            case easy:
                vals[0] = new int[] {0,0,0,1,3,0,0,0,0};
                vals[1] = new int[] {7,0,0,0,4,2,0,8,3};
                vals[2] = new int[] {8,0,0,0,0,0,0,4,0};
                vals[3] = new int[] {0,6,0,0,8,4,0,3,9};
                vals[4] = new int[] {0,0,0,0,0,0,0,0,0};
                vals[5] = new int[] {9,8,0,3,6,0,0,5,0};
                vals[6] = new int[] {0,1,0,0,0,0,0,0,4};
                vals[7] = new int[] {3,4,0,5,2,0,0,0,8};
                vals[8] = new int[] {0,0,0,0,7,3,0,0,0};
                break;
            case medium:
                vals[0] = new int[] {0,4,0,0,9,8,0,0,5};
                vals[1] = new int[] {0,0,0,4,0,0,6,0,8};
                vals[2] = new int[] {0,5,0,0,0,0,0,0,0};
                vals[3] = new int[] {7,0,1,0,0,9,0,2,0};
                vals[4] = new int[] {0,0,0,0,8,0,0,0,0};
                vals[5] = new int[] {0,9,0,6,0,0,3,0,1};
                vals[6] = new int[] {0,0,0,0,0,0,0,7,0};
                vals[7] = new int[] {6,0,2,0,0,7,0,0,0};
                vals[8] = new int[] {3,0,0,8,4,0,0,6,0};
                break;
            case hard:
                vals[0] = new int[] {1,2,0,4,0,0,3,0,0};
                vals[1] = new int[] {3,0,0,0,1,0,0,5,0};
                vals[2] = new int[] {0,0,6,0,0,0,1,0,0};
                vals[3] = new int[] {7,0,0,0,9,0,0,0,0};
                vals[4] = new int[] {0,4,0,6,0,3,0,0,0};
                vals[5] = new int[] {0,0,3,0,0,2,0,0,0};
                vals[6] = new int[] {5,0,0,0,8,0,7,0,0};
                vals[7] = new int[] {0,0,7,0,0,0,0,0,5};
                vals[8] = new int[] {0,0,0,0,0,0,0,9,8};
                break;
            case random:
            default:
                ArrayList<Integer> preset = new ArrayList<Integer>();
                while (preset.size() < numCells)
                {
                    int r = rand.nextInt(81);
                    if (!preset.contains(r))
                    {
                        preset.add(r);
                        int x = r / 9;
                        int y = r % 9;
                        if (!assignRandomValue(x, y))
                            return false;
                    }
                }
                break;
        }
        return true;
    }

    public final boolean assignRandomValue(int x, int y){
        ArrayList<Integer> pval = new ArrayList<Integer>(Arrays.asList(1,2,3,4,5,6,7,8,9));

        while(!pval.isEmpty()){
            int ind = rand.nextInt(pval.size());
            int i = pval.get(ind);
            if (valid(x,y,i)) {
                vals[x][y] = i;
                return true;
            } else
                pval.remove(ind);
        }
        System.err.println("No valid moves exist.  Recreating board.");
        for (int r = 0; r < 9; r++){
            for(int c=0;c<9;c++){
                vals[r][c] = 0;
            }    }
        return false;
    }

    private void Finished(boolean success){
        if(success) {
            board.writeVals();
            board.showMessage("Solved in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
        } else {
            board.showMessage("No valid configuration found in " + myformat.format(ops) + " ops \t(" + myformat.format(recursions) + " recursive ops)");
        }
    }

    public static void main(String[] args) {

        Scanner scan = new Scanner(System.in);

        System.out.println("difficulty? \teasy (e), medium (m), hard (h), random (r)");

        char c='*';


        while(c!='e'&& c!='m'&&c!='n'&&c!='h'&&c!='r'){
            c = scan.nextLine().charAt(0);

            if(c=='e')
                level = difficulty.valueOf("easy");
            else if(c=='m')
                level = difficulty.valueOf("medium");
            else if(c=='h')
                level = difficulty.valueOf("hard");
            else if(c=='r')
                level = difficulty.valueOf("random");
            else{
                System.out.println("difficulty? \teasy (e), medium (m), hard (h), random(r)");
            }
            //System.out.println("2: "+c+" "+level);
        }

        System.out.println("Gui? y or n ");
        c=scan.nextLine().charAt(0);

        if (c=='n')
            gui = false;
        else
            gui = true;

        //System.out.println("c: "+c+", Difficulty: " + level);

        //System.out.println("Difficulty: " + level);

        if(!gui){
            System.out.println("Algorithm? AC3 (1) or Custom (2)");
            if(scan.nextInt()==1)
                alg = algorithm.valueOf("AC3");
            else
                alg = algorithm.valueOf("Custom");
        }

        SudokuPlayer app = new SudokuPlayer();

        app.run();

    }


    class Board {
        GUI G = null;
        boolean gui = true;

        public Board(boolean X, SudokuPlayer s) {
            gui = X;
            if (gui)
                G = new GUI(s);
        }

        public void initVals(int[][] vals){
            G.initVals(vals);
        }

        public void writeVals(){
            if (gui)
                G.writeVals();
            else {
                for (int r = 0; r < 9; r++) {
                    if (r % 3 == 0)
                        System.out.println(" ----------------------------");
                    for (int c = 0; c < 9; c++) {
                        if (c % 3 == 0)
                            System.out.print (" | ");
                        if (vals[r][c] != 0) {
                            System.out.print(vals[r][c] + " ");
                        } else {
                            System.out.print("_ ");
                        }
                    }
                    System.out.println(" | ");
                }
                System.out.println(" ----------------------------");
            }
        }

        public void Clear(){
            if(gui)
                G.clear();
        }

        public void showMessage(String msg) {
            if (gui)
                G.showMessage(msg);
            System.out.println(msg);
        }

        public void updateVals(int[][] vals){
            if (gui)
                G.updateVals(vals);
        }

    }

    class GUI {
        // ---- Graphics ---- //
        int size = 40;
        JFrame mainFrame = null;
        JTextField[][] cells;
        JPanel[][] blocks;

        public void initVals(int[][] vals){
            // Mark in gray as fixed
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (vals[r][c] != 0) {
                        cells[r][c].setText(vals[r][c] + "");
                        cells[r][c].setEditable(false);
                        cells[r][c].setBackground(Color.lightGray);
                    }
                }
            }
        }

        public void showMessage(String msg){
            JOptionPane.showMessageDialog(null,
                    msg,"Message",JOptionPane.INFORMATION_MESSAGE);
        }

        public void updateVals(int[][] vals) {

            // System.out.println("calling update");
            for (int r = 0; r < 9; r++) {
                for (int c=0; c < 9; c++) {
                    try {
                        vals[r][c] = Integer.parseInt(cells[r][c].getText());
                    } catch (java.lang.NumberFormatException e) {
                        System.out.println("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        showMessage("Invalid Board: row col: "+(r+1)+" "+(c+1));
                        return;
                    }
                }
            }
        }

        public void clear() {
            for (int r = 0; r < 9; r++){
                for (int c = 0; c < 9; c++){
                    if (cells[r][c].isEditable())
                    {
                        cells[r][c].setText("");
                        vals[r][c] = 0;
                    } else {
                        cells[r][c].setText("" + vals[r][c]);
                    }
                }
            }
        }

        public void writeVals(){
            for (int r=0;r<9;r++){
                for(int c=0; c<9; c++){
                    cells[r][c].setText(vals[r][c] + "");
                }   }
        }

        public GUI(SudokuPlayer s){

            mainFrame = new javax.swing.JFrame();
            mainFrame.setLayout(new BorderLayout());
            mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel gamePanel = new javax.swing.JPanel();
            gamePanel.setBackground(Color.black);
            mainFrame.add(gamePanel, BorderLayout.NORTH);
            gamePanel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            gamePanel.setLayout(new GridLayout(3,3,3,3));

            blocks = new JPanel[3][3];
            for (int i = 0; i < 3; i++){
                for(int j =2 ;j>=0 ;j--){
                    blocks[i][j] = new JPanel();
                    blocks[i][j].setLayout(new GridLayout(3,3));
                    gamePanel.add(blocks[i][j]);
                }
            }

            cells = new JTextField[9][9];
            for (int cell = 0; cell < 81; cell++){
                int i = cell / 9;
                int j = cell % 9;
                cells[i][j] = new JTextField();
                cells[i][j].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                cells[i][j].setHorizontalAlignment(JTextField.CENTER);
                cells[i][j].setSize(new java.awt.Dimension(size, size));
                cells[i][j].setPreferredSize(new java.awt.Dimension(size, size));
                cells[i][j].setMinimumSize(new java.awt.Dimension(size, size));
                blocks[i/3][j/3].add(cells[i][j]);
            }

            JPanel buttonPanel = new JPanel(new FlowLayout());
            mainFrame.add(buttonPanel, BorderLayout.SOUTH);
            //JButton DFS_Button = new JButton("DFS");
            //DFS_Button.addActionListener(s);
            JButton AC3_Button = new JButton("AC-3");
            AC3_Button.addActionListener(s);
            JButton Clear_Button = new JButton("Clear");
            Clear_Button.addActionListener(s);
            JButton Check_Button = new JButton("Check");
            Check_Button.addActionListener(s);
            //buttonPanel.add(DFS_Button);
            JButton Custom_Button = new JButton("Custom");
            Custom_Button.addActionListener(s);
            //added
            buttonPanel.add(AC3_Button);
            buttonPanel.add(Custom_Button);
            buttonPanel.add(Clear_Button);
            buttonPanel.add(Check_Button);






            mainFrame.pack();
            mainFrame.setVisible(true);

        }
    }

    Random rand = new Random();

    // ----- Helper ---- //
    static algorithm alg = algorithm.AC3;
    static difficulty level = difficulty.easy;
    static boolean gui = true;
    static int ops;
    static int recursions;
    static int numCells = 15;
    static DecimalFormat myformat = new DecimalFormat("###,###");
}



