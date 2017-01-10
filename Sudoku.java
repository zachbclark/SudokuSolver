
/////////////////////////////////////////////////////////////////////////////////
// CS 430 - Artificial Intelligence
// Project 4 - Sudoku Solver w/ Variable Ordering and Forward Checking
// File: Sudoku.java
//
// Group Member Names:
// Due Date:
// 
//
// Description: A Backtracking program in Java to solve the Sudoku problem.
// Code derived from a C++ implementation at:
// http://www.geeksforgeeks.org/backtracking-set-7-suduku/
/////////////////////////////////////////////////////////////////////////////////

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;

//import com.sun.corba.se.impl.io.TypeMismatchException;

public class Sudoku {
	// Constants
	final static int UNASSIGNED = 0; // UNASSIGNED is used for empty cells in
	// sudoku grid
	final static int N = 9; // N is used for size of Sudoku grid. Size will be
	// NxN
	static int numBacktracks = 0;

	// static ArrayList<SudokuCoord> coordsLeft = new ArrayList<SudokuCoord>();

	static boolean alreadyCalled = false;

	//static boolean[][][] varDomain = new boolean[9][9][9];
	

	/////////////////////////////////////////////////////////////////////
	// Main function used to test solver.
	public static void main(String[] args) throws FileNotFoundException {
		// Reads in from TestCase.txt (sample sudoku puzzle).
		// 0 means unassigned cells - You can search the Internet for more test
		// cases.

		// User enters which test case they want
		System.out.println("Enter the desired test case number (1, 2, 3, 4, 5): ");
		Scanner scan = new Scanner(System.in);
		String caseNum = scan.next();

		Scanner fileScan = new Scanner(new File("Case" + caseNum + ".txt"));

		// Reads case into grid 2D int array
		int grid[][] = new int[9][9];
		for (int r = 0; r < 9; r++) {
			String row = fileScan.nextLine();
			String[] cols = row.split(",");
			for (int c = 0; c < cols.length; c++)
				grid[r][c] = Integer.parseInt(cols[c].trim());
		}

		// Forward checking array
		boolean[][][] varDomain = new boolean[9][9][9];
		for (int i = 0; i < N; i++)
			for (int j = 0; j < N; j++)
				for (int k = 0; k < N; k++)
					//if (grid[i][j] == 0) // if an empty space
					varDomain[i][j][k] = true;

		// Prints out the unsolved sudoku puzzle (as is)
		System.out.println("Unsolved sudoku puzzle:");
		printGrid(grid);

		// Menus
		System.out.println("Enter the desired variable ordering function:");
		System.out.println("---------------------------------------------\n" 
				+ "1.) Default static ordering\n"
				+ "2.) Our original static ordering\n" 
				+ "3.) Random ordering\n"
				+ "4.) Minimum Remaining Values Ordering\n" 
				+ "5.) Maximum Remaining Values Ordering");

		int varOrderNum = scan.nextInt();

		System.out.println("Enter the desired inference method:");
		System.out.println("-----------------------------------\n" 
				+ "1.) None (standard backtracking search)\n"
				+ "2.) Forward checking");

		int infMethodNum = scan.nextInt();

		System.out.println("Algorithm Running ...");

		// Setup timer - Obtain the time before solving
		long stopTime = 0L;
		long startTime = System.currentTimeMillis();

		// Attempts to solve and prints results
		if (SolveSudoku(grid, varOrderNum, infMethodNum, varDomain) == true) {
			// Get stop time once the algorithm has completed solving the puzzle
			stopTime = System.currentTimeMillis();
			System.out.println("\n\nAlgorithmic runtime: " + (stopTime - startTime) + "ms");
			System.out.println("Number of backtracks: " + numBacktracks);

			// Sanity check to make sure the computed solution really IS solved
			if (!isSolved(grid)) {
				System.err.println("An error has been detected in the solution.");
				System.exit(0);
			}
			System.out.println("\n\nSolved sudoku puzzle:");
			printGrid(grid);
		} else
			System.out.println("No solution exists");
	}

