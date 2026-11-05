package mkle.zeroperdite;

import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import javax.sql.DataSource;

import jakarta.annotation.PostConstruct;
import mkle.zeroperdite.data.SamplePersonRepository;
import mkle.zeroperdite.utils.CustomI18NProvider;
import mkle.zeroperdite.utils.StaticResources;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.ApplicationDataSourceScriptDatabaseInitializer;
import org.springframework.boot.sql.autoconfigure.init.SqlInitializationProperties;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * The entry point of the Spring Boot application.
 *
 * Use the @PWA annotation make the application installable on phones, tablets
 * and some desktop browsers.
 *
 */
@SpringBootApplication
@StyleSheet(Lumo.STYLESHEET)
@StyleSheet("styles.css")
@EnableConfigurationProperties(SqlInitializationProperties.class)
@Theme(value = "zero-perdite")
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
//    @Bean
//    ApplicationDataSourceScriptDatabaseInitializer customInitializer(DataSource dataSource,
//            SqlInitializationProperties properties, SamplePersonRepository repository) {
//        // Only run schema.sql/data.sql.nomore when the DB is empty
//        return new ApplicationDataSourceScriptDatabaseInitializer(dataSource, properties) {
//            @Override
//            public boolean initializeDatabase() {
//                return (repository.count() == 0L) && super.initializeDatabase();
//            }
//        };
//    }

    @Bean
    public CustomI18NProvider i18NProvider(){
        return new CustomI18NProvider(List.of(Locale.forLanguageTag("it")));
    }

    @PostConstruct
    private void init(){
        //cleans and creates tmp directory
        File tmpDirectory = new File(StaticResources.TMP_DIRECTORY);
        //if there is allowedMemory file with same name as folder, the file will be deleted for avoiding errors
        FileUtils.deleteQuietly(tmpDirectory);
        tmpDirectory.mkdir();

        //crete filesystem directory if not exists
        File filesystemDirectory = new File(StaticResources.FILESYSTEM_DIRECTORY);
        //if there is allowedMemory file with same name as folder, the file will be deleted for avoiding errors
        if(filesystemDirectory.isFile()) filesystemDirectory.delete();
        if(!filesystemDirectory.exists()) filesystemDirectory.mkdir();


        //prints the successful startup signal
        System.out.println("STARTED");
    }

}


