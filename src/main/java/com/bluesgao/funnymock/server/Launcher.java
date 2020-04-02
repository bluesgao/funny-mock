package com.bluesgao.funnymock.server;

import com.alibaba.fastjson.JSON;
import com.bluesgao.funnymock.cmd.CommandParser;
import com.bluesgao.funnymock.data.DataReader;
import com.bluesgao.funnymock.handler.HttpRequestRouteHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * 启动器
 */
@Slf4j
public class Launcher {

    public static void main(String[] args) {
        log.info("Launcher start args:{}", JSON.toJSONString(args));
        try {
            Launcher launcher = new Launcher();
            //parse console args
            CommandParser.parse(args);
            log.info("Launcher parse command args commandMap:{}", JSON.toJSONString(CommandParser.commandMap));

            //parse file data
            DataReader.parse(CommandParser.commandMap.get("f").toString());
            log.info("Launcher parse data file recordMap:{}", JSON.toJSONString(DataReader.recordMap));

            //start server
            launcher.start(Integer.valueOf(CommandParser.commandMap.get("p").toString()));
        } catch (Exception e) {
            log.error("Launcher main error:{}", e);
        }
    }

    public void start(int port) {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap server = new ServerBootstrap();
            server.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            // 请求解码器
                            ch.pipeline().addLast("http-decoder", new HttpRequestDecoder());
                            // 将HTTP消息的多个部分合成一条完整的HTTP消息
                            ch.pipeline().addLast("http-aggregator", new HttpObjectAggregator(65535));
                            // 响应转码器
                            ch.pipeline().addLast("http-encoder", new HttpResponseEncoder());
                            // 解决大码流的问题，ChunkedWriteHandler：向客户端发送HTML5文件
                            ch.pipeline().addLast("http-chunked", new ChunkedWriteHandler());
                            // 自定义处理handler
                            ch.pipeline().addLast("http-request-route", new HttpRequestRouteHandler());
                        }
                    });

            // 监听端口（服务器host和port端口），同步返回
            // ChannelFuture future = server.bind(inetHost, this.inetPort).sync();
            ChannelFuture future = server.bind(port).sync();
            log.info("netty server start success at port:{}", port);

            // 当通道关闭时继续向后执行，这是一个阻塞方法
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("netty server start error:{}", e);
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }


}
