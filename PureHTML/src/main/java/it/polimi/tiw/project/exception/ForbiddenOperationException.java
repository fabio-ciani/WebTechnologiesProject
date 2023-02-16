package it.polimi.tiw.project.exception;

public class ForbiddenOperationException extends Exception {
	public ForbiddenOperationException() {
		super();
	}

	public ForbiddenOperationException(String message) {
		super(message);
	}
}