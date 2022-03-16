package SudokuSolver;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class SudokuTests {

    private static String sudokuInstancesPath;

    @BeforeAll
    public static void init() {
        sudokuInstancesPath = "src/test/data/sudoku_instances/";
    }

    private static int[][] parseLevel(String levelFileName) {
        int[][] sudoku = null;

        File level = new File(levelFileName);
        try (Scanner input = new Scanner(level)) {
            int n = input.nextInt();
            sudoku = new int[n*n][n*n];
            input.nextInt();    // skip random value
            for (int row = 0; row < n*n; row++) {
                for (int col = 0; col < n*n; col++) {
                    sudoku[row][col] = input.nextInt();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return sudoku;
    }

    private static boolean isValid(int[][] board, int row, int column) {
        // test row
        int occurrences = 0;
        for (int x = 0; x < board.length; x++) {
            if (board[row][x] == board[row][column]) occurrences++;
        }
        if (occurrences > 1) return false;
        
        // test column
        occurrences = 0;
        for (int y = 0; y < board.length; y++) {
            if (board[y][column] == board[row][column]) occurrences++;
        }
        if (occurrences > 1) return false;
        
        // test subsquares
        occurrences = 0;
        int subsize = (int) Math.round(Math.sqrt(board.length));
        int subXStart = (column / subsize) * subsize;
        int subYStart = (row / subsize) * subsize;
        int subXEnd = subXStart + subsize;
        int subYEnd = subYStart + subsize;
        for (int x = subXStart; x < subXEnd; x++) {
            for (int y = subYStart; y < subYEnd; y++) {
                if (board[x][y] == board[row][column]) occurrences++;
            }
        }
        if (occurrences > 1) return false;
        
        return true;
    }

    private static boolean isSolutionValid(int[][] grid) {
        if (grid == null) return false;
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (isValid(grid, row, col) == false) {
                    return false;
                }
            }
        }
        return true;
    }

    @Test
    public void testWithBasicLevelFiles() {
        String sudokuDirectoryPath = sudokuInstancesPath + "basic/";
        File sudokuDirectory = new File(sudokuDirectoryPath);
        String[] sudokuLevels = sudokuDirectory.list();

        List<int[][]> levels = new LinkedList<>();

        for (String levelName : sudokuLevels) {
            int[][] level = parseLevel(sudokuDirectoryPath + levelName);
            levels.add(level);
        }

        for (int[][] level : levels) {
            int[][] solution = SudokuDLX.solve(level);
            //assertThat(isSolutionValid(solution)).isTrue();
            assertThat(solution).isDeepEqualTo(BacktrackingSudokuSolver.solve(level));
        }
    }
    @Test
    public void testWithPruningLevelFiles() {
        String sudokuDirectoryPath = sudokuInstancesPath + "pruning/";
        File sudokuDirectory = new File(sudokuDirectoryPath);
        String[] sudokuLevels = sudokuDirectory.list();

        List<int[][]> levels = new LinkedList<>();

        for (String levelName : sudokuLevels) {
            int[][] level = parseLevel(sudokuDirectoryPath + levelName);
            levels.add(level);
        }

        for (int[][] level : levels) {
            int[][] solution = SudokuDLX.solve(level);
            assertThat(isSolutionValid(solution)).isTrue();
        }
    }
    @Test
    public void testWithVarSelectionLevelFiles() {
        String sudokuDirectoryPath = sudokuInstancesPath + "var_selection/";
        File sudokuDirectory = new File(sudokuDirectoryPath);
        String[] sudokuLevels = sudokuDirectory.list();

        List<int[][]> levels = new LinkedList<>();

        for (String levelName : sudokuLevels) {
            int[][] level = parseLevel(sudokuDirectoryPath + levelName);
            levels.add(level);
        }

        for (int[][] level : levels) {
            int[][] solution = SudokuDLX.solve(level);
            assertThat(isSolutionValid(solution)).isTrue();
        }
    }

}
