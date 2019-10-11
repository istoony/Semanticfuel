package cefriel.semanticfuel.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public abstract class AbstractTest {
	protected final Logger LOG = LogManager.getLogger(this.getClass());
}
