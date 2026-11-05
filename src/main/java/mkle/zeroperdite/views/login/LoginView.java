package mkle.zeroperdite.views.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import com.vaadin.flow.router.internal.RouteUtil;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.annotation.PostConstruct;
import mkle.zeroperdite.security.AuthenticatedUser;
import mkle.zeroperdite.utils.CustomI18NProvider;
import mkle.zeroperdite.utils.ImageUtils;
import mkle.zeroperdite.utils.StaticResources;
import org.springframework.beans.factory.annotation.Autowired;

@AnonymousAllowed
@PageTitle("Login")
@Route(value = "login")
//@Layout("login")
public class LoginView extends Div implements BeforeEnterObserver/*, */ {

    private final AuthenticatedUser authenticatedUser;
    private LoginForm loginForm;
    private HorizontalLayout languageSelectionContainer;
    private HorizontalLayout pageLayout;
    private VerticalLayout loginWrapperLayout;
    private VerticalLayout loginBlock;
    private Image loginImage;


    @Autowired
    private CustomI18NProvider i18NProvider;
//    @Autowired
//    private SessionUtils sessionUtils;
    @Autowired
    private ImageUtils imageUtils;
//    @Autowired
//    private UserService userService;
//    @Autowired
//    private PasswordEncoder passwordEncoder;

    public LoginView(AuthenticatedUser authenticatedUser) {
        this.authenticatedUser = authenticatedUser;
    }
    @PostConstruct
    private void init(){
//        createLanguageComponent();
        createLoginForm();
        createFormWrapperLayout();
        createPageLayout();
//        createLogoImage();
    }

    private void createLoginForm(){
        loginForm = new LoginForm();
        loginForm.setI18n(getI18n());
        loginForm.setAction(RouteUtil.getRoutePath(VaadinService.getCurrent().getContext(), getClass()));
        loginForm.setForgotPasswordButtonVisible(false);
//        add(loginForm);
        loginForm.addClassName("login-form");

        UI ui = UI.getCurrent();
        ui.getPage().executeJs("document.getElementById('form').style.background = 'transparent'");

        H1 loginTitle = new H1(StaticResources.APPLICATION_NAME);
        loginTitle.addClassNames("login-custom-title");
//        ui.getPage().executeJs("document.querySelector('[name=\"username\"]').style.backgroundColor = 'black'");
//        ui.getPage().executeJs("document.querySelector('[name=\"password\"]').style.backgroundColor = 'black'");

        loginBlock = new VerticalLayout(loginTitle, loginForm);
        loginBlock.addClassName("custom-login-container");

    }

    private LoginI18n getI18n(){
        LoginI18n i18n = LoginI18n.createDefault();
//        i18n.setHeader(new LoginI18n.Header());
//        i18n.getHeader().setTitle(StaticResources.APPLICATION_NAME);
//        i18n.getHeader().setDescription("Login using user/user or admin/admin");

        i18n.getForm().setForgotPassword(i18NProvider.getTranslation("login-forgot-password"));
        LoginI18n.ErrorMessage errorMessage = i18n.getErrorMessage();
        errorMessage.setTitle(i18NProvider.getTranslation("login-error-title"));
        errorMessage.setMessage(i18NProvider.getTranslation("login-error-message"));
        errorMessage.setUsername(i18NProvider.getTranslation("login-error-username"));
        errorMessage.setPassword(i18NProvider.getTranslation("login-error-password"));
        return i18n;
    }

    private void createPageLayout() {
        loginImage = new Image(imageUtils.getAssetImage(ImageUtils.AssetImage.LOGIN_IMAGE), i18NProvider.getTranslation("login-image-alt"));
        loginImage.addClassName("login-image");

        Div imageContainer = new Div(loginImage);
        imageContainer.addClassName("login-image-container");
        pageLayout = new HorizontalLayout(loginWrapperLayout, imageContainer/*, languageSelectionContainer*/);
        pageLayout.addClassName("login-page-layout");

        Div pageContainer = new Div(pageLayout);
        pageContainer.addClassName("login-page-container");

        add(pageContainer);
        addClassName("main-login-page-container");
    }

    private void createFormWrapperLayout() {
        loginWrapperLayout = new VerticalLayout(loginBlock);
        loginWrapperLayout.setId("login-wrapper-layout");
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (authenticatedUser.get().isPresent()) {
            // Already logged in
            event.forwardTo("");
        }
        QueryParameters queryParameters = event.getLocation().getQueryParameters();
        loginForm.setError(queryParameters.getParameters().containsKey("error"));

        populateWithDemoUser(
                queryParameters.getSingleParameter("username").orElse(""),
                queryParameters.getSingleParameter("password").orElse("")
        );

    }

    private void populateWithDemoUser(String username, String password){
        UI ui = UI.getCurrent();
        ui.getPage().executeJs("document.querySelector('[name=\"username\"]').value = $0", username);
        ui.getPage().executeJs("document.querySelector('[name=\"password\"]').value = $0", password);
    }

//    private void createLanguageComponent(){
//
//        Span itButton = new Span("IT");
//        itButton.addClassNames("language-selector-component");
//        Span enButton = new Span("EN");
//        enButton.addClassNames("language-selector-component");
//        if("it".equals(sessionUtils.getLocale())) itButton.addClassName("selected-language");
//        else enButton.addClassName("selected-language");
//        itButton.addClickListener(click -> {
//            if("it".equals(sessionUtils.getLocale())) return;
//            sessionUtils.setLocale("it");
//            enButton.removeClassName("selected-language");
//            itButton.addClassName("selected-language");
//            UI.getCurrent().getPage().reload();
//        });
//        enButton.addClickListener(click -> {
//            if("en".equals(sessionUtils.getLocale())) return;
//            sessionUtils.setLocale("en");
//            itButton.removeClassName("selected-language");
//            enButton.addClassName("selected-language");
//            UI.getCurrent().getPage().reload();
//        });
//        languageSelectionContainer = new HorizontalLayout(enButton, itButton);
//        languageSelectionContainer.addClassName("language-selection-container");
//    }
}

