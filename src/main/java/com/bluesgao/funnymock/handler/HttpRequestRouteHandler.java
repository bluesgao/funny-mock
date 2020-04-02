package com.bluesgao.funnymock.handler;

import com.alibaba.fastjson.JSON;
import com.bluesgao.funnymock.data.Record;
import com.bluesgao.funnymock.http.HttpRequestParser;
import com.bluesgao.funnymock.http.HttpResponseBuilder;
import com.bluesgao.funnymock.matcher.RecordMatcher;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class HttpRequestRouteHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private RecordMatcher matcher = new RecordMatcher();

    /*
     * 处理请求
     */
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpRequest request) {
        log.info("HttpRequestRouteHandler channelRead0 request:{}", JSON.toJSONString(request));
        // 获取http请求参数
        Map<String, Object> requestParams = HttpRequestParser.parseParams(request);
        log.info("HttpRequestRouteHandler channelRead0 requestParams:{}", JSON.toJSONString(requestParams));

        // 匹配响应数据
        Record record = matcher.match(HttpRequestParser.parseMethod(request), HttpRequestParser.parseUri(request));

        // 构造响应
        FullHttpResponse response = HttpResponseBuilder.build(HttpResponseStatus.OK, Unpooled.copiedBuffer(JSON.toJSONString(record), CharsetUtil.UTF_8));

        // 发送响应
        channelHandlerContext.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }


}
