package com.vegatrader.ai;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

/**
 * Stub implementation of Inference Engine.
 * Placeholder until ONNX Runtime dependencies are added.
 */
@Service
public class StubInferenceEngine implements InferenceEngine {

    private static final Logger logger = LoggerFactory.getLogger(StubInferenceEngine.class);

    @Override
    public Map<String, Object> runInference(String modelName, Map<String, Object> inputs) {
        logger.warn("Inference requested for {} but engine is STUB. Returning empty.", modelName);
        return Collections.emptyMap();
    }

    @Override
    public void loadModel(String modelName, String modelPath) {
        logger.info("Loading model stub: {} from {}", modelName, modelPath);
    }

    @Override
    public boolean isAvailable() {
        return false;
    }
}
