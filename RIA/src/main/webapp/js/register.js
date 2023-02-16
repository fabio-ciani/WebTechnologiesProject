function RegistrationForm(form) {
	this.data = new FormData(form);

	this.checkLenghts = function() {
		return this.data.get("name").length <= 32 && this.data.get("surname").length <= 32 && this.data.get("email").length <= 128 && this.data.get("pwd").length >= 4;
	};

	this.nullCheck = function() {
		return this.data.get("name") != null && this.data.get("surname") != null && this.data.get("email") != null && this.data.get("pwd") != null && this.data.get("repeat") != null;
	};

	this.checkRepeat = function() {
		return this.data.get("pwd") === this.data.get("repeat");
	};

	this.showErrors = function() {
		if (!this.checkLenghts()) {
			if (this.data.get("name").length > 32)
				document.getElementById("nameError").textContent = "Too long value";

			if (this.data.get("surname").length > 32)
				document.getElementById("surnameError").textContent = "Too long value";

			if (this.data.get("email").length > 128)
				document.getElementById("emailError").textContent = "Too long value";

			if (this.data.get("pwd").length < 4)
				document.getElementById("pwdError").textContent = "Too short value";
		}

		if (!this.checkRepeat())
			document.getElementById("repeatError").textContent = "Passwords do not coincide";
	}
}

(function() {
	var register = document.getElementById("registerBtn");

	register.addEventListener("click", (e) => {
		var form = document.getElementById("form");

		var fields = new RegistrationForm(form);
		var fieldsBool = fields.checkLenghts() && fields.nullCheck() && fields.checkRepeat();
		var regexBool = applyRegex();

		if (!fieldsBool)
			fields.showErrors();

		if (form.checkValidity() && fieldsBool && regexBool) {
			formAsyncCall("POST", "register", form, function(x) {
				// Could === operator be used?
				if (x.readyState == XMLHttpRequest.DONE) {
					var message = x.responseText;

					switch (x.status) {
						case 200:
							window.location.href = "login.html";
							break;
						case 400:
						case 401:
						case 500:
							// document.getElementById("error").textContent = message;
							alert(message);
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
	var fields = ["name", "surname", "email", "pwd", "repeat"];
	fields.forEach(function(element) {
		var target = document.getElementById(element);
		target.addEventListener("keypress", (e) => {
			if (e.key === "Enter") {
				register.click();
				// login.preventDefault();
			}
		});
	});
})();
