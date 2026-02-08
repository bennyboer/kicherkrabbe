package de.bennyboer.kicherkrabbe.assets;

import de.bennyboer.kicherkrabbe.assets.image.ImageDimensions;
import de.bennyboer.kicherkrabbe.assets.image.ImageProcessor;
import de.bennyboer.kicherkrabbe.assets.samples.SampleImage;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.Agent;
import de.bennyboer.kicherkrabbe.eventsourcing.event.metadata.agent.AgentId;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryAssetContentWithWidthTest extends AssetsModuleTest {

    @Test
    void shouldServeOriginalWhenNoWidthRequested() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        byte[] originalImage = SampleImage.createJpeg(1000, 800);

        String assetId = uploadAsset("image/jpeg", originalImage, agent);

        byte[] content = getAssetContent(assetId, agent);

        assertThat(content).isEqualTo(originalImage);
    }

    @Test
    void shouldGenerateVariantsOnFirstWidthRequest() throws IOException {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        byte[] originalImage = SampleImage.createJpeg(1000, 800);

        String assetId = uploadAsset("image/jpeg", originalImage, agent);

        byte[] content = getAssetContent(assetId, 500, agent);

        assertThat(content).isNotEmpty();
        ImageDimensions dims = ImageProcessor.readDimensions(content);
        assertThat(dims.getWidth()).isEqualTo(768);
    }

    @Test
    void shouldServeExistingVariantsOnSubsequentRequests() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        byte[] originalImage = SampleImage.createJpeg(1000, 800);

        String assetId = uploadAsset("image/jpeg", originalImage, agent);

        byte[] firstRequest = getAssetContent(assetId, 500, agent);
        byte[] secondRequest = getAssetContent(assetId, 500, agent);

        assertThat(firstRequest).isEqualTo(secondRequest);
    }

    @Test
    void shouldServeBestMatchingVariant() throws IOException {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        byte[] originalImage = SampleImage.createJpeg(2000, 1500);

        String assetId = uploadAsset("image/jpeg", originalImage, agent);

        byte[] smallVariant = getAssetContent(assetId, 200, agent);
        ImageDimensions smallDims = ImageProcessor.readDimensions(smallVariant);
        assertThat(smallDims.getWidth()).isEqualTo(384);

        byte[] mediumVariant = getAssetContent(assetId, 500, agent);
        ImageDimensions mediumDims = ImageProcessor.readDimensions(mediumVariant);
        assertThat(mediumDims.getWidth()).isEqualTo(768);

        byte[] largeVariant = getAssetContent(assetId, 1000, agent);
        ImageDimensions largeDims = ImageProcessor.readDimensions(largeVariant);
        assertThat(largeDims.getWidth()).isEqualTo(1536);
    }

    @Test
    void shouldIgnoreWidthForNonImageAssets() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        byte[] textContent = "Hello, World!".getBytes();

        String assetId = uploadAsset("text/plain", textContent, agent);

        byte[] content = getAssetContent(assetId, 500, agent);

        assertThat(content).isEqualTo(textContent);
    }

    @Test
    void shouldNotUpscaleSmallImages() throws IOException {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        byte[] smallImage = SampleImage.createJpeg(300, 200);

        String assetId = uploadAsset("image/jpeg", smallImage, agent);

        byte[] content = getAssetContent(assetId, 500, agent);

        ImageDimensions dims = ImageProcessor.readDimensions(content);
        assertThat(dims.getWidth()).isEqualTo(300);
    }

    @Test
    void shouldDeleteVariantsWhenAssetDeleted() throws IOException {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        byte[] originalImage = SampleImage.createJpeg(1000, 800);

        String assetId = uploadAsset("image/jpeg", originalImage, agent);
        getAssetContent(assetId, 500, agent);

        deleteAsset(assetId, 0L, agent);

        byte[] content = getAssetContent(assetId, agent);
        assertThat(content).isEmpty();
    }

    @Test
    void shouldReturnWebPContentTypeForVariants() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        byte[] originalImage = SampleImage.createJpeg(1000, 800);

        String assetId = uploadAsset("image/jpeg", originalImage, agent);

        AssetContent content = getAssetContentWithMetadata(assetId, 500, agent);

        assertThat(content.getContentType().getValue()).isEqualTo("image/webp");
    }

    @Test
    void shouldReturnOriginalContentTypeWhenNoWidth() {
        allowUserToCreateAssets("USER_ID");
        var agent = Agent.user(AgentId.of("USER_ID"));
        byte[] originalImage = SampleImage.createJpeg(1000, 800);

        String assetId = uploadAsset("image/jpeg", originalImage, agent);

        AssetContent content = getAssetContentWithMetadata(assetId, null, agent);

        assertThat(content.getContentType().getValue()).isEqualTo("image/jpeg");
    }

}
