package SudokuSolver;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import SudokuSolver.SudokuDLX;

import java.io.IOException;
import java.util.regex.Pattern;
import java.util.Arrays;

public class App 
{
    public static void main( String[] args ) throws IOException
    {

        // Setup terminal and screen layers
        Terminal terminal = new DefaultTerminalFactory().createTerminal();
        Screen screen = new TerminalScreen(terminal);
        screen.startScreen();

        // Create panel to hold components
        Panel panel = new Panel();
        panel.setLayoutManager(new GridLayout(11));

	TextBox[] fields = new TextBox[9*9];

	for (int i = 0; i < 9; i++) {
	    panel.addComponent(new EmptySpace(new TerminalSize(0,0)));
	    for (int j = 0; j < 9; j++) {
		TextBox tb = new TextBox(new TerminalSize(2,1)).setValidationPattern(Pattern.compile("[1-9]"));
		fields[i*9+j] = tb;
		panel.addComponent(tb);
	    }
	    panel.addComponent(new EmptySpace(new TerminalSize(0,0)));
	    // create some empty space
	    for (int j = 0; j < 11; j++) {
		panel.addComponent(new EmptySpace(new TerminalSize(1,1)));
	    }
	}

	new Button("Clear", new Runnable() {
		@Override
		public void run() {
		    for (TextBox tb: fields) {
			tb.setText("");
		    }
		}
	    }).addTo(panel);
	for (int i = 0; i < 9; i++) {
	    panel.addComponent(new EmptySpace(new TerminalSize(0,0)));
	}
	new Button("Solve", new Runnable() {
		@Override
		public void run() {
		    int[][] grid = new int[9][9];
		    int i = 0;
		    for (TextBox tb : fields) {
			int a = i / 9;
			int b = i % 9;
			try {
			    grid[a][b] = Integer.parseInt(tb.getText());
			} catch (NumberFormatException e) {
			    grid[a][b] = 0;
			}
			i++;
		    }
		    int[][] solution = SudokuDLX.solve(grid);
		    i = 0;
		    for (TextBox tb : fields) {
			int a = i / 9;
			int b = i % 9;
			tb.setText(Integer.toString(solution[a][b]));
			i++;
		    }
		}
	    }).addTo(panel);

        // Create window to hold the panel
        BasicWindow window = new BasicWindow();
	window.setHints(Arrays.asList(Window.Hint.CENTERED));
        window.setComponent(panel);

        // Create gui and start gui
        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLUE));
        gui.addWindowAndWait(window);
        // TODO: link with sudoku solvers to create T/GUI for this application
    }
}
