package smartthings.dropwizard.aws.internal.backoff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a mechanism for exponential backoff.
 */
public class ExponentialBackoff {

    private static final Logger log = LoggerFactory.getLogger(ExponentialBackoff.class);
    private static final long MAX_WAIT = 60000;
    private int attempts = 0;

    public void reset() {
        attempts = 0;
    }

    public void backoff() {
        log.debug("Circuit is OPEN.  Waiting...");
        try {
            Thread.sleep(waitTime());
        } catch (InterruptedException ex) {
            // ignore
        }
        mark();
    }

    private void mark() {
        attempts++;
    }

    private long waitTime() {
        final long wait = Math.round(Math.pow(2, attempts)) * 1000;
        return wait >= MAX_WAIT ? MAX_WAIT : wait;
    }
}
