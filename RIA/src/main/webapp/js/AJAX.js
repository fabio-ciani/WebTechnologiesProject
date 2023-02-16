function asyncCall(method, url, callback) {
	var req = new XMLHttpRequest();

	// CLOSURE
	req.onreadystatechange = function() {
		callback(req);
	};

	req.open(method, url);
	req.send();
}

function formAsyncCall(method, url, form, callback) {
	var req = new XMLHttpRequest();

	// CLOSURE
	req.onreadystatechange = function() {
		callback(req);
	};

	req.open(method, url);

	if (form == null) {
		req.send();
	} else {
		req.send(new FormData(form));
	}
}
