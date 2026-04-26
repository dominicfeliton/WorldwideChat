package com.dominicfeliton.worldwidechat.input;

public class InputResult {
    public enum Action {
        COMPLETE,
        REPEAT,
        ASYNC_COMPLETE
    }

    private static final InputResult COMPLETE = new InputResult(Action.COMPLETE);
    private static final InputResult REPEAT = new InputResult(Action.REPEAT);
    private static final InputResult ASYNC_COMPLETE = new InputResult(Action.ASYNC_COMPLETE);

    private final Action action;

    private InputResult(Action action) {
        this.action = action;
    }

    public static InputResult complete() {
        return COMPLETE;
    }

    public static InputResult repeat() {
        return REPEAT;
    }

    public static InputResult asyncComplete() {
        return ASYNC_COMPLETE;
    }

    public Action getAction() {
        return action;
    }
}
