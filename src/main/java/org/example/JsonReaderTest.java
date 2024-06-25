package org.example;


import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;


public class JsonReaderTest
{
    public static void main(String[] args) throws IOException
    {
        var reader = new FileReader("src/main/resources/browsers.json");
        var typeToken = new TypeToken<ArrayList<LinkedTreeMap<String, String>>>() {}.getType();
        ArrayList<LinkedTreeMap<String, String>> arrayList = new Gson().fromJson(reader, typeToken);
        arrayList.forEach(map -> map.forEach((key, value) -> System.out.println(key + ": " + value)));
    }
}
