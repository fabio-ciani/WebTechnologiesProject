(function() {
	var login = document.getElementById("loginBtn");

	login.addEventListener("click", (e) => {
		var form = document.getElementById("form");

		if (form.checkValidity()) {
			formAsyncCall("POST", "login", form, function(x) {
				// Could === operator be used?
				if (x.readyState == XMLHttpRequest.DONE) {
					var message = x.responseText;

					switch (x.status) {
						case 200:
							sessionStorage.setItem("email", message);
							window.location.href = "home.html";
							break;
						case 400:
						case 401:
						case 500:
							document.getElementById("error").textContent = message;
							// alert(message);
							break;
					}
				}
			});
		} else {
			form.reportValidity();
		}

		// e.preventDefault() if <input type="submit"/> is used (?)
	});

	// Add ENTER key support.
	var fields = ["email", "pwd"];
	fields.forEach(function(element) {
		var target = document.getElementById(element);
		target.addEventListener("keypress", (e) => {
			if (e.key === "Enter") {
				login.click();
				// login.preventDefault();
			}
		});
	});
})();
