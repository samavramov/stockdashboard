package com.sam.main;
import java.util.Date;
public class sentiment {
    public String stockSymbol;
    public String companyName;
    public double sentimentValue;
    public Date sentimentTimestamp;
    public String url1;
    public String url2;
    public String url3;
    public String llmAnalysis;

    public sentiment(String stockSymbol,String companyName, double sentimentValue, Date sentimentTimestamp, String url1, String url2, String url3, String llmAnalysis) {
        this.stockSymbol = stockSymbol;
        this.companyName = companyName;
        this.sentimentValue = sentimentValue;
        this.sentimentTimestamp = sentimentTimestamp;
        this.url1 = url1;
        this.url2 = url2;
        this.url3 = url3;
        this.llmAnalysis = llmAnalysis;
    }
    @Override
    public String toString() {
        return "Sentiment{" +
                "stockSymbol='" + stockSymbol + '\'' +
                ", companyName='" + companyName + '\'' +
                ", sentimentValue=" + sentimentValue +
                ", sentimentTimestamp=" + sentimentTimestamp +
                ", url1='" + url1 + '\'' +
                ", url2='" + url2 + '\'' +
                ", url3='" + url3 + '\'' +
                ", llmAnalysis='" + llmAnalysis + '\'' +
                '}';
    }
}
