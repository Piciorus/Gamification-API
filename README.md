```

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.SoapFaultClientException;

import javax.xml.bind.JAXBElement;
import java.util.Collections;

/**
 * Enhanced version of SoapBaseClient that supports configurable error-handling strategies.
 *
 * <p>By default, any SOAP fault will throw a {@link CommonException} (same behaviour as the
 * base class). Subclasses can override {@link #defaultErrorStrategy()} to change the strategy
 * for every call, or pass a {@link ErrorStrategy} per call-site to be explicit.
 *
 * <p>The {@link #relayErrorCode} hook is still called unconditionally, so custom logging /
 * metrics work as before; only the subsequent throw is governed by the strategy.
 *
 * @param <REQ>  SOAP request type
 * @param <RESP> SOAP response type
 */
public class EnhancedSoapBaseClient<REQ, RESP>  {

    private static final Logger log = LoggerFactory.getLogger(EnhancedSoapBaseClient.class);

    // -------------------------------------------------------------------------
    // Strategy
    // -------------------------------------------------------------------------

    /**
     * Controls what happens after {@link #relayErrorCode} is called.
     *
     * <ul>
     *   <li>{@code THROW}    – propagate a {@link CommonException} (default / backward-compat)
     *   <li>{@code LOG_ONLY} – swallow the exception and return {@code null}
     * </ul>
     */
    public enum ErrorStrategy {
        THROW,
        LOG_ONLY
    }

    // -------------------------------------------------------------------------
    // Constructors
    // -------------------------------------------------------------------------

    public EnhancedSoapBaseClient(WebServiceTemplate webServiceTemplate) {
        super(webServiceTemplate);
    }

    // -------------------------------------------------------------------------
    // Strategy hook — override to set a class-wide default
    // -------------------------------------------------------------------------

    /**
     * Returns the default {@link ErrorStrategy} for all calls made through this instance.
     * Subclasses can override to change the class-wide behaviour:
     *
     * <pre>{@code
     * @Override
     * protected ErrorStrategy defaultErrorStrategy() {
     *     return ErrorStrategy.LOG_ONLY;
     * }
     * }</pre>
     */
    protected ErrorStrategy defaultErrorStrategy() {
        return ErrorStrategy.THROW;
    }

    // -------------------------------------------------------------------------
    // Overloads — accept an explicit per-call strategy
    // -------------------------------------------------------------------------

    /**
     * Executes a SOAP call with a {@link JAXBElement}-wrapped request and the
     * class-wide {@link #defaultErrorStrategy()}.
     */
    protected RESP soapCall(JAXBElement<REQ> request) {
        return soapCall(request, defaultErrorStrategy());
    }

    /**
     * Executes a SOAP call with a {@link JAXBElement}-wrapped request and an
     * explicit per-call {@link ErrorStrategy}.
     */
    protected RESP soapCall(JAXBElement<REQ> request, ErrorStrategy strategy) {
        JAXBElement<RESP> response = null;
        try {
            //noinspection unchecked
            response = (JAXBElement<RESP>) getWebServiceTemplate()
                    .marshalSendAndReceive(request);
            return response.getValue();

        } catch (SoapFaultClientException e) {
            REQ  reqVal  = request  != null ? request.getValue()  : null;
            RESP respVal = response != null ? response.getValue() : null;

            relayErrorCode(e, reqVal, respVal);
            return handleSoapFault(e, strategy);

        } catch (Exception e) {
            return handleGenericException(e, strategy);
        }
    }

    /**
     * Executes a SOAP call with an unwrapped request and the class-wide
     * {@link #defaultErrorStrategy()}.
     */
    protected RESP soapCall(REQ request) {
        return soapCall(request, defaultErrorStrategy());
    }

    /**
     * Executes a SOAP call with an unwrapped request and an explicit per-call
     * {@link ErrorStrategy}.
     */
    protected RESP soapCall(REQ request, ErrorStrategy strategy) {
        try {
            //noinspection unchecked
            return (RESP) getWebServiceTemplate().marshalSendAndReceive(request);

        } catch (SoapFaultClientException e) {
            relayErrorCode(e, request, null);
            return handleSoapFault(e, strategy);

        } catch (Exception e) {
            return handleGenericException(e, strategy);
        }
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private RESP handleSoapFault(SoapFaultClientException e, ErrorStrategy strategy) {
        String reason = e.getFaultStringOrReason();
        String message = StringUtils.isNotBlank(reason) ? reason : e.getMessage();

        if (strategy == ErrorStrategy.LOG_ONLY) {
            log.error("SOAP fault intercepted (LOG_ONLY strategy) — reason: {}", message, e);
            return null;
        }

        throw new CommonException(
                CommonExceptionCode.SERVER_ERROR,
                Collections.singletonList(message)
        );
    }

    private RESP handleGenericException(Exception e, ErrorStrategy strategy) {
        if (strategy == ErrorStrategy.LOG_ONLY) {
            log.error("Unexpected SOAP exception intercepted (LOG_ONLY strategy) — message: {}",
                    e.getMessage(), e);
            return null;
        }

        throw new CommonException(
                CommonExceptionCode.SERVER_ERROR,
                Collections.singletonList(e.getMessage())
        );
    }

    /**
     * Exposes the underlying template to subclasses (needed because the field
     * is private in the base class). If your base class already has a protected
     * getter, remove this and delegate to it.
     */
    protected WebServiceTemplate getWebServiceTemplate() {
        // If SoapBaseClient exposes a getter, call super.getWebServiceTemplate() instead.
        throw new UnsupportedOperationException(
                "Provide access to WebServiceTemplate — either inject it here or " +
                        "add a protected getter to SoapBaseClient."
        );
    }
}
```
