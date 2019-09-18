package cefriel.semanticfuel.service.map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.api.GraphHopperWeb;
import com.graphhopper.util.shapes.GHPoint;

@Service
public class GraphHopperService {

	private Logger Log = LogManager.getLogger(this.getClass());

	@Autowired
	private GraphHopperWeb graphHopperWeb;

	public GHResponse computePath() {
		GHRequest req = new GHRequest().addPoint(new GHPoint(49.6724, 11.3494)).addPoint(new GHPoint(49.6550, 11.4180));
		req.setVehicle("bike");
		// Optionally enable/disable elevation in output PointList, currently bike and
		// foot support elevation, default is false
		req.getHints().put("elevation", false);
		// Optionally enable/disable turn instruction information, defaults is true
		req.getHints().put("instructions", true);
		// Optionally enable/disable path geometry information, default is true
		req.getHints().put("calc_points", true);
		GHResponse fullRes = graphHopperWeb.route(req);

		Log.debug("Mah -> {}", fullRes);
		
		return fullRes;
	}
}
