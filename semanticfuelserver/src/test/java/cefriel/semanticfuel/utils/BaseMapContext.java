package cefriel.semanticfuel.utils;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

import cefriel.semanticfuel.conf.GraphHopperConf;

@TestConfiguration
@Import({GraphHopperConf.class})
public class BaseMapContext extends AbstractTest {

}
