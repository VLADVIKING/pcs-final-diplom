import com.google.gson.Gson;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        BooleanSearchEngine engine = new BooleanSearchEngine(new File("pdfs"));
        try (ServerSocket server = new ServerSocket(8989)) {
            while (true) {
                try (Socket socket = server.accept();
                     PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ) {
                    String searchRequest = reader.readLine();
                    Gson gson = new Gson();
                    List<PageEntry> pageEntries = new ArrayList<>();
                    pageEntries.addAll(engine.search(searchRequest));
                    writer.println(gson.toJson(pageEntries));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}