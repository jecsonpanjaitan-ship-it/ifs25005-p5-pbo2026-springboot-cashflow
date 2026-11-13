package org.delcom.ifs25005springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.dialog.DialogVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.progressbar.ProgressBar;
import java.util.ArrayList;
import java.util.List;

/**
 * Dialog modal standar: header primer (#2563EB) + badge, ProgressBar indeterminat,
 * footer abu-abu (#F3F4F6) dengan tombol aksi di sisi kanan. Gaya ada di styles.css.
 */
public class ModalDialog extends Dialog {

    private final Div body = new Div();
    private final Div footer = new Div();
    private final ProgressBar progress = new ProgressBar();
    private final List<Button> actions = new ArrayList<>();

    public ModalDialog(String title, String badge) {
        addThemeVariants(DialogVariant.LUMO_NO_PADDING);
        addClassName("app-dialog");
        setModal(true);
        setCloseOnOutsideClick(false);
        setDraggable(false);
        setResizable(false);

        H2 heading = new H2(title);
        heading.addClassName("app-dialog-title");
        Span badgeSpan = new Span(badge);
        badgeSpan.addClassName("app-dialog-badge");
        Div header = new Div(heading, badgeSpan);
        header.addClassName("app-dialog-header");

        body.addClassName("app-dialog-body");
        progress.setIndeterminate(true);
        progress.setVisible(false);
        progress.addClassName("app-dialog-progress");
        progress.getElement().setAttribute("aria-label", "Sedang memproses");
        footer.addClassName("app-dialog-footer");

        Div root = new Div(header, progress, body, footer);
        root.addClassName("app-dialog-root");
        super.add(root);
    }

    public void addBody(Component... components) {
        body.add(components);
    }

    /** Tombol aksi ditaruh berurutan di sisi kanan footer. */
    public void addActions(Button... buttons) {
        for (Button button : buttons) {
            actions.add(button);
            footer.add(button);
        }
    }

    /** Tampilkan ProgressBar dan nonaktifkan tombol selama menunggu proses. */
    public void setBusy(boolean busy) {
        progress.setVisible(busy);
        actions.forEach(button -> button.setEnabled(!busy));
    }
}
