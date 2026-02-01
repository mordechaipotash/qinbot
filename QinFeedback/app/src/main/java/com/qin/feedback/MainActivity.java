package com.qin.feedback;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.ScrollView;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.media.MediaRecorder;
import android.speech.tts.TextToSpeech;
import java.util.Locale;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MainActivity extends Activity {

    private TextView titleText;
    private TextView menuText;
    private TextView statusText;
    private TextView transcriptText;
    private TextView responseText;
    private TextView optionsText;
    private ScrollView responseScroll;

    private MediaRecorder recorder;
    private boolean isRecording = false;
    private String audioFilePath;
    private String lastTranscript = "";
    private static final int MAX_RECORDING_SECONDS = 30;
    
    // Text-to-Speech
    private TextToSpeech tts;
    private boolean ttsReady = false;
    private boolean ttsEnabled = true;  // Toggle with * key

    // States
    private static final int STATE_MENU = 0;
    private static final int STATE_RECORDING = 1;
    private static final int STATE_CONFIRM = 2;
    private static final int STATE_SENDING = 3;
    private static final int STATE_VOICE_PROMPT = 4;
    private static final int STATE_RESPONSE = 5;  // New: viewing response with dynamic options
    private static final int STATE_RECORDING_APPEND = 6;  // Recording to append to existing transcript
    private int currentState = STATE_MENU;

    // Server
    private static final String SERVER_BASE = "https://qin.mordechaipotash.com";
    private static final String MENU_URL = SERVER_BASE + "/menu";
    private static final String ACTION_URL = SERVER_BASE + "/action";
    private static final String AUDIO_URL = SERVER_BASE + "/audio";
    private static final String CHAT_URL = SERVER_BASE + "/chat";

    // Menu data (initial menu from server)
    private Map<String, MenuItem> menuItems = new HashMap<>();
    
    // Dynamic options (parsed from response)
    private Map<String, String> dynamicOptions = new HashMap<>();
    private boolean hasDynamicOptions = false;
    
    private String pendingAction = null;
    private String voicePrompt = null;

    // Pattern to match [1] Option text, [2] Another option, etc.
    // Matches both [1] Option and 1. Option formats
    private static final Pattern OPTION_PATTERN = Pattern.compile("(?:^|\\n)\\s*(?:\\[(\\d)\\]|(\\d)\\.)\\s*([^\\n\\[]+?)(?=\\n|$)", Pattern.MULTILINE);
    private static final Pattern BACK_PATTERN = Pattern.compile("\\[0\\].*(?:back|menu|done|exit|cancel)", Pattern.CASE_INSENSITIVE);

    private static class MenuItem {
        String label;
        String type;
        String command;
        String prompt;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setBackgroundColor(Color.parseColor("#0f0f23"));
        layout.setPadding(16, 32, 16, 16);

        // Title
        titleText = new TextView(this);
        titleText.setText("🤖 QinBot");
        titleText.setTextColor(Color.parseColor("#00d4ff"));
        titleText.setTextSize(28);
        titleText.setGravity(Gravity.CENTER);
        layout.addView(titleText);

        // Menu (shown in STATE_MENU)
        menuText = new TextView(this);
        menuText.setText("Loading...");
        menuText.setTextColor(Color.WHITE);
        menuText.setTextSize(20);
        menuText.setPadding(8, 12, 8, 12);
        layout.addView(menuText);

        // Status
        statusText = new TextView(this);
        statusText.setText("");
        statusText.setTextColor(Color.GREEN);
        statusText.setTextSize(22);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, 8, 0, 8);
        layout.addView(statusText);

        // Transcript (what you said)
        transcriptText = new TextView(this);
        transcriptText.setText("");
        transcriptText.setTextColor(Color.parseColor("#ffcc00"));
        transcriptText.setTextSize(22);
        transcriptText.setPadding(8, 4, 8, 4);
        layout.addView(transcriptText);

        // Response scroll
        responseScroll = new ScrollView(this);
        responseScroll.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f));

        // Response container
        LinearLayout responseContainer = new LinearLayout(this);
        responseContainer.setOrientation(LinearLayout.VERTICAL);
        responseContainer.setBackgroundColor(Color.parseColor("#1a1a2e"));
        responseContainer.setPadding(10, 10, 10, 10);

        responseText = new TextView(this);
        responseText.setText("");
        responseText.setTextColor(Color.WHITE);
        responseText.setTextSize(22);
        responseContainer.addView(responseText);

        // Dynamic options (highlighted)
        optionsText = new TextView(this);
        optionsText.setText("");
        optionsText.setTextColor(Color.parseColor("#00ff88"));
        optionsText.setTextSize(20);
        optionsText.setPadding(0, 12, 0, 0);
        responseContainer.addView(optionsText);

        responseScroll.addView(responseContainer);
        layout.addView(responseScroll);

        setContentView(layout);
        audioFilePath = getFilesDir().getAbsolutePath() + "/voice.3gp";

        // Initialize Text-to-Speech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
                tts.setSpeechRate(1.1f);  // Slightly faster
                ttsReady = true;
            }
        });

        loadMenu();
    }

    private void loadMenu() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    URL url = new URL(MENU_URL);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(5000);

                    if (conn.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        reader.close();
                        return sb.toString();
                    }
                } catch (Exception e) {}
                return null;
            }

            @Override
            protected void onPostExecute(String result) {
                if (result != null) {
                    parseMenu(result);
                } else {
                    setupDefaultMenu();
                    showMainMenu();
                }
            }
        }.execute();
    }

    private void parseMenu(String json) {
        try {
            JSONObject data = new JSONObject(json);
            JSONObject items = data.getJSONObject("items");
            
            Iterator<String> keys = items.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                JSONObject item = items.getJSONObject(key);
                
                MenuItem mi = new MenuItem();
                mi.label = item.optString("label", "Action " + key);
                mi.type = item.optString("type", "instant");
                mi.command = item.optString("command", null);
                mi.prompt = item.optString("prompt", null);
                menuItems.put(key, mi);
            }
            showMainMenu();
        } catch (Exception e) {
            setupDefaultMenu();
            showMainMenu();
        }
    }

    private void setupDefaultMenu() {
        String[][] defaults = {
            {"1", "🎯 Focus", "instant"},
            {"2", "📧 Emails", "instant"},
            {"3", "📅 Calendar", "instant"},
            {"4", "🌤️ Weather", "instant"},
            {"5", "🎤 Chat", "voice"},
            {"6", "⏰ Remind", "voice"},
            {"7", "📝 Note", "voice"},
            {"8", "🔍 Search", "voice"},
            {"9", "📰 News", "instant"}
        };
        for (String[] d : defaults) {
            MenuItem mi = new MenuItem();
            mi.label = d[1];
            mi.type = d[2];
            menuItems.put(d[0], mi);
        }
    }

    private void showMainMenu() {
        stopSpeaking();  // Stop any TTS when returning to menu
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 9; i++) {
            String key = String.valueOf(i);
            if (menuItems.containsKey(key)) {
                sb.append(key).append(": ").append(menuItems.get(key).label).append("\n");
            }
        }
        sb.append("0: Exit");
        
        // Restore header elements
        titleText.setVisibility(View.VISIBLE);
        menuText.setText(sb.toString());
        menuText.setVisibility(View.VISIBLE);
        statusText.setText("Press 1-9");
        statusText.setTextColor(Color.GREEN);
        statusText.setTextSize(22);  // Restore normal size
        optionsText.setText("");
        dynamicOptions.clear();
        hasDynamicOptions = false;
        currentState = STATE_MENU;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String key = keyCodeToString(keyCode);
        
        // Global: 0 or Back always goes back/exits
        if (key.equals("0") || keyCode == KeyEvent.KEYCODE_BACK) {
            if (isRecording) stopRecording();
            
            if (currentState == STATE_MENU) {
                finish();
            } else if (currentState == STATE_RECORDING_APPEND) {
                // Cancel append, return to confirm with existing transcript
                statusText.setText("1=Send  2=Add  3=Redo  0=Cancel");
                statusText.setTextColor(Color.CYAN);
                currentState = STATE_CONFIRM;
            } else if (currentState == STATE_RESPONSE && hasDynamicOptions && dynamicOptions.containsKey("0")) {
                // [0] ← Back in dynamic options means go to main menu
                showMainMenu();
            } else {
                showMainMenu();
            }
            return true;
        }

        // Handle based on state
        switch (currentState) {
            case STATE_MENU:
                return handleMenuKey(key);
            case STATE_RESPONSE:
                return handleResponseKey(key);
            case STATE_RECORDING:
                return handleRecordingKey(key);
            case STATE_RECORDING_APPEND:
                return handleRecordingAppendKey(key);
            case STATE_CONFIRM:
                return handleConfirmKey(key);
            case STATE_VOICE_PROMPT:
                return handleVoicePromptKey(key);
        }

        return super.onKeyDown(keyCode, event);
    }

    private String keyCodeToString(int keyCode) {
        if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
            return String.valueOf(keyCode - KeyEvent.KEYCODE_0);
        }
        if (keyCode >= 144 && keyCode <= 153) {
            return String.valueOf(keyCode - 144);
        }
        if (keyCode == KeyEvent.KEYCODE_STAR) {
            return "*";
        }
        if (keyCode == KeyEvent.KEYCODE_POUND) {
            return "#";
        }
        return "";
    }

    private boolean handleMenuKey(String key) {
        if (menuItems.containsKey(key)) {
            MenuItem item = menuItems.get(key);
            
            // CLEAR OLD OUTPUT when new menu item pressed
            clearResponseArea();
            
            if (item.type.equals("instant")) {
                pendingAction = key;
                executeAction(key, null);
            } else {
                pendingAction = key;
                voicePrompt = item.prompt;
                menuText.setVisibility(View.GONE);
                if (voicePrompt != null && !voicePrompt.isEmpty()) {
                    statusText.setText(voicePrompt + "\n\nPress 1 to speak");
                } else {
                    statusText.setText("Press 1 to speak");
                }
                statusText.setTextColor(Color.CYAN);
                currentState = STATE_VOICE_PROMPT;
            }
            return true;
        }
        return false;
    }
    
    private void clearResponseArea() {
        responseText.setText("");
        optionsText.setText("");
        transcriptText.setText("");
        dynamicOptions.clear();
        hasDynamicOptions = false;
    }

    private boolean handleResponseKey(String key) {
        // Check if this key has a dynamic option
        if (dynamicOptions.containsKey(key)) {
            String optionText = dynamicOptions.get(key);
            stopSpeaking();  // Stop TTS when selecting option
            sendFollowUp(optionText);
            return true;
        }
        
        // * key = toggle TTS
        if (key.equals("*")) {
            toggleTts();
            return true;
        }
        
        // # key = repeat last response
        if (key.equals("#")) {
            String text = responseText.getText().toString();
            if (!text.isEmpty()) {
                ttsEnabled = true;  // Force enable for repeat
                speakResponse(text);
            }
            return true;
        }
        
        // 5 key = voice input in response context
        if (key.equals("5")) {
            stopSpeaking();
            menuText.setVisibility(View.GONE);
            statusText.setText("Press 1 to speak");
            statusText.setTextColor(Color.CYAN);
            currentState = STATE_VOICE_PROMPT;
            pendingAction = "chat";  // Free-form follow-up
            return true;
        }
        
        return false;
    }

    private boolean handleVoicePromptKey(String key) {
        if (key.equals("1")) {
            startRecording();
            return true;
        }
        return false;
    }

    private boolean handleRecordingKey(String key) {
        if (key.equals("1")) {
            stopRecordingAndTranscribe();
            return true;
        }
        return false;
    }
    
    private boolean handleRecordingAppendKey(String key) {
        if (key.equals("1")) {
            stopRecordingAndTranscribeAppend();
            return true;
        }
        return false;
    }

    private boolean handleConfirmKey(String key) {
        if (key.equals("1")) {
            // SEND
            if (!lastTranscript.isEmpty()) {
                if (pendingAction != null && menuItems.containsKey(pendingAction)) {
                    executeAction(pendingAction, lastTranscript);
                } else {
                    // Free-form chat
                    sendChat(lastTranscript);
                }
            }
            return true;
        } else if (key.equals("2")) {
            // ADD MORE - append to current transcript
            statusText.setText("🎤 Add more... (1=stop)");
            statusText.setTextColor(Color.MAGENTA);
            startRecordingAppend();
            return true;
        } else if (key.equals("3")) {
            // REDO - start fresh
            lastTranscript = "";
            transcriptText.setText("");
            startRecording();
            return true;
        }
        return false;
    }
    
    private void startRecordingAppend() {
        // Same as startRecording but will append to lastTranscript
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(audioFilePath);
            recorder.setMaxDuration(MAX_RECORDING_SECONDS * 1000);

            recorder.setOnInfoListener((mr, what, extra) -> {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecordingAndTranscribeAppend();
                }
            });

            recorder.prepare();
            recorder.start();
            isRecording = true;
            currentState = STATE_RECORDING_APPEND;

            statusText.setText("🎤 Adding... (1=stop)");
            statusText.setTextColor(Color.MAGENTA);
        } catch (Exception e) {
            statusText.setText("Mic Error!");
            statusText.setTextColor(Color.RED);
            isRecording = false;
            // Return to confirm state
            statusText.setText("1=Send  2=Add  3=Redo  0=Cancel");
            statusText.setTextColor(Color.CYAN);
            currentState = STATE_CONFIRM;
        }
    }
    
    private void stopRecordingAndTranscribeAppend() {
        stopRecording();
        statusText.setText("🔄 Transcribing...");
        statusText.setTextColor(Color.YELLOW);
        transcribeAudioAppend();
    }
    
    private void transcribeAudioAppend() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                File audioFile = new File(audioFilePath);
                if (!audioFile.exists() || audioFile.length() == 0) {
                    return "ERROR:No audio";
                }

                HttpURLConnection conn = null;
                FileInputStream fis = null;

                try {
                    URL url = new URL(AUDIO_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "audio/3gpp");
                    conn.setRequestProperty("X-Transcribe-Only", "true");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(60000);

                    OutputStream os = conn.getOutputStream();
                    fis = new FileInputStream(audioFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.flush();
                    os.close();

                    if (conn.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        reader.close();

                        JSONObject json = new JSONObject(sb.toString());
                        return json.optString("transcript", "");
                    } else {
                        return "ERROR:Server " + conn.getResponseCode();
                    }
                } catch (Exception e) {
                    return "ERROR:" + e.getMessage();
                } finally {
                    try {
                        if (fis != null) fis.close();
                        if (conn != null) conn.disconnect();
                    } catch (IOException e) {}
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.startsWith("ERROR:")) {
                    statusText.setText(result.substring(6));
                    statusText.setTextColor(Color.RED);
                    // Return to confirm state after error
                    statusText.postDelayed(() -> {
                        statusText.setText("1=Send  2=Add  3=Redo  0=Cancel");
                        statusText.setTextColor(Color.CYAN);
                        currentState = STATE_CONFIRM;
                    }, 2000);
                } else {
                    // APPEND to existing transcript
                    if (!result.isEmpty()) {
                        lastTranscript = lastTranscript + " " + result;
                    }
                    transcriptText.setText("You: " + lastTranscript);
                    statusText.setText("1=Send  2=Add  3=Redo  0=Cancel");
                    statusText.setTextColor(Color.CYAN);
                    currentState = STATE_CONFIRM;
                }
            }
        }.execute();
    }

    private void startRecording() {
        try {
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(audioFilePath);
            recorder.setMaxDuration(MAX_RECORDING_SECONDS * 1000);

            recorder.setOnInfoListener((mr, what, extra) -> {
                if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
                    stopRecordingAndTranscribe();
                }
            });

            recorder.prepare();
            recorder.start();
            isRecording = true;
            currentState = STATE_RECORDING;

            statusText.setText("🎤 Recording... (1=stop)");
            statusText.setTextColor(Color.RED);
            transcriptText.setText("");
            lastTranscript = "";
        } catch (Exception e) {
            statusText.setText("Mic Error!");
            statusText.setTextColor(Color.RED);
            isRecording = false;
            showMainMenuDelayed();
        }
    }

    private void stopRecording() {
        if (recorder != null) {
            try { recorder.stop(); } catch (Exception e) {}
            recorder.release();
            recorder = null;
        }
        isRecording = false;
    }

    private void stopRecordingAndTranscribe() {
        stopRecording();
        statusText.setText("🔄 Transcribing...");
        statusText.setTextColor(Color.YELLOW);
        transcribeAudio();
    }

    private void transcribeAudio() {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                File audioFile = new File(audioFilePath);
                if (!audioFile.exists() || audioFile.length() == 0) {
                    return "ERROR:No audio";
                }

                HttpURLConnection conn = null;
                FileInputStream fis = null;

                try {
                    URL url = new URL(AUDIO_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "audio/3gpp");
                    conn.setRequestProperty("X-Transcribe-Only", "true");
                    conn.setConnectTimeout(10000);
                    conn.setReadTimeout(60000);

                    OutputStream os = conn.getOutputStream();
                    fis = new FileInputStream(audioFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    os.flush();
                    os.close();

                    if (conn.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        reader.close();

                        JSONObject json = new JSONObject(sb.toString());
                        return json.optString("transcript", "(empty)");
                    } else {
                        return "ERROR:Server " + conn.getResponseCode();
                    }
                } catch (Exception e) {
                    return "ERROR:" + e.getMessage();
                } finally {
                    try {
                        if (fis != null) fis.close();
                        if (conn != null) conn.disconnect();
                    } catch (IOException e) {}
                }
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.startsWith("ERROR:")) {
                    statusText.setText(result.substring(6));
                    statusText.setTextColor(Color.RED);
                    showMainMenuDelayed();
                } else {
                    lastTranscript = result;
                    transcriptText.setText("You: " + result);
                    statusText.setText("1=Send  2=Add  3=Redo  0=Cancel");
                    statusText.setTextColor(Color.CYAN);
                    currentState = STATE_CONFIRM;
                }
            }
        }.execute();
    }

    private void executeAction(final String actionKey, final String voiceInput) {
        currentState = STATE_SENDING;
        MenuItem item = menuItems.get(actionKey);
        menuText.setVisibility(View.GONE);
        statusText.setText("⏳ " + (item != null ? item.label : "Working..."));
        statusText.setTextColor(Color.YELLOW);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(ACTION_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(120000);
                    conn.setDoOutput(true);

                    JSONObject json = new JSONObject();
                    json.put("action", actionKey);
                    if (voiceInput != null) {
                        json.put("voice_input", voiceInput);
                    }

                    OutputStream os = conn.getOutputStream();
                    os.write(json.toString().getBytes("UTF-8"));
                    os.close();

                    if (conn.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        reader.close();

                        JSONObject responseJson = new JSONObject(sb.toString());
                        return responseJson.optString("response", sb.toString());
                    } else {
                        return "Server error: " + conn.getResponseCode();
                    }
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }

            @Override
            protected void onPostExecute(String response) {
                displayResponse(response);
            }
        }.execute();
    }

    private void sendChat(final String text) {
        currentState = STATE_SENDING;
        menuText.setVisibility(View.GONE);
        statusText.setText("⏳ Thinking...");
        statusText.setTextColor(Color.YELLOW);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(CHAT_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(120000);
                    conn.setDoOutput(true);

                    JSONObject json = new JSONObject();
                    json.put("text", text);

                    OutputStream os = conn.getOutputStream();
                    os.write(json.toString().getBytes("UTF-8"));
                    os.close();

                    if (conn.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        reader.close();

                        JSONObject responseJson = new JSONObject(sb.toString());
                        return responseJson.optString("response", sb.toString());
                    } else {
                        return "Server error: " + conn.getResponseCode();
                    }
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }

            @Override
            protected void onPostExecute(String response) {
                displayResponse(response);
            }
        }.execute();
    }

    private void sendFollowUp(final String optionText) {
        currentState = STATE_SENDING;
        statusText.setText("⏳ " + optionText.substring(0, Math.min(20, optionText.length())) + "...");
        statusText.setTextColor(Color.YELLOW);

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(CHAT_URL);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setConnectTimeout(5000);
                    conn.setReadTimeout(120000);
                    conn.setDoOutput(true);

                    JSONObject json = new JSONObject();
                    json.put("text", optionText);

                    OutputStream os = conn.getOutputStream();
                    os.write(json.toString().getBytes("UTF-8"));
                    os.close();

                    if (conn.getResponseCode() == 200) {
                        BufferedReader reader = new BufferedReader(
                            new InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            sb.append(line);
                        }
                        reader.close();

                        JSONObject responseJson = new JSONObject(sb.toString());
                        return responseJson.optString("response", sb.toString());
                    } else {
                        return "Server error: " + conn.getResponseCode();
                    }
                } catch (Exception e) {
                    return "Error: " + e.getMessage();
                } finally {
                    if (conn != null) conn.disconnect();
                }
            }

            @Override
            protected void onPostExecute(String response) {
                displayResponse(response);
            }
        }.execute();
    }

    private void displayResponse(String response) {
        // Parse dynamic options from response
        dynamicOptions.clear();
        hasDynamicOptions = false;
        
        String mainText = response;
        StringBuilder optionsSb = new StringBuilder();
        
        Matcher matcher = OPTION_PATTERN.matcher(response);
        while (matcher.find()) {
            // Group 1 = [1] format, Group 2 = 1. format, Group 3 = option text
            String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            String option = matcher.group(3).trim();
            if (key != null && !option.isEmpty()) {
                dynamicOptions.put(key, option);
                hasDynamicOptions = true;
                optionsSb.append("[").append(key).append("] ").append(option).append("\n");
            }
        }
        
        // Remove options from main text for cleaner display
        if (hasDynamicOptions) {
            mainText = response.replaceAll("(?:^|\\n)\\s*(?:\\[\\d\\]|\\d\\.)\\s*[^\\n]+", "").trim();
        }
        
        responseText.setText(mainText);
        
        // Speak the response immediately (speed is priority)
        speakResponse(mainText);
        
        // Hide header elements to maximize response space
        titleText.setVisibility(View.GONE);
        menuText.setVisibility(View.GONE);
        
        String ttsStatus = ttsEnabled ? "🔊" : "🔇";
        if (hasDynamicOptions) {
            optionsText.setText(optionsSb.toString().trim());
            optionsText.setVisibility(View.VISIBLE);
            statusText.setText(ttsStatus + " 0=Menu 5=Voice *=TTS");
        } else {
            optionsText.setVisibility(View.GONE);
            statusText.setText(ttsStatus + " 0=Menu 5=Voice *=TTS");
        }
        
        statusText.setTextColor(Color.GREEN);
        statusText.setTextSize(18);  // Smaller status text in response view
        responseScroll.post(() -> responseScroll.fullScroll(View.FOCUS_UP));

        pendingAction = null;
        voicePrompt = null;
        lastTranscript = "";
        transcriptText.setText("");
        currentState = STATE_RESPONSE;
    }

    @SuppressWarnings("deprecation")
    private void speakResponse(String text) {
        if (!ttsEnabled || !ttsReady || tts == null) return;
        
        // Stop any current speech
        tts.stop();
        
        // Clean text for speech (remove excessive formatting)
        String cleanText = text
            .replaceAll("\\n+", ". ")           // Newlines to pauses
            .replaceAll("[\\[\\]\\|\\-]{2,}", " ")  // Remove table chars
            .replaceAll("\\s+", " ")            // Collapse whitespace
            .trim();
        
        // Speak immediately (using deprecated API for older Android)
        tts.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null);
    }
    
    private void stopSpeaking() {
        if (tts != null) {
            tts.stop();
        }
    }
    
    private void toggleTts() {
        ttsEnabled = !ttsEnabled;
        stopSpeaking();
        String status = ttsEnabled ? "🔊 TTS ON" : "🔇 TTS OFF";
        statusText.setText(status);
        statusText.postDelayed(() -> {
            String ttsStatus = ttsEnabled ? "🔊" : "🔇";
            statusText.setText(ttsStatus + " 0=Menu 5=Voice *=TTS");
        }, 1000);
    }

    private void showMainMenuDelayed() {
        statusText.postDelayed(() -> showMainMenu(), 3000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isRecording) stopRecording();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
    }
}
