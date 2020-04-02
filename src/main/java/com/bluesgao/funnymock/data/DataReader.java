package com.bluesgao.funnymock.data;

import com.alibaba.fastjson.JSONObject;
import com.google.common.io.ByteStreams;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;


public class DataReader {
    //default record
    public static Record DEFAULT_RECORD = new Record();

    static {
        DEFAULT_RECORD.setDescription("请求匹配失败");
        Resp resp = new Resp();
        resp.setText("请求匹配失败");
        DEFAULT_RECORD.setResp(resp);

        Req req = new Req();
        DEFAULT_RECORD.setReq(req);
    }

    public static Map<String, Record> recordMap = new HashMap<>(32);

    public static String readFile(String filePath) {
        try (InputStream inputStream = readFileAsStream(filePath)) {
            return new String(ByteStreams.toByteArray(inputStream), UTF_8.name());
        } catch (IOException ioe) {
            throw new RuntimeException("Exception while loading \"" + filePath + "\"", ioe);
        }
    }

    public static InputStream readFileAsStream(String filename) throws FileNotFoundException {
        InputStream inputStream = DataReader.class.getClassLoader().getResourceAsStream(filename);
        if (inputStream == null) {
            // load from path if not found in classpath
            inputStream = new FileInputStream(filename);
        }
        return inputStream;
    }

    public static void parse(String path) {
        //parse data file
        String dataStr = DataReader.readFile(path);
        //parse to json
        List<Record> recordList = JSONObject.parseArray(dataStr, Record.class);

        for (Record item : recordList) {
            // method转为大写，与httpRequest中的method保持一致。eg: GET:/user/info.html
            recordMap.put(item.getReq().getMethod().toUpperCase() + ":" + item.getReq().getUri(), item);
        }
    }
}
