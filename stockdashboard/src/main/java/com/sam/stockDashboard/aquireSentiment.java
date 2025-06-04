package com.sam.stockDashboard;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.response.OllamaResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class aquireSentiment {

    private static final String DIFFBOT_TOKEN = "91ef285fb5465e5d1717853ff8284ecb";
    private static final String DIFFBOT_ENDPOINT = "https://api.diffbot.com/v3/article";

    /** Build a Diffbot URL that returns title, text, and sentiment. */
    public static String buildDiffbotUrl(String articleUrl) throws Exception {
        String encodedUrl = URLEncoder.encode(articleUrl, StandardCharsets.UTF_8.toString());
        String fields = "title,text,sentiment";
        return DIFFBOT_ENDPOINT
             + "?token=" + DIFFBOT_TOKEN
             + "&url="   + encodedUrl
             + "&fields=" + fields;
    }

    /** Perform an HTTP GET and return the entire response body. */
    public static String fetchHttpGet(String urlString) throws Exception {
        URL url = java.net.URI.create(urlString).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(30_000);
        conn.setReadTimeout(30_000);

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    /**
     * Fetches the article text via Diffbot, then feeds that text to Ollama’s “mistral” model
     * by writing the prompt into stdin. Returns exactly one double in [-2.0, +2.0].
     */
    public static double getOllamaSentiment(String url, String company) throws Exception {
        // 1) Build & fetch Diffbot JSON
        //String diffbotUrl = buildDiffbotUrl(url);
        //String jsonResponse = fetchHttpGet(diffbotUrl);
        String jsonResponse = 
       "{\"request\":{\"pageUrl\":\"https://finance.yahoo.com/news/nvidia-stock-plummets-loses-record-589-billion-as-deepseek-prompts-questions-over-ai-spending-135105824.html\",\"api\":\"article\",\"fields\":\"title,text,sentiment\",\"version\":3},\"objects\":[{\"date\":\"Mon, 27 Jan 2025 13:07:00 GMT\",\"sentiment\":-0.191,\"author\":\"Laura Bratton\",\"estimatedDate\":\"Mon, 27 Jan 2025 13:07:00 GMT\",\"publisherRegion\":\"North America\",\"icon\":\"https://s.yimg.com/cv/apiv2/default/finance/favicon-180x180.png\",\"diffbotUri\":\"article|3|-953540445\",\"siteName\":\"Yahoo! News\",\"type\":\"article\",\"title\":\"Nvidia stock plummets, loses record $589 billion as DeepSeek prompts questions over AI spending\",\"tags\":[{\"score\":0.9192866086959839,\"sentiment\":-0.94,\"count\":7,\"label\":\"Nvidia\",\"uri\":\"https://diffbot.com/entity/E9nQaOvC9MXaUzXCuN81tFQ\",\"rdfTypes\":[\"http://dbpedia.org/ontology/Organisation\"]},{\"score\":0.6130013465881348,\"sentiment\":0,\"count\":10,\"label\":\"artificial intelligence\",\"uri\":\"https://diffbot.com/entity/E_lYDrjmAMlKKwXaDf958zg\",\"rdfTypes\":[\"http://dbpedia.org/ontology/Skill\",\"http://dbpedia.org/ontology/Activity\",\"http://dbpedia.org/ontology/TopicalConcept\",\"http://dbpedia.org/ontology/AcademicSubject\"]},{\"score\":0.549480140209198,\"sentiment\":0,\"count\":10,\"label\":\"DeepSeek\",\"uri\":\"https://diffbot.com/entity/En0PzENzENueaG-eNTshnYg\",\"rdfTypes\":[\"http://dbpedia.org/ontology/Organisation\"]}],\"publisherCountry\":\"United States\",\"humanLanguage\":\"en\",\"authorUrl\":\"https://finance.yahoo.com/author/laura-bratton/\",\"pageUrl\":\"https://finance.yahoo.com/news/nvidia-stock-plummets-loses-record-589-billion-as-deepseek-prompts-questions-over-ai-spending-135105824.html\",\"html\":\"<p>Nvidia (<a data-i13n=\\\"cpos:1;pos:1\\\" data-ylk=\\\"slk:NVDA;cpos:1;pos:1;elm:context_link;itc:0;sec:content-canvas\\\" href=\\\"https://finance.yahoo.com/quote/NVDA\\\">NVDA<\\/a>) stock dropped nearly 17% Monday, leading a sell-off across chip stocks and the broader market after a new AI model from China's DeepSeek raised questions about AI investment and the rise of more cost-efficient artificial intelligence agents.<\\/p>\\n<p>Nvidia's decline shaved $589 billion off the AI chipmaker's market cap, the largest single-day loss in stock market history.<\\/p>\\n<p>Chinese startup DeepSeek <a data-i13n=\\\"cpos:2;pos:1\\\" data-ylk=\\\"slk:released a new AI model;cpos:2;pos:1;elm:context_link;itc:0;sec:content-canvas\\\" href=\\\"https://api-docs.deepseek.com/news/news250120\\\">released a new AI model<\\/a> on Jan. 20 viewed <a data-i13n=\\\"cpos:3;pos:1\\\" data-ylk=\\\"slk:as a threat to OpenAI;cpos:3;pos:1;elm:context_link;itc:0;sec:content-canvas\\\" href=\\\"https://www.nytimes.com/2025/01/23/technology/deepseek-china-ai-chips.html\\\">as a threat to OpenAI<\\/a>. American venture capitalist Marc Andreessen called the model &ldquo;<a data-i13n=\\\"cpos:4;pos:1\\\" data-ylk=\\\"slk:one of the most amazing and impressive breakthroughs I\\u2019ve ever seen;cpos:4;pos:1;elm:context_link;itc:0;sec:content-canvas\\\" href=\\\"https://x.com/pmarca/status/1882719769851474108\\\">one of the most amazing and impressive breakthroughs I\\u2019ve ever seen<\\/a>.&rdquo.<\\/p>\\n<p>The news came just a month after DeepSeek said one of its latest AI models cost just $5.6 million to train. OpenAI&rsquo;s GPT-4 model <a data-i13n=\\\"cpos:5;pos:1\\\" data-ylk=\\\"slk:cost more than $100 million;cpos:5;pos:1;elm:context_link;itc:0;sec:content-canvas\\\" href=\\\"https://www.wired.com/story/openai-ceo-sam-altman-the-age-of-giant-ai-models-is-already-over/\\\">cost more than $100 million<\\/a> to train.<\\/p>\\n<p>The announcements spurred fears that AI models may begin to require fewer chips and energy than they currently use. Nvidia has become the world's largest company on the back of exploding demand for its high-end chips that help train and use AI models.<\\/p>\\n<p>&ldquo;If DeepSeek&rsquo;s innovations are adopted broadly, an argument can be made that model training costs could come down significantly even at U.S. hyperscalers, potentially raising questions about the need for <a data-i13n=\\\"cpos:6;pos:1\\\" data-ylk=\\\"slk:1-million XPU/GPU clusters;cpos:6;pos:1;elm:context_link;itc:0;sec:content-canvas\\\" href=\\\"https://urldefense.com/v3/__https://raymondjames.bluematrix.com/docs/html/fdeb1be5-8f90-49f1-8d4e-7381ad312b38.html__;!!Op6eflyXZCqGR5I!DLbZyc0iMYpgX7WjbKt18JaNgqJQ97Sjy0jUdH-Iw0sqP6H6iHUqtdVSFaWI-v67fQ7azIBbgi9Vf0zWQAcZrgTZCMe3Y-DLSQ$\\\">1-million XPU/GPU clusters<\\/a> as projected by some,&rdquo; wrote Raymond James semiconductor analyst Srini Pajjuri in a note to investors Sunday evening.<\\/p>\\n<p>Chip stocks also dropped across the board Monday, with Broadcom (<a data-i13n=\\\"cpos:7;pos:1\\\" data-ylk=\\\"slk:AVGO;cpos:7;pos:1;elm:context_link;itc:0;sec:content-canvas\\\" href=\\\"https://finance.yahoo.com/quote/AVGO\\\">AVGO<\\/a>) down over 17%, Micron (<a data-i13n=\\\"cpos:8;pos:1\\\" data-ylk=\\\"slk:MU;cpos:8;pos:1;elm:context_link;itc:0;sec:content-canvas\\\" href=\\\"https://finance.yahoo.com/quote/MU\\\">MU<\\/a>) off almost 12%, and Advanced Micro Devices (<a data-i13n=\\\"cpos:9;pos:1\\\" data-ylk=\\\"slk:AMD;cpos:9;pos:1;elm:context_link;itc:0;sec:content-canvas\\\" href=\\\"https://finance.yahoo.com/quote/AMD\\\">AMD<\\/a>) down more than 6%.<\\/p>\\n<p>However, Pajjuri continued, &ldquo;A more logical implication is that DeepSeek will drive even more urgency among U.S. hyperscalers to leverage their key advantage (access to GPUs) to distance themselves from cheaper alternatives.&rdquo;<\\/p>\\n<p>Bernstein analyst Stacy Rasgon also called into question the $5.6 million training cost for DeepSeek&rsquo;s model, which &ldquo;does not include all the other costs associated with prior research and experiments on architectures, algorithms, or data.&rdquo;<\\/p>\\n<p>Rasgon believes DeepSeek's announcement was \\\"not really worthy of the hysteria that has taken over the Twitterverse over the last several days.\\\"<\\/p>\\n<p>Nvidia itself didn't seem too worried about the DeepSeek buzz, calling R1 \\\"an excellent AI advancement\\\" in a statement.<\\/p>\\n<p><a data-i13n=\\\"cpos:10;pos:1\\\" data-ylk=\\\"slk:Tightened US export restrictions;cpos:10;pos:1;elm:context_link;itc:0;sec:content-canvas;outcm:mb_qualified_link;_E:mb_qualified_link;ct:story;\\\" href=\\\"https://finance.yahoo.com/news/ai-watch-as-his-presidency-winds-down-joe-biden-aims-to-preserve-the-us-lead-over-china-172457072.html\\\">Tightened US export restrictions<\\/a> announced in former President Joe Biden&rsquo;s final days in office could also add a threat to DeepSeek&rsquo;s ability to continue training new models.<\\/p>\\n<p>The new rules limit China&rsquo;s ability to buy Nvidia&rsquo;s AI chips through resellers and access chips in remote data centers. And China is restricted from importing the most advanced chipmaking machines required to make artificial intelligence semiconductors from the Dutch firm ASML (<a data-i13n=\\\"cpos:11;pos:1\\\" data-ylk=\\\"slk:ASML;cpos:11;pos:1;elm:context_link;itc:0;sec:content-canvas\\\" href=\\\"https://finance.yahoo.com/quote/ASML\\\">ASML<\\/a>).<\\/p>\",\"categories\":[{\"score\":0.747,\"name\":\"Business\",\"id\":\"iabv2-53\"},{\"score\":0.928,\"name\":\"Business and Finance\",\"id\":\"iabv2-52\"},{\"score\":1,\"name\":\"Artificial Intelligence\",\"id\":\"iabv2-597\"},{\"score\":1,\"name\":\"Technology & Computing\",\"id\":\"iabv2-596\"}],\"text\":\"Nvidia (NVDA) stock dropped nearly 17% Monday, leading a sell-off across chip stocks and the broader market after a new AI model from China's DeepSeek raised questions about AI investment and the rise of more cost-efficient artificial intelligence agents";

        OllamaAPI ollamaAPI = new OllamaAPI();
        
        
        // 2) Build prompt for Ollama
        String prompt = ""
            + "You are a sentiment‐analysis engine.\n"
            + "If the article is negative toward " + company + ", return a number between –1.0 and 0.0.\n"
            + "If positive, return a number between 0.0 and +1.0. If neutral, return 0.0. do not print any control or unicode characters. Do not output anything else.\n\n"
            + "EXAMPLE:\n"
            + company + "’s stock plunged 15% after weak quarterly guidance. → –0.75\n"
            + company + " beat EPS estimates and raised guidance. → +0.80\n"
            + "Now analyze the following article about " + company + ". Return only one floating‐point number:\n"
            + jsonResponse;
        
        OllamaResult result = ollamaAPI.generate("mistral", prompt, null);
        Double score = Double.parseDouble(result.getResponse().trim());
        return score;
    }

    public static void main(String[] args) throws Exception {
            String articleUrl = "https://finance.yahoo.com/news/nvidia-stock-plummets-loses-record-589-billion-as-deepseek-prompts-questions-over-ai-spending-135105824.html";
            Double score = getOllamaSentiment(articleUrl, "Nvidia");
            System.out.println("Sentiment score for Nvidia = " + score);
    }
}
