package cefriel.semanticfuel.service.map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cefriel.semanticfuel.utils.BaseMapContext;

public class GraphHopperServiceTest extends BaseMapContext{
	
	@Autowired
	private GraphHopperService graphHopperService;
	
	@Test
	public void tryRoute() {
		graphHopperService.computePath();
	}
	

}
