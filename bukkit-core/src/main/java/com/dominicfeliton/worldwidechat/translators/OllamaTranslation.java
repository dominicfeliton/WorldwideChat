package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import com.google.gson.Gson;
import io.github.ollama4j.Ollama;
import io.github.ollama4j.models.generate.OllamaGenerateRequest;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.utils.OptionsBuilder;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class OllamaTranslation extends BasicTranslation {

    private Ollama api;

    private YamlConfiguration aiConf = main.getConfigManager().getAIConfig();
    private YamlConfiguration conf = main.getConfigManager().getMainConfig();

    public OllamaTranslation(String url, boolean isInitializing, ExecutorService callbackExecutor) {
        super(isInitializing, callbackExecutor);
        api = new Ollama(url);
        api.setRequestTimeoutSeconds(WorldwideChat.translatorConnectionTimeoutSeconds);
    }

    @Override
    protected translationTask createTranslationTask(String textToTranslate, String inputLang, String outputLang) {
        return new ollamaTask(textToTranslate, inputLang, outputLang);
    }

    private class ollamaTask extends translationTask {

        public class Response {
            private boolean success;
            private String reason;
            private String output;

            public Response(boolean success, String reason, String output) {
                this.success = success;
                this.reason = reason;
                this.output = output;
            }

            public boolean isSuccess() {
                return success;
            }

            public String getReason() {
                return reason;
            }

            public String getOutput() {
                return output;
            }
        }

        public ollamaTask(String textToTranslate, String inputLang, String outputLang) {
            super(textToTranslate, inputLang, outputLang);
        }

        @Override
        public String call() throws Exception {
            /* Initialize translation object again */
            if (isInitializing) {
                /* Get languages */
                Map<String, SupportedLang> supportedLangs = new HashMap<>();

                // We're using OpenAI for reference...not great but the user should configure this manually
                // https://help.openai.com/en/articles/8357869-how-to-change-your-language-setting-in-chatgpt
                Set<String> langs = new HashSet<>(aiConf.getStringList("supportedLangs"));
                for (String key : langs) {
                    supportedLangs.put(key, new SupportedLang(key));
                }

                /* Parse languages */
                main.setInputLangs(refs.fixLangNames(supportedLangs, false, false));
                main.setOutputLangs(refs.fixLangNames(supportedLangs, false, false));

                /* Setup test translation */
                inputLang = "en";
                outputLang = "es";
                textToTranslate = "How are you?";
            }

            /* Get language code of current input/output language.
             * APIs generally recognize language codes (en, es, etc.)
             * instead of full names (English, Spanish) */
            if (!isInitializing) {
                if (!inputLang.equals("None")) {
                    inputLang = refs.getSupportedLang(inputLang, CommonRefs.LangType.INPUT).getLangCode();
                }
                outputLang = refs.getSupportedLang(outputLang, CommonRefs.LangType.OUTPUT).getLangCode();
            }
            // If we do not know the input lang, Ollama should guess.

            /* Actual translation */
            String prompt = main.getAISystemPrompt() + "\n\n" +
                    formatTranslationInput(inputLang, outputLang, textToTranslate);

            OllamaGenerateRequest request = OllamaGenerateRequest.builder()
                    .withModel(conf.getString("Translator.ollamaModel"))
                    .withPrompt(prompt)
                    .withStreaming(false)
                    .withOptions(new OptionsBuilder().build())
                    .build();

            OllamaResult response = api.generate(request, null);

            refs.debugMsg(response.getResponse());
            Response parsedResponse = new Gson().fromJson(response.getResponse(), Response.class);
            if (parsedResponse == null) {
                throw new TranslationFailureException("General", "Ollama returned an empty response.", false);
            }
            if (!parsedResponse.isSuccess()) {
                boolean guidelinesFailure = "Guidelines".equals(parsedResponse.getReason())
                        || containsGuidelinesBlockedSentinel(parsedResponse.getOutput());
                throw new TranslationFailureException(
                        guidelinesFailure ? "Guidelines" : parsedResponse.getReason(),
                        "Ollama translation failed: " + parsedResponse.getReason(),
                        guidelinesFailure
                );
            }
            if (containsGuidelinesBlockedSentinel(parsedResponse.getOutput())) {
                throw new TranslationFailureException("Guidelines", "Ollama returned a blocked sentinel.", true);
            }
            if (parsedResponse.getOutput() == null || parsedResponse.getOutput().isBlank()
                    || parsedResponse.getOutput().equalsIgnoreCase("none")) {
                throw new TranslationFailureException("General", "Ollama returned an empty translation.", false);
            }
            return parsedResponse.getOutput();
        }
    }


}
