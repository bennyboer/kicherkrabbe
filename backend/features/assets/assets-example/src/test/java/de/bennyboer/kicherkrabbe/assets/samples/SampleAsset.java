package de.bennyboer.kicherkrabbe.assets.samples;

import lombok.Builder;

import java.nio.charset.StandardCharsets;

@Builder
public class SampleAsset {

    @Builder.Default
    private String contentType = "image/png";

    @Builder.Default
    private byte[] content = "sample-content".getBytes(StandardCharsets.UTF_8);

    public String getContentType() {
        return contentType;
    }

    public byte[] getContent() {
        return content;
    }

}
