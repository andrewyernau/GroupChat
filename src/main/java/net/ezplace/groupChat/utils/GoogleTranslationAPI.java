//package net.ezplace.groupChat.utils;
//
//import com.google.cloud.translate.Translate;
//import com.google.cloud.translate.TranslateOptions;
//import com.google.cloud.translate.Translation;
//
//public class GoogleTranslationAPI implements TranslationAPI {
//    private final Translate translateService;
//
//    public GoogleTranslationAPI(String apiKey) {
//        translateService = TranslateOptions.newBuilder()
//                .setApiKey(apiKey)
//                .build()
//                .getService();
//    }
//
//    @Override
//    public String translate(String text, String targetLang) {
//        Translation translation = translateService.translate(text,
//                Translate.TranslateOption.targetLanguage(targetLang));
//        return translation.getTranslatedText();
//    }
//}
