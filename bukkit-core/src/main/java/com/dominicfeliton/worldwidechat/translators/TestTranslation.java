package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.util.SupportedLang;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

public class TestTranslation extends BasicTranslation {

    public TestTranslation(boolean isInitializing, ExecutorService callbackExecutor) {
        super(isInitializing, callbackExecutor);
    }

    @Override
    protected translationTask createTranslationTask(String textToTranslate, String inputLang, String outputLang) {
        return new testTask(textToTranslate, inputLang, outputLang);
    }

    private class testTask extends translationTask {

        public testTask(String textToTranslate, String inputLang, String outputLang) {
            super(textToTranslate, inputLang, outputLang);
        }

        @Override
        public String call() throws Exception {
            if (isInitializing) {
                /* Generate fake supported langs list */
                Map<String, SupportedLang> outMap = new HashMap<>();
                SupportedLang en = new SupportedLang("en", "English", "");
                SupportedLang es = new SupportedLang("es", "Spanish", "");
                SupportedLang fr = new SupportedLang("fr", "French", "");

                outMap.put(en.getLangCode(), en);
                outMap.put(en.getLangName(), en);

                outMap.put(es.getLangCode(), es);
                outMap.put(es.getLangName(), es);

                outMap.put(fr.getLangCode(), fr);
                outMap.put(fr.getLangName(), fr);

                /* Set langList in Main */
                main.setOutputLangs(refs.fixLangNames(outMap, true, false));
                main.setInputLangs(refs.fixLangNames(outMap, true, false));

                /* Test translation setup */
                outputLang = "es";
                textToTranslate = "How many diamonds do you have?";
            }
            /* Test cases */
            if (outputLang.equals("es") && textToTranslate.equals("How many diamonds do you have?")) {
                return "Cuantos diamantes tienes?";
            }
            if (inputLang.equals("en") && outputLang.equals("es") && textToTranslate.equals("Hello, how are you?")) {
                return "Hola, como estas?";
            }

            // Invalid test case
            return textToTranslate;
        }
    }
}
