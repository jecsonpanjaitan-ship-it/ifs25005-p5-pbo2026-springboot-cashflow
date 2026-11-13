package org.delcom.ifs25005springbootcashflow.framework.view;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;

/** Modal preview gambar: klik gambar toggle 1.0x <-> 1.8x; Perkecil/Perbesar/Tutup di footer kanan. */
public class ImagePreviewDialog extends ModalDialog {

    private static final double MIN_ZOOM = 0.4;
    private static final double MAX_ZOOM = 3.0;
    private static final double STEP = 0.2;
    private static final double DEFAULT_ZOOM = 1.0;
    private static final double TOGGLE_ZOOM = 1.8;

    private final Image image;
    private final Span zoomLabel = new Span();
    private double zoom = DEFAULT_ZOOM;

    public ImagePreviewDialog(String title, String imageUrl) {
        super(title, "Pratinjau");
        setWidth("min(92vw, 760px)");

        image = new Image(imageUrl, "Pratinjau " + title);
        image.addClassName("preview-image");
        image.addClickListener(e -> {
            zoom = zoom > DEFAULT_ZOOM ? DEFAULT_ZOOM : TOGGLE_ZOOM;
            applyZoom();
        });
        Div viewport = new Div(image);
        viewport.addClassName("preview-viewport");
        zoomLabel.addClassName("preview-zoom-label");
        addBody(viewport, zoomLabel);

        Button zoomOut = new Button("Perkecil", e -> {
            zoom = Math.max(MIN_ZOOM, zoom - STEP);
            applyZoom();
        });
        Button zoomIn = new Button("Perbesar", e -> {
            zoom = Math.min(MAX_ZOOM, zoom + STEP);
            applyZoom();
        });
        Button close = new Button("Tutup", e -> close());
        close.addThemeName("primary");
        addActions(zoomOut, zoomIn, close);
        applyZoom();
    }

    private void applyZoom() {
        image.getStyle().set("width", Math.round(zoom * 100) + "%");
        zoomLabel.setText(Math.round(zoom * 100) + "%");
        image.getElement().setAttribute("title", "Klik untuk memperbesar/memperkecil (" + zoomLabel.getText() + ")");
    }
}
