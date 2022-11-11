package com.krickert.search.model.util;

import com.google.protobuf.Timestamp;
import com.krickert.search.model.wiki.DownloadFileRequest;
import com.krickert.search.model.wiki.ErrorCheck;
import com.krickert.search.model.wiki.ErrorCheckType;
import com.krickert.search.model.wiki.WikiSiteInfo;
import org.apache.commons.io.FileUtils;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtobufUtilsTest {

    @Test
    void nowIsNowNotThen() throws InterruptedException {
        Timestamp now = ProtobufUtils.now();
        assertThat(now).isInstanceOf(Timestamp.class);
        Thread.sleep(1000);//sleep 1 second so next now() is a second later.
        assertThat(ProtobufUtils.now().getSeconds() > now.getSeconds()).isTrue();
    }

    @Test
    void stamp() {
        long time = System.currentTimeMillis() / 1000;
        Timestamp stamp = ProtobufUtils.stamp(time);
        assertThat(stamp.getSeconds()).isEqualTo(time);
        assertThat(stamp.getNanos()).isEqualTo(0);
    }

    @Test
    void testSaveProtoToDisk() throws IOException {
        DownloadFileRequest request = DownloadFileRequest.newBuilder()
                .setFileName("File Name").setUrl("someurl")
                .setErrorCheck(ErrorCheck.newBuilder()
                        .setErrorCheck("error check")
                        .setErrorCheckType(ErrorCheckType.MD5).build())
                .setFileDumpDate("20221111")
                .setUrl("www.example.com").build();
        ProtobufUtils.saveProtobufToDisk("somefile.bin", request);
        File result = new File("somefile.bin");
        assertThat(result)
                .isNotNull()
                .isFile();
        DownloadFileRequest requestOnDisk = DownloadFileRequest.parseFrom(FileUtils.readFileToByteArray(result));
        assertThat(request)
                .isEqualTo(requestOnDisk);
        FileUtils.forceDelete(result);
    }

    @Test
    void testSaveProtosToDisk() throws IOException {
        DownloadFileRequest request1 = DownloadFileRequest.newBuilder()
                .setFileName("File Name")
                .setErrorCheck(ErrorCheck.newBuilder()
                        .setErrorCheck("error check")
                        .setErrorCheckType(ErrorCheckType.MD5).build())
                .setFileDumpDate("20221111")
                .setUrl("www.example.com").build();
        DownloadFileRequest request2 = DownloadFileRequest.newBuilder()
                .setFileName("File Name2")
                .setErrorCheck(ErrorCheck.newBuilder()
                        .setErrorCheck("error check2")
                        .setErrorCheckType(ErrorCheckType.SHA1).build())
                .setFileDumpDate("20221112")
                .setUrl("www.example2.com").build();

        Collection<DownloadFileRequest> requests = Lists.newArrayList(request1, request2);
        ProtobufUtils.saveProtocoBufsToDisk("request", requests);
        File result0 = new File("request0.bin");
        File result1 = new File("request1.bin");
        assertThat(result0)
                .isNotNull()
                .isFile();
        DownloadFileRequest request1OnDisk = DownloadFileRequest.parseFrom(FileUtils.readFileToByteArray(result0));
        DownloadFileRequest request2OnDisk = DownloadFileRequest.parseFrom(FileUtils.readFileToByteArray(result1));
        assertThat(request1)
                .isEqualTo(request1OnDisk);
        assertThat(request2)
                .isEqualTo(request2OnDisk);
        FileUtils.forceDelete(result0);
        FileUtils.forceDelete(result1);

    }
}