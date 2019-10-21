var START;
var END;
var FUEL;
var STEP = 0;
var listOfFuel = [ 'Benzina', 'Gasolio', 'Metano', 'GPL', 'Excellium Diesel',
		'Blue Super', 'Blue Diesel', 'Gasolio Alpino', 'Gasolio Oro Diesel',
		'Gasolio artico', 'Benzina WR 100', 'Gasolio Premium', 'Hi-Q Diesel',
		'HiQ Perform+', 'Gasolio Speciale', 'Gasolio Ecoplus',
		'Benzina Plus 98', 'Gasolio Gelo', 'L-GNC', 'GNL', 'DieselMax',
		'Benzina speciale', 'Diesel e+10', 'F101', 'GP DIESEL',
		'Gasolio Energy D', 'Benzina Energy 98 ottani', 'Supreme Diesel',
		'E-DIESEL', 'Benzina Shell V Power', 'Diesel Shell V Power',
		'Magic Diesel', 'Blu Diesel Alpino', 'S-Diesel', 'R100', 'V-Power',
		'V-Power Diesel', 'Benzina 100 ottani' ];

$(document).ready(function() {

	$("#refresh-button").on("click", function() {
		location.reload(true);
	});

	$("#label-input").html('Scegli il luogo di partenza');

	$("#search-form").submit(function(event) {
		// stop submit the form, we will post it manually.
		event.preventDefault();
		fire_ajax_submit();
	});

	$("#fuel-form").submit(function(event) {
		// stop submit the form, we will post it manually.
		event.preventDefault();
	});

});

function fire_ajax_submit() {
	var search = {}
	search["findplace"] = $("#start").val();

	$("#btn-search").prop("disabled", true);
	$.ajax({
		type : "GET",
		contentType : "application/json",
		url : "/api/findplace/" + search["findplace"],
		dataType : 'json',
		cache : false,
		timeout : 600000,
		success : printPlaceTable,
		error : function(e) {
			var json = "<h3>Risposta del server: </h3><pre>" + e.responseText
					+ "</pre>";
			$('#feedback').html(json);

			console.log("ERROR : ", e);
			$("#btn-search").prop("disabled", false);
		}
	});
}

function placeTableUtil(data) {
	var htmlFormatted = '<table class="table table-striped table-hover" id="place">'
	htmlFormatted += '<tr>' + '<th scope="col">Id</th>'
			+ '<th scope="col">Nome</th>' + '<th scope="col">Regione</th>'
			+ '<th scope="col">Nazione</th>'
			+ '<th scope="col">Latitudine</th>'
			+ '<th scope="col">Longitudine</th>'
	var i;
	for (i = 0; i < data.length; i++) {
		htmlFormatted += '<tr>'
		htmlFormatted += '<td>' + i + '</td>';
		htmlFormatted += '<td>' + data[i]['name'] + '</td>';
		htmlFormatted += '<td>' + data[i]['region'] + '</td>';
		htmlFormatted += '<td>' + data[i]['country'] + '</td>';
		htmlFormatted += '<td>' + data[i]['coordinates']['latitude'] + '</td>';
		htmlFormatted += '<td>' + data[i]['coordinates']['longitude'] + '</td>';
		htmlFormatted += '</tr>'
	}
	htmlFormatted += '</table>'
	return htmlFormatted;
}

function printPlaceTable(data) {
	var htmlFormatted = "<h3>Risultati ricerca</h3>";
	htmlFormatted += 'Seleziona la città che preferisci:';
	htmlFormatted += placeTableUtil(data);
	$('#feedback').html(htmlFormatted);

	console.log("SUCCESS : ", data);
	$("#btn-search").prop("disabled", false);

	$("#place tr").click(function() {
		$(this).addClass('selected').siblings().removeClass('selected');
		var value = $(this).find('td:first').html();
		console.log(data[value]['coordinates']);
		$('#feedback').html('');
		$('#start').val('')
		if (STEP == 0) {
			START = data[value];
			$("#label-input").html('Scegli il luogo di destinazione');
			STEP = 1;
		} else {
			END = data[value];
			$('#search-div').html('');
			printFuelSelect();
		}
	});
}

function printFuelSelect() {
	var optionList = "<option value=''>None</option>";
	listOfFuel.forEach(function(el) {
		optionList += "<option value='" + el + "'>" + el + "</option>";
	});
	$("#combobox").html(optionList);

	$("#div-fuel").removeClass('display-none');
	$("#div-fuel").addClass('display-block');

	$("#fuel-form").submit(function() {
		event.preventDefault();
		FUEL = $("#combobox").val();
		console.log(FUEL);
		printLoadingPage();
		$("#div-fuel").addClass('display-none');
		$("#div-fuel").removeClass('display-block');
	});

}

