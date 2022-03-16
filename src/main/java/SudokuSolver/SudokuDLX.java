package SudokuSolver;

import java.util.*;

/*
##########################
https://www.geeksforgeeks.org/exact-cover-problem-algorithm-x-set-1
    - for understanding the basics of the exact cover problem
https://medium.com/javarevisited/building-a-sudoku-solver-in-java-with-dancing-links-180274b0b6c1
    - for the logic behind the dancing links data structure
https://shivankaul.com/blog/sudoku
https://www.ocf.berkeley.edu/~jchu/publicportal/sudoku/sudoku.paper.html
    - namely the handwritten cover matrix of the 4x4 sudoku
##########################
 */

public class SudokuDLX {

    public static int[][] solve(int[][] grid) {
        ColumnNode header = createDLX(grid);
        List<DancingNode> solution = new ArrayList<>();
        boolean isSolved = _solve(header, solution);

        // System.out.println("found solution? " + isSolved);

        if (isSolved) {
            int[][] solved = reconstructSolution(grid, solution);
            return solved;
        } else return null;
    }

    private static int[][] reconstructSolution(int[][] grid, List<DancingNode> solution) {
        int[][] solved = new int[grid.length][grid.length];
        int n = grid.length;

        for (DancingNode s : solution) {
            int[] posVal = getPositionAndValue(s, n);
            solved[posVal[0]][posVal[1]] = posVal[2];
        }

        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                if (grid[row][col] >= 1) {
                    solved[row][col] = grid[row][col];
                }
            }
        }

        return solved;
    }

    private static boolean _solve(ColumnNode header, List<DancingNode> solution) {
        if (header.right == header) {
            // we have reached a solution, all constraints are met
            return true;
        } else {
            ColumnNode c = selectColumnNode(header);

            c.cover();
            for (DancingNode row = c.bottom; row != c; row = row.bottom) {
                solution.add(row);
                // cover all columns/constraints the picked row (val x pos combi) satisfies
                for (DancingNode sat = row.right; sat != row; sat = sat.right) {
                    sat.column.cover();
                }

                boolean reachedSolution = _solve(header, solution);
                if (reachedSolution) return true;

                // no solution reached, undo choice
                solution.remove(solution.size()-1);
                for (DancingNode sat = row.right; sat != row; sat = sat.right) {
                    sat.column.uncover();
                }
            }
            // the current game state cannot reach a solution, undo choices and start backtracking
            c.uncover();
            // System.out.println("no solution for this gamestate");

            return false;
        }
    }

    public static ColumnNode selectColumnNode(ColumnNode header) {
        ColumnNode selected = null;
        int min = Integer.MAX_VALUE;
        for (ColumnNode c = header.right.column; c != header; c = c.right.column) {
            if (c.size >= min) continue;
            selected = c;
            min = c.size;
        }
        // System.out.println("selected column: " + selected.id + " with " + min + " rows");
        return selected;
    }

    public static ColumnNode createDLX(int[][] grid) {
        int[][] coverMatrix = createCoverMatrix(grid);
        // Solution2.print2dArray(coverMatrix);
        int n = grid.length;
        ColumnNode header = new ColumnNode(-1);
        List<ColumnNode> columns = new ArrayList<>(4*n*n);
        for (int i = 0; i < coverMatrix.length; i++) {
            ColumnNode c = new ColumnNode(i);
            header = header.linkRight(c).column;
            columns.add(c);
        }
        header = header.right.column;   // return to the original header

        for (int[] constraints : coverMatrix) {
            DancingNode prev = null;
            for (int j = 0; j < constraints.length; j++) {
                if (constraints[j] != 1) continue;
                ColumnNode c = columns.get(j);
                DancingNode node = new DancingNode(c);
                c.top.linkDown(node);
                c.size++;
                if (prev == null) prev = node;
                prev = prev.linkRight(node);
            }
        }

        for (ColumnNode c : columns) {
            if (c.size == 0) c.cover();
        }

        return header;
    }

    public static int[][] createCoverMatrix(int[][] grid) {
        final int n = grid.length;
        // 4*n*n: each row needs every value (n*n) 
                //+ each col needs every value (n*n)
                //+ each subgrid needs every value (n*n)
                //+ each cell needs to be occupied (n*n)
        int[][] coverMatrix = new int[n*n*n][4*n*n];
        initCoverMatrix(coverMatrix, n);

        // remove constraints that are already satisfied by the initial board
        // and remove all val x pos combinations that satisfy that constraint (as they cannot exist anymore)
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                if (grid[row][col] <= 0) continue;

                int val = grid[row][col];
                int index = (row*n*n) + (col*n) + (val-1);
                // loop through all 'headers'/constraints to find all constraints this val x pos combination satisfies
                for (int header = 0; header < 4*n*n; header++) {
                    if (coverMatrix[index][header] != 1) continue;  // val pos combination does not satisfy constraint
                    // found a constraint that is satisfied by this val x pos combination
                    // remove all positions that satisfy this constraint
                    for (int i = 0; i < n*n*n; i++) {
                        if (i == index) continue;   // we need this row for later to find all the other constraints
                        if (coverMatrix[i][header] != 1) continue;
                        // remove val x pos combination
                        for (int j = 0; j < 4*n*n; j++) coverMatrix[i][j] = 0;
                    }
                    coverMatrix[index][header] = 0;
                }
            }
        }

        return coverMatrix;
    }

    // cover matrix headers are cells -> rows -> cols -> subgrids
    private static void initCoverMatrix(int[][] cover, int n) {
        final int SUBGRID_SIZE = (int) Math.round(Math.sqrt(n));
        for (int row = 0; row < n; row++) {
            for (int col = 0; col < n; col++) {
                for (int val = 0; val < n; val++) {
                    int index = (row*n*n) + (col*n) + (val);
                    // cell constraint, 1 value in a cell satisfies its cellconstraint
                    cover[index][(row*n) + col] = 1;
                    // row constraint, 1 value satisfies a single value x row constraint
                    cover[index][n*n + (row*n) + val] = 1;
                    // col constraint, 1 value satisfies a single value x col constraint
                    cover[index][2*n*n + (col*n) + val] = 1;
                    // subgrid constraint, 1 value satisfies a single value for a subgrid
                    // subgrid is defined as a values [0-n) from left to right, top to bottom
                    int subgrid = ((row/SUBGRID_SIZE)*SUBGRID_SIZE) + (col/SUBGRID_SIZE);
                    cover[index][3*n*n + (subgrid*n) + val] = 1;
                }
            }
        }
    }

    public static int[] getPositionAndValue(DancingNode node, int n) {
        // get utmost left DancingNode, as its column is the cell constraint with which we can get its position
        DancingNode cellConstraint = node;
        for (DancingNode j = node.left; j != node; j = j.left) {
            if (j.column.id >= cellConstraint.column.id) continue;
            cellConstraint = j;
        }
        int row = cellConstraint.column.id / n;
        int col = cellConstraint.column.id % n;
        // the DancingNode right of cellconstraint is in its row constraint column, whose id = n*n + row*n + val
        int val = (cellConstraint.right.column.id - n*n) % n+1;
        return new int[]{row, col, val};
    }
    
}

