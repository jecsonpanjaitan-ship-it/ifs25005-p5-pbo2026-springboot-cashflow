package org.delcom.ifs25005springbootcashflow.framework.view;

import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;
import org.springframework.stereotype.Component;

/** Konfigurasi global indikator loading Vaadin + atribut lang pada halaman. */
@Component
public class LoadingIndicatorInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            var config = uiEvent.getUI().getLoadingIndicatorConfiguration();
            config.setFirstDelay(150);
            config.setSecondDelay(600);
            config.setThirdDelay(1500);
        });
        event.addIndexHtmlRequestListener(response -> {
            var html = response.getDocument().select("html").first();
            if (html != null) {
                html.attr("lang", "id");
            }
        });
    }
}
