package jsonreader;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

public class JsonUtils
{
    private static int randomIndex;
    private ArrayList<HashMap<String, String>> getBrowsersMapList() throws IOException
    {
        ArrayList<HashMap<String, String>> browsers;
        try (var reader = new FileReader("src/main/resources/browsers.json"))
        {
            var mapper = new ObjectMapper();
            browsers = mapper.readValue(reader, new TypeReference<ArrayList<HashMap<String, String>>>()
            {
            });
        }
        return browsers;
    }

    public String getRandomBrowser() throws IOException
    {
        var browsersList = getBrowsersMapList();
        var random = new SecureRandom();
        int tempIndex;
        do
        {
            tempIndex = random.nextInt(browsersList.size());
        }
        while (randomIndex == tempIndex);
        randomIndex = tempIndex;
        return browsersList.get(randomIndex).get("browser");
    }
}
