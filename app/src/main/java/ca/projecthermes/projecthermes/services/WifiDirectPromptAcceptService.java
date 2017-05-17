package ca.projecthermes.projecthermes.services;

import android.accessibilityservice.AccessibilityService;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

import java.util.logging.Logger;

import ca.projecthermes.projecthermes.util.ErrorCodeHelper;

public class WifiDirectPromptAcceptService extends AccessibilityService {
    private Logger _logger;

    public WifiDirectPromptAcceptService() {
        _logger = Logger.getLogger("WifiDirectPromptAcceptService");
    }

    @Override
    public void onServiceConnected() {
        _logger.info("Service connected");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        _logger.info("Service disconnected");
        return false;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        boolean sourceNotNull = event.getSource() != null;
        _logger.info("Got accessibility event: [type] " + ErrorCodeHelper.findPossibleConstantsForInt(event.getEventType(), AccessibilityEvent.class) +
                                             " [class] " + event.getClassName() +
                                             " [pkg] " + event.getPackageName() +
                                             " [source] " + sourceNotNull +
                                             " [text] " + event.getText() +
                                             " [content-desc] " + event.getContentDescription());

        //TODO decide how we want to handle the case of forcing a WIFI disconnect to allow for peer device connection on older devices.
    }

    @Override
    public void onInterrupt() {

    }
}
