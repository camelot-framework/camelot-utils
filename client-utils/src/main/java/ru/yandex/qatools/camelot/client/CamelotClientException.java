package ru.yandex.qatools.camelot.client;

/**
 * @author Innokenty Shuvalov innokenty@yandex-team.ru
 */
public class CamelotClientException extends Exception {

    public CamelotClientException(String message) {
        super(message);
    }

    public CamelotClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
