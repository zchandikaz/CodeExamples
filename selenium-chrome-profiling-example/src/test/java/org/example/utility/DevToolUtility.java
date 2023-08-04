package org.example.utility;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.v114.io.IO;
import org.openqa.selenium.devtools.v114.io.model.StreamHandle;
import org.openqa.selenium.devtools.v114.tracing.Tracing;
import org.openqa.selenium.devtools.v114.tracing.model.TraceConfig;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * @author : chandika
 * @since : 2023-08-04(Fri) 11:45
 **/
public class DevToolUtility {
    private static final Logger LOGGER = Logger.getLogger(DevToolUtility.class.getName());
    private final DevTools devTools;
    private final Semaphore traceSemaphore = new Semaphore(1);
    private boolean traceCompleted = false;

    public static class TraceStartCommandFactory {
        private static final Logger LOGGER = Logger.getLogger(TraceStartCommandFactory.class.getName());

        public enum CategoryMode {
            ALL_AVAILABLE_CATEGORIES, PUPPETEER_CATEGORIES, EXTRA_CATEGORIES
        }

        public static Command<Void> create(DevTools devTools, CategoryMode categoryMode) {
            List<String> incTraceCategories;
            List<String> excTraceCategories;

            switch (categoryMode) {
                case ALL_AVAILABLE_CATEGORIES:
                    incTraceCategories = devTools.send(Tracing.getCategories());
                    excTraceCategories = List.of();
                    break;
                case PUPPETEER_CATEGORIES:
                    var traceCategoriesPup = List.of(
                            "-*",
                            "devtools.timeline",
                            "v8.execute",
                            "disabled-by-default-devtools.timeline",
                            "disabled-by-default-devtools.timeline.frame",
                            "toplevel",
                            "blink.console",
                            "blink.user_timing",
                            "latencyInfo",
                            "disabled-by-default-devtools.timeline.stack",
                            "disabled-by-default-v8.cpu_profiler",
                            "disabled-by-default-devtools.screenshot"
                    );
                    excTraceCategories = traceCategoriesPup.stream().filter(cat -> cat.startsWith("-")).map(cat -> cat.substring(1)).collect(Collectors.toList());
                    incTraceCategories = traceCategoriesPup.stream().filter(cat -> !cat.startsWith("-")).collect(Collectors.toList());
                    break;
                case EXTRA_CATEGORIES:
                    var traceCategoriesExtra = List.of(
                            "-*",
                            "benchmark",
                            "benchmark,loading",
                            "blink",
                            "blink,devtools.timeline",
                            "blink,loading",
                            "blink.animations,devtools.timeline,benchmark,rail",
                            "blink.console",
                            "blink.user_timing",
                            "blink.user_timing,rail",
                            "blink_gc",
                            "browser",
                            "cc",
                            "cc.debug",
                            "cc.debug.quads",
                            "cc,benchmark,disabled-by-default-devtools.timeline.frame",
                            "cc,disabled-by-default-devtools.timeline",
                            "devtools,animation",
                            "devtools.timeline",
                            "devtools.timeline,disabled-by-default-v8.gc",
                            "devtools.timeline,rail",
                            "devtools.timeline,v8",
                            "devtools.timeline.picture",
                            "disabled-by-default-blink.debug",
                            "disabled-by-default-blink.feature_usage",
                            "disabled-by-default-cc.debug,disabled-by-default-viz.quads,disabled-by-default-devtools.timeline.layers",
                            "disabled-by-default-cc.debug.display_items,disabled-by-default-cc.debug.picture,disabled-by-default-devtools.timeline.picture",
                            "disabled-by-default-devtools.screenshot",
                            "disabled-by-default-devtools.timeline",
                            "disabled-by-default-devtools.timeline.frame",
                            "disabled-by-default-devtools.timeline.invalidationTracking",
                            "disabled-by-default-devtools.timeline.stack",
                            "disabled-by-default-devtools.timeline_frames",
                            "disabled-by-default-devtools.timeline_gpu",
                            "disabled-by-default-devtools.timeline_layout",
                            "disabled-by-default-devtools.timeline_paint",
                            "disabled-by-default-devtools.timeline_raster",
                            "disabled-by-default-v8.compile",
                            "disabled-by-default-v8.cpu_profile",
                            "disabled-by-default-v8.cpu_profiler",
                            "disabled-by-default-v8.runtime_stats",
                            "gpu",
                            "gpu.debug",
                            "gpu.device",
                            "gpu.service",
                            "input,benchmark,devtools.timeline,latencyInfo",
                            "latencyInfo",
                            "loading",
                            "loading,rail,devtools.timeline",
                            "netlog",
                            "network",
                            "rail",
                            "renderer",
                            "scheduler",
                            "sequence_manager",
                            "toplevel",
                            "v8",
                            "v8,devtools.timeline",
                            "v8,devtools.timeline,disabled-by-default-v8.compile",
                            "v8.execute",
                            "viz",
                            "memory",
                            "disabled-by-default-memory-infra",
                            "disabled-by-default-memory-infra.v8.code_stats",
                            "disabled-by-default-java-heap-profiler"
                    );
                    excTraceCategories = traceCategoriesExtra.stream().filter(cat -> cat.startsWith("-")).map(cat -> cat.substring(1)).collect(Collectors.toList());
                    incTraceCategories = traceCategoriesExtra.stream().filter(cat -> !cat.startsWith("-")).collect(Collectors.toList());
                    break;
                default:
                    throw new RuntimeException("Unsupported mode");
            }

            LOGGER.log(Level.INFO, "incTraceCategories: {0}", new Object[]{incTraceCategories});
            LOGGER.log(Level.INFO, "excTraceCategories: {0}", new Object[]{excTraceCategories});

            // Ref: https://chromedevtools.github.io/devtools-protocol/tot/Tracing/#method-end
            return Tracing.start(
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(Tracing.StartTransferMode.RETURNASSTREAM),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.of(new TraceConfig(
                            Optional.of(TraceConfig.RecordMode.RECORDASMUCHASPOSSIBLE), // Ref: https://chromium.googlesource.com/chromium/src/+/56398249f62df4942576ebbaec482d6fd8cea9a7/base/trace_event/trace_config.h
                            Optional.of(400 * 1024),
                            Optional.of(false),
                            Optional.of(true),
                            Optional.of(true),
                            Optional.of(incTraceCategories),
                            Optional.of(excTraceCategories),
                            Optional.empty(),
                            Optional.empty()
                    )),
                    Optional.empty(),
                    Optional.empty()
            );
        }
    }

