package cefriel.semanticfuel.conf;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.opencsv.CSVReader;

@Configuration
@ComponentScan(basePackageClasses = { CSVReader.class })
public class ApplicationConfiguration {

}