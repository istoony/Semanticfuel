package cefriel.semanticfuel.service.map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import cefriel.semanticfuel.AbstractTest;

public class GraphHopperServiceTest extends AbstractTest{
	
	@Autowired
	private GraphHopperService graphHopperService;
	
	@Test
	public void tryRoute() {
		graphHopperService.computePath();
	}
	

}
