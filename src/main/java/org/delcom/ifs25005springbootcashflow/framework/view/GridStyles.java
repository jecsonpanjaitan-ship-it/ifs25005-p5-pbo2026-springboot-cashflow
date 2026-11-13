package org.delcom.ifs25005springbootcashflow.framework.view;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;

/** Gaya grid yang konsisten di seluruh halaman. */
public final class GridStyles {

    private GridStyles() {
    }

    public static void apply(Grid<?> grid, String ariaLabel) {
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);
        grid.addClassName("app-grid");
        grid.setAllRowsVisible(true);
        grid.getElement().setAttribute("aria-label", ariaLabel);
    }
}
