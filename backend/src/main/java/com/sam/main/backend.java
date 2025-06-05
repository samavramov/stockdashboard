package com.sam.main;
public class backend {
    public static void main(String[] args) {
        // Example usage of the classes
        sentiment AAPLTODAY = new sentiment(
            "AAPL",
            "Apple Inc.",
            0.75,
            new java.util.Date(),
            "https://example.com/article1",
            "https://example.com/article2",
            "https://example.com/article3",
            "Positive sentiment due to strong earnings report."
        );
        sentiment MSFTTODAY = new sentiment(
            "MSFT",
            "Microsoft Corporation",
            -0.25,
            new java.util.Date(),
            "https://example.com/article4",
            "https://example.com/article5",
            "https://example.com/article6",
            "Negative sentiment due to regulatory concerns."
        );
        databaseInteractions db = new databaseInteractions();
        db.addSentiment(AAPLTODAY);
        db.addSentiment(MSFTTODAY);
        System.out.println("Sentiments added to the database successfully.");
        System.out.println(db.getLatestSentimentsByStockSymbol("AAPL", 10));

     }
}
