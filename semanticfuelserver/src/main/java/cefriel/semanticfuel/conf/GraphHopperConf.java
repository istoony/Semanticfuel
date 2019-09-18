package cefriel.semanticfuel.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.graphhopper.GraphHopperAPI;
import com.graphhopper.api.GraphHopperWeb;

@Configuration
public class GraphHopperConf {

	@Bean
	public GraphHopperWeb graphHopperWeb() {
		GraphHopperWeb gh = new GraphHopperWeb();
		// insert your key here
		gh.setKey("a31e6862-deb0-40fd-99ab-4cbaec1bdc8c");
		return gh;
	}
}