    private static class ChromeDevToolIOReader implements Iterator<ChromeDevToolIOReader.ResponseChunk> {
        private final DevTools client;
        private final StreamHandle stream;
        private final int chunkSize;

        private enum Status {
            NOT_STARTED, IN_PROGRESS, COMPLETED
        }

        private static class ResponseChunk {
            private final byte[] data;

            public ResponseChunk(byte[] data) {
                this.data = data;
            }

            public byte[] get() {
                return data;
            }
        }

        private Status status = Status.NOT_STARTED;

        public ChromeDevToolIOReader(DevTools client, StreamHandle stream, int chunkSize) {
            this.client = client;
            this.stream = stream;
            this.chunkSize = chunkSize;
        }

        private byte[] read(int size) {
            if (status == Status.COMPLETED) {
                return new byte[]{};
            } else if (status == Status.NOT_STARTED) {
                status = Status.IN_PROGRESS;
            }

            var response = client.send(
                    IO.read(
                            stream,
                            Optional.empty(), Optional.of(size)
                    )
            );
            if (response.getEof()) {
                this.status = Status.COMPLETED;
                client.send(IO.close(stream));
            }
            return response.getBase64Encoded().orElse(false)
                    ?
                    Base64.getDecoder().decode(response.getData())
                    :
                    response.getData().getBytes();
        }

        @Override
        public boolean hasNext() {
            return status != Status.COMPLETED;
        }

        @Override
        public ResponseChunk next() {
            return new ResponseChunk(read(chunkSize));
        }
    }


    public DevToolUtility(ChromeDriver driver) {
        devTools = driver.getDevTools();
    }

    public void startTracing(String profileName) throws Exception {
        devTools.createSession();

        devTools.send(TraceStartCommandFactory.create(devTools, TraceStartCommandFactory.CategoryMode.EXTRA_CATEGORIES));
        traceSemaphore.acquire();

        devTools.addListener(Tracing.dataCollected(), maps -> {
            try {
                var objMapper = new ObjectMapper();
                Files.writeString(
                        Paths.get(
                                "target",
                                "Trace - " + profileName + ".json"
                        ),
                        objMapper.writeValueAsString(maps)
                );
                traceSemaphore.release();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "", e);
            }
        });

        devTools.addListener(Tracing.tracingComplete(), tracingComplete -> {
            var readable = new ChromeDevToolIOReader(
                    devTools,
                    tracingComplete.getStream().orElseThrow(),
                    50 * 1024 * 1024
            );
            var reportFile = Paths.get(
                                    "target",
                                    "Trace - " + profileName + ".json"
                            ).toFile();
            LOGGER.log(Level.INFO, "Profile path: {0}", new Object[]{ reportFile.getName() });
            try (
                    FileOutputStream fos = new FileOutputStream(reportFile)
            ) {
                while (readable.hasNext()) {
                    fos.write(readable.next().get());
                }
                traceCompleted = true;
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                traceSemaphore.release();
            }
        });
    }

    public void stopTracing() throws Exception {
        try {
            devTools.send(Tracing.end());
            var startTime = System.currentTimeMillis();
            if (traceSemaphore.tryAcquire(15, TimeUnit.MINUTES)) {
                var duration = (System.currentTimeMillis() - startTime) / 1000;
                LOGGER.log(Level.INFO, "Trace profile saved | Duration: {0}s | Completed: {1} ", new Object[]{duration, traceCompleted});
            } else {
                LOGGER.log(Level.SEVERE, "Trace profile not saved within given time");
            }
        } finally {
            devTools.close();
        }
    }
}
