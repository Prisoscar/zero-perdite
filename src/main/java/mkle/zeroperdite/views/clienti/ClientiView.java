package mkle.zeroperdite.views.clienti;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import com.vaadin.flow.theme.lumo.LumoUtility;
import jakarta.annotation.security.RolesAllowed;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import mkle.zeroperdite.data.Client;
import mkle.zeroperdite.services.ClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

@PageTitle("Clienti")
@Route("")
@Menu(order = 0, icon = LineAwesomeIconUrl.ADDRESS_CARD_SOLID)
@RolesAllowed("USER")
@Uses(Icon.class)
public class ClientiView extends Div {

    private static final Logger log = LoggerFactory.getLogger(ClientiView.class);

    private Grid<Client> grid;
    private final Filters filters;
    private final ClientService clientService;

    public ClientiView(ClientService clientService) {
        this.clientService = clientService;
        setSizeFull();
        addClassNames("clienti-view");

        filters = new Filters(this::refreshGrid, this::apriDialogNuovo);
        VerticalLayout layout = new VerticalLayout(createMobileFilters(), filters, createGrid());
        layout.setSizeFull();
        layout.setPadding(false);
        layout.setSpacing(false);
        add(layout);
    }

    private HorizontalLayout createMobileFilters() {
        HorizontalLayout mobileFilters = new HorizontalLayout();
        mobileFilters.setWidthFull();
        mobileFilters.addClassNames(LumoUtility.Padding.MEDIUM, LumoUtility.BoxSizing.BORDER,
                LumoUtility.AlignItems.CENTER);
        mobileFilters.addClassName("mobile-filters");

        Icon mobileIcon = new Icon("lumo", "plus");
        Span filtersHeading = new Span("Filtri");
        mobileFilters.add(mobileIcon, filtersHeading);
        mobileFilters.setFlexGrow(1, filtersHeading);
        mobileFilters.addClickListener(e -> {
            if (filters.getClassNames().contains("visible")) {
                filters.removeClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:plus");
            } else {
                filters.addClassName("visible");
                mobileIcon.getElement().setAttribute("icon", "lumo:minus");
            }
        });
        return mobileFilters;
    }

    public static class Filters extends Div implements Specification<Client> {

        private final TextField nome = new TextField("Nome");
        private final TextField codiceFiscale = new TextField("Codice fiscale");
        private final TextField citta = new TextField("Città");
        private final TextField telefono = new TextField("Telefono");
        private final TextField mail = new TextField("Mail");

        public Filters(Runnable onSearch, Runnable onAdd) {
            setWidthFull();
            addClassName("filter-layout");
            addClassNames(LumoUtility.Padding.Horizontal.LARGE, LumoUtility.Padding.Vertical.MEDIUM,
                    LumoUtility.BoxSizing.BORDER);
            nome.setPlaceholder("Cognome o Nome");

            Button resetBtn = new Button("Reset");
            resetBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            resetBtn.addClickListener(e -> {
                nome.clear();
                codiceFiscale.clear();
                citta.clear();
                telefono.clear();
                mail.clear();
                onSearch.run();
            });

            Button searchBtn = new Button("Cerca");
            searchBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
            searchBtn.addClickListener(e -> onSearch.run());

            Button addBtn = new Button("Aggiungi", new Icon("lumo", "plus"));
            addBtn.addClickListener(e -> onAdd.run());

            Div actions = new Div(resetBtn, searchBtn, addBtn);
            actions.addClassNames(LumoUtility.Gap.SMALL, "actions");

            add(nome, codiceFiscale, citta, telefono, mail, actions);
        }

        @Override
        public Predicate toPredicate(Root<Client> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
            List<Predicate> predicates = new ArrayList<>();

            if (!nome.isEmpty()) {
                String value = nome.getValue().trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("cognome")), value),
                        cb.like(cb.lower(root.get("nome")), value)));
            }
            addContains(predicates, cb, root, "codiceFiscale", codiceFiscale);
            addContains(predicates, cb, root, "citta", citta);
            addContains(predicates, cb, root, "telefono", telefono);
            addContains(predicates, cb, root, "mail", mail);

            return cb.and(predicates.toArray(Predicate[]::new));
        }

        private void addContains(List<Predicate> predicates, CriteriaBuilder cb, Root<Client> root,
                                 String attribute, TextField field) {
            if (!field.isEmpty()) {
                String value = field.getValue().trim().toLowerCase();
                predicates.add(cb.like(cb.lower(root.get(attribute)), "%" + value + "%"));
            }
        }
    }

    private Grid<Client> createGrid() {
        grid = new Grid<>(Client.class, false);
        grid.addColumn(Client::getCognome).setHeader("Cognome").setAutoWidth(true);
        grid.addColumn(Client::getNome).setHeader("Nome").setAutoWidth(true);
        grid.addColumn(Client::getCitta).setHeader("Città").setAutoWidth(true);
        grid.addColumn(Client::getCodiceFiscale).setHeader("Codice fiscale").setAutoWidth(true);
        grid.addColumn(Client::getIndirizzo).setHeader("Indirizzo").setAutoWidth(true);
        grid.addColumn(Client::getTelefono).setHeader("Telefono").setAutoWidth(true);
        grid.addColumn(Client::getMail).setHeader("Mail").setAutoWidth(true);

        grid.setItems(query -> clientService
                .list(VaadinSpringDataHelpers.toSpringPageRequest(query), filters)
                .stream());

        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);
        grid.setSizeFull();

        // Il click su qualsiasi cella della riga apre la dialog di modifica
        grid.addItemClickListener(e -> apriDialogModifica(e.getItem()));
        return grid;
    }

    private void apriDialogNuovo() {
        new ClientDialog("Nuovo cliente", new Client(), client -> {
            try {
                clientService.insertClient(client);
                refreshGrid();
                notifica("Cliente aggiunto", false);
            } catch (Exception ex) {
                log.error("Errore durante il salvataggio", ex);
                notifica("Errore durante il salvataggio: " + ex.getMessage(), true);
            }
        }).open();
    }

    private void apriDialogModifica(Client selezionato) {
        new ClientDialog("Modifica cliente", selezionato, client -> {
            try {
                clientService.updateClient(client);
                refreshGrid();
                notifica("Cliente aggiornato", false);
            } catch (Exception ex) {
                log.error("Errore durante il salvataggio", ex);
                notifica("Errore durante l'aggiornamento: " + ex.getMessage(), true);
                refreshGrid();
            }
        }).open();
    }

    private void notifica(String messaggio, boolean errore) {
        Notification n = Notification.show(messaggio, 3000, Notification.Position.BOTTOM_START);
        n.addThemeVariants(errore ? NotificationVariant.LUMO_ERROR : NotificationVariant.LUMO_SUCCESS);
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }
}