	/////////////////////////////////////////////////////////////////////
	// Write code here which returns true if the sudoku puzzle was solved
	// correctly, and false otherwise. In short, it should check that each
	// row, column, and 3x3 square of 9 cells maintain the ALLDIFF constraint.
	private static boolean isSolved(int[][] grid) {
		// add: 1 + 2 + ... + 9 = 45
		// mul: 9! = 362880

		// Check rows and columns
		for (int i = 0; i < 9; i++) {
			int rowAdd = 0, rowMul = 1, colAdd = 0, colMul = 1;
			for (int j = 0; j < 9; j++) {
				rowAdd += grid[i][j];
				rowMul *= grid[i][j];
				colAdd += grid[j][i];
				colMul *= grid[j][i];
			}
			if (!(rowAdd == 45 && rowMul == 362880 && colAdd == 45 && colMul == 362880))
				return false;
		}

		// Check boxes
		for (int i = 0; i < 9; i += 3)
			for (int j = 0; j < 9; j += 3) {
				int boxAdd = 0, boxMul = 1;
				for (int k = 0; k < 3; k++)
					for (int m = 0; m < 3; m++) {
						boxAdd += grid[i + k][j + m];
						boxMul *= grid[i + k][j + m];
					}
				if (!(boxAdd == 45 && boxMul == 362880))
					return false;
			}
		return true;
	}

	/////////////////////////////////////////////////////////////////////
	// Takes a partially filled-in grid and attempts to assign values to
	// all unassigned locations in such a way to meet the requirements
	// for Sudoku solution (non-duplication across rows, columns, and boxes)
	/////////////////////////////////////////////////////////////////////
	static boolean SolveSudoku(int grid[][], int varOrderNum, int infMethodNum, boolean[][][] domain) {
		// TODO: Here, you will create an IF-ELSEIF-ELSE statement to select
		// the next variables using 1 of the 5 orderings selected by the user.
		// By default, it is hardcoded to the method FindUnassignedVariable(),
		// which corresponds to the "1) Default static ordering" option.
		// User enters which variable ordering function they want to use

		// Select next unassigned variable
		SudokuCoord variable = null;

		if (varOrderNum == 1)
			variable = FindUnassignedVariable(grid);
		else if (varOrderNum == 2)
			variable = MyOriginalStaticOrderingOpt2(grid);
		else if (varOrderNum == 3)
			variable = MyOriginalRandomOrderingOpt3(grid);
		else if (varOrderNum == 4)
			variable = MyMinRemainingValueOrderingOpt4(grid);
		else if (varOrderNum == 5)
			variable = MyMaxRemainingValueOrderingOpt5(grid);
		else
			System.out.println("Not a valid integer");

		// If there is no unassigned location, we are done
		if (variable == null)
			return true; // success!

		int row = variable.row;
		int col = variable.col;

		// (1) Plain ol' backtracking search
		if (infMethodNum == 1) {
			// consider digits 1 to 9
			for (int num = 1; num <= 9; num++) {
				// if looks promising
				if (isSafe(grid, row, col, num)) {
					// make tentative assignment
					grid[row][col] = num;

					// return, if success, yay!
					if (SolveSudoku(grid, varOrderNum, infMethodNum, domain))
						return true;

					// failure, un-assign & try again
					grid[row][col] = UNASSIGNED;
				}
			}
		}
		// (2) Forward checking
		else if (infMethodNum == 2) {

			// consider digits 1 to 9
			for (int num = 1; num <= 9; num++) {
				// if looks promising
				if (isSafe(grid, row, col, num)) {
					// make tentative assignment
					grid[row][col] = num;

					//Forward checking :(
					for (int i = 0; i < N; i++)
						if (grid[i][col] == UNASSIGNED)
							domain[i][col][num-1] = false;
					for (int i = 0; i < N; i++)
						if (grid[row][i] == UNASSIGNED)
							domain[row][i][num-1] = false;
					for (int i = 0; i < 3; i++)
						for (int j = 0; j < 3; j++)
							if (grid[i + row-row%3][j + col-col%3] == UNASSIGNED)
								domain[i + row-row%3][j + col-col%3][num-1] = false;

					// Check for any empty domains
					for (int i = 0; i < N; i++)
						for (int j = 0; j < N; j++)
							if (grid[i][j] == UNASSIGNED && isEmpty(domain[i][j])) { // if cell is unassigned and empty
								System.out.println(numBacktracks);
								
								// return, if success, yay!
								if (SolveSudoku(grid, varOrderNum, infMethodNum, domain))
									return true;
								
								// failure, un-assign & try again
								grid[row][col] = UNASSIGNED;
								numBacktracks++;
								return false; // backtrack!
							}

					// return, if success, yay!
					if (SolveSudoku(grid, varOrderNum, infMethodNum, domain))
						return true;

					// failure, un-assign & try again
					grid[row][col] = UNASSIGNED;
				}

			}
		}


		// Increment the number of backtracks
		numBacktracks++;
		return false; // This triggers backtracking
	}

