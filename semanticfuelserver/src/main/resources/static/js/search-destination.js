var START;
var END;
var STEP = 0;

$(document).ready(function() {

	$("#label-input").html('Scegli il luogo di partenza');

	$("#search-form").submit(function(event) {
		// stop submit the form, we will post it manually.
		event.preventDefault();
		fire_ajax_submit();
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
	$("#div-fuel").removeClass('display-none');
	$("#div-fuel").addClass('display-block');
	
	
}

function printLoadingPage() {
	var result = '<div class="row">'
			+ '<div class="col-12"><h3>Città di partenza</h3></div>'
			+ '<div class="col-12">' + placeTableUtil([START]) + '</div>'
			+ '<div class="col-12"><h3>Città di arrivo</h3></div>'
			+ '<div class="col-12">' + placeTableUtil([END]) + '</div>'
			+ '</div>';
	$('#search-div').html(result);
	
	var toSend = {};
	toSend['start'] = START;
	toSend['end'] = END;
	console.log(JSON.stringify(toSend));

	$.ajax({
		type : "POST",
		contentType : "application/json",
		url : "/api/direction",
		dataType : 'json',
		data: JSON.stringify(toSend),
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

function printGasStationResult(data){
	alert(data.responseText);
}