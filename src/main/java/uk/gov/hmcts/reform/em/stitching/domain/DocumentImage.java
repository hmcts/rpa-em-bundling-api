package uk.gov.hmcts.reform.em.stitching.domain;

import uk.gov.hmcts.reform.em.stitching.domain.enumeration.ImageRendering;
import uk.gov.hmcts.reform.em.stitching.domain.enumeration.ImageRenderingLocation;

public class DocumentImage {

    private boolean enabled;
    private String docmosisAssetId;
    private ImageRenderingLocation imageRenderingLocation;
    private Integer coordinateX;
    private Integer coordinateY;
    private ImageRendering imageRendering;

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getDocmosisAssetId() {
        return docmosisAssetId;
    }

    public void setDocmosisAssetId(String docmosisAssetId) {
        this.docmosisAssetId = docmosisAssetId;
    }

    public ImageRenderingLocation getImageRenderingLocation() {
        return imageRenderingLocation;
    }

    public void setImageRenderingLocation(ImageRenderingLocation imageRenderingLocation) {
        this.imageRenderingLocation = imageRenderingLocation;
    }

    public Integer getCoordinateX() {
        return coordinateX;
    }

    public void setCoordinateX(Integer coordinateX) {
        this.coordinateX = coordinateX;
    }

    public Integer getCoordinateY() {
        return coordinateY;
    }

    public void setCoordinateY(Integer coordinateY) {
        this.coordinateY = coordinateY;
    }

    public ImageRendering getImageRendering() {
        return imageRendering;
    }

    public void setImageRendering(ImageRendering imageRendering) {
        this.imageRendering = imageRendering;
    }
}
