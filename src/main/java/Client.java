import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
public class Client {

    public static void main(String[] args) {
        try (Socket socket = new Socket("127.0.0.1", 8989);
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        ) {
            String searchText = "артефакт простота проекта анализ выгоды";
            writer.println(searchText);
            String jsonText = reader.readLine();
            JsonElement jsonElement = JsonParser.parseString(jsonText);
            String[] lines = jsonElement.toString().split(",|\\|\\}");
            for (String line : lines) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
