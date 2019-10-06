package cefriel.semanticfuel.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.graphhopper.GHResponse;

import cefriel.semanticfuel.service.map.GraphHopperService;

@RestController
public class GraphController {

	@Autowired
	private GraphHopperService graphHopperService;
	
	@GetMapping("/graph")
	public GHResponse getResponse() {
		return graphHopperService.computePath();
	}
}
