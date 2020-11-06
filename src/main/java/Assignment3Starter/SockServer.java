package Assignment3Starter;
import java.net.*;
import java.util.Base64;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import java.awt.image.BufferedImage;
import java.io.*;
import org.json.*;


/**
 * A class to demonstrate a simple client-server connection using sockets.
 * Ser321 Foundations of Distributed Software Systems
 * see http://pooh.poly.asu.edu/Ser321
 * @author Tim Lindquist Tim.Lindquist@asu.edu
 *         Software Engineering, CIDSE, IAFSE, ASU Poly
 * @version August 2020
 * 
 * @modified-by David Clements <dacleme1@asu.edu> September 2020
 */
public class SockServer {
  public static void main (String args[]) {
    Socket sock;
    try {
      //open socket
      ServerSocket serv = new ServerSocket(8888); // create server socket on port 8888
      System.out.println("Server ready for connetion");

        System.out.println("Server waiting for a connection");
        sock = serv.accept(); // blocking wait
        // setup the object reading channel
        ObjectInputStream in = new ObjectInputStream(sock.getInputStream());
        OutputStream out = sock.getOutputStream();
        GridMaker maker = new GridMaker();
        // create an object output writer (Java only)
        
        
        // read in one object, the message. we know a string was written only by knowing what the client sent. 
        // must cast the object from Object to desired type to be useful
        while(true) {
        String s = (String) in.readObject();
        JSONObject json = new JSONObject(s);
        System.out.println("Received the String "+s);
        
        if (json.getString("type").equals("start")){
    			System.out.println("- Got a start");
//    			maker.createImages("img/To-Funny-For-Words1.png", json.getInt("value"));
    			File file = new File("img/Pineapple-Upside-down-cake_0_0.jpg");
    		    if (file.exists()) {
    		      // import image
    		    	FileInputStream imageInFile = new FileInputStream(file);
    	            byte imageData[] = new byte[(int) file.length()];
    	            imageInFile.read(imageData);

    	            //Image conversion byte array in Base64 String
    	            
    	            String imageDataString = Base64.getEncoder().encodeToString(imageData);
    	            imageInFile.close();
    	            System.out.println("Image Successfully Manipulated!");

    	            //the object that will be send to Server
    	            JSONObject obj = new JSONObject();

    	            //name of the image
    	            obj.put("type","image");
    	            //string obteined by the conversion of the image
    	            System.out.println("tet");
    	            System.out.println(imageDataString);
    	            obj.put("value",imageDataString );
    	            
    		      PrintWriter outWrite = new PrintWriter(sock.getOutputStream(), true);
    		      outWrite.println(obj.toString());
    		    }
        }
    			
        }
//        ObjectOutputStream os = new ObjectOutputStream(out);
//        // make sure it wrote and doesn't get cached in a buffer
//        os.flush();

    } catch(Exception e) {e.printStackTrace();}
  }
}
