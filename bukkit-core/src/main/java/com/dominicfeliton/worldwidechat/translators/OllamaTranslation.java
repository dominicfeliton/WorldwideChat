package com.dominicfeliton.worldwidechat.translators;

import com.deepl.api.Language;
import com.deepl.api.TextResult;
import com.deepl.api.Translator;
import com.dominicfeliton.worldwidechat.util.CommonRefs;
import com.dominicfeliton.worldwidechat.util.SupportedLang;
import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.models.OllamaResult;
import io.github.ollama4j.models.chat.OllamaChatRequestBuilder;
import io.github.ollama4j.utils.OptionsBuilder;
import io.github.ollama4j.utils.PromptBuilder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class OllamaTranslation extends BasicTranslation {

    private OllamaAPI api;

    private CommonRefs refs = main.getServerFactory().getCommonRefs();
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
                    inputLang = refs.getSupportedLang(inputLang, "in").getLangCode();
                }
                outputLang = refs.getSupportedLang(outputLang, "out").getLangCode();
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

            return response.getResponse();
        }
    }


}
