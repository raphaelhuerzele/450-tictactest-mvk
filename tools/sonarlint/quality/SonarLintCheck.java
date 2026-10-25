package quality;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.sonar.api.batch.rule.ActiveRule;
import org.sonar.api.rule.RuleKey;
import org.sonarsource.sonarlint.core.analysis.AnalysisScheduler;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisConfiguration;
import org.sonarsource.sonarlint.core.analysis.api.AnalysisSchedulerConfiguration;
import org.sonarsource.sonarlint.core.analysis.api.ClientInputFile;
import org.sonarsource.sonarlint.core.analysis.api.Issue;
import org.sonarsource.sonarlint.core.analysis.api.TriggerType;
import org.sonarsource.sonarlint.core.analysis.command.AnalyzeCommand;
import org.sonarsource.sonarlint.core.commons.api.SonarLanguage;
import org.sonarsource.sonarlint.core.commons.log.LogOutput;
import org.sonarsource.sonarlint.core.commons.progress.SonarLintCancelMonitor;
import org.sonarsource.sonarlint.core.commons.progress.TaskManager;
import org.sonarsource.sonarlint.core.commons.tracing.Trace;
import org.sonarsource.sonarlint.core.plugin.commons.PluginsLoader;
import org.sonarsource.sonarlint.core.rule.extractor.RuleSettings;
import org.sonarsource.sonarlint.core.rule.extractor.RulesDefinitionExtractor;

/** Build-only adapter for SonarSource's analysis engine; never included in the game JAR. */
public final class SonarLintCheck {
    private static final System.Logger LOGGER = System.getLogger(SonarLintCheck.class.getName());

    private SonarLintCheck() {
    }

    public static void main(String[] args) throws Exception {
        var base = Path.of(args[0]);
        var languages = Set.of(SonarLanguage.JAVA);
        var plugins = new PluginsLoader().load(new PluginsLoader.Configuration(
                Set.of(Path.of(args[1])), languages, false, Optional.empty()), Set.of()).getLoadedPlugins();
        if (!plugins.getAnalysisPluginInstancesByKeys().containsKey("java")) {
            throw new IllegalStateException("Java analyzer was not loaded");
        }
        var activeRules = new ArrayList<ActiveRule>();
        var definitions = new RulesDefinitionExtractor().extractRules(plugins.getAllPluginInstancesByKeys(),
                languages, false, false, new RuleSettings(Map.of()));
        for (var rule : definitions) {
            if (rule.isActiveByDefault()) {
                activeRules.add(new JavaRule(RuleKey.parse(rule.getKey()), rule.getDefaultSeverity().name(),
                        rule.getDefaultParams(), rule.getInternalKey().orElse(null)));
            }
        }
        if (activeRules.isEmpty()) {
            throw new IllegalStateException("No default Java rules were activated");
        }
        var files = sourceFiles(base);
        var config = AnalysisConfiguration.builder().setBaseDir(base).addInputFiles(files)
                .addActiveRules(activeRules)
                .putExtraProperty("sonar.java.source", "25")
                .putExtraProperty("sonar.java.jdkHome", System.getProperty("java.home"))
                .putExtraProperty("sonar.java.binaries", base.resolve("build/classes/java/main") + ","
                        + base.resolve("build/classes/java/quality"))
                .putExtraProperty("sonar.java.test.binaries", base.resolve("build/classes/java/test").toString())
                .putExtraProperty("sonar.java.libraries", args[2].replace(java.io.File.pathSeparator, ","))
                .putExtraProperty("sonar.java.test.libraries", args[2].replace(java.io.File.pathSeparator, ","))
                .build();
        var issues = new ArrayList<Issue>();
        var scheduler = new AnalysisScheduler(AnalysisSchedulerConfiguration.builder()
                .setWorkDir(base.resolve("build/sonarlint-work")).build(), plugins,
                new LogOutput() {
                    @Override
                    public void log(String message, Level level) {
                        if (level != Level.DEBUG && level != Level.TRACE) {
                            LOGGER.log(System.Logger.Level.INFO, level + ": " + message);
                        }
                    }
                });
        try {
            var command = new AnalyzeCommand("tictactest", UUID.randomUUID(), TriggerType.FORCED,
                    () -> config, issues::add, Trace.begin("analysis", "TicTacTest"), new SonarLintCancelMonitor(), new TaskManager(),
                    ignored -> { }, () -> true, Set.of(), Map.of());
            scheduler.post(command);
            var result = command.getFutureResult().get(120, TimeUnit.SECONDS);
            if (!result.failedAnalysisFiles().isEmpty()) {
                throw new IllegalStateException("Analysis failed for " + result.failedAnalysisFiles());
            }
        } finally {
            scheduler.stop();
            plugins.close();
        }
        var lines = new ArrayList<String>();
        lines.add("SonarLint Core 12.2.0.87867 / SonarJava 8.45.0.59462");
        lines.add("Analyzed files: " + files.size() + "; active default rules: " + activeRules.size());
        for (var issue : issues) {
            var file = issue.getInputFile() == null ? "<project>" : issue.getInputFile().relativePath();
            var line = issue.getTextRange() == null ? "" : ":" + issue.getTextRange().getStartLine();
            lines.add(issue.getRuleKey() + " " + file + line + " " + issue.getMessage());
        }
        lines.add("Issues: " + issues.size());
        var report = base.resolve("build/reports/sonarlint/report.txt");
        Files.createDirectories(report.getParent());
        Files.write(report, lines, StandardCharsets.UTF_8);
        lines.forEach(line -> LOGGER.log(System.Logger.Level.INFO, line));
        if (!issues.isEmpty()) {
            throw new IllegalStateException("SonarLint reported " + issues.size() + " issues; see " + report);
        }
    }

    private static List<ClientInputFile> sourceFiles(Path base) throws IOException {
        var files = new ArrayList<ClientInputFile>();
        for (var folder : new String[]{"src", "tools/sonarlint"}) {
            try (var paths = Files.walk(base.resolve(folder))) {
                paths.filter(path -> path.toString().endsWith(".java"))
                        .forEach(path -> files.add(new SourceFile(base, path)));
            }
        }
        return files;
    }

    private record JavaRule(RuleKey ruleKey, String severity, Map<String, String> params,
                            String internalKey) implements ActiveRule {
        @Override
        public String language() {
            return "java";
        }

        @Override
        public String param(String key) {
            return params.get(key);
        }

        @Override
        public String templateRuleKey() {
            return null;
        }

        @Override
        public String qpKey() {
            return "default";
        }
    }

    private record SourceFile(Path base, Path path) implements ClientInputFile {
        @Override
        public String getPath() {
            return path.toString();
        }

        @Override
        public String relativePath() {
            return base.relativize(path).toString().replace('\\', '/');
        }

        @Override
        public boolean isTest() {
            return relativePath().startsWith("src/test/");
        }

        @Override
        public SonarLanguage language() {
            return SonarLanguage.JAVA;
        }

        @Override
        public Charset getCharset() {
            return StandardCharsets.UTF_8;
        }

        @Override
        public <G> G getClientObject() {
            return null;
        }

        @Override
        public InputStream inputStream() throws IOException {
            return Files.newInputStream(path);
        }

        @Override
        public String contents() throws IOException {
            return Files.readString(path, StandardCharsets.UTF_8);
        }

        @Override
        public URI uri() {
            return path.toUri();
        }
    }
}
