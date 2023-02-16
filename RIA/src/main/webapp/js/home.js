// Define an object to store information regarding drag & drop operation.
let orchestrator = new DragDropOrchestrator();

// Instantiate homepage.
window.addEventListener("load", () => {
	if (sessionStorage.getItem("email") == null) {
		window.location.href = "login.html";
	} else {
		document.getElementById("user").textContent = sessionStorage.getItem("email");

		asyncCall("GET", "home", function(x) {
			// Could === operator be used?
			if (x.readyState == XMLHttpRequest.DONE) {
				var message = x.responseText;

				switch (x.status) {
					case 200:
						var ans = JSON.parse(message);

						var folders = ans[0];
						var subfolders = ans[1];
						var documents = ans[2];

						if (folders.length == 0) {
							document.getElementById("tree").style.display = "none";
							document.getElementById("no-folders").style.removeProperty("display");
						} else {
							var fData = new FoldersLoader();
							fData.initialize(folders);

							var sData = new SubfoldersLoader();
							sData.initialize(subfolders);

							var dData = new DocumentsLoader();
							dData.initialize(documents);
						}

						break;
					case 500:
						// document.getElementById("error").textContent = message;
						alert(message);
						break;
				}
			}
		});

		document.getElementById("parent-creation").addEventListener("click", () => {
			var formNode = document.getElementById("form");

			// Reset <div> content.
			formNode.innerHTML = "";

			// Create HTML form.
			var form = document.createElement("form");
			form.style.visibility = "visible";

			var nameText = document.createElement("span");
			nameText.textContent = "Name: ";
			form.appendChild(nameText);

			var nameField = document.createElement("input");
			nameField.type = "text";
			nameField.name = "name";
			nameField.required = true;
			form.appendChild(nameField);

			// Insert line breaks.
			form.appendChild(document.createElement("br"));
			form.appendChild(document.createElement("br"));

			var btn = document.createElement("input");
			btn.type = "button";
			btn.value = "Create entity";
			btn.addEventListener("click", (e) => {
				var form = e.target.closest("form");

				if (form.checkValidity()) {
					formAsyncCall("POST", "FolderCreation", form, function(x) {
						// Could === operator be used?
						if (x.readyState == XMLHttpRequest.DONE) {
							var message = x.responseText;

							switch (x.status) {
								case 200:
									form.reset();

									document.getElementById("tree").style.removeProperty("display");
									document.getElementById("no-folders").style.display = "none";

									var updater = new HomepageUpdater();
									updater.update();

									break;
								case 400:
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
			});
			form.appendChild(btn);

			// Add ENTER key support.
			nameField.addEventListener("keypress", (e) => {
				if (e.key === "Enter") {
					btn.click();
					e.preventDefault();
				}
			});

			formNode.appendChild(form);

			// Set table cell as visible.
			document.getElementById("form-container").style.visibility = "visible";

			// Update visual information.
			document.getElementById("root-creation").style.display = "inline";
			document.getElementById("entity-creation").style.display = "none";
		});

		document.querySelector("a[href='logout']").addEventListener("click", () => {
			window.sessionStorage.removeItem("email");
		});
	}
});

function HomepageUpdater() {
	this.update = function() {
		asyncCall("GET", "home", function(x) {
			// Could === operator be used?
			if (x.readyState == XMLHttpRequest.DONE) {
				var message = x.responseText;

				switch (x.status) {
					case 200:
						var ans = JSON.parse(message);

						var fData = new FoldersLoader();
						fData.initialize(ans[0]);

						var sData = new SubfoldersLoader();
						sData.initialize(ans[1]);

						var dData = new DocumentsLoader();
						dData.initialize(ans[2]);

						break;
					case 500:
						// document.getElementById("error").textContent = message;
						alert(message);
						break;
				}
			}
		});
	}
}

function FoldersLoader() {
	this.roots = document.getElementById("tree");

	this.initialize = function(list) {
		// Erase container content.
		this.roots.innerHTML = "";

		// Create file-tree structure.
		var container = document.createElement("ul");
		this.roots.appendChild(container);

		// Show folders list.
		var self = this;
		var element, button;
		list.forEach(function(f) {
			element = document.createElement("li");
			element.setAttribute("id", "f" + f["id"]);	// Generate unique HTML id.
			element.textContent = f["name"];
			if (list.indexOf(f) == list.length - 1)
				element.className = "last";

			container.appendChild(element);

			button = document.createElement("input");
			button.type = "button";
			button.value = "Create subfolder";
			button.className = "create";
			button.setAttribute("folder_id", f["id"]);
			button.addEventListener("click", () => {
				var formNode = document.getElementById("form");

				// Reset <div> content.
				formNode.innerHTML = "";

				// Create HTML form.
				var form = document.createElement("form");
				form.style.visibility = "visible";

				var nameText = document.createElement("span");
				nameText.textContent = "Name: ";
				form.appendChild(nameText);

				var nameField = document.createElement("input");
				nameField.type = "text";
				nameField.name = "name";
				nameField.required = true;
				form.appendChild(nameField);

				var parentField = document.createElement("input");
				parentField.type = "hidden";
				parentField.name = "parent_folder";
				parentField.value = f["id"];
				form.appendChild(parentField);

				// Insert line breaks.
				form.appendChild(document.createElement("br"));
				form.appendChild(document.createElement("br"));

				var btn = document.createElement("input");
				btn.type = "button";
				btn.value = "Create entity";
				btn.addEventListener("click", (e) => {
					var form = e.target.closest("form");

					if (form.checkValidity()) {
						formAsyncCall("POST", "SubfolderCreation", form, function(x) {
							// Could === operator be used?
							if (x.readyState == XMLHttpRequest.DONE) {
								var message = x.responseText;

								switch (x.status) {
									case 200:
										form.reset();

										var updater = new SubfoldersUpdater(f["id"]);
										updater.update();

										break;
									case 400:
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
				});
				form.appendChild(btn);

				// Add ENTER key support.
				nameField.addEventListener("keypress", (e) => {
					if (e.key === "Enter") {
						btn.click();
						e.preventDefault();
					}
				});

				formNode.appendChild(form);

				// Set table cell as visible.
				document.getElementById("form-container").style.visibility = "visible";

				// Update visual information.
				document.getElementById("root-creation").style.display = "none";
				document.getElementById("entity-creation").style.display = "inline";
				document.getElementById("entity").textContent = "subfolder";
				document.getElementById("target-entity").textContent = f["name"];
			});

			container.appendChild(button);

			container.appendChild(document.createElement("br"));
		});
	}
}

function SubfoldersUpdater(id) {
	this.id = id;

	this.update = function() {
		var self = this;

		asyncCall("GET", "subfolders?folder=" + self.id, function(x) {
			// Could === operator be used?
			if (x.readyState == XMLHttpRequest.DONE) {
				var message = x.responseText;

				switch (x.status) {
					case 200:
						var ans = new Object();
						ans[self.id] = JSON.parse(message);

						// Erase outdated content.
						document.getElementById("f" + self.id + "-subfolders").remove();

						var sLoader = new SubfoldersLoader();
						sLoader.initialize(ans);

						var dLoader;
						ans[self.id].forEach(function(s) {
							asyncCall("GET", "documents?subfolder=" + s["id"], function(x) {
								// Could === operator be used?
								if (x.readyState == XMLHttpRequest.DONE) {
									var message = x.responseText;

									switch (x.status) {
										case 200:
											var ans = new Object();
											ans[s["id"]] = JSON.parse(message);

											var dLoader = new DocumentsLoader();
											dLoader.initialize(ans);

											break;
										case 400:
										case 500:
											// document.getElementById("error").textContent = message;
											alert(message);
											break;
									}
								}
							});
						});

						break;
					case 400:
					case 500:
						// document.getElementById("error").textContent = message;
						alert(message);
						break;
				}
			}
		});
	}
}

function SubfoldersLoader() {
	this.initialize = function(map) {
		var parent, target;
		for (var key of Object.keys(map)) {
			// Query parent folder HTML element.
			parent = document.getElementById("f" + key);
			target = parent.nextElementSibling.nextElementSibling;	// Obtain <br> tag.

			// Create a new tree level.
			var container = document.createElement("ul");
			container.setAttribute("id", "f" + key + "-subfolders");	// Generate unique HTML id.

			// Show subfolders list.
			var element, text, button;
			var eventsLoader;
			map[key].forEach(function(s) {
				element = document.createElement("li");

				element.setAttribute("id", "s" + s["id"]);	// Generate unique HTML id.
				text = document.createElement("span");
				text.textContent = s["name"];
				element.appendChild(text);
				if (map[key].indexOf(s) == map[key].length - 1)
					element.className = "last";

				// Add drag & drop support.
				element.setAttribute("drop", true);
				text.draggable = true;
				eventsLoader = new SubfolderDragDropOrchestrator(text, element);
				eventsLoader.addListeners();

				container.appendChild(element);

				button = document.createElement("input");
				button.type = "button";
				button.value = "Create document";
				button.className = "create";
				button.setAttribute("subfolder_id", s["id"]);
				button.addEventListener("click", () => {
					var formNode = document.getElementById("form");

					// Reset <div> content.
					formNode.innerHTML = "";

					// Create HTML form.
					var form = document.createElement("form");
					form.style.visibility = "visible";

					var text, field;
					var elements = ["name", "type", "summary"];
					var fields = [];
					elements.forEach(function(elem) {
						text = document.createElement("span");
						text.textContent = elem[0].toUpperCase() + elem.substring(1) + ": ";
						form.appendChild(text);

						var field = document.createElement("input");
						field.type = "text";
						field.name = elem;
						if (elem != "summary")
							field.required = true;
						form.appendChild(field);

						fields.push(field);

						form.appendChild(document.createElement("br"));
					});

					var parentField = document.createElement("input");
					parentField.type = "hidden";
					parentField.name = "subfolder";
					parentField.value = s["id"];
					form.appendChild(parentField);

					// Insert line break.
					form.appendChild(document.createElement("br"));

					var btn = document.createElement("input");
					btn.type = "button";
					btn.value = "Create entity";
					btn.addEventListener("click", (e) => {
						var form = e.target.closest("form");

						if (form.checkValidity()) {
							formAsyncCall("POST", "DocumentCreation", form, function(x) {
								// Could === operator be used?
								if (x.readyState == XMLHttpRequest.DONE) {
									var message = x.responseText;

									switch (x.status) {
										case 200:
											form.reset();

											var updater = new DocumentsUpdater(s["id"]);
											updater.update();

											break;
										case 400:
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
					});
					form.appendChild(btn);

					// Add ENTER key support.
					fields.forEach(function(box) {
						box.addEventListener("keypress", (e) => {
							if (e.key === "Enter") {
								btn.click();
								e.preventDefault();
							}
						});
					});

					formNode.appendChild(form);

					// Set table cell as visible.
					document.getElementById("form-container").style.visibility = "visible";

					// Update visual information.
					document.getElementById("root-creation").style.display = "none";
					document.getElementById("entity-creation").style.display = "inline";
					document.getElementById("entity").textContent = "document";
					document.getElementById("target-entity").textContent = s["name"];
				});

				container.appendChild(button);

				container.appendChild(document.createElement("br"));
			});

			target.parentNode.insertBefore(container, target.nextSibling);
			// target.insertAdjacentHTML("afterend", container);
		}
	}
}

function DocumentsUpdater(id) {
	this.id = id;

	this.update = function() {
		var self = this;

		asyncCall("GET", "documents?subfolder=" + self.id, function(x) {
			// Could === operator be used?
			if (x.readyState == XMLHttpRequest.DONE) {
				var message = x.responseText;

				switch (x.status) {
					case 200:
						var ans = new Object();
						ans[self.id] = JSON.parse(message);

						// Erase outdated content.
						document.getElementById("s" + self.id + "-documents").remove();

						var loader = new DocumentsLoader();
						loader.initialize(ans);

						break;
					case 400:
					case 500:
						// document.getElementById("error").textContent = message;
						alert(message);
						break;
				}
			}
		});
	}
}

function DocumentsLoader() {
	this.initialize = function(map) {
		var parent, target;
		for (var key of Object.keys(map)) {
			// Query parent folder HTML element.
			parent = document.getElementById("s" + key);
			target = parent.nextElementSibling.nextElementSibling;	// Obtain <br> tag.

			// Create a new tree level.
			var container = document.createElement("ul");
			container.setAttribute("id", "s" + key + "-documents");	// Generate unique HTML id.

			// Show documents list.
			var element, text, button;
			var eventsLoader;
			map[key].forEach(function(d) {
				element = document.createElement("li");

				element.setAttribute("id", "d" + d["id"]);	// Generate unique HTML id.
				text = document.createElement("span");
				text.textContent = d["name"] + '.' + d["type"];
				element.appendChild(text);
				if (map[key].indexOf(d) == map[key].length - 1)
					element.className = "last";

				// Add event listeners on drag & drop.
				text.draggable = true;
				eventsLoader = new DocumentDragDropOrchestrator(text, parent);
				eventsLoader.addListeners();

				container.appendChild(element);

				button = document.createElement("input");
				button.type = "button";
				button.value = "View detail";
				button.className = "detail";
				button.setAttribute("document_id", d["id"]);
				button.addEventListener("click", (e) => {
					/*
					asyncCall("GET", "detail?subfolder=" + d["subfolderID"] + "&document=" + d["id"], function(x) {
						// Could === operator be used?
						if (x.readyState == XMLHttpRequest.DONE) {
							var message = x.responseText;

							switch (x.status) {
								case 200:
									console.log(message);
									break;
								case 400:
								case 500:
									// document.getElementById("error").textContent = message;
									alert(message);
									break;
							}
						}
					});
					*/

					document.getElementById("detail-container").style.visibility = "visible";

					document.getElementById("title-document").textContent = d["name"] + '.' + d["type"];
					// Query parent subfolder HTML element.
					var subfolder = document.getElementById("s" + d["subfolderID"]);
					// var subfolder = e.target.closest("ul").previousSibling.previousSibling.previousSibling;
					document.getElementById("title-subfolder").textContent = subfolder.textContent;

					document.getElementById("detail-name").textContent = d["name"];
					document.getElementById("detail-type").textContent = "*." + d["type"];
					document.getElementById("detail-owner").textContent = sessionStorage.getItem("email");
					document.getElementById("detail-date").textContent = d["creation_date"];
					if (d["summary"].length == 0) {
						document.getElementById("summary").style.display = "none";
						document.getElementById("no-summary").style.removeProperty("display");
					} else {
						document.getElementById("summary").style.removeProperty("display");
						document.getElementById("detail-summary").textContent = d["summary"];
						document.getElementById("no-summary").style.display = "none";
					}
				});

				container.appendChild(button);

				container.appendChild(document.createElement("br"));
			});

			target.parentNode.insertBefore(container, target.nextSibling);
			// target.insertAdjacentHTML("afterend", container);
		}
	}
}

function DragDropOrchestrator() {
	this.src = undefined;
	this.dest = undefined;
	this.target = undefined;

	this.setSource = function(src) {
		this.src = src;
	}

	this.setDestination = function(dest) {
		this.dest = dest;
	}

	this.setTarget = function(target) {
		this.target = target;
	}

	this.call = function() {
		// Fire event iff internal state arguments are not undefined.
		if (this.src != undefined && this.dest != undefined && this.target != undefined) {
			// Save AJAX call arguments in order to update UI.
			var args = [this.src, this.dest, this.target];

			if (this.dest["id"] != "recycle-bin") {
				asyncCall("POST", "move?source=" + args[0]["id"].substring(1) + "&destination=" + args[1]["id"].substring(1) + "&document=" + args[2]["id"].substring(1), function(x) {
					// Could === operator be used?
					if (x.readyState == XMLHttpRequest.DONE) {
						var message = x.responseText;

						switch (x.status) {
							case 200:
								var srcUpdater = new DocumentsUpdater(args[0]["id"].substring(1));
								var destUpdater = new DocumentsUpdater(args[1]["id"].substring(1));

								srcUpdater.update();
								destUpdater.update();

								break;
							case 400:
							case 500:
								// document.getElementById("error").textContent = message;
								alert(message);
								break;
						}
					}
				});
			} else {
				if (window.confirm("You are requesting the deletion of " + args[2].children[0].textContent + " document within " + args[0].children[0].textContent + " subfolder. Are you sure?")) {
					asyncCall("POST", "delete?source=" + args[0]["id"].substring(1) + "&document=" + args[2]["id"].substring(1), function(x) {
						// Could === operator be used?
						if (x.readyState == XMLHttpRequest.DONE) {
							var message = x.responseText;

							switch (x.status) {
								case 200:
									var updater = new DocumentsUpdater(args[0]["id"].substring(1));
									updater.update();

									break;
								case 400:
								case 500:
									// document.getElementById("error").textContent = message;
									alert(message);
									break;
							}
						}
					});
				}
			}

			// Reset internal state.
			this.src = undefined;
			this.dest = undefined;
			this.target = undefined;
		}
	}
}

function SubfolderDragDropOrchestrator(node, dest) {
	this.node = node;
	this.dest = dest;

	this.addListeners = function() {
		// Disable drag animation.
		this.node.addEventListener("dragstart", (e) => {
			e.preventDefault();
		});

		// Change color based on event state.
		this.node.addEventListener("dragover", (e) => {
			var elem = e.target;

			if (elem.style.color != "green")
				elem.style.color = "blue";

			// Update orchestrator internal state.
			orchestrator.setDestination(this.dest);

			// Disable default behaviour to let ondrop event firing.
			e.preventDefault();
		});

		// Change color based on event state.
		this.node.addEventListener("dragleave", (e) => {
			var elem = e.target;

			if (elem.style.color != "green")
				e.target.style.removeProperty("color");

			// Update orchestrator internal state.
			orchestrator.setDestination(undefined);
		});

		// Execute operation.
		this.node.addEventListener("drop", (e) => {
			e.target.style.removeProperty("color");

			// Call orchestrator main function.
			orchestrator.call();

			// Let the orchestrator set undefined references.
		});
	}
}

function DocumentDragDropOrchestrator(node, src) {
	this.node = node;
	this.src = src;

	this.addListeners = function() {
		// Change subfolder color while drag operation occurs.
		this.node.addEventListener("dragstart", (e) => {
			this.src.children[0].style.color = "green";
			// Update orchestrator internal state.
			orchestrator.setSource(this.src);

			// Update orchestrator internal state.
			orchestrator.setTarget(node.closest("li"));
		});

		// Change subfolder color after drag & drop abort.
		this.node.addEventListener("dragend", (e) => {
			this.src.children[0].style.removeProperty("color");

			// Update orchestrator internal state.
			orchestrator.setSource(undefined);
			orchestrator.setTarget(undefined);
		});

		// Change subfolder color before drag & drop event firing.
		this.node.addEventListener("drop", (e) => {
			this.src.children[0].style.removeProperty("color");

			// Let the orchestrator set undefined references.
		});
	}
}

// Define recycle bin behaviour.
var trash = document.getElementById("recycle-bin");

// Disable drag animation.
trash.addEventListener("dragstart", (e) => {
	e.preventDefault();
});

// Change color based on event state.
trash.addEventListener("dragover", (e) => {
	e.target.style.color = "blue";

	// Update orchestrator internal state.
	orchestrator.setDestination(trash);

	// Disable default behaviour to let ondrop event firing.
	e.preventDefault();
});

// Change color based on event state.
trash.addEventListener("dragleave", (e) => {
	e.target.style.color = "red";

	// Update orchestrator internal state.
	orchestrator.setDestination(undefined);
});

// Execute operation.
trash.addEventListener("drop", (e) => {
	e.target.style.color = "red";

	// Call orchestrator main function.
	orchestrator.call();

	// Let the orchestrator set undefined references.
});
