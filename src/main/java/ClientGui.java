import java.awt.Dimension;

import org.json.*;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Base64;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

/**
 * The ClientGui class is a GUI frontend that displays an image grid, an input text box,
 * a button, and a text area for status. 
 * 
 * Methods of Interest
 * ----------------------
 * show(boolean modal) - Shows the GUI frame with the current state
 *     -> modal means that it opens the GUI and suspends background processes. Processing 
 *        still happens in the GUI. If it is desired to continue processing in the 
 *        background, set modal to false.
 * newGame(int dimension) - Start a new game with a grid of dimension x dimension size
 * insertImage(String filename, int row, int col) - Inserts an image into the grid
 * appendOutput(String message) - Appends text to the output panel
 * submitClicked() - Button handler for the submit button in the output panel
 * 
 * Notes
 * -----------
 * > Does not show when created. show() must be called to show he GUI.
 * 
 */
public class ClientGui implements OutputPanel.EventHandlers {
	JDialog frame;
	PicturePanel picturePanel;
	OutputPanel outputPanel;
	boolean gameStarted = false;
	String currentMessage;
	Socket sock;
	OutputStream out;
	ObjectOutputStream os;
	BufferedReader bufferedReader;
	int points = 10;
	String currentPrompt = "name";
	String playerName;

	/**
	 * Construct dialog
	 * @throws IOException 
	 */
	public ClientGui() throws IOException {
		frame = new JDialog();
		frame.setLayout(new GridBagLayout());
		frame.setMinimumSize(new Dimension(500, 500));
		frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


		// setup the top picture frame
		picturePanel = new PicturePanel();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.weighty = 0.25;
		frame.add(picturePanel, c);

		// setup the input, button, and output area
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.weighty = 0.75;
		c.weightx = 1;
		c.fill = GridBagConstraints.BOTH;
		outputPanel = new OutputPanel();
		outputPanel.addEventHandlers(this);
		frame.add(outputPanel, c);

		picturePanel.newGame(1);
		insertImage("img/hi.png", 0, 0);
		outputPanel.appendOutput("Hello, please tell me your name.");

	}

	/**
	 * Shows the current state in the GUI
	 * @param makeModal - true to make a modal window, false disables modal behavior
	 */
	public void show(boolean makeModal) {
		frame.pack();
		frame.setModal(makeModal);
		frame.setVisible(true);
	}

	/**
	 * Insert an image into the grid at position (col, row)
	 * 
	 * @param filename - filename relative to the root directory
	 * @param row - the row to insert into
	 * @param col - the column to insert into
	 * @return true if successful, false if an invalid coordinate was provided
	 * @throws IOException An error occured with your image file
	 */
	public boolean insertImage(String filename, int row, int col) throws IOException {
		System.out.println("Image insert");
		String error = "";
		try {
			// insert the image
			if (picturePanel.insertImage(filename, row, col)) {
				// put status in output
				// outputPanel.appendOutput("Inserting " + filename + " in position (" + row + ", " + col + ")"); // you can of course remove this
				return true;
			}
			error = "File(\"" + filename + "\") not found.";
		} catch(PicturePanel.InvalidCoordinateException e) {
			// put error in output
			error = e.toString();
		}
		outputPanel.appendOutput(error);
		return false;
	}

