/*
 *  Copyright 2022 Nurture.Farm
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package farm.nurture.laminar.generator;

import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory;
import io.netty.handler.codec.http.multipart.FileUpload;
import io.netty.handler.codec.http.multipart.HttpDataFactory;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;

/** Handles uploading of file and then saves it to a known location. */
public class MyHttUploadServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    // Factory that writes to disk
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(true);
    private static final String FILE_UPLOAD_LOCN = "/tmp/uploads/";
    private HttpRequest httpRequest;
    private HttpPostRequestDecoder httpDecoder;
    private static final Logger logger = LoggerFactory.getLogger(MyHttUploadServerHandler.class);

    /**
     * Sends a response back.
     *
     * @param ctx
     * @param status
     * @param message
     */
    private static void sendResponse(
        ChannelHandlerContext ctx, HttpResponseStatus status, String message, File file)
        throws Exception {
        final FullHttpResponse response;
        String msgDesc = message;
        if (message == null) {
            msgDesc = "Failure: " + status;
        }
        msgDesc += " \r\n";

        final ByteBuf buffer = Unpooled.buffer();
        buffer.writeBytes(new FileInputStream(file), Integer.parseInt(String.valueOf(file.length())));
        if (status.code() >= HttpResponseStatus.BAD_REQUEST.code()) {
            response = new DefaultFullHttpResponse(HTTP_1_1, status, buffer);
        } else {
            response = new DefaultFullHttpResponse(HTTP_1_1, status, buffer);
        }
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/pdf");

        //      Close the connection as soon as the response is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    protected void channelRead0(final ChannelHandlerContext ctx, final HttpObject httpObject)
        throws Exception {
        if (httpObject instanceof HttpRequest) {
            httpRequest = (HttpRequest) httpObject;
            final URI uri = new URI(httpRequest.uri());
            logger.debug("Got URI {}",uri);
            if (httpRequest.method() == POST) {
                httpDecoder = new HttpPostRequestDecoder(factory, httpRequest);
                httpDecoder.setDiscardThreshold(0);
            } else {
                try {
                    sendResponse(ctx, METHOD_NOT_ALLOWED, null, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (httpDecoder != null && httpObject instanceof HttpContent) {
            final HttpContent chunk = (HttpContent) httpObject;
            httpDecoder.offer(chunk);
            readChunk(ctx);

            if (chunk instanceof LastHttpContent) {
                resetPostRequestDecoder();
            }
        }
    }

    private void readChunk(ChannelHandlerContext ctx) throws Exception {
        while (httpDecoder.hasNext()) {
            InterfaceHttpData data = httpDecoder.next();
            if (data != null) {
                try {
                    switch (data.getHttpDataType()) {
                        case Attribute:
                            break;
                        case FileUpload:
                            final FileUpload fileUpload = (FileUpload) data;
                            final File file = new File(FILE_UPLOAD_LOCN + fileUpload.getFilename());
                            if (!file.exists()) {
                                file.createNewFile();
                            }
                            logger.info("Created file {}",file);
                            try (FileChannel inputChannel = new FileInputStream(fileUpload.getFile()).getChannel();
                                 FileChannel outputChannel = new FileOutputStream(file).getChannel()) {
                                outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
                                sendResponse(ctx, CREATED, "file name: " + file.getAbsolutePath(), file);
                            }
                            break;
                        default:
                            break;

                    }
                } finally {
                    data.release();
                }
            }
        }
    }

    private void resetPostRequestDecoder() {
        httpRequest = null;
        httpDecoder.destroy();
        httpDecoder = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        logger.error("Got exception ",cause);
        ctx.channel().close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (httpDecoder != null) {
            httpDecoder.cleanFiles();
        }
    }
}
