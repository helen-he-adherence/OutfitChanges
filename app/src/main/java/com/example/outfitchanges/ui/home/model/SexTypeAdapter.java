package com.example.outfitchanges.ui.home.model;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 自定义TypeAdapter，处理sex字段的字符串和数组两种格式
 */
public class SexTypeAdapter extends TypeAdapter<List<String>> {
    
    @Override
    public void write(JsonWriter out, List<String> value) throws IOException {
        if (value == null || value.isEmpty()) {
            out.nullValue();
            return;
        }
        // 序列化时，始终输出数组格式（符合API要求）
        out.beginArray();
        for (String item : value) {
            out.value(item);
        }
        out.endArray();
    }
    
    @Override
    public List<String> read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return new ArrayList<>();
        }
        
        List<String> result = new ArrayList<>();
        
        if (in.peek() == JsonToken.BEGIN_ARRAY) {
            // 如果是数组格式
            in.beginArray();
            while (in.hasNext()) {
                result.add(in.nextString());
            }
            in.endArray();
        } else if (in.peek() == JsonToken.STRING) {
            // 如果是字符串格式
            String value = in.nextString();
            if (value != null && !value.isEmpty()) {
                result.add(value);
            }
        } else {
            // 其他格式，跳过
            in.skipValue();
        }
        
        return result;
    }
}