class DancingNode {
    public DancingNode left, right, top, bottom;
    public ColumnNode column;

    public DancingNode() {
        left = right = top = bottom = this;
    }
    public DancingNode(ColumnNode c) {
        this();
        this.column = c;
    }

    public DancingNode linkDown(DancingNode node) {
        node.bottom = this.bottom;
        node.bottom.top = node;
        node.top = this;
        this.bottom = node;
        return node;
    }

    public DancingNode linkRight(DancingNode node) {
        node.right = this.right;
        node.right.left = node;
        node.left = this;
        this.right = node;
        return node;
    }

    public void removeTB() {
        this.top.bottom = this.bottom;
        this.bottom.top = this.top;
    }
    public void reinsertTB() {
        this.top.bottom = this;
        this.bottom.top = this;
    }

    public void removeLR() {
        this.left.right = this.right;
        this.right.left = this.left;
    }
    public void reinsertLR() {
        this.left.right = this;
        this.right.left = this;
    }
}

class ColumnNode extends DancingNode {
    public int id;
    public int size;

    public ColumnNode(int id) {
        super();
        this.id = id;
        this.size = 0;
        this.column = this;
    }

    public void cover() {
        removeLR();
        for (DancingNode i = this.bottom; i != this; i = i.bottom) {
            for (DancingNode j = i.right; j != i; j = j.right) {
                j.removeTB();
                j.column.size--;
            }
        }
    }
    public void uncover() {
        for (DancingNode i = this.bottom; i != this; i = i.bottom) {
            for (DancingNode j = i.right; j != i; j = j.right) {
                j.reinsertTB();
                j.column.size++;
            }
        }
        reinsertLR();
    }
}
