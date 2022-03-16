package SudokuSolver;

import java.util.*;

public class BacktrackingSudokuSolver {
    
    public static int[][] solve(int[][] grid) {
        int[][] _grid = new int[grid.length][grid.length];
        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid.length; c++) {
                _grid[r][c] = grid[r][c];
            }
        }

        _solve(_grid);

        return _grid;
    }

    private static boolean _solve(int[][] grid) {
        int[] pos = findUnfilled(grid); // get the first unfilled position
        if (pos[0] == -1) {
            return true;   // no unfilled position found
        }

        int row = pos[0];
        int col = pos[1];

        Set<Integer> possibleNums = findPossibleNumbers(grid, row, col);
        for (int num : possibleNums) {
            grid[row][col] = num;
            if (_solve(grid) == true) {
                return true;        // solution was found
            }
            grid[row][col] = -1;    // no solution was found (yet)
        }

        // none of the possible numbers returned a winning val, this solution is not viable
        // thus start backtracking
        return false;
    }

    public static Set<Integer> findPossibleNumbers(int[][] grid, int row, int col) {
        Set<Integer> nums = new HashSet<>();

        boolean[] existingNums = new boolean[grid.length+1];
        testRow(grid, row, existingNums);
        testCol(grid, col, existingNums);
        testSubGrid(grid, row, col, existingNums);

        for (int i = 1; i < existingNums.length; i++) {
            if (existingNums[i] == false) {
                nums.add(i);
            }
        }

        return nums;
    }
    private static void testRow(int[][] grid, int row, boolean[] existingNums) {
        for (int col = 0; col < grid.length; col++) {
            if (grid[row][col] > 0) {
                existingNums[grid[row][col]] = true;
            }
        }
    }
    private static void testCol(int[][] grid, int col, boolean[] existingNums) {
        for (int row = 0; row < grid.length; row++) {
            if (grid[row][col] > 0) {
                existingNums[grid[row][col]] = true;
            }
        }
    }
    private static void testSubGrid(int[][] grid, int row, int col, boolean[] existingNums) {
        int n = (int) Math.round(Math.sqrt(grid.length));
        int sgRowStart = 0;
        int sgColStart = 0;
        while (sgRowStart+n <= row) sgRowStart += n;
        while (sgColStart+n <= col) sgColStart += n;

        for (int sgRow = sgRowStart; sgRow < sgRowStart+n; sgRow++) {
            for (int sgCol = sgColStart; sgCol < sgColStart+n; sgCol++) {
                if (grid[sgRow][sgCol] > 0) {
                    existingNums[grid[sgRow][sgCol]] = true;
                }
            }
        }
    }

    /**
     * finds and returns the first unfilled position of the grid.
     * @param grid the sudoku grid to search, if a position is unfilled (<= 0) then return the location
     * @return the location as a int[2], if no location is found it returns (-1,-1)
     */
    public static int[] findUnfilled(int[][] grid) {
        int[] unfilledPos = new int[2];
        unfilledPos[0] = -1;
        unfilledPos[1] = -1;
        int min = Integer.MAX_VALUE;

        for (int r = 0; r < grid.length; r++) {
            for (int c = 0; c < grid[r].length; c++) {
                int size = Integer.MAX_VALUE;
                if (grid[r][c] <= 0 && (size = findPossibleNumbers(grid, r, c).size()) < min) {
                    unfilledPos[0] = r;
                    unfilledPos[1] = c;
                    min = size;
                    return unfilledPos;
                }
            }
        }

        return unfilledPos;
    }

}
