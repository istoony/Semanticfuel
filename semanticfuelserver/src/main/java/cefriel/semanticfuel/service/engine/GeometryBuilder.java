package cefriel.semanticfuel.service.engine;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.util.GeometricShapeFactory;
import org.springframework.stereotype.Service;

import cefriel.semanticfuel.model.Point;
import cefriel.semanticfuel.service.AbstractService;

@Service
public class GeometryBuilder extends AbstractService {

	public Polygon createPolygon(int npoints, Point center, double diameter) {
		GeometricShapeFactory shapeFactory = new GeometricShapeFactory();
		shapeFactory.setNumPoints(npoints);
		shapeFactory.setCentre(new Coordinate(center.getLatitude(), center.getLongitude()));
		shapeFactory.setHeight(diameter);
		shapeFactory.setWidth(diameter);

		return shapeFactory.createEllipse();
	}
}
