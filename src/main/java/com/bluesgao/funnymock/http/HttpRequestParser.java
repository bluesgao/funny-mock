package com.bluesgao.funnymock.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.handler.codec.http.multipart.MemoryAttribute;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class HttpRequestParser {
    /**
     * 解析method
     *
     * @param request
     * @return
     */
    public static String parseMethod(FullHttpRequest request) {
        return request.getMethod().name().toUpperCase();//转为大写
    }

    /**
     * 解析uri
     *
     * @param request
     * @return
     */
    public static String parseUri(FullHttpRequest request) {
        String uri = null;
        if (request.method() == HttpMethod.GET) {
            // 处理get请求
            try {
                uri = new URI(request.getUri()).getPath();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        } else if (request.method() == HttpMethod.POST) {
            // 处理POST请求
            uri = request.getUri();
        }
        return uri;
    }

    /**
     * 解析http请求参数
     *
     * @param request
     * @return
     */
    public static Map<String, Object> parseParams(FullHttpRequest request) {
        Map<String, Object> params = new HashMap<String, Object>();
        if (request.method() == HttpMethod.GET) {
            // 处理get请求，获取get查询参数
            QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
            Map<String, List<String>> paramList = decoder.parameters();
            for (Map.Entry<String, List<String>> entry : paramList.entrySet()) {
                params.put(entry.getKey(), entry.getValue().get(0));
            }
        } else if (request.method() == HttpMethod.POST) {
            // 处理POST请求
            String strContentType = request.headers().get("Content-Type").trim();
            if (strContentType.contains("x-www-form-urlencoded")) {
                params = getFormParams(request);
            } else if (strContentType.contains("application/json")) {
                try {
                    params = getJsonParams(request);
                } catch (UnsupportedEncodingException e) {
                    log.error("HttpRequestRouteHandler parseParams error:{}", e);
                }
            }
        }
        return params;
    }


    /*
     * 解析from表单数据（Content-Type = x-www-form-urlencoded）
     */
    public static Map<String, Object> getFormParams(FullHttpRequest fullHttpRequest) {
        Map<String, Object> params = new HashMap<String, Object>();

        HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(new DefaultHttpDataFactory(false), fullHttpRequest);
        List<InterfaceHttpData> postData = decoder.getBodyHttpDatas();

        for (InterfaceHttpData data : postData) {
            if (data.getHttpDataType() == InterfaceHttpData.HttpDataType.Attribute) {
                MemoryAttribute attribute = (MemoryAttribute) data;
                params.put(attribute.getName(), attribute.getValue());
            }
        }

        return params;
    }

    /*
     * 解析json数据（Content-Type = application/json）
     */
    public static Map<String, Object> getJsonParams(FullHttpRequest fullHttpRequest) throws UnsupportedEncodingException {
        Map<String, Object> params = new HashMap<String, Object>();

        ByteBuf content = fullHttpRequest.content();
        byte[] reqContent = new byte[content.readableBytes()];
        content.readBytes(reqContent);
        String strContent = new String(reqContent, "UTF-8");

        JSONObject jsonParams = JSON.parseObject(strContent);
        for (Object key : jsonParams.keySet()) {
            params.put(key.toString(), jsonParams.get(key));
        }

        return params;
    }
}
