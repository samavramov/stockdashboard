import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Scanner;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class aquireSentiment{

    private static final String DIFFBOT_TOKEN = "91ef285fb5465e5d1717853ff8284ecb";
    private static final String DIFFBOT_ENDPOINT = "https://api.diffbot.com/v3/article";

    // produce url for Diffbot API request
    public static String buildDiffbotUrl(String articleUrl) throws Exception {
        String encodedUrl = URLEncoder.encode(articleUrl, StandardCharsets.UTF_8.toString());
        String fields = "title,text,sentiment";
        return DIFFBOT_ENDPOINT
             + "?token=" + DIFFBOT_TOKEN
             + "&url="   + encodedUrl
             + "&fields=" + fields;
    }

    /**
     * Performs a GET to the given URL and returns the JSON response as a String.
     */
    public static String fetchHttpGet(String urlString) throws Exception {

        // Create a URL object from the string we created
        URL url = java.net.URI.create(urlString).toURL();

        // Open a connection to the URL
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        // Specify that we want to get data
        conn.setRequestMethod("GET");

        // Accept json response
        conn.setRequestProperty("Accept", "application/json");

        // Set timeouts for connection and read
        conn.setConnectTimeout(30_000); 
        conn.setReadTimeout(30_000);    

        // Save the response as a String
        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();

        // Return the response as a String
        return sb.toString();
    }

    public static double getSentiment(String url){
        try {
          
            // Make diffbot URL
            String diffbotUrl = buildDiffbotUrl(url);
            System.out.println("Made a Diffbot request with the following URL:\n  " + diffbotUrl);

            //String jsonResponse = fetchHttpGet(diffbotUrl);
            String jsonResponse = 
            System.out.println("\nRaw Diffbot JSON:\n" + jsonResponse +"\n\n");
             JSONObject root = new JSONObject(jsonResponse);

        // 3. Get the "objects" array if it exists:
        if (root.has("objects")) {
            JSONArray objectsArray = root.getJSONArray("objects");
            for (int i = 0; i < objectsArray.length(); i++) {
            JSONObject obj = objectsArray.getJSONObject(i);

        // 1. (Optionally) print the overall article sentiment:
        if (obj.has("sentiment")) {
            double articleSent = obj.getDouble("sentiment");
            System.out.println("Article‐level sentiment: " + articleSent);
        }

        // 2. Check the "tags" array for a tag‐object whose "label" is "Donald Trump"
        if (obj.has("tags")) {
            JSONArray tagsArray = obj.getJSONArray("tags");

            boolean foundTrumpTag = false;
            for (int j = 0; j < tagsArray.length(); j++) {
                JSONObject tag = tagsArray.getJSONObject(j);

                // Compare label (case‐insensitive or exact, depending on your JSON)
                if (tag.has("label") 
                    && "Donald Trump".equalsIgnoreCase(tag.getString("label"))) 
                {
                    // We found the "Donald Trump" tag—print its sentiment:
                    if (tag.has("sentiment")) {
                        double trumpSent = tag.getDouble("sentiment");
                        System.out.println("Donald Trump tag sentiment = " + trumpSent);
                    } else {
                        System.out.println("Donald Trump tag exists but no 'sentiment' field.");
                    }
                    foundTrumpTag = true;
                    break;  // stop looking once we found it
                }
            }

            if (!foundTrumpTag) {
                System.out.println("No tag called \"Donald Trump\" in object #" + i);
            }
        } else {
            System.out.println("No \"tags\" array in object #" + i);
        }
    }
        } else {
            System.out.println("'objects' not found in JSON.");
        }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
   
