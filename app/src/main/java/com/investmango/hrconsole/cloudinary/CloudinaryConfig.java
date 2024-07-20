package com.investmango.hrconsole.cloudinary;
import android.content.Context;
import com.cloudinary.android.MediaManager;
import java.util.HashMap;
import java.util.Map;

public class CloudinaryConfig {
    private static final String cloudName = "dzvsmmraz";
    private static final String apiKey = "974981595445112";
    private static final String apiSecret = "4URnjaut9IehzWDZZ8_AVH8pKoQ";
    private static boolean isInitialized = false;

    public static void initCloudinary(Context context) {
        if (!isInitialized) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", cloudName);
            config.put("api_key", apiKey);
            config.put("api_secret", apiSecret);
            MediaManager.init(context, config);
            isInitialized = true;
        }
    }
}