	static boolean isEmpty(boolean[] array) {
		for (boolean bool : array)
			if (bool)
				return false;
		return true;
	}

	/////////////////////////////////////////////////////////////////////
	// Searches the grid to find an entry that is still unassigned. If
	// found, the reference parameters row, col will be set the location
	// that is unassigned, and true is returned. If no unassigned entries
	// remain, null is returned.
	/////////////////////////////////////////////////////////////////////
	static SudokuCoord FindUnassignedVariable(int grid[][]) {
		for (int row = 0; row < N; row++)
			for (int col = 0; col < N; col++)
				if (grid[row][col] == UNASSIGNED)
					return new SudokuCoord(row, col);
		return null;
	}

	/////////////////////////////////////////////////////////////////////
	// TODO: Implement the following orderings, as specified in the
	// project description. You MAY feel free to add extra parameters if
	// needed (you shouldn't need to for the first two, but it may prove
	// helpful for the last two methods).
	/////////////////////////////////////////////////////////////////////
	static SudokuCoord MyOriginalStaticOrderingOpt2(int grid[][]) {
		// yes i know this is terrible lol feel free to change it
		for (int row = N - 1; row >= 0; row--)
			for (int col = N - 1; col >= 0; col--)
				if (grid[row][col] == UNASSIGNED)
					return new SudokuCoord(row, col);
		return null;
	}

	static SudokuCoord MyOriginalRandomOrderingOpt3(int grid[][]) {
		Random r = new Random();
		ArrayList<SudokuCoord> coordsLeft = new ArrayList<SudokuCoord>();

		coordsLeft = UnassignedCoordsLeft(grid);

		if (coordsLeft.size() > 0) {
			int idx = r.nextInt(coordsLeft.size());
			SudokuCoord curr = coordsLeft.get(idx);
			return curr;
		}
		return null;
	}

	static SudokuCoord MyMinRemainingValueOrderingOpt4(int grid[][]) 
	{
		ArrayList<SudokuCoord> coordsLeft = new ArrayList<SudokuCoord>();
		coordsLeft = UnassignedCoordsLeft(grid);
		int mrv = 0;

		// create map of (SudokuCoord, possible # of values to be assigned)
		Map<SudokuCoord, Integer> map = new HashMap<SudokuCoord, Integer>();

		if (coordsLeft.size() > 0) 
		{
			for (SudokuCoord curr : coordsLeft) 
			{
				// consider digits 1 to 9
				for (int num = 1; num <= 9; num++) 
				{
					// if looks promising
					if (isSafe(grid, curr.row, curr.col, num))
						mrv++;
				}
				map.put(curr, mrv);

			}
			////////////////////////////////////////////////////////////////////////
			// Stack Overflow: https://stackoverflow.com/questions/16079338/how-to-get-all-the-min-values-from-a-hash-map-in-java
			// find minimum first
			int min = Integer.MAX_VALUE;
			for(Entry<SudokuCoord, Integer> entry : map.entrySet()) 
				min = Math.min(min, entry.getValue());

			// add all elements that have a value equal to min
			List<SudokuCoord> minCoordList = new ArrayList<SudokuCoord>();
			for(Entry<SudokuCoord, Integer> entry : map.entrySet()) 
			{
				if(entry.getValue() == min) 
				{
					minCoordList.add(entry.getKey());
				}
			}

			// tie breakers!
			if(minCoordList.size() > 1)
				return MostDependentCoord(minCoordList, grid);
			else
				return minCoordList.get(0);
			////////////////////////////////////////////////////////////////////////
		}

		return null;
	}

	static SudokuCoord MyMaxRemainingValueOrderingOpt5(int grid[][]) 
	{
		ArrayList<SudokuCoord> coordsLeft = new ArrayList<SudokuCoord>();
		coordsLeft = UnassignedCoordsLeft(grid);
		int mrv = 0;

		// create map of (SudokuCoord, possible # of values to be assigned)
		Map<SudokuCoord, Integer> map = new HashMap<SudokuCoord, Integer>();

		if (coordsLeft.size() > 0) 
		{
			for (SudokuCoord curr : coordsLeft) 
			{
				// consider digits 1 to 9
				for (int num = 1; num <= 9; num++) 
				{
					// if looks promising
					if (isSafe(grid, curr.row, curr.col, num))
						mrv++;
				}
				map.put(curr, mrv);

			}
			////////////////////////////////////////////////////////////////////////
			// Shameful Stack Overflow: https://stackoverflow.com/questions/16079338/how-to-get-all-the-min-values-from-a-hash-map-in-java
			// find minimum first
			int max = Integer.MIN_VALUE;
			for(Entry<SudokuCoord, Integer> entry : map.entrySet()) 
				max = Math.max(max, entry.getValue());

			// add all elements that have a value equal to max
			List<SudokuCoord> maxCoordList = new ArrayList<SudokuCoord>();
			for(Entry<SudokuCoord, Integer> entry : map.entrySet()) 
			{
				if(entry.getValue() == max) 
				{
					maxCoordList.add(entry.getKey());
				}
			}

			// tie breakers!
			if(maxCoordList.size() > 1)
				return MostDependentCoord(maxCoordList, grid);
			else
				return maxCoordList.get(0);
			////////////////////////////////////////////////////////////////////////
		}

		return null;
	}

