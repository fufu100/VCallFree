package vcall.free.international.phone.wifi.calling.pjsua;

import org.pjsip.pjsua2.pjsip_status_code;

/**
 * Created by lyf on 2020/5/9.
 */
public interface MyAppObserver {
        abstract void notifyRegState(pjsip_status_code code, String reason,
                                     int expiration);

        abstract void notifyIncomingCall(MyCall call);

        abstract void notifyCallState(MyCall call);

        abstract void notifyCallMediaState(MyCall call);

        abstract void notifyBuddyState(MyBuddy buddy);

        abstract void notifyChangeNetwork();
}