	/**
	 * Submit button handling
	 * 
	 * TODO: This is where your logic will go or where you will call appropriate methods you write. 
	 * Right now this method opens and closes the connection after every interaction, if you want to keep that or not is up to you. 
	 */
	@Override
	public void submitClicked() {
		System.out.println("submit clicked ");
	
		// Pulls the input box text
		String input = outputPanel.getInputText();
		currentMessage = "{'type': 'name', 'value' : '"+input+"'}";
		switch (currentPrompt) {
			case "name":
				playerName = input;
				outputPanel.appendOutput("Hello: " + playerName);
				outputPanel.appendOutput("Would you like to guess a city (ci) or a country (co) or see the leaderboard (leader)");
				outputPanel.setPoints(points);
				currentPrompt = "type";
				break;
			case "type":
				switch (input) {
					case "ci":
						outputPanel.appendOutput("Thank you " + playerName + ", I will show you a picture of a city and you have to guess which one it is");
						currentPrompt = "city";
						try {
							insertImage("img/city/rome.jpg", 0, 0);
						} catch (Exception e){
							System.out.println(e);
						}
						break;
					case "co":
						outputPanel.appendOutput("Thank you " + playerName + ", I will show you a picture of a country and you have to guess which one it is");
						currentPrompt = "country";
						try {
							insertImage("img/country/germany.jpg", 0, 0);
						} catch (Exception e){
							System.out.println(e);
						}
						break;
					case "leader":
						outputPanel.appendOutput("Thank you " + playerName + ", here is the leaderboard!");
						outputPanel.appendOutput("Would you like to play again (y/n)?");
						currentPrompt = "again";
						break;
					default:
						break;
				}
				break;
			case "again":
				switch (input) {
					case "y":
						outputPanel.appendOutput("Hello, please tell me your name.");
						currentPrompt = "name";
						break;
					default:
						outputPanel.appendOutput("Goodbye 😊");
						currentPrompt = "exit";
						break;
				}
				break;
			case "city":
				String wordToGuess = "rome";
				char[] wordToGuessArray = wordToGuess.toCharArray();
				char[] progress = new char[wordToGuess.length()];
				Arrays.fill(progress, '_');
				String currentProgress = new String(progress);
	
				int incorrectGuesses = 0;
				int maxIncorrectGuesses = 6;
				boolean wordGuessed = false;
	
				while (!wordGuessed && incorrectGuesses < maxIncorrectGuesses) {
					if (input.equals(wordToGuess)) {
						outputPanel.appendOutput("You got it! The word was " + wordToGuess);
						wordGuessed = true;
					} else {
						for (int i = 0; i < wordToGuessArray.length; i++) {
							if (input.charAt(0) == wordToGuessArray[i]) {
								progress[i] = input.charAt(0);
								currentProgress = new String(progress);
								}
								}
								if (currentProgress.equals(wordToGuess)) {
									outputPanel.appendOutput("You got it! The word was " + wordToGuess);
									wordGuessed = true;
								} else {
									incorrectGuesses++;
									outputPanel.appendOutput("Incorrect! You have " + (maxIncorrectGuesses - incorrectGuesses) + " left.");
									outputPanel.appendOutput("Current progress: " + currentProgress);
								}
							}
						}
				
						if (!wordGuessed) {
							outputPanel.appendOutput("You ran out of guesses. The word was " + wordToGuess);
						}
				
						outputPanel.appendOutput("Would you like to play again (y/n)?");
						currentPrompt = "again";
						break;
					case "country":
						String wordToGuessCo = "germany";
						char[] wordToGuessArrayCo = wordToGuessCo.toCharArray();
						char[] progressCo = new char[wordToGuessCo.length()];
						Arrays.fill(progressCo, '_');
						String currentProgressCo = new String(progressCo);
				
						int incorrectGuessesCo = 0;
						int maxIncorrectGuessesCo = 6;
						boolean wordGuessedCo = false;
				
						while (!wordGuessedCo && incorrectGuessesCo < maxIncorrectGuessesCo) {
							if (input.equals(wordToGuessCo)) {
								outputPanel.appendOutput("You got it! The word was " + wordToGuessCo);
								wordGuessedCo = true;
							} else {
								for (int i = 0; i < wordToGuessArrayCo.length; i++) {
									if (input.charAt(0) == wordToGuessArrayCo[i]) {
										progressCo[i] = input.charAt(0);
										currentProgressCo = new String(progressCo);
									}
								}
				
								if (currentProgressCo.equals(wordToGuessCo)) {
									outputPanel.appendOutput("You got it! The word was " + wordToGuessCo);
									wordGuessedCo = true;
								} else {
									incorrectGuessesCo++;
									outputPanel.appendOutput("Incorrect! You have " + (maxIncorrectGuessesCo - incorrectGuessesCo) + " left.");
									outputPanel.appendOutput("Current progress: " + currentProgressCo);
								}
							}
						}
				
						if (!wordGuessedCo) {
							outputPanel.appendOutput("You ran out of guesses. The word was " + wordToGuessCo);
						}
				
						outputPanel.appendOutput("Would you like to play again (y/n)?");
						currentPrompt = "again";
						break;
					default:
						break;
				}
	}

	/**
	 * Key listener for the input text box
	 * 
	 * Change the behavior to whatever you need
	 */
	@Override
	public void inputUpdated(String input) {
		if (input.equals("surprise")) {
			outputPanel.appendOutput("You found me!");
		}
	}


	public static void main(String[] args) throws IOException {
		// create the frame

		try {
			ClientGui main = new ClientGui();
			main.show(true);

		} catch (Exception e) {e.printStackTrace();}



	}
}