	static ArrayList<SudokuCoord> UnassignedCoordsLeft(int grid[][]) {
		ArrayList<SudokuCoord> Coords = new ArrayList<SudokuCoord>();

		for (int row = 0; row < 9; row++)
			for (int col = 0; col < N; col++)
				if (grid[row][col] == UNASSIGNED)
					Coords.add(new SudokuCoord(row, col));
		return Coords;
	}

	static SudokuCoord MostDependentCoord(List<SudokuCoord> coordList, int grid[][]) 
	{
		// This method returns the SudokuCoord that has the max number of other SudokuCoords dependent upon it
		int unassignedVars = 0;
		Map<SudokuCoord, Integer> map = new HashMap<SudokuCoord, Integer>();

		// row & col
		for (SudokuCoord c : coordList) 
		{	
			// row
			for (int col = 0; col < N; col++)
				if (grid[c.row][col] == UNASSIGNED)
					unassignedVars++;

			// col
			for (int row = 0; row < N; row++)
				if (grid[row][c.col] == UNASSIGNED)
					unassignedVars++;

			int boxStartRow = c.row - c.row % 3;
			int boxStartCol = c.col - c.col % 3;

			// box
			for (int row = 0; row < 3; row++)
				for (int col = 0; col < 3; col++)
					if (grid[row + boxStartRow][col + boxStartCol] == UNASSIGNED)
						unassignedVars++;

			map.put(c, unassignedVars);
		}

		// source: https://stackoverflow.com/questions/5911174/finding-key-associated-with-max-value-in-a-java-map
		Map.Entry<SudokuCoord, Integer> maxEntry = null;

		for (Map.Entry<SudokuCoord, Integer> entry : map.entrySet())
		{
			if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
			{
				maxEntry = entry;
			}
		}

		return maxEntry.getKey();
	}


	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether any assigned entry
	// in the specified row matches the given number.
	/////////////////////////////////////////////////////////////////////
	static boolean UsedInRow(int grid[][], int row, int num) {
		for (int col = 0; col < N; col++)
			if (grid[row][col] == num)
				return true;
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether any assigned entry
	// in the specified column matches the given number.
	/////////////////////////////////////////////////////////////////////
	static boolean UsedInCol(int grid[][], int col, int num) {
		for (int row = 0; row < N; row++)
			if (grid[row][col] == num)
				return true;
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether any assigned entry
	// within the specified 3x3 box matches the given number.
	/////////////////////////////////////////////////////////////////////
	static boolean UsedInBox(int grid[][], int boxStartRow, int boxStartCol, int num) {
		for (int row = 0; row < 3; row++)
			for (int col = 0; col < 3; col++)
				if (grid[row + boxStartRow][col + boxStartCol] == num)
					return true;
		return false;
	}

	/////////////////////////////////////////////////////////////////////
	// Returns a boolean which indicates whether it will be legal to assign
	// num to the given row, col location.
	/////////////////////////////////////////////////////////////////////
	static boolean isSafe(int grid[][], int row, int col, int num) {
		// Check if 'num' is not already placed in current row,
		// current column and current 3x3 box
		return !UsedInRow(grid, row, num) && !UsedInCol(grid, col, num)
				&& !UsedInBox(grid, row - row % 3, col - col % 3, num);
	}

	/////////////////////////////////////////////////////////////////////
	// A utility function to print grid
	/////////////////////////////////////////////////////////////////////
	static void printGrid(int grid[][]) {
		for (int row = 0; row < N; row++) {
			for (int col = 0; col < N; col++) {
				if (grid[row][col] == 0)
					System.out.print("- ");
				else
					System.out.print(grid[row][col] + " ");

				if ((col + 1) % 3 == 0)
					System.out.print(" ");
			}
			System.out.print("\n");
			if ((row + 1) % 3 == 0)
				System.out.println();
		}
	}
}