function printLoadingPage() {
	var result = '<div class="row"><div class="col-md-6" style="padding-right: 35px;"><div class="row">'
			+ '<div class="col-12"><h3>Città di partenza</h3></div>'
			+ '<div class="col-12">'
			+ placeTableUtil([ START ])
			+ '</div>'
			+ '</div></div><div class="col-md-6" style="padding-left: 35px;"><div class="row">'
			+ '<div class="col-12"><h3>Città di arrivo</h3></div>'
			+ '<div class="col-12">'
			+ placeTableUtil([ END ])
			+ '</div>'
			+ '</div></div>'
			+ '<div class="col-md-12"><h3><b>Carburante:</b>'
			+ FUEL + '</h3></div></div>';
	$('#search-div').html(result);

	var toSend = {};
	toSend['start'] = START;
	toSend['end'] = END;
	toSend['fuel'] = FUEL;
	console.log(JSON.stringify(toSend));

	$.ajax({
		type : "POST",
		contentType : "application/json",
		url : "/api/direction",
		dataType : 'json',
		data : JSON.stringify(toSend),
		cache : false,
		timeout : 600000,
		success : printGasStationResult,
		error : function(e) {
			var json = "<h3>Risposta del server: </h3><pre>" + e.responseText
					+ "</pre>";
			$('#feedback').html(json);

			console.log("ERROR : ", e);
			$("#btn-search").prop("disabled", false);
		}
	});
}

function printGasStationResult(data) {

	var mediaLat = (START['coordinates']['latitude'] + END['coordinates']['latitude']) / 2;
	var mediaLong = (START['coordinates']['longitude'] + END['coordinates']['longitude']) / 2;

	document.addEventListener("DOMContentLoaded", function() {
		// The DOMContentLoaded event fires when the page is ready (like
		// $(document).ready() in jQuery)
		var map = createMap('map', -21.208312, 46.262851, 6);
		// map is the id of our div element and the other options are
		// lattitude, longitude and zoom
	});

	var map = createMap('map', mediaLat, mediaLong, 8);

	var latlngs = [];

	var pathCoordinates = data['pathCoordinates']
	var i;
	for (i = 0; i < pathCoordinates.length; i++) {
		latlngs.push([ pathCoordinates[i]['latitude'],
				pathCoordinates[i]['longitude'] ]);
	}

	var greenIcon = L
			.icon({
				iconUrl : 'https://raw.githubusercontent.com/iconic/open-iconic/master/png/map-marker-8x.png',
				iconSize : [ 32, 32 ],
				iconAnchor : [ 16, 32 ],
				popupAnchor : [ -3, -76 ]
			});

	var pumps = data['gasStations'];

	for (i = 0; i < pumps.length; i++) {

		var isSelfPrice = 'Non presente';
		var isNotSelfPrice = 'Non presente';

		for (var j = 0; j < pumps[i]['pumps'].length; j++) {
			if (pumps[i]['pumps'][j]['isSelf'] == "True") { // TODO occhio qua
				isSelfPrice = (Math.round(pumps[i]['pumps'][j]['price'] * 1000) / 1000)
						+ "€";
			} else {
				isNotSelfPrice = (Math
						.round(pumps[i]['pumps'][j]['price'] * 1000) / 1000)
						+ "€";
			}
		}

		var marker = L.marker(
				[ pumps[i]['coordinate']['latitude'],
						pumps[i]['coordinate']['longitude'] ], {
					icon : greenIcon
				}).addTo(map).bindPopup(
				pumps[i]['flag'] + "<br/>SELF: " + isSelfPrice
						+ "<br/>NON-SELF: " + isNotSelfPrice);
	}

	var polyline = L.polyline(latlngs, {
		color : 'red'
	}).addTo(map);
	// zoom the map to the polyline
	map.fitBounds(polyline.getBounds());

	function createMap(elemId, centerLat, centerLng, zoom) {
		var map = new L.Map(elemId);

		// Data provider
		var osmUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
		var osmAttrib = 'Map data © <a href="https://openstreetmap.org">OpenStreetMap</a> contributors';

		// Layer
		var osmLayer = new L.TileLayer(osmUrl, {
			minZoom : 4,
			maxZoom : 20,
			attribution : osmAttrib
		});

		// Map
		map.setView(new L.LatLng(centerLat, centerLng), zoom);
		map.addLayer(osmLayer);
		return map;
	}
}