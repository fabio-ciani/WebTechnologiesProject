function applyRegex() {
	// https://stackoverflow.com/questions/59515074/z-pcre-equivalent-in-javascript-regex-to-match-all-markdown-list-items
	var emailRegex = new RegExp("^[a-z\\d\\.\\-_]+@[a-z\\d\\.-]+\\.[a-z]{2,4}$(?![^])");
	var pwdRegex = new RegExp("^[\\w\\.\\-\\!$%&?#@]+$(?![^])");

	var emailBool = emailRegex.test(document.getElementById("email").value);
	var pwdBool = pwdRegex.test(document.getElementById("pwd").value);

	if (!emailBool || !pwdBool) {
		if (!emailBool)
			document.getElementById("emailFormatError").textContent = "Invalid email format";

		if (!pwdBool)
			document.getElementById("pwdFormatError").textContent = "Invalid password format";

		return false;
	}

	return true;
}
