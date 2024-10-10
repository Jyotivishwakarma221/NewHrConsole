package com.investmango.hrconsole.service;

import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class AccessToken {
    private static final String firebaseMessagingScope =
            "https://www.googleapis.com/auth/firebase.messaging";

    public String getAccessToken() {
        try {
            String jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"hr-console\",\n" +
                    "  \"private_key_id\": \"94787693e2e0e9f304fdd59acb17dbe70ad2509e\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDS33MUxSdex5AC\\nb+DntdCStq8vRMRTJFnkdGXGvFb6XXONiLC2kTzb2CYAz/neoIAmCFuceZjBsShm\\n2NKGybfUTPVQeKZoXOk2If2ZqkwJLTP6tlxDvmNneeoKX1JftPuxdOWMKWnxPMms\\nU0ooJEYjqoidtfoqzh/mVhlVJsPRDoKhXjDQDNRGaHvHnil/L5Mk0DWssuYUkDSq\\nhRzTYIqCPZ7rzHAa2eIrREYhjvwS90d84nhQIEUPR/NGELPZI/eLjiYy4Mt8IcZf\\nwxq6DTufiJo6QJeT/XCoI66wFMsNi3LTsbS4i2TjjcKlbGuvqeluZQE2nChf+s/M\\njHPfa5aFAgMBAAECggEABMUmOsmM7vFvugZi3Yr42WDFjLmvATx4xhi19q1rAEXd\\n5804RiD58fUtQuZxfg4+Z93XiK4dfQAvbEgqVv84J9bH09au2PyFEALr1C6Mgsah\\nfuHiK2winoQc/bgTSnPzH5wH10hWi6z22O2XZ1oOth7U8VBdBNvdz92RvCHg2xlA\\n6cMxKOCMQR4lkKg0sgFOPIYb7e2aiGfm7Vr4kAztANLVGfDrlamExJPCpRgIIG7z\\n9YcdmanYqXK5mBQ1xUONWv6OTqyi6+ddURLbZYtQu4uvgWUnpGEPCiMULAdaxWU7\\nGlFE35teqHech652MhbPk7//yI+iiTja5xowIMBLAQKBgQDwHlbQ3nNg2juuUj7s\\nxGU/HukfcADDwsKzhtTY8rMrFK3EJtIvcPWDNUJ+lZ5oM7Tz1EzeEFS7ubsq4YqU\\ndPQUBWbPICiz0QwYAM5qdkzIFgJsxbQnckRIFpaLLYW8ZkXnrE0KQUzJkQjoq1cm\\n6rFcK6ZAQpeLZiG0k0tiTisJBQKBgQDg0e0N6H9wi3KIw+UtUxhWpImAbf2aPruT\\nDcaOIHwmOje2t9mchA6ahPtwMfE68n/R5FG3eHGZg+80Lf2Oxnkr3pTwzljXmLso\\nd56GlEVePC1S/9/sSKDTDrmM/+u8PcNXmaTez0Ufc6CROjxQ2Sxp79hOBf2wbWQB\\nwrFbqH3PgQKBgQDrtuzEp8sdxb4Wl1h8NyOo/iAdo2asxbE6aV7jBgWKggc+6lkl\\nBz+MKSl4eEoDwtadEcjPqbp0epRZOQVATTsZuF+/brNmKg40Nf5sFmuzc5+3xbDf\\nlFWhasMoJ20EkGWJUuRh786AhOb4+NJaOsQXViYjKPv5dS4bRQkYnOG0PQKBgBg5\\nxseW5BSmkiL2qP1nVNoRRNfYAlnapK/F7j1mdrpPz60axpI9EK3J4qZGW240r11Y\\nsw2IJwWZ1+7Rfd8it+/oPjhso9onlRNZQHXUCF2NMn0dIW3+rYXQsmHlOevA5AnO\\nfpubziEDfqvnkJRNsqF/vNk7gAjA4OPnrODpmRUBAoGAR8zoH30UF4ARtlK1lvae\\ntRpmjD94j3namUA3RiarSpDhDJtIg0kj7o+QMY5SxuslVvrxBHzDCHRbY1bTL4QA\\nEIXvKNlXMIxxRGaf0YueX6KknfSGyB0aQ84YwfD9Bh/VcpmdNgj7LP+6jUfIF8Au\\nGAzG8tgMb7HwsG2a185y6tg=\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-104qc@hr-console.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"101996828299009596030\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-104qc%40hr-console.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}";
            InputStream stream = new ByteArrayInputStream(jsonString.getBytes(StandardCharsets.UTF_8));
            GoogleCredentials googleCredentials = GoogleCredentials.fromStream(stream).createScoped(firebaseMessagingScope);
            googleCredentials.refresh();
            return googleCredentials.getAccessToken().getTokenValue();
        } catch (Exception e) {
            Log.e("AccessToken", "getAccessToken: " + e);
            return null;
        }
    }
}
