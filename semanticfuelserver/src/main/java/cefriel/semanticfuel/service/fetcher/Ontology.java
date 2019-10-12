package cefriel.semanticfuel.service.fetcher;

public final class Ontology {
	protected final static class SourceList {
		public static final String STATION_ID = "idImpianto";
		public static final String STATION_OWNER = "Gestore";
		public static final String STATION_TYPE = "Tipo impianto";
		public static final String STATION_NAME = "Nome impianto";
		public static final String STATION_FLAG = "Bandiera";

		public static class StationAddress {
			public static final String STATION_ADDRESS = "Indirizzo";
			public static final String STATION_CITY = "Comune";
			public static final String STATION_PROVINCE = "Provincia";
		}

		public static class StationCoordinate {
			public static final String STATION_LATITUDE = "Latitudine";
			public static final String STATION_LONGITUDE = "Longitudine";
		}
	}

	protected final static class SourcePrices {
		public static final String STATION_ID = "idImpianto";

		public static class StationPump {
			public static final String PUMP_FUEL = "descCarburante";
			public static final String PUMP_PIRCE = "Prezzo";
			public static final String PUMP_SERVICE = "isSelf";
			public static final String PUMP_UPDATE = "dtComu";
		}
	}
}
