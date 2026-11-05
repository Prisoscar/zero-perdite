package mkle.zeroperdite.io;

import jakarta.servlet.http.HttpServletRequest;
import mkle.zeroperdite.utils.StaticResources;
import mkle.zeroperdite.utils.ZeroPerditeRestException;
import mkle.zeroperdite.utils.ZeroPerditeUserMustKnowException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

@RestControllerAdvice
public class ExceptionOutputController {

//    @Autowired
//    private LogService logService;

    @ExceptionHandler(mkle.zeroperdite.utils.ZeroPerditeRestException.class)
    public ResponseEntity<ErrorResponse> handleZeroPerditeRestException(HttpServletRequest request, ZeroPerditeRestException e) {
        StringBuilder errorMessage = new StringBuilder(e.getCause().toString());
        Throwable cause = e.getCause().getCause();
        int counter = 0;
        while (cause!= null && counter < 5){
            System.out.println(errorMessage);
            errorMessage.append("  ----->  ").append(cause);
            cause = cause.getCause();
            counter++;
        }
        ErrorResponse errorResponse = new ErrorResponse(request.getRequestURI(), ZonedDateTime.now(ZoneId.of(mkle.zeroperdite.utils.StaticResources.TIMEZONE_ID)), errorMessage.toString());
        try{
//            Log log = new Log()
//                    .setLogType(LogType.ERROR)
//                    .setLogMessage("Error during Api execution: endpoint -> " + errorResponse.endpoint + " || error message -> " + errorResponse.error);
//            logService.insertLog(log);
        } catch (Exception ex){
            System.out.println("Error during log during exception handling in controller advice for ZeroPerditeRestException. Exception: " + ex);
        }
        return ResponseEntity.internalServerError()
                .contentType(MediaType.parseMediaType(MimeTypeUtils.APPLICATION_JSON_VALUE))
                .body(errorResponse);
    }

    @ExceptionHandler(ZeroPerditeUserMustKnowException.class)
    public ResponseEntity<Map<String, String>> handleZeroPerditeUserMustKnowException(HttpServletRequest request, ZeroPerditeUserMustKnowException e) {
        ErrorResponse errorResponse = new ErrorResponse(request.getRequestURI(), ZonedDateTime.now(ZoneId.of(StaticResources.TIMEZONE_ID)), e.getMessage());
        try{
//            Log log = new Log()
//                    .setLogType(LogType.ERROR)
//                    .setLogMessage("Error during Api execution: endpoint -> " + errorResponse.endpoint + " || error message -> " + errorResponse.error);
//            logService.insertLog(log);
        } catch (Exception ex){
            System.out.println("Error during log during exception handling in controller advice for ZeroPerditeRestException. Exception: " + ex);
        }
        return ResponseEntity.internalServerError()
                .contentType(MediaType.parseMediaType(MimeTypeUtils.APPLICATION_JSON_VALUE))
                .body(Map.of("error", e.getMessage()));
    }

    public record ErrorResponse(String endpoint, ZonedDateTime timestamp, String error){}
}
