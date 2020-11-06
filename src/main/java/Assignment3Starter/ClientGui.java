package Assignment3Starter;

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
public class ClientGui implements Assignment3Starter.OutputPanel.EventHandlers {
	JDialog frame;
	PicturePanel picturePanel;
	OutputPanel outputPanel;
	boolean gameStarted = false;
	String currentMessage;
	Socket sock;
	OutputStream out;
	ObjectOutputStream os;
	BufferedReader bufferedReader;

	/**
	 * Construct dialog
	 * @throws IOException 
	 */
	public ClientGui(Socket sock) throws IOException {
		this.sock = sock;
		
		// get output channel
		this.out = sock.getOutputStream();
		// create an object output writer (Java only)
		this.os = new ObjectOutputStream(out);
		this.bufferedReader = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		
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
	}

	/**
	 * Shows the current state in the GUI
	 * @param makeModal - true to make a modal window, false disables modal behavior
	 */
	public void show(boolean makeModal) {
		frame.pack();
		System.out.println("bli");
		frame.setModal(makeModal);
		System.out.println("bla");
		frame.setVisible(true);
		System.out.println("blub");
		
	}

	/**
	 * Creates a new game and set the size of the grid 
	 * @param dimension - the size of the grid will be dimension x dimension
	 */
	public void newGame(int dimension) {
		picturePanel.newGame(dimension);
		outputPanel.appendOutput("Started new game with a " + dimension + "x" + dimension + " board.");
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
		String error = "";
		try {
			// insert the image
			if (picturePanel.insertImage(filename, row, col)) {
				// put status in output
				outputPanel.appendOutput("Inserting " + filename + " in position (" + row + ", " + col + ")");
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
	 * Change this to whatever you need
	 */
	@Override
	public void submitClicked() {
		// Pulls the input box text
		String input = outputPanel.getInputText();
		// if has input
		if (input.length() > 0) {
			// append input to the output panel
			outputPanel.appendOutput(input);
			// clear input text box
			outputPanel.setInputText("");
			this.newGame(Integer.valueOf(input));
		}
		if (!gameStarted) {
			currentMessage = "{'type': 'start', 'value' : '"+input+"'}";
			try {
				os.writeObject(currentMessage);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				System.out.println("what");
				String blub = this.bufferedReader.readLine();
//				System.out.println("blub");
				JSONObject json = new JSONObject(blub);
				System.out.println("read");
				System.out.println(json);
				
				byte[] imageByteArray = Base64.getDecoder().decode(json.getString("value"));
				System.out.println("Image");
				System.out.println(json.getString("value"));
				ByteArrayInputStream bis = new ByteArrayInputStream(imageByteArray);
				picturePanel.insertImage(bis);
				} catch (Exception e) {
					e.printStackTrace();
				}
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
			Socket sock = null;
			String host = "localhost";
			sock = new Socket(host, 8888); // connect to host and socket on port 8888

			ClientGui main = new ClientGui(sock);
			main.show(true);

			// write the whole message
			main.outputPanel.appendOutput("How many pieces do you want?");
			

			
//			os.writeObject( "");
//			os.writeObject(main.currentMessage);
//			// make sure it wrote and doesn't get cached in a buffer
//			os.flush();

		} catch (Exception e) {e.printStackTrace();}



		// be sure to run in terminal at least once: 
		//     gradle Maker --args="img/Pineapple-Upside-down-cake.jpg 2"

		// prepare the GUI for display
		//    main.newGame(2);
		//    
		//    // add images to the grid
		//    main.insertImage("img/Pineapple-Upside-down-cake_1_1.jpg", 1, 1);
		//    main.insertImage("img/Pineapple-Upside-down-cake_0_0.jpg", 0, 0);
		//    main.insertImage("img/Pineapple-Upside-down-cake_1_0.jpg", 1, 0);
		//    main.insertImage("img/Pineapple-Upside-down-cake_0_1.jpg", 0, 1);
		//    
		//    // show an error for a missing image file
		//    main.insertImage("does not exist.jpg", 0, 0);
		//    // show an error for too big coordinate
		//    main.insertImage("img/Pineapple-Upside-down-cake_1_0.jpg", 2, 0);
		//    // show an error for too little coordinate
		//    main.insertImage("img/Pineapple-Upside-down-cake_1_0.jpg", -1, 0);
		//    
		//    

		// run in terminal at least once in terminal: 
		//     gradle Maker --args="img/To-Funny-For-Words1.png 3"
		//
		//    main.newGame(3);
		//    for(int i = 0; i < 3; i++) {
		//      for (int j = 0; j < 3; j++) {
		//        main.insertImage("img/To-Funny-For-Words1_"+i+"_"+j+".jpg", i, j);
		//      }
		//    }

		// show the GUI dialog as modal

	}
}