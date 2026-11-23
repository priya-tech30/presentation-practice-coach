
package com.presentationcoach.data;

import android.content.Context;

import com.presentationcoach.model.SessionData;

import java.util.ArrayList;
import java.util.List;

public class SessionManager {

    private static final List<SessionData> sessions =
            new ArrayList<>();

    private static boolean initialized = false;

    // Initialize local storage and Firebase
    public static synchronized void initialize(Context context) {

        if (initialized) {
            return;
        }

        Context appContext = context.getApplicationContext();

        sessions.clear();

        sessions.addAll(
                SessionStorage.loadSessions(appContext)
        );

        initialized = true;

        // Start Firebase anonymous authentication
        FirebaseSessionSync.initialize(appContext);

        // Sync existing local sessions to Firestore
        FirebaseSessionSync.syncAllSessions(
                new ArrayList<>(sessions)
        );
    }

    // Add a new session
    public static synchronized void addSession(
            Context context,
            SessionData session
    ) {

        initialize(context);

        if (session == null) {
            return;
        }

        sessions.add(0, session);

        Context appContext = context.getApplicationContext();

        // Save locally first
        SessionStorage.saveSessions(
                appContext,
                sessions
        );

        // Upload to Firestore
        FirebaseSessionSync.uploadSession(session);
    }

    // Get all sessions
    public static synchronized List<SessionData> getSessions(
            Context context
    ) {

        initialize(context);

        return new ArrayList<>(sessions);
    }

    // Get latest session
    public static synchronized SessionData getLatestSession(
            Context context
    ) {

        initialize(context);

        if (sessions.isEmpty()) {
            return null;
        }

        return sessions.get(0);
    }

    // Get session count
    public static synchronized int getSessionCount(
            Context context
    ) {

        initialize(context);

        return sessions.size();
    }

    // Clear local sessions
    public static synchronized void clearSessions(
            Context context
    ) {

        initialize(context);

        sessions.clear();

        SessionStorage.saveSessions(
                context.getApplicationContext(),
                sessions
        );

        // This intentionally does not delete cloud sessions.
    }
}
