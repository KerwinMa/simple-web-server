import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public final class WebServer {

	private final static int PORT = 8080;
	// FILEPATH points to root of web server files
	private final static String FILEPATH = "";
	private final static String SERVERSTRING = "Server: Jastonex/0.1";

	// Muh relatively hidden Java techniques
	// Mime map
	private static final Map<String, String> mimeMap = new HashMap<String, String>() {{
		put("html", "text/html");
		put("css", "text/css");
		put("js", "application/js");
		put("jpg", "image/jpg");
		put("jpeg", "image/jpeg");
		put("png", "image/png");
	}};

	private static void respondHeader(String code, String mime, DataOutputStream out) throws Exception {
		System.out.println(" (" + code + ") ");
		out.writeBytes("HTTP/1.0 " + code + " OK\r\n");
		out.writeBytes("Content-Type: " + mimeMap.get(mime) + "\r\n"); 
		out.writeBytes(SERVERSTRING);
		out.writeBytes("\r\n\r\n");
	}

	private static void respondContent(String inString, DataOutputStream out) throws Exception {
		String method = inString.substring(0, inString.indexOf("/")-1);
		String file = inString.substring(inString.indexOf("/")+1, inString.lastIndexOf("/")-5);
		// Set default file to index.html
		if(file.equals(""))
			file = "index.html";	
		String mime = file.substring(file.indexOf(".")+1);		

		if(method.equals("GET")) {
			try {
				// Open file
				byte[] fileBytes = null;
				InputStream is = new FileInputStream(FILEPATH+file);
				fileBytes = new byte[is.available()];
				is.read(fileBytes);
	
				// Send header
				respondHeader("200", mime, out);
				
				// Write content of file
				out.write(fileBytes);
			
			} catch(FileNotFoundException e) {
				// Try to use 404.html
				try {
					byte[] fileBytes = null;
					InputStream is = new FileInputStream(FILEPATH+"404.html");
					fileBytes = new byte[is.available()];
					is.read(fileBytes);
					respondHeader("404", "html", out);
					out.write(fileBytes);
				} catch(FileNotFoundException e2) {
					respondHeader("404", "html", out);
					out.write(("404 File Not Found").getBytes());
				}
			}
		} else if(method.equals("POST")) {

		} else if(method.equals("HEAD")) {
			respondHeader("200", "html", out);
		} else {
			respondHeader("501", "html", out);
		}
	}

	private static class WorkerRunnable implements Runnable {

		protected Socket socket = null;

		BufferedReader in;
		DataOutputStream out;
		String inString;

		public WorkerRunnable(Socket connectionSocket) throws Exception {
			this.socket = connectionSocket;
			this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			this.out = new DataOutputStream(this.socket.getOutputStream());

			this.inString = this.in.readLine();

			Calendar cal = Calendar.getInstance();
			cal.getTime();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			String time = "[" + sdf.format(cal.getTime()) + "] ";
			System.out.print(time + this.socket.getInetAddress().toString() + " " + this.inString);			
		}

		public void run() {
			try{
				if(this.inString != null)
					respondContent(this.inString, this.out);

				this.out.flush();
				this.out.close();
				this.in.close();

			} catch (Exception e) { 
				System.out.println("Error flushing and closing");				
			}
		}
	}

	public static void main(String argv[]) throws Exception {
		ServerSocket serverSocket = new ServerSocket(PORT);

		for(;;) {
			Socket connectionSocket = serverSocket.accept();
		
			new Thread(new WorkerRunnable(connectionSocket)).start();	
		}
	}
}
