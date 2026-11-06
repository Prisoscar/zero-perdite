package mkle.zeroperdite.views.clienti;

import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.Setter;
import com.vaadin.flow.data.validator.BeanValidator;
import com.vaadin.flow.function.SerializableFunction;
import com.vaadin.flow.function.ValueProvider;
import java.util.function.Consumer;
import mkle.zeroperdite.data.Client;

public class ClientDialog extends Dialog {

    private static final SerializableFunction<String, String> EMPTY_TO_NULL =
            v -> v == null || v.isBlank() ? null : v.trim();
    private static final SerializableFunction<String, String> NULL_TO_EMPTY =
            v -> v == null ? "" : v;

    private final TextField cognome = new TextField("Cognome");
    private final TextField nome = new TextField("Nome");
    private final TextField citta = new TextField("Città");
    private final TextField codiceFiscale = new TextField("Codice fiscale");
    private final TextField indirizzo = new TextField("Indirizzo");
    private final TextField telefono = new TextField("Telefono");
    private final EmailField mail = new EmailField("Mail");

    private final Binder<Client> binder = new Binder<>(Client.class);
    private final Client client;
    private final Consumer<Client> onSave;

    public ClientDialog(String title, Client client, Consumer<Client> onSave) {
        this.client = client;
        this.onSave = onSave;

        setHeaderTitle(title);
        setWidth("600px");
        setMaxWidth("100%");
        setCloseOnOutsideClick(false);

        FormLayout form = new FormLayout(cognome, nome, citta, codiceFiscale, indirizzo, telefono, mail);
        form.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2));
        form.setColspan(indirizzo, 2);
        form.setColspan(mail, 2);
        add(form);

        configureBinder();

        Button annulla = new Button("Annulla", e -> close());
        annulla.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        Button salva = new Button("Salva", e -> salva());
        salva.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        getFooter().add(annulla, salva);

        binder.readBean(client);
    }

    private void configureBinder() {
        binder.forField(cognome)
                .asRequired("Il cognome è obbligatorio")
                .withConverter(EMPTY_TO_NULL, NULL_TO_EMPTY)
                .withValidator(new BeanValidator(Client.class, "cognome"))
                .bind(Client::getCognome, Client::setCognome);

        bindOptional(nome, "nome", Client::getNome, Client::setNome);
        bindOptional(citta, "citta", Client::getCitta, Client::setCitta);
        bindOptional(codiceFiscale, "codiceFiscale", Client::getCodiceFiscale, Client::setCodiceFiscale);
        bindOptional(indirizzo, "indirizzo", Client::getIndirizzo, Client::setIndirizzo);
        bindOptional(telefono, "telefono", Client::getTelefono, Client::setTelefono);
        bindOptional(mail, "mail", Client::getMail, Client::setMail);
    }

    /** Campo facoltativo: stringa vuota -> null, poi valgono le annotazioni dell'entità. */
    private void bindOptional(HasValue<?, String> field, String property,
                              ValueProvider<Client, String> getter, Setter<Client, String> setter) {
        binder.forField(field)
                .withConverter(EMPTY_TO_NULL, NULL_TO_EMPTY)
                .withValidator(new BeanValidator(Client.class, property))
                .bind(getter, setter);
    }

    private void salva() {
        if (binder.writeBeanIfValid(client)) {
            onSave.accept(client);
            close();
        }
    }
}