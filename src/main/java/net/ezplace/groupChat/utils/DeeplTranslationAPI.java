package net.ezplace.groupChat.utils;

// Importa las dependencias necesarias para Deepl

import com.deepl.api.DeepLClient;
import com.deepl.api.DeepLException;
import com.deepl.api.TextResult;

public class DeeplTranslationAPI implements TranslationAPI {
    private final String apiKey;
    private DeepLClient client;
    public DeeplTranslationAPI(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String translate(String text, String targetLang) throws DeepLException, InterruptedException {
        client = new DeepLClient(apiKey);
        TextResult result =
                client.translateText(text, null, targetLang);
        System.out.println(result.getText()); // "Bonjour, le monde !"
        return text;
    }
}