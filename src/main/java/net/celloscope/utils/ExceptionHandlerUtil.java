package net.celloscope.utils;

import lombok.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ExceptionHandlerUtil extends Exception{
    public HttpStatus code;
    public String message;
    public String tpErrorCode;
    public String originalMessage;

    public ExceptionHandlerUtil(HttpStatus code, String message) {
        this.code = code;
        this.message = message;
    }

    public ExceptionHandlerUtil(HttpStatus code, String message, String originalMessage) {
        this.code = code;
        this.message = message;
        this.originalMessage = originalMessage;
    }

    public ExceptionHandlerUtil(HttpStatusCode httpStatusCode) {
    }
}
