package com.sam.stockDashboard;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.util.Map;
import java.util.HashMap;

public class aquireURL {
    
    private static final String API_TOKEN = "FdgrqW570cau6k6xcmmYIYTbN9uLzJWKVxFAhJBE";

    // Method to build the endpoint URL
    private static String buildEndpoint(String company, String date) {
        return String.format(
            "https://api.thenewsapi.com/v1/news/all?api_token=%s&search=%s&language=en&categories=business&published_on=%s&limit=1",
            API_TOKEN,
            company.replace(" ", "%20"),
            date
        );
    }

    // Method to fetch news articles
    public static Map<String, String> getURL(String companyQuery){
        String company = companyQuery;
        String todayDate = LocalDate.now().toString();
        Map<String, String> output = new HashMap<>();
        try {

            // Use the encapsulated method to build the endpoint
            String endpoint = buildEndpoint(company, todayDate);

            // Create endpoint uri object
            java.net.URI uri = java.net.URI.create(endpoint);

            // Convert to a url and open connection to the url
            URL url = uri.toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Specify that we want to get data
            conn.setRequestMethod("GET");

            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder responseBuilder = new StringBuilder();
            String line;

            // Read each line of the response
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            reader.close();

            // Create new json object from the response
            JSONObject json = new JSONObject(responseBuilder.toString());
            JSONArray articles = json.getJSONArray("data");

            if (articles.length() == 0) {
                System.out.println("No articles found for: " + company);
            } 
            
            // Store the articles in the output map
            else {
                System.out.println("\nTop News Articles:");
                for (int i = 0; i < articles.length(); i++) {
                    JSONObject article = articles.getJSONObject(i);
                    String title = article.getString("title");
                    String urlStr = article.getString("url");
                    output.put(title, urlStr);
                }
            }

        } 
        // Catch exceptions that may occur during the process
        catch (Exception e) {
            System.out.println("Error while fetching news: " + e.getMessage());
            e.printStackTrace();
        }
        return output;
    }
}