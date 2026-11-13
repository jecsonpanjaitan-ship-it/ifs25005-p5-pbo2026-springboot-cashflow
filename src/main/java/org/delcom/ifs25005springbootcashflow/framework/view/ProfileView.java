package org.delcom.ifs25005springbootcashflow.framework.view;

import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.exception.DomainException;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.framework.auth.AuthSession;
import org.delcom.ifs25005springbootcashflow.usecase.UserUseCase;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.avatar.AvatarVariant;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.UploadHandler;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profil | Cashflow")
public class ProfileView extends VerticalLayout {

    private final transient IUserRepository userRepository;
    private final transient UserUseCase userUseCase;
    private final transient FileStorage fileStorage;

    private final Avatar avatar = new Avatar();
    private final TextField username = new TextField("Username");
    private final TextField fullName = new TextField("Nama lengkap");
    private final PasswordField newPassword = new PasswordField("Password baru");
    private final PasswordField confirmPassword = new PasswordField("Konfirmasi password baru");
    private final Span fileInfo = new Span("Belum ada foto baru dipilih");
    private final ProgressBar progress = new ProgressBar();
    private volatile byte[] pendingBytes;
    private volatile String pendingName;
    private Integer userId;

    public ProfileView(IUserRepository userRepository, UserUseCase userUseCase, FileStorage fileStorage) {
        this.userRepository = userRepository;
        this.userUseCase = userUseCase;
        this.fileStorage = fileStorage;
        setPadding(true);
        addClassName("page");
        UI ui = UI.getCurrent();

        H1 title = new H1("Profil Saya");
        title.addClassName("page-title");

        avatar.addThemeVariants(AvatarVariant.LUMO_XLARGE);
        username.setReadOnly(true);
        username.setWidthFull();
        fullName.setWidthFull();
        newPassword.setWidthFull();
        newPassword.setHelperText("Kosongkan bila tidak ingin mengganti password");
        newPassword.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.NEW_PASSWORD);
        confirmPassword.setWidthFull();
        confirmPassword.setAutocomplete(com.vaadin.flow.component.textfield.Autocomplete.NEW_PASSWORD);

        Upload upload = new Upload(UploadHandler.inMemory((metadata, data) -> {
            pendingBytes = data;
            pendingName = metadata.fileName();
            ui.access(() -> fileInfo.setText("Foto baru dipilih: " + metadata.fileName()));
        }));
        upload.setAcceptedFileTypes("image/png", "image/jpeg", "image/gif", "image/webp");
        upload.setMaxFiles(1);
        upload.setMaxFileSize((int) FileStorage.MAX_BYTES);
        upload.setUploadButton(new Button("Pilih foto profil"));
        upload.setDropLabel(new Span("atau seret foto ke sini"));
        upload.getElement().addEventListener("file-remove", e -> {
            pendingBytes = null;
            pendingName = null;
            fileInfo.setText("Belum ada foto baru dipilih");
        });
        fileInfo.addClassName("muted");

        progress.setIndeterminate(true);
        progress.setVisible(false);
        progress.getElement().setAttribute("aria-label", "Sedang menyimpan profil");

        Button save = new Button("Simpan Perubahan");
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        save.addClickListener(e -> save(save));

        VerticalLayout form = new VerticalLayout(avatar, username, fullName, newPassword, confirmPassword,
                upload, fileInfo, progress, save);
        form.setPadding(false);
        PageCard card = new PageCard(form);
        card.addClassName("form-card");
        add(title, card);
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        User user = AuthSession.currentUser(userRepository);
        if (user != null) {
            userId = user.getId();
            show(user);
        }
    }

    private void show(User user) {
        username.setValue(user.getUsername());
        fullName.setValue(user.getFullName());
        avatar.setName(user.getFullName());
        avatar.setImage(user.getPhotoPath() == null ? null : "/uploads/" + user.getPhotoPath());
    }

    private void save(Button saveButton) {
        if (!newPassword.getValue().equals(confirmPassword.getValue())) {
            Notifier.error("Konfirmasi password tidak sama");
            return;
        }
        saveButton.setEnabled(false);
        progress.setVisible(true);
        String newPhoto = null;
        try {
            if (pendingBytes != null) {
                newPhoto = fileStorage.save(pendingBytes, pendingName);
            }
            User updated = userUseCase.updateProfile(userId, fullName.getValue(), newPassword.getValue());
            if (newPhoto != null) {
                String old = updated.getPhotoPath();
                updated = userUseCase.updatePhoto(userId, newPhoto);
                fileStorage.delete(old);
            }
            pendingBytes = null;
            pendingName = null;
            newPassword.clear();
            confirmPassword.clear();
            fileInfo.setText("Belum ada foto baru dipilih");
            show(updated);
            getParent().ifPresent(parent -> {
                if (parent instanceof MainLayout layout) {
                    layout.refreshUser();
                }
            });
            Notifier.success("Profil berhasil diperbarui");
        } catch (DomainException ex) {
            if (newPhoto != null) {
                fileStorage.delete(newPhoto);
            }
            Notifier.error(ex.getMessage());
        } finally {
            progress.setVisible(false);
            saveButton.setEnabled(true);
        }
    }
}
