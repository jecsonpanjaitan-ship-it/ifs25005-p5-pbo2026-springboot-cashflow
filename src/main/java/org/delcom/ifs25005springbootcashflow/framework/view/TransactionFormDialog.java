package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.domain.entity.Transaction;
import org.delcom.ifs25005springbootcashflow.domain.entity.TransactionType;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceUseCase;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.server.streams.UploadHandler;

/** Dialog tambah (pemasukan/pengeluaran) dan ubah transaksi dengan upload bukti opsional. */
public class TransactionFormDialog extends ModalDialog {

    private final TextField description = new TextField("Keterangan");
    private final TextField amount = new TextField("Jumlah (Rp)");
    private final Select<TransactionType> type = new Select<>();
    private final Span fileInfo = new Span("Belum ada gambar dipilih (opsional)");
    private volatile byte[] pendingBytes;
    private volatile String pendingName;

    public TransactionFormDialog(Integer userId, TransactionType initialType, Transaction existing,
                                 FinanceUseCase useCase, FinancePresenter presenter,
                                 FileStorage fileStorage, Runnable onSaved) {
        super(existing == null
                        ? (initialType == TransactionType.INCOME ? "Tambah Pemasukan" : "Tambah Pengeluaran")
                        : "Ubah Transaksi #" + existing.getId(),
                presenter.typeLabel(initialType));
        setWidth("min(92vw, 520px)");
        UI ui = UI.getCurrent();

        description.setWidthFull();
        description.setRequiredIndicatorVisible(true);
        description.setPlaceholder("contoh: Gaji bulanan");
        amount.setWidthFull();
        amount.setRequiredIndicatorVisible(true);
        amount.setPlaceholder("contoh: 150000");
        amount.setHelperText("Angka bulat lebih dari 0");

        type.setLabel("Tipe");
        type.setItems(TransactionType.values());
        type.setItemLabelGenerator(presenter::typeLabel);
        type.setValue(initialType);
        type.setWidthFull();
        type.setVisible(existing != null);

        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            pendingBytes = data;
            pendingName = metadata.fileName();
            ui.access(() -> fileInfo.setText("Dipilih: " + metadata.fileName()));
        }));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize((int) FileStorage.MAX_BYTES);
        upload.setUploadButton(new Button("Pilih gambar bukti"));
        upload.setDropLabel(new Span("atau seret gambar ke sini"));
        upload.getElement().addEventListener("file-remove", e -> {
            pendingBytes = null;
            pendingName = null;
            fileInfo.setText("Belum ada gambar dipilih (opsional)");
        });
        fileInfo.addClassName("muted");

        if (existing != null) {
            description.setValue(existing.getDescription());
            amount.setValue(String.valueOf(existing.getAmount()));
            if (existing.getProofPath() != null) {
                fileInfo.setText("Bukti saat ini dipertahankan bila tidak memilih gambar baru");
            }
        }

        VerticalLayout form = new VerticalLayout(description, amount, type, upload, fileInfo);
        form.setPadding(false);
        addBody(form);

        Button cancel = new Button("Batal", e -> close());
        Button save = new Button("Simpan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> {
            setBusy(true);
            String newProof = null;
            try {
                if (pendingBytes != null) {
                    newProof = fileStorage.save(pendingBytes, pendingName);
                }
                Transaction saved = existing == null
                        ? useCase.add(userId, description.getValue(), amount.getValue(), initialType, newProof)
                        : useCase.update(userId, existing.getId(), description.getValue(), amount.getValue(),
                        type.getValue(), newProof);
                if (existing != null && newProof != null) {
                    fileStorage.delete(existing.getProofPath());
                }
                Notifier.success(existing == null ? presenter.addedMessage(saved) : presenter.updatedMessage(saved));
                close();
                onSaved.run();
            } catch (DomainException ex) {
                if (newProof != null) {
                    fileStorage.delete(newProof);
                }
                Notifier.error(presenter.errorMessage(ex));
            } finally {
                setBusy(false);
            }
        });
        addActions(cancel, save);
    }
}
