package com.dominicfeliton.worldwidechat.translators;

import com.dominicfeliton.worldwidechat.WorldwideChat;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import com.google.gson.Gson;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.response.OllamaResult;
import io.github.ollama4j.utils.OptionsBuilder;
import io.github.ollama4j.utils.PromptBuilder;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class OllamaTranslation extends BasicTranslation {

    private OllamaAPI api;

    private YamlConfiguration aiConf = main.getConfigManager().getAIConfig();
    private YamlConfiguration conf = main.getConfigManager().getMainConfig();

    public OllamaTranslation(String url, boolean isInitializing, ExecutorService callbackExecutor) {
        super(isInitializing, callbackExecutor);
        api = new OllamaAPI(url);
        api.setVerbose(false);
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
            PromptBuilder prompt = new PromptBuilder().add(main.getAISystemPrompt())
                    .addLine("Input Lang: " + inputLang)
                    .addLine("Output Lang: " + outputLang)
                    .addLine("Text To Translate: " + textToTranslate);

            OllamaResult response = api.generate(
                    conf.getString("Translator.ollamaModel"),
                    prompt.build(),
                    false,
                    new OptionsBuilder().build());
            response.setResponseTime(WorldwideChat.translatorConnectionTimeoutSeconds);

            refs.debugMsg(response.getResponse());
            return new Gson().fromJson(response.getResponse(), Response.class).getOutput();
        }
    }


}
