package mkle.zeroperdite.utils;

import com.vaadin.flow.i18n.DefaultI18NProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class CustomI18NProvider {

    DefaultI18NProvider defaultI18NProvider;

    public CustomI18NProvider(List<Locale> providedLocales) {
        defaultI18NProvider = new DefaultI18NProvider(providedLocales);
    }

    public String getTranslation(String key, Object... params) {
        Locale locale = Locale.forLanguageTag("it");
        return defaultI18NProvider.getTranslation(key, locale, params);
    }
}
