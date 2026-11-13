package org.delcom.ifs25005springbootcashflow.framework.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;

/** Kartu konten halaman. */
public class PageCard extends Div {

    public PageCard(Component... components) {
        addClassName("page-card");
        add(components);
    }
